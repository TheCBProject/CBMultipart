package codechicken.multipart

import java.util.{Random, ArrayList => JArrayList, List => JList}
import codechicken.lib.raytracer.{DistanceRayTraceResult, RayTracer}
import codechicken.lib.vec.Vector3
import codechicken.multipart.util.MultiPartLoadHandler.TileNBTContainer
import codechicken.multipart.util.PartRayTraceResult
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.block.material.Material
import net.minecraft.block.{Block, BlockState, SoundType}
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.ParticleManager
import net.minecraft.client.renderer.{ActiveRenderInfo, IRenderTypeBuffer}
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.IFluidState
import net.minecraft.item.ItemStack
import net.minecraft.util.math._
import net.minecraft.util.math.shapes.{ISelectionContext, VoxelShapes}
import net.minecraft.util.{ActionResultType, Direction, Hand, SoundCategory}
import net.minecraft.world._
import net.minecraft.world.storage.loot.{LootContext, LootParameters}
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

object BlockMultipart {

    def getTile(world: IBlockReader, pos: BlockPos) = world.getTileEntity(pos) match {
        case t: TileMultipart if t.partList.nonEmpty => t
        case _ => null
    }

    def getClientTile(world: IBlockReader, pos: BlockPos) = world.getTileEntity(pos) match {
        case t: TileMultipartClient if t.partList.nonEmpty => t
        case _ => null
    }

    def getPart(world: IBlockReader, pos: BlockPos, slot: Int) = world.getTileEntity(pos) match {
        case t: TileMultipart => t.partMap(slot)
        case _ => null
    }

    def retracePart(world: IBlockReader, pos: BlockPos, player: PlayerEntity) =
        RayTracer.retraceBlock(world, player, pos) match {
            case partHit: PartRayTraceResult => partHit
            case _ => null
        }
}

/**
 * Block class for all multiparts, should be internal use only.
 */
class BlockMultipart extends Block(Block.Properties.create(Material.ROCK)) {

    import BlockMultipart._

    override def hasTileEntity(state: BlockState) = true

    override def createTileEntity(state: BlockState, world: IBlockReader) = new TileNBTContainer

    override def ticksRandomly(state: BlockState) = true

    override def isAir(state: BlockState, world: IBlockReader, pos: BlockPos) =
        getTile(world, pos) match {
            case null => true
            case tile => tile.partList.isEmpty
        }

    override def getShape(state: BlockState, world: IBlockReader, pos: BlockPos, context: ISelectionContext) = getTile(world, pos) match {
        case null => VoxelShapes.empty()
        case tile => tile.getOutlineShape
    }

    override def getCollisionShape(state: BlockState, world: IBlockReader, pos: BlockPos, context: ISelectionContext) = getTile(world, pos) match {
        case null => VoxelShapes.empty()
        case tile => tile.getCollisionShape
    }

    override def getRenderShape(state: BlockState, world: IBlockReader, pos: BlockPos) = getTile(world, pos) match {
        case null => VoxelShapes.empty()
        case tile => tile.getCullingShape
    }

    override def getRaytraceShape(state: BlockState, world: IBlockReader, pos: BlockPos) = getTile(world, pos) match {
        case null => VoxelShapes.empty()
        case tile => tile.getRayTraceShape
    }

    override def getExplosionResistance(state: BlockState, world: IWorldReader, pos: BlockPos, exploder: Entity, explosion: Explosion) =
        getTile(world, pos) match {
            case null => 0F
            case tile => tile.getExplosionResistance(exploder)
        }

    override def getLightValue(state: BlockState, world: IBlockReader, pos: BlockPos): Int =
        getTile(world, pos) match {
            case null => 0
            case tile => tile.getLightValue
        }

    override def getPlayerRelativeBlockHardness(state: BlockState, player: PlayerEntity, world: IBlockReader, pos: BlockPos): Float =
        (getTile(world, pos), retracePart(world, pos, player)) match {
            case (tile: TileMultipart, hit: PartRayTraceResult) => tile.getPlayerRelativeBlockHardness(player, hit)
            case _ => 1 / 100F
        }

    override def removedByPlayer(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, willHarvest: Boolean, fluid: IFluidState): Boolean = {
        val hit = retracePart(world, pos, player)
        val tile = getTile(world, pos)
        world.playEvent(player, 2001, pos, Block.getStateId(state))

        if (hit == null || tile == null) {
            dropAndDestroy(world, pos, state)
            return true
        }

        if (world.isRemote && tile.isInstanceOf[TileMultipartClient]) {
            tile.asInstanceOf[TileMultipartClient].addDestroyEffects(hit, Minecraft.getInstance.particles)
            return true
        }

        tile.harvestPart(hit, player)
        world.getTileEntity(pos) == null
    }

    def dropAndDestroy(world: World, pos: BlockPos, state: BlockState): Unit = {
        val tile = getTile(world, pos)
        if (tile != null && !world.isRemote) {
            tile.dropItems(tile.getDrops)
        }

        world.removeBlock(pos, false)
    }

    override def getDrops(state: BlockState, builder: LootContext.Builder): JList[ItemStack] =
        getTile(builder.getWorld, builder.get(LootParameters.POSITION)) match {
            case null => new JArrayList[ItemStack]()
            case tile => tile.getDrops
        }

    override def getPickBlock(state: BlockState, target: RayTraceResult, world: IBlockReader, pos: BlockPos, player: PlayerEntity): ItemStack =
        (getTile(world, pos), retracePart(world, pos, player)) match {
            case (tile: TileMultipart, hit: PartRayTraceResult) => tile.getPickBlock(hit)
            case _ => ItemStack.EMPTY
        }

    override def onBlockActivated(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockRayTraceResult): ActionResultType =
        (getTile(world, pos), retracePart(world, pos, player)) match {
            case (tile: TileMultipart, hit: PartRayTraceResult) => tile.onBlockActivated(player, hit, hand)
            case _ => ActionResultType.FAIL
        }

    override def onBlockClicked(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity): Unit = {
        (getTile(world, pos), retracePart(world, pos, player)) match {
            case (tile: TileMultipart, hit: PartRayTraceResult) => tile.onBlockClicked(player, hit)
            case _ =>
        }
    }

    override def onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity): Unit = {
        getTile(world, pos) match {
            case null =>
            case tile => tile.onEntityCollision(entity)
        }
    }

    override def onEntityWalk(world: World, pos: BlockPos, entity: Entity): Unit = {
        getTile(world, pos) match {
            case null =>
            case tile => tile.onEntityStanding(entity)
        }
    }

    override def neighborChanged(state: BlockState, world: World, pos: BlockPos, blockIn: Block, fromPos: BlockPos, isMoving: Boolean): Unit = {
        getTile(world, pos) match {
            case null =>
            case tile =>
                tile.onNeighborBlockChanged(fromPos)
        }
    }

    override def onNeighborChange(state: BlockState, world: IWorldReader, pos: BlockPos, neighbor: BlockPos): Unit = {
        getTile(world, pos) match {
            case null =>
            case tile => tile.onNeighborTileChange(neighbor)
        }
    }

    override def getWeakChanges(state: BlockState, world: IWorldReader, pos: BlockPos): Boolean =
        getTile(world, pos) match {
            case null => false
            case tile => tile.getWeakChanges
        }

    override def canProvidePower(state: BlockState) = true

    override def canConnectRedstone(state: BlockState, world: IBlockReader, pos: BlockPos, side: Direction): Boolean =
        getTile(world, pos) match {
            case null => false
            case tile => side != null && tile.canConnectRedstone(side.getIndex ^ 1) //'side' is respect to connecting block, we want with respect to this block
        }

    override def getStrongPower(state: BlockState, world: IBlockReader, pos: BlockPos, side: Direction): Int =
        getTile(world, pos) match {
            case null => 0
            case tile => tile.strongPowerLevel(side.getIndex ^ 1) //'side' is respect to connecting block, we want with respect to this block
        }

    override def getWeakPower(state: BlockState, world: IBlockReader, pos: BlockPos, side: Direction): Int =
        getTile(world, pos) match {
            case null => 0
            case tile => tile.weakPowerLevel(side.getIndex ^ 1) //'side' is respect to connecting block, we want with respect to this block
        }

    @OnlyIn(Dist.CLIENT)
    override def animateTick(state: BlockState, world: World, pos: BlockPos, rand: Random): Unit = {
        getClientTile(world, pos) match {
            case null =>
            case tile => tile.animateTick(rand)
        }
    }

    @OnlyIn(Dist.CLIENT)
    override def addHitEffects(state: BlockState, world: World, hit: RayTraceResult, manager: ParticleManager) = {
        (getClientTile(world, hit.asInstanceOf[BlockRayTraceResult].getPos), hit) match {
            case (tile: TileMultipartClient, pHit: PartRayTraceResult) => tile.addHitEffects(pHit, manager)
            case _ =>
        }
        true
    }

    override def addDestroyEffects(state: BlockState, world: World, pos: BlockPos, manager: ParticleManager) = true
}
