package codechicken.microblock

import java.util.function.Supplier

import codechicken.lib.render.BlockRenderer.BlockFace
import codechicken.lib.render.CCRenderState
import codechicken.lib.render.buffer.TransformingVertexBuilder
import codechicken.lib.vec.{Cuboid6, Matrix4, Scale, Vector3}
import codechicken.microblock.api.MicroMaterial
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.{IRenderTypeBuffer, RenderState, RenderType}
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockRayTraceResult

object MicroblockRender {

    val highlighRenderType = RenderType.makeType("cbmp_highlight", DefaultVertexFormats.BLOCK, 7, 255, RenderType.State.getBuilder
        .texture(RenderState.BLOCK_SHEET)
        .transparency(RenderState.TRANSLUCENT_TRANSPARENCY)
        .build(false))

    def renderHighlight(player: PlayerEntity, hand:Hand, hit: BlockRayTraceResult, mcrFactory: CommonMicroFactory, size: Int, material: MicroMaterial, mStack: MatrixStack, getter: IRenderTypeBuffer, partialTicks: Float) {
        mcrFactory.placementProperties.placementGrid.render(new Vector3(hit.getHitVec), hit.getFace.ordinal, mStack, getter)

        val placement = MicroblockPlacement(player, hand, hit, size, material, !player.abilities.isCreativeMode, mcrFactory.placementProperties)
        if (placement == null) {
            return
        }
        val pos = placement.pos
        val part = placement.part.asInstanceOf[MicroblockClient]

        val mat = new Matrix4(mStack)
        mat.translate(pos)
        mat.apply(new Scale(1.002, 1.002, 1.002).at(Vector3.CENTER))

        val ccrs = CCRenderState.instance()
        ccrs.reset()
        ccrs.bind(highlighRenderType, getter, mat)
        ccrs.alphaOverride = 80
        part.render(null, ccrs)
    }

    private val instances = ThreadLocal.withInitial(new Supplier[BlockFace] {
        override def get = new BlockFace
    })

    def face = instances.get()

    def renderCuboid(ccrs: CCRenderState, mat: MicroMaterial, layer: RenderType, c: Cuboid6, faces: Int) {
        MicroMaterialRegistry.loadIcons()

        val f = face
        ccrs.setModel(f)
        for (s <- 0 until 6 if (faces & 1 << s) == 0) {
            f.loadCuboidFace(c, s).computeLightCoords()
            val ops = mat.getMicroRenderOps(s, layer, c)
            for (opSet <- ops)
                ccrs.render(opSet: _*)
        }
    }
}
