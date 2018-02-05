package codechicken.microblock

import codechicken.lib.vec.{Rotation, Vector3}
import codechicken.multipart.ControlKeyModifer._
import codechicken.multipart.{PartRayTraceResult, TileMultipart}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.{BlockPos, RayTraceResult}
import net.minecraft.world.World

abstract class ExecutablePlacement(val pos:BlockPos, val part:Microblock)
{
    def place(world:World, player:EntityPlayer, item:ItemStack)
    def consume(world:World, player:EntityPlayer, item:ItemStack)
}

class AdditionPlacement($pos:BlockPos, $part:Microblock) extends ExecutablePlacement($pos, $part)
{
    def place(world:World, player:EntityPlayer, item:ItemStack)
    {
        TileMultipart.addPart(world, pos, part)
    }

    def consume(world:World, player:EntityPlayer, item:ItemStack)
    {
        item.shrink(1)
    }
}

class ExpandingPlacement($pos:BlockPos, $part:Microblock, opart:Microblock) extends ExecutablePlacement($pos, $part)
{
    def place(world:World, player:EntityPlayer, item:ItemStack)
    {
        opart.shape = part.shape
        opart.tile.notifyPartChange(opart)
        opart.sendShapeUpdate()
    }

    def consume(world:World, player:EntityPlayer, item:ItemStack)
    {
        item.shrink(1)
    }
}

abstract class PlacementProperties
{
    def opposite(slot:Int, side:Int):Int

    def sneakOpposite(slot:Int, side:Int) = true

    def expand(slot:Int, side:Int) = true

    def microFactory:MicroblockFactory

    def placementGrid:PlacementGrid

    def customPlacement(pmt:MicroblockPlacement):ExecutablePlacement = null
}

object MicroblockPlacement
{
    def apply(player:EntityPlayer, hit:RayTraceResult, size:Int, material:Int, checkMaterial:Boolean, pp:PlacementProperties):ExecutablePlacement =
        new MicroblockPlacement(player, hit, size, material, checkMaterial, pp).apply()
}

class MicroblockPlacement(val player:EntityPlayer, val hit:RayTraceResult, val size:Int, val material:Int, val checkMaterial:Boolean, val pp:PlacementProperties)
{
    val world = player.world
    val mcrFactory = pp.microFactory
    val pos = new BlockPos(hit.getBlockPos)
    val vhit = new Vector3(hit.hitVec).add(-pos.getX, -pos.getY, -pos.getZ)
    val gtile = TileMultipart.getOrConvertTile2(world, pos)
    val htile = gtile._1
    val slot = pp.placementGrid.getHitSlot(vhit, hit.sideHit.ordinal)
    val oslot = pp.opposite(slot, hit.sideHit.ordinal)

    val d = getHitDepth(vhit, hit.sideHit.ordinal)
    val useOppMod = pp.sneakOpposite(slot, hit.sideHit.ordinal)
    val oppMod = player.isControlDown
    val internal = d < 1 && htile != null
    val doExpand = internal && !gtile._2 && !player.isSneaking && !(oppMod && useOppMod) && pp.expand(slot, hit.sideHit.ordinal)
    val side = hit.sideHit.ordinal

    def apply():ExecutablePlacement =
    {
        val customPlacement = pp.customPlacement(this)
        if(customPlacement != null)
            return customPlacement

        if(slot < 0)
            return null

        if(doExpand)
        {
            val hpart = htile.partList(hit.asInstanceOf[PartRayTraceResult].partIndex)
            if(hpart.getType == mcrFactory.getName)
            {
                val mpart = hpart.asInstanceOf[CommonMicroblock]
                if(mpart.material == material && mpart.getSize + size < 8)
                    return expand(mpart)
            }
        }

        if(internal)
        {
            if(d < 0.5 || !useOppMod)
            {
                val ret = internalPlacement(htile, slot)
                if(ret != null) {
                    if(!useOppMod || !oppMod) return ret
                    else return internalPlacement(htile, oslot)
                }
            }
            if(useOppMod && !oppMod)
                return internalPlacement(htile, oslot)
            else
                return externalPlacement(slot)
        }

        if(!useOppMod || !oppMod)
            return externalPlacement(slot)
        else
            return externalPlacement(oslot)
    }

    def expand(mpart:CommonMicroblock):ExecutablePlacement = expand(mpart, create(mpart.getSize+size, mpart.getSlot, mpart.material))

    def expand(mpart:Microblock, npart:Microblock):ExecutablePlacement =
    {
        val pos = mpart.tile.getPos
        if(TileMultipart.checkNoEntityCollision(world, pos, npart) && mpart.tile.canReplacePart(mpart, npart))
            return new ExpandingPlacement(pos, npart, mpart)
        return null
    }

    def internalPlacement(htile:TileMultipart, slot:Int):ExecutablePlacement = internalPlacement(htile, create(size, slot, material))

    def internalPlacement(htile:TileMultipart, npart:Microblock):ExecutablePlacement =
    {
        val pos = htile.getPos
        if(TileMultipart.checkNoEntityCollision(world, pos, npart) && htile.canAddPart(npart))
            return new AdditionPlacement(pos, npart)
        return null
    }

    def externalPlacement(slot:Int):ExecutablePlacement = externalPlacement(create(size, slot, material))

    def externalPlacement(npart:Microblock):ExecutablePlacement =
    {
        val pos = this.pos.offset(EnumFacing.VALUES.apply(side))
        if(TileMultipart.canPlacePart(world, pos, npart))
            return new AdditionPlacement(pos, npart)
        null
    }

    def getHitDepth(vhit:Vector3, side:Int):Double =
        vhit.copy.scalarProject(Rotation.axes(side)) + (side%2^1)

    def create(size:Int, slot:Int, material:Int) = {
        val part = mcrFactory.create(world.isRemote, material)
        part.setShape(size, slot)
        part
    }
}
