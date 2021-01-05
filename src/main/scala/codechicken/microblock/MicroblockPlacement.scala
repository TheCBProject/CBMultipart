package codechicken.microblock

import codechicken.lib.vec.{Rotation, Vector3}
import codechicken.microblock.api.MicroMaterial
import codechicken.multipart.block.TileMultiPart
import codechicken.multipart.util.{ControlKeyModifier, MultiPartHelper, OffsetItemUseContext, PartRayTraceResult}
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.{ItemStack, ItemUseContext}
import net.minecraft.util.math.{BlockPos, BlockRayTraceResult}
import net.minecraft.util.{Direction, Hand}
import net.minecraft.world.World

abstract class ExecutablePlacement(val pos: BlockPos, val part: Microblock) {
    def place(world: World, player: PlayerEntity, item: ItemStack): Unit

    def consume(world: World, player: PlayerEntity, item: ItemStack): Unit
}

class AdditionPlacement($pos: BlockPos, $part: Microblock) extends ExecutablePlacement($pos, $part) {
    def place(world: World, player: PlayerEntity, item: ItemStack) {
        TileMultiPart.addPart(world, pos, part)
    }

    def consume(world: World, player: PlayerEntity, item: ItemStack) {
        item.shrink(1)
    }
}

class ExpandingPlacement($pos: BlockPos, $part: Microblock, opart: Microblock) extends ExecutablePlacement($pos, $part) {
    def place(world: World, player: PlayerEntity, item: ItemStack) {
        opart.shape = part.shape
        opart.tile.notifyPartChange(opart)
        opart.sendShapeUpdate()
    }

    def consume(world: World, player: PlayerEntity, item: ItemStack) {
        item.shrink(1)
    }
}

abstract class PlacementProperties {
    def opposite(slot: Int, side: Int): Int

    def sneakOpposite(slot: Int, side: Int) = true

    def expand(slot: Int, side: Int) = true

    def microFactory: MicroblockFactory

    def placementGrid: PlacementGrid

    def customPlacement(pmt: MicroblockPlacement): ExecutablePlacement = null
}

object MicroblockPlacement {
    def apply(player: PlayerEntity, hand: Hand, hit: BlockRayTraceResult, size: Int, material: MicroMaterial, checkMaterial: Boolean, pp: PlacementProperties): ExecutablePlacement =
        new MicroblockPlacement(player, hand, hit, size, material, checkMaterial, pp).apply()
}

class MicroblockPlacement(val player: PlayerEntity, val hand: Hand, val hit: BlockRayTraceResult, val size: Int, val material: MicroMaterial, val checkMaterial: Boolean, val pp: PlacementProperties) {
    val world = player.world
    val mcrFactory = pp.microFactory
    val pos = hit.getPos
    val vhit = new Vector3(hit.getHitVec).add(-pos.getX, -pos.getY, -pos.getZ)
    val gtile = MultiPartHelper.getOrConvertTile2(world, pos)
    val htile = gtile.getLeft
    val slot = pp.placementGrid.getHitSlot(vhit, hit.getFace.ordinal)
    val oslot = pp.opposite(slot, hit.getFace.ordinal)

    val d = getHitDepth(vhit, hit.getFace.ordinal)
    val useOppMod = pp.sneakOpposite(slot, hit.getFace.ordinal)
    val oppMod = ControlKeyModifier.isControlDown(player)
    val internal = d < 1 && htile != null
    val doExpand = internal && !gtile.getRight && !player.isCrouching && !(oppMod && useOppMod) && pp.expand(slot, hit.getFace.ordinal)
    val side = hit.getFace.ordinal

    def apply(): ExecutablePlacement = {
        val customPlacement = pp.customPlacement(this)
        if (customPlacement != null) {
            return customPlacement
        }

        if (slot < 0) {
            return null
        }

        if (doExpand) {
            val hpart = hit.asInstanceOf[PartRayTraceResult].part
            if (hpart.getType == mcrFactory.getType) {
                val mpart = hpart.asInstanceOf[CommonMicroblock]
                if (mpart.material == material && mpart.getSize + size < 8) {
                    return expand(mpart)
                }
            }
        }

        if (internal) {
            if (d < 0.5 || !useOppMod) {
                val ret = internalPlacement(htile, slot)
                if (ret != null) {
                    if (!useOppMod || !oppMod) {
                        return ret
                    } else {
                        return internalPlacement(htile, oslot)
                    }
                }
            }
            if (useOppMod && !oppMod) {
                return internalPlacement(htile, oslot)
            } else {
                return externalPlacement(slot)
            }
        }

        if (!useOppMod || !oppMod) {
            return externalPlacement(slot)
        } else {
            return externalPlacement(oslot)
        }
    }

    def expand(mpart: CommonMicroblock): ExecutablePlacement = expand(mpart, create(mpart.getSize + size, mpart.getSlot, mpart.material))

    def expand(mpart: Microblock, npart: Microblock): ExecutablePlacement = {
        val pos = mpart.tile.getPos
        if (TileMultiPart.checkNoEntityCollision(world, pos, npart) && mpart.tile.canReplacePart(mpart, npart)) {
            return new ExpandingPlacement(pos, npart, mpart)
        }
        return null
    }

    def internalPlacement(htile: TileMultiPart, slot: Int): ExecutablePlacement = internalPlacement(htile, create(size, slot, material))

    def internalPlacement(htile: TileMultiPart, npart: Microblock): ExecutablePlacement = {
        val pos = htile.getPos
        if (TileMultiPart.checkNoEntityCollision(world, pos, npart) && htile.canAddPart(npart)) {
            return new AdditionPlacement(pos, npart)
        }
        return null
    }

    def externalPlacement(slot: Int): ExecutablePlacement = externalPlacement(create(size, slot, material))

    def externalPlacement(npart: Microblock): ExecutablePlacement = {
        val pos = this.pos.offset(Direction.BY_INDEX.apply(side))
        if (TileMultiPart.canPlacePart(new OffsetItemUseContext(new ItemUseContext(player, hand, hit)), npart)) {
            return new AdditionPlacement(pos, npart)
        }
        null
    }

    def getHitDepth(vhit: Vector3, side: Int): Double =
        vhit.copy.scalarProject(Rotation.axes(side)) + (side % 2 ^ 1)

    def create(size: Int, slot: Int, material: MicroMaterial) = {
        val part = mcrFactory.create(world.isRemote, material)
        part.setShape(size, slot)
        part
    }
}
