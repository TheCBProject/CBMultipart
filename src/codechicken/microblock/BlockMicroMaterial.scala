package codechicken.microblock


import java.util.{LinkedList => JLinkedList, List => JList}
import codechicken.lib.render.CCRenderState
import codechicken.lib.render.pipeline.{ColourMultiplier, IVertexOperation}
import codechicken.lib.texture.TextureUtils
import codechicken.lib.vec.uv.{IconTransformation, MultiIconTransformation, UVTransformation}
import codechicken.lib.vec.{Cuboid6, Vector3}
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.{BlockRenderLayer, EnumFacing}
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.JavaConversions._

object MaterialRenderHelper
{
    private val instances = new ThreadLocal[MaterialRenderHelper] {
        override def initialValue() = new MaterialRenderHelper
    }

    def instance = instances.get()
}

class MaterialRenderHelper
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
            builder += CCRenderState.instance().lightMatrix
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
        @SideOnly(Side.CLIENT)
        def getSideIcon(state: IBlockState, s: Int): TextureAtlasSprite = {
            val side = EnumFacing.VALUES(s)
            val model = Minecraft.getMinecraft.getBlockRendererDispatcher.getModelForState(state)
            var winner = TextureUtils.getMissingSprite
            if (model != null) {
                val quads = new JLinkedList[BakedQuad]
                quads.addAll(model.getQuads(state, side, 0))
                quads.addAll(model.getQuads(state, null, 0).filter((quad : BakedQuad) => quad.getFace eq side))
                if (quads.size > 0) {
                    val list = new JLinkedList[TextureAtlasSprite]

                    for (quad <- quads) {
                        val sprite: TextureAtlasSprite = quad.getSprite
                        list.add(sprite)
                    }
                    if (!list.isEmpty) winner = list.get(0)
                }
            }
            winner
        }
        try {
            icont = new MultiIconTransformation(Array.tabulate(6)(side => getSideIcon(state, side)):_*)
            pIconT = new IconTransformation(TextureUtils.getParticleIconForBlock(state))
        } catch {
            case e:RuntimeException =>
                logger.error(s"unable to load microblock icons for block ${state.getBlock} with state $state")
        }
    }

    override def getMicroRenderOps(pos:Vector3, side:Int, layer:BlockRenderLayer, bounds:Cuboid6):Seq[Seq[IVertexOperation]] =
    {
        Seq(MaterialRenderHelper.instance.start(pos, layer, icont).blockColour(getColour(layer)).lighting().result())
    }

    def getColour(layer:BlockRenderLayer) =
    {
        layer match {
            case null =>
                Minecraft.getMinecraft.getBlockColors.colorMultiplier(state, null, null, 0)<<8|0xFF
            case _ =>
                Minecraft.getMinecraft.getBlockColors.colorMultiplier(state,
                    CCRenderState.instance().lightMatrix.access, CCRenderState.instance().lightMatrix.pos, 0)<<8|0xFF
        }
    }

    override def canRenderInLayer(layer:BlockRenderLayer) = state.getBlock.canRenderInLayer(state, layer)

    @SideOnly(Side.CLIENT)
    def getBreakingIcon(side:Int) = pIconT.icon

    def getItem = new ItemStack(Item.getItemFromBlock(state.getBlock), 1, state.getBlock.getMetaFromState(state))

    def getLocalizedName = getItem.getDisplayName

    def getStrength(player:EntityPlayer) =
        ForgeHooks.blockStrength(state, player, player.worldObj, new BlockPos(0, -1, 0))

    def isTransparent = !state.isOpaqueCube

    def getLightValue = state.getLightValue

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
            import scala.collection.JavaConversions._
            val properties = state.getProperties.iterator
            while(properties.hasNext) {
                val (p, v) = properties.next()
                key += p.getName + "=" + v.toString
                if (properties.hasNext)
                    key += ","
            }
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

    def createAndRegister(block:Block, metas:Seq[Int])
    {
        createAndRegister(metas map block.getStateFromMeta)
    }
}
