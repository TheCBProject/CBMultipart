package codechicken.microblock

import java.util.{ArrayList => JArrayList}

import codechicken.lib.model.bakery.CCModelBakery
import codechicken.lib.render.BlockRenderer.BlockFace
import codechicken.lib.render.CCRenderState
import codechicken.lib.texture.TextureUtils
import codechicken.lib.vec.{Cuboid6, Vector3}
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.math.RayTraceResult
import org.lwjgl.opengl.GL11._

import scala.collection.JavaConversions._

object MicroblockRender
{
    def renderHighlight(player:EntityPlayer, hit:RayTraceResult, mcrFactory:CommonMicroFactory, size:Int, material:Int)
    {
        mcrFactory.placementProperties.placementGrid.render(new Vector3(hit.hitVec), hit.sideHit.ordinal)

        val placement = MicroblockPlacement(player, hit, size, material, !player.capabilities.isCreativeMode, mcrFactory.placementProperties)
        if(placement == null)
            return
        val pos = placement.pos
        val part = placement.part.asInstanceOf[MicroblockClient]

        glPushMatrix()
        glTranslated(pos.getX+0.5, pos.getY+0.5, pos.getZ+0.5)
        glScaled(1.002, 1.002, 1.002)
        glTranslated(-0.5, -0.5, -0.5)

        glEnable(GL_BLEND)
        glDepthMask(false)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        val ccrs = CCRenderState.instance()
        TextureUtils.bindBlockTexture()
        ccrs.reset()
        ccrs.alphaOverride = 80
        ccrs.startDrawing(GL_QUADS, DefaultVertexFormats.ITEM)
        part.render(Vector3.zero, null, ccrs)
        ccrs.draw()

        glDisable(GL_BLEND)
        glDepthMask(true)
        glPopMatrix()
    }

    val face = new BlockFace()
    def renderCuboid(pos:Vector3, ccrs:CCRenderState, mat:IMicroMaterial, layer:BlockRenderLayer, c:Cuboid6, faces:Int)
    {
        MicroMaterialRegistry.loadIcons()

        ccrs.setModel(face)
        for(s <- 0 until 6 if (faces & 1<<s) == 0) {
            face.loadCuboidFace(c, s)
            val ops = mat.getMicroRenderOps(pos, s, layer, c)
            for (opSet <- ops)
                ccrs.render(opSet:_*)
        }
    }

    def getQuadsList(pos:Vector3, mat:IMicroMaterial, layer:BlockRenderLayer, c:Cuboid6, faces:Int) =
    {
        val list = Seq.newBuilder[BakedQuad]

        for(s <- 0 until 6 if (faces & 1<<s) == 0) {
            face.loadCuboidFace(c, s)
            val ops = mat.getMicroRenderOps(pos, s, layer, c)
            for (opSet <- ops)
                list ++= CCModelBakery.bakeModel(face, false, DefaultVertexFormats.ITEM, 0, face.getVertices.length, opSet:_*)
        }

        list.result()
    }
}
