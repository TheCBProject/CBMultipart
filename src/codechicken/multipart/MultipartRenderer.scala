package codechicken.multipart

import java.util.{HashMap => JHashMap, Map => JMap}

import codechicken.lib.render.block.{BlockRenderingRegistry, ICCBlockRenderer}
import codechicken.lib.render.{CCRenderState, TextureUtils}
import codechicken.lib.vec.Vector3
import codechicken.multipart.BlockMultipart._
import net.minecraft.block.Block
import net.minecraft.block.properties.IProperty
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model._
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper
import net.minecraft.client.renderer.texture.{TextureMap, TextureAtlasSprite}
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.{GlStateManager, RenderHelper, VertexBuffer}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.lwjgl.opengl.GL11

import scala.collection.JavaConversions._

/**
 * Internal class for rendering callbacks. Should be moved to the handler package
 */
@SideOnly(Side.CLIENT)
object MultipartRenderer extends TileEntitySpecialRenderer[TileMultipartClient] with ICCBlockRenderer
{
    val renderType = BlockRenderingRegistry.createRenderType("fmpcbe_mpblock")

    def register()
    {
        BlockRenderingRegistry.registerRenderer(renderType, this)
    }


    override def renderTileEntityAt(tile:TileMultipartClient, x:Double, y:Double, z:Double, frame:Float, destroyStage:Int)
    {
        if (tile.partList.isEmpty)
            return

        CCRenderState.reset()
        tile.renderDynamic(new Vector3(x, y, z), MinecraftForgeClient.getRenderPass, frame)

        //Simulate fast render
        import GL11._

        //Set GL state
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.enableBlend()
        GlStateManager.disableCull()
        GlStateManager.shadeModel(if (Minecraft.isAmbientOcclusionEnabled) GL_SMOOTH else GL_FLAT)

        //Set MC Render state
        RenderHelper.disableStandardItemLighting()
        TextureUtils.bindBlockTexture()

        //Render through CC render pipeline
        CCRenderState.reset()
        CCRenderState.startDrawing(GL_QUADS, DefaultVertexFormats.BLOCK)
        val buffer = CCRenderState.getBuffer
        tile.renderFast(new Vector3(x, y, z), MinecraftForgeClient.getRenderPass, frame, buffer)
        buffer.setTranslation(0, 0, 0)
        CCRenderState.draw()

        //Reset MC Render state
        RenderHelper.enableStandardItemLighting()
    }

    override def renderTileEntityFast(tile:TileMultipartClient, x:Double, y:Double, z:Double, frame:Float, destroyStage:Int, buffer:VertexBuffer)
    {
        if (tile.partList.isEmpty)
            return

        CCRenderState.reset()
        CCRenderState.bind(buffer)
        tile.renderFast(new Vector3(x, y, z), MinecraftForgeClient.getRenderPass, frame, buffer)
    }

    override def renderBlock(world:IBlockAccess, pos:BlockPos, state:IBlockState, buffer:VertexBuffer) =
        getClientTile(world, pos) match {
            case null => false
            case tile =>
                CCRenderState.reset()
                CCRenderState.bind(buffer)
                CCRenderState.lightMatrix.locate(world, pos)
                tile.renderStatic(new Vector3(pos), MinecraftForgeClient.getRenderLayer)
        }

    override def handleRenderBlockDamage(world:IBlockAccess, pos:BlockPos, state:IBlockState, sprite:TextureAtlasSprite, buffer:VertexBuffer)
    {
        getClientTile(world, pos) match {
            case null =>
            case tile =>
                CCRenderState.reset()
                CCRenderState.bind(buffer)
                tile.renderDamage(new Vector3(pos), sprite)
        }
    }

    override def renderBrightness(state:IBlockState, brightness:Float){}

    override def registerTextures(map:TextureMap){}
}

object MultipartStateMapper extends DefaultStateMapper
{
    private var replaceNormal:Boolean = true

    override def putStateModelLocations(block:Block):JMap[IBlockState, ModelResourceLocation] =
    {
        val mappings = new JHashMap[IBlockState, ModelResourceLocation]
        replaceNormal = false
        mappings.putAll(super.putStateModelLocations(block))
        replaceNormal = true

        import MultiPartRegistryClient._

        for ((partName, container) <- nameToStateContainer) {

            nameToModelMapper.get(partName) match {
                case Some(mapper) =>
                    mappings.putAll(mapper.putStateModelLocations(partName, container))
                case None =>
                    val modelPath = MultiPartRegistryClient.nameToModelPath(partName)
                    for (state <- container.getValidStates)
                        mappings.put(state, new ModelResourceLocation(modelPath, getPropertyString(state.getProperties)))
            }
        }

        mappings
    }

    override def getPropertyString(map:JMap[IProperty[_], Comparable[_]]):String =
    {
        val str = super.getPropertyString(map)
        if (replaceNormal && (str == "normal")) return "multipart"
        str
    }
}