package codechicken.multipart

import java.util.{ArrayList => JArrayList, EnumSet => JEnumSet, List => JList, Random}

import codechicken.lib.raytracer.{CuboidRayTraceResult, RayTracer}
import codechicken.lib.vec.Vector3
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.ParticleManager
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.math.{AxisAlignedBB, BlockPos, RayTraceResult, Vec3d}
import net.minecraft.util.{BlockRenderLayer, EnumFacing, EnumHand}
import net.minecraft.world.{Explosion, IBlockAccess, World}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.JavaConversions._

/**
  * Internal RayTracing class that can save parts as part of their hit data
  */
class PartRayTraceResult(val partIndex:Int, crtr:CuboidRayTraceResult)
        extends CuboidRayTraceResult(new Vector3(crtr.hitVec), crtr.getBlockPos, crtr.sideHit, crtr.cuboid6, crtr.dist)

object BlockMultipart
{
    def getTile(world:IBlockAccess, pos:BlockPos) = world.getTileEntity(pos) match {
        case t:TileMultipart if t.partList.nonEmpty => t
        case _ => null
    }

    def getClientTile(world:IBlockAccess, pos:BlockPos) = world.getTileEntity(pos) match {
        case t:TileMultipartClient if t.partList.nonEmpty => t
        case _ => null
    }

    def getPart(world:IBlockAccess, pos:BlockPos, slot:Int) = world.getTileEntity(pos) match {
        case t:TileMultipart => t.partMap(slot)
        case _ => null
    }

    def retracePart(world:World, pos:BlockPos, player:EntityPlayer) =
        RayTracer.retraceBlock(world, player, pos) match {
            case partHit:PartRayTraceResult => partHit
            case _ => null
        }

    def drawHighlight(world:World, player:EntityPlayer, hit:RayTraceResult, frame:Float):Boolean =
    {
        (getClientTile(world, hit.getBlockPos), hit) match {
            case (null, _) => false
            case (tile, pHit:PartRayTraceResult) => tile.drawHighlight(player, pHit, frame)
        }
    }

    /**
      * Replacement for ForgeHooks.blockStrength, because it does dumb things like
      * check if the block is actually in position for no reason. Use this when
      * the block isnt actually in-world.
      */
    def getStrength(player:EntityPlayer, state:IBlockState) =
    {
        var hardness = 30F
        try {
            hardness = state.getBlockHardness(null, BlockPos.ORIGIN)
        } catch {
            case e:Exception =>
        }

        def canHarvest:Boolean =
        {
            if (state.getMaterial.isToolNotRequired)
                return true

            val stack = player.getHeldItemMainhand
            val tool = state.getBlock.getHarvestTool(state)
            if (stack == null || tool == null)
                return player.canHarvestBlock(state)

            val lvl = stack.getItem.getHarvestLevel(stack, tool)
            if (lvl < 0)
                return player.canHarvestBlock(state)

            lvl >= state.getBlock.getHarvestLevel(state)
        }

        player.getDigSpeed(state) / hardness / (if (canHarvest) 30F else 100F)
    }
}

/**
 * Block class for all multiparts, should be internal use only.
 */
class BlockMultipart extends Block(Material.ROCK)
{
    import BlockMultipart._

    override def hasTileEntity(state:IBlockState) = true

    override def isBlockNormalCube(state:IBlockState) = false

    override def isOpaqueCube(state:IBlockState) = false

    override def isFullCube(state:IBlockState) = false

    override def isFullBlock(state:IBlockState) = false

    override def getTickRandomly = true

    override def isAir(state:IBlockState, world:IBlockAccess, pos:BlockPos) = isReplaceable(world, pos)

    override def isReplaceable(world:IBlockAccess, pos:BlockPos) =
        getTile(world, pos) match {
            case null => true
            case tile => tile.partList.isEmpty
        }

    override def addCollisionBoxToList(state:IBlockState, world:World, pos:BlockPos, entityBox:AxisAlignedBB, collidingBoxes:JList[AxisAlignedBB], entity:Entity)
    {
        getTile(world, pos) match {
            case null =>
            case tile => tile.addCollisionBoxToList(entityBox, collidingBoxes)
        }
    }

    override def collisionRayTrace(state:IBlockState, world:World, pos:BlockPos, start:Vec3d, end:Vec3d):PartRayTraceResult =
        getTile(world, pos) match {
            case null => null
            case tile => tile.collisionRayTrace(start, end)
        }

    def rayTraceAll(world:World, pos:BlockPos, start:Vec3d, end:Vec3d):Iterable[PartRayTraceResult] =
        getTile(world, pos) match {
            case null => Seq()
            case tile => tile.rayTraceAll(start, end)
        }

    override def isBlockSolid(world:IBlockAccess, pos:BlockPos, side:EnumFacing) =
        getTile(world, pos) match {
            case null => false
            case tile => tile.isSolid(side.getIndex)
        }

    override def isSideSolid(state:IBlockState, world:IBlockAccess, pos:BlockPos, side:EnumFacing) = isBlockSolid(world, pos, side)

    override def canPlaceTorchOnTop(state:IBlockState, world:IBlockAccess, pos:BlockPos) =
        getTile(world, pos) match {
            case null => false
            case tile => tile.canPlaceTorchOnTop
        }

    override def getExplosionResistance(world:World, pos:BlockPos, exploder:Entity, explosion:Explosion) =
        getTile(world, pos) match {
            case null => 0F
            case tile => tile.getExplosionResistance(exploder)
        }

    override def getLightValue(state:IBlockState, world:IBlockAccess, pos:BlockPos):Int =
        getTile(world, pos) match {
            case null => 0
            case tile => tile.getLightValue
        }

    override def getPlayerRelativeBlockHardness(state:IBlockState, player:EntityPlayer, world:World, pos:BlockPos):Float =
        getTile(world, pos) match {
            case null => 1/100F
            case tile => tile.getPlayerRelativeBlockHardness(player, retracePart(world, pos, player))
        }

    override def removedByPlayer(state:IBlockState, world:World, pos:BlockPos, player:EntityPlayer, willHarvest:Boolean):Boolean =
    {
        val hit = retracePart(world, pos, player)
        val tile = getTile(world, pos)

        if(hit == null || tile == null) {
            dropAndDestroy(world, pos, state)
            return true
        }

        if (world.isRemote && tile.isInstanceOf[TileMultipartClient]) {
            tile.asInstanceOf[TileMultipartClient].addDestroyEffects(hit, Minecraft.getMinecraft.effectRenderer)
            return true
        }

        tile.harvestPart(hit, player)
        world.getTileEntity(pos) == null
    }

    def dropAndDestroy(world:World, pos:BlockPos, state:IBlockState)
    {
        val tile = getTile(world, pos)
        if(tile != null && !world.isRemote)
            tile.dropItems(getDrops(world, pos, state, 0))

        world.setBlockToAir(pos)
    }

    override def quantityDropped(state:IBlockState, fortune:Int, random:Random) = 0

    override def getDrops(world:IBlockAccess, pos:BlockPos, state:IBlockState, fortune:Int):JArrayList[ItemStack] =
        getTile(world, pos) match {
            case null => new JArrayList[ItemStack]()
            case tile => tile.getDrops
        }

    override def getPickBlock(state:IBlockState, target:RayTraceResult, world:World, pos:BlockPos, player:EntityPlayer):ItemStack =
        getTile(world, pos) match {
            case null => null
            case tile => tile.getPickBlock(retracePart(world, pos, player))
        }

    override def onBlockActivated(world:World, pos:BlockPos, state:IBlockState, player:EntityPlayer, hand:EnumHand, heldItem:ItemStack, side:EnumFacing, hitX:Float, hitY:Float, hitZ:Float):Boolean =
        getTile(world, pos) match {
            case null => false
            case tile => tile.onBlockActivated(player, retracePart(world, pos, player), hand)
        }

    override def onBlockClicked(world:World, pos:BlockPos, player:EntityPlayer)
    {
        getTile(world, pos) match {
            case null =>
            case tile => tile.onBlockClicked(player, retracePart(world, pos, player))
        }
    }

    override def onEntityCollidedWithBlock(world:World, pos:BlockPos, state:IBlockState, entity:Entity)
    {
        getTile(world, pos) match {
            case null =>
            case tile => tile.onEntityCollision(entity)
        }
    }

    override def onEntityWalk(world:World, pos:BlockPos, entity:Entity)
    {
        getTile(world, pos) match {
            case null =>
            case tile => tile.onEntityStanding(entity)
        }
    }

    override def neighborChanged(state:IBlockState, world:World, pos:BlockPos, neighborBlock:Block)
    {
        getTile(world, pos) match {
            case null =>
            case tile => tile.onNeighborBlockChange()
        }
    }

    override def onNeighborChange(world:IBlockAccess, pos:BlockPos, neighbor:BlockPos)
    {
        getTile(world, pos) match {
            case null =>
            case tile => tile.onNeighborTileChange(pos)
        }
    }

    override def getWeakChanges(world:IBlockAccess, pos:BlockPos):Boolean =
        getTile(world, pos) match {
            case null => false
            case tile => tile.getWeakChanges
        }

    override def canProvidePower(state:IBlockState) = true

    override def canConnectRedstone(state:IBlockState, world:IBlockAccess, pos:BlockPos, side:EnumFacing):Boolean =
        getTile(world, pos) match {
            case null => false
            case tile => side != null && tile.canConnectRedstone(side.getIndex)
        }

    override def getStrongPower(state:IBlockState, world:IBlockAccess, pos:BlockPos, side:EnumFacing):Int =
        getTile(world, pos) match {
            case null => 0
            case tile => tile.strongPowerLevel(side.getIndex^1)
        }

    override def getWeakPower(state:IBlockState, world:IBlockAccess, pos:BlockPos, side:EnumFacing):Int =
        getTile(world, pos) match {
            case null => 0
            case tile => tile.weakPowerLevel(side.getIndex^1)
        }

    override def getRenderType(state:IBlockState) = MultipartRenderer.renderType

    override def canRenderInLayer(state:IBlockState, layer:BlockRenderLayer) = true

    @SideOnly(Side.CLIENT)
    override def randomDisplayTick(state:IBlockState, world:World, pos:BlockPos, rand:Random)
    {
        getClientTile(world, pos) match {
            case null =>
            case tile => tile.randomDisplayTick(rand)
        }
    }

    @SideOnly(Side.CLIENT)
    override def addHitEffects(state:IBlockState, world:World, hit:RayTraceResult, manager:ParticleManager) =
    {
        (getClientTile(world, hit.getBlockPos), hit) match {
            case (null, _) =>
            case (tile, pHit:PartRayTraceResult) =>
                tile.addHitEffects(pHit, manager)
        }
        true
    }

    @SideOnly(Side.CLIENT)
    override def addDestroyEffects(world:World, pos:BlockPos, manager:ParticleManager) = true
}
