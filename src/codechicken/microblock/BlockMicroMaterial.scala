package codechicken.microblock

import codechicken.lib.render.CCRenderState.IVertexOperation
import codechicken.lib.render.uv.{IconTransformation, MultiIconTransformation, UVTransformation}
import codechicken.lib.render.{TextureUtils, CCRenderPipeline, CCRenderState, ColourMultiplier}
import codechicken.lib.vec.{Cuboid6, Vector3}
import codechicken.microblock.MicroMaterialRegistry.IMicroMaterial
import codechicken.multipart.{BlockMultipart, MultipartStateMapper}
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{BlockRenderLayer, EnumFacing}
import net.minecraft.world.IBlockAccess
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.JavaConversions._
import scala.ref.WeakReference

object MaterialRenderHelper
{
    private var layer:BlockRenderLayer = null
    private var builder = Seq.newBuilder[IVertexOperation]

    def start(pos:Vector3, layer:BlockRenderLayer, uvt:UVTransformation) =
    {
        this.layer = layer
        builder.clear()
        builder += pos.translation()
        builder += uvt
        this
    }

    def blockColour(colour:Int) =
    {
        builder += ColourMultiplier.instance(colour)
        this
    }

    def lighting() = {
        if(layer != null)
            builder += CCRenderState.lightMatrix
        this
    }

    def result() = builder.result()
}

/**
 * Standard micro material class suitable for most blocks.
 */
class BlockMicroMaterial(val state:IBlockState) extends IMicroMaterial
{
    val blockKey = state.getPropertyNames

    @SideOnly(Side.CLIENT)
    var icont:MultiIconTransformation = _

    @SideOnly(Side.CLIENT)
    var pIconT:IconTransformation = _

    @SideOnly(Side.CLIENT)
    override def loadIcons()
    {
        icont = new MultiIconTransformation(Array.tabulate(6)(
            side => TextureUtils.getIconsForBlock(state, side)(0)):_*)
        pIconT = new IconTransformation(TextureUtils.getParticleIconForBlock(state))
    }

    override def getMicroRenderOps(pos:Vector3, side:Int, layer:BlockRenderLayer, bounds:Cuboid6):Seq[Seq[CCRenderState.IVertexOperation]] =
    {
        Seq(MaterialRenderHelper.start(pos, layer, icont).blockColour(getColour(layer)).lighting().result())
    }

    def getColour(layer:BlockRenderLayer) =
    {
        layer match {
            case null =>
                Minecraft.getMinecraft.getBlockColors.colorMultiplier(state, null, null, 0)<<8|0xFF
            case world =>
                Minecraft.getMinecraft.getBlockColors.colorMultiplier(state,
                    CCRenderState.lightMatrix.access, CCRenderState.lightMatrix.pos.pos(), 0)<<8|0xFF
        }
    }

    override def canRenderInLayer(layer:BlockRenderLayer) = state.getBlock.canRenderInLayer(state, layer)

    @SideOnly(Side.CLIENT)
    def getBreakingIcon(side:Int) = pIconT.icon

    def getItem = new ItemStack(Item.getItemFromBlock(state.getBlock), 1, state.getBlock.getMetaFromState(state))

    def getLocalizedName = getItem.getDisplayName

    def getStrength(player:EntityPlayer) = BlockMultipart.getStrength(player, state)

    def isTransparent = !state.isOpaqueCube

    def getLightValue = state.getLightValue()

    def toolClasses = Seq("axe", "pickaxe", "shovel")

    def getCutterStrength = state.getBlock.getHarvestLevel(state)

    def getSound = state.getBlock.getSoundType

    def explosionResistance(entity:Entity):Float = state.getBlock.getExplosionResistance(entity)
}

/**
 * Utility functions for cleaner registry code
 */
object BlockMicroMaterial
{
    def materialKey(block:Block):String =
        materialKey(block.getDefaultState)

    def materialKey(state:IBlockState):String =
    {
        var key = state.getBlock.getRegistryName.toString
        val numOfProps = state.getProperties.size
        if (numOfProps > 0) {
            key += "["
            key += MultipartStateMapper.getPropertyString(state.getProperties)
            key += "]"
        }
        key
    }

    def createAndRegister(block:Block)
    {
        createAndRegister(block.getDefaultState)
    }

    def createAndRegister(state:IBlockState)
    {
        MicroMaterialRegistry.registerMaterial(new BlockMicroMaterial(state), materialKey(state))
    }

    def createAndRegister(states:Seq[IBlockState])
    {
        states.foreach(createAndRegister)
    }
}