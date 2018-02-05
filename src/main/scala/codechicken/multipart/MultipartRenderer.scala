package codechicken.multipart

import java.util.{HashMap => JHashMap, Map => JMap}

import codechicken.lib.render.CCRenderState
import codechicken.lib.render.block.{BlockRenderingRegistry, ICCBlockRenderer}
import codechicken.lib.texture.TextureUtils
import codechicken.lib.vec.Vector3
import codechicken.multipart.BlockMultipart._
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model._
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper
import net.minecraft.client.renderer.texture.{TextureAtlasSprite, TextureMap}
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.{BufferBuilder, GlStateManager, RenderHelper}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.lwjgl.opengl.GL11

/**
 * Internal class for rendering callbacks. Should be moved to the handler package
 */
@SideOnly(Side.CLIENT)
object MultipartRenderer extends TileEntitySpecialRenderer[TileMultipartClient] with ICCBlockRenderer {
    val renderType = BlockRenderingRegistry.createRenderType("fmpcbe_mpblock")

    def register() {
        BlockRenderingRegistry.registerRenderer(renderType, this)
    }


    override def render(tile: TileMultipartClient, x: Double, y: Double, z: Double, frame: Float, destroyStage: Int, alpha: Float) {
        if (tile.partList.isEmpty) {
            return
        }
        val ccrs = CCRenderState.instance()
        ccrs.reset()
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
        ccrs.reset()
        ccrs.startDrawing(GL_QUADS, DefaultVertexFormats.BLOCK)
        tile.renderFast(new Vector3(x, y, z), MinecraftForgeClient.getRenderPass, frame, ccrs)
        ccrs.getBuffer.setTranslation(0, 0, 0)
        ccrs.draw()

        //Reset MC Render state
        RenderHelper.enableStandardItemLighting()
        GlStateManager.enableCull()
        GlStateManager.disableBlend()
    }

    override def renderTileEntityFast(tile: TileMultipartClient, x: Double, y: Double, z: Double, frame: Float, destroyStage: Int, alpha: Float, buffer: BufferBuilder) {
        if (tile.partList.isEmpty) {
            return
        }
        val ccrs = CCRenderState.instance()
        ccrs.reset()
        ccrs.bind(buffer)
        tile.renderFast(new Vector3(x, y, z), MinecraftForgeClient.getRenderPass, frame, ccrs)
    }

    override def renderBlock(world: IBlockAccess, pos: BlockPos, state: IBlockState, buffer: BufferBuilder) =
        getClientTile(world, pos) match {
            case null => false
            case tile =>
                val ccrs = CCRenderState.instance()
                ccrs.reset()
                ccrs.bind(buffer)
                ccrs.lightMatrix.locate(world, pos)
                tile.renderStatic(Vector3.fromBlockPos(pos), MinecraftForgeClient.getRenderLayer, ccrs)
        }

    override def handleRenderBlockDamage(world: IBlockAccess, pos: BlockPos, state: IBlockState, sprite: TextureAtlasSprite, buffer: BufferBuilder) {
        getClientTile(world, pos) match {
            case null =>
            case tile =>
                val ccrs = CCRenderState.instance()
                ccrs.reset()
                ccrs.bind(buffer)
                tile.renderDamage(Vector3.fromBlockPos(pos), sprite, ccrs)
        }
    }

    override def renderBrightness(state: IBlockState, brightness: Float) {}

    override def registerTextures(map: TextureMap) {}
}

//TODO, This is probably not needed?
object MultipartStateMapper extends DefaultStateMapper {
    private var replaceNormal: Boolean = true

    override protected def getModelResourceLocation(state: IBlockState) = new ModelResourceLocation(state.getBlock.getRegistryName, "normal")

    //    override def putStateModelLocations(block:Block):JMap[IBlockState, ModelResourceLocation] =
    //    {
    //        val mappings = new JHashMap[IBlockState, ModelResourceLocation]
    //        replaceNormal = false
    //        mappings.putAll(super.putStateModelLocations(block))
    //        replaceNormal = true
    //
    //        import MultiPartRegistryClient._
    //
    //        for ((partName, container) <- nameToStateContainer) {
    //
    //            nameToModelMapper.get(partName) match {
    //                case Some(mapper) =>
    //                    mappings.putAll(mapper.putStateModelLocations(partName, container))
    //                case None =>
    //                    val modelPath = MultiPartRegistryClient.nameToModelPath(partName)
    //                    for (state <- container.getValidStates)
    //                        mappings.put(state, new ModelResourceLocation(modelPath, getPropertyString(state.getProperties)))
    //            }
    //        }
    //
    //        mappings
    //    }

    //    override def getPropertyString(map:JMap[IProperty[_ <: Comparable[_]], Comparable[_]]):String =
    //    {
    //        val str = super.getPropertyString(map)
    //        if (replaceNormal && (str == "normal")) return "multipart"
    //        str
    //    }
}
