package codechicken.microblock

import java.util.OptionalDouble

import codechicken.lib.render.buffer.TransformingVertexBuilder
import codechicken.lib.vec.Rotation._
import codechicken.lib.vec.{Matrix4, Rotation, Vector3}
import codechicken.multipart.util.PartMap
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.{IRenderTypeBuffer, RenderState, RenderType}

trait PlacementGrid {
    def getHitSlot(vhit: Vector3, side: Int): Int

    def render(hit: Vector3, side: Int, mStack: MatrixStack, getter: IRenderTypeBuffer) {
        val mat = new Matrix4(mStack)
        transformFace(hit, side, mat)
        val builder = new TransformingVertexBuilder(getter.getBuffer(PlacementGrid.lineType), mat)
        drawLines(builder)
    }

    def drawLines(builder: IVertexBuilder) {}

    def transformFace(hit: Vector3, side: Int, mat: Matrix4) {
        val pos = hit.copy.floor()
        mat.translate(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
        mat.apply(sideRotations(side))
        val rhit = new Vector3(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5).subtract(hit).apply(sideRotations(side ^ 1).inverse)
        mat.translate(0, rhit.y - 0.002, 0)
    }
}

object PlacementGrid {
    val lineType = RenderType.makeType("placement_lines", DefaultVertexFormats.POSITION_COLOR, 1, 256, RenderType.State.getBuilder
        .line(new RenderState.LineState(OptionalDouble.of(2.0)))
        .layer(RenderState.PROJECTION_LAYERING)
        .transparency(RenderState.TRANSLUCENT_TRANSPARENCY)
        .writeMask(RenderState.COLOR_WRITE)
        .build(false)
    )
}

class FaceEdgeGrid(size: Double) extends PlacementGrid {
    override def drawLines(builder: IVertexBuilder) {
        builder.pos(-0.5, 0, -0.5).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(-0.5, 0, 0.5).color(0f, 0f, 0f, 1f).endVertex()

        builder.pos(-0.5, 0, 0.5).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(0.5, 0, 0.5).color(0f, 0f, 0f, 1f).endVertex()

        builder.pos(0.5, 0, 0.5).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(0.5, 0, -0.5).color(0f, 0f, 0f, 1f).endVertex()

        builder.pos(0.5, 0, -0.5).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(-0.5, 0, -0.5).color(0f, 0f, 0f, 1f).endVertex()

        builder.pos(0.5, 0, 0.5).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(size, 0, size).color(0f, 0f, 0f, 1f).endVertex()

        builder.pos(-0.5, 0, 0.5).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(-size, 0, size).color(0f, 0f, 0f, 1f).endVertex()

        builder.pos(0.5, 0, -0.5).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(size, 0, -size).color(0f, 0f, 0f, 1f).endVertex()

        builder.pos(-0.5, 0, -0.5).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(-size, 0, -size).color(0f, 0f, 0f, 1f).endVertex()

        builder.pos(-size, 0, -size).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(-size, 0, size).color(0f, 0f, 0f, 1f).endVertex()

        builder.pos(-size, 0, size).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(size, 0, size).color(0f, 0f, 0f, 1f).endVertex()

        builder.pos(size, 0, size).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(size, 0, -size).color(0f, 0f, 0f, 1f).endVertex()

        builder.pos(size, 0, -size).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(-size, 0, -size).color(0f, 0f, 0f, 1f).endVertex()
    }

    def getHitSlot(vhit: Vector3, side: Int) = {
        val s1 = (side + 2) % 6
        val s2 = (side + 4) % 6
        val u = vhit.copy.add(-0.5, -0.5, -0.5).scalarProject(Rotation.axes(s1))
        val v = vhit.copy.add(-0.5, -0.5, -0.5).scalarProject(Rotation.axes(s2))

        if (Math.abs(u) < size && Math.abs(v) < size) {
            side ^ 1
        } else if (Math.abs(u) > Math.abs(v)) {
            if (u > 0) s1 else s1 ^ 1
        } else if (v > 0) s2 else s2 ^ 1
    }
}

object FacePlacementGrid extends FaceEdgeGrid(1 / 4D)

object CornerPlacementGrid extends PlacementGrid {
    override def drawLines(builder: IVertexBuilder) {
        builder.pos(-0.5, 0, -0.5).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(-0.5, 0, 0.5).color(0f, 0f, 0f, 1f).endVertex()

        builder.pos(-0.5, 0, 0.5).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(0.5, 0, 0.5).color(0f, 0f, 0f, 1f).endVertex()

        builder.pos(0.5, 0, 0.5).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(0.5, 0, -0.5).color(0f, 0f, 0f, 1f).endVertex()

        builder.pos(0.5, 0, -0.5).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(-0.5, 0, -0.5).color(0f, 0f, 0f, 1f).endVertex()

        builder.pos(0, 0, -0.5).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(0, 0, 0.5).color(0f, 0f, 0f, 1f).endVertex()

        builder.pos(-0.5, 0, 0).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(0.5, 0, 0).color(0f, 0f, 0f, 1f).endVertex()
    }

    def getHitSlot(vhit: Vector3, side: Int): Int = {
        val s1 = ((side & 6) + 3) % 6
        val s2 = ((side & 6) + 5) % 6
        val u = vhit.copy().add(-0.5, -0.5, -0.5).scalarProject(Rotation.axes(s1))
        val v = vhit.copy().add(-0.5, -0.5, -0.5).scalarProject(Rotation.axes(s2))

        val bu = if (u >= 0) 1 else 0
        val bv = if (v >= 0) 1 else 0
        val bw = (side & 1) ^ 1

        7 + (
            bw << (side >> 1) |
                bu << (s1 >> 1) |
                bv << (s2 >> 1))
    }
}

object EdgePlacementGrid extends PlacementGrid {
    override def drawLines(builder: IVertexBuilder) {
        builder.pos(-0.5, 0, -0.5).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(-0.5, 0, 0.5).color(0f, 0f, 0f, 1f).endVertex()

        builder.pos(-0.5, 0, 0.5).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(0.5, 0, 0.5).color(0f, 0f, 0f, 1f).endVertex()

        builder.pos(0.5, 0, 0.5).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(0.5, 0, -0.5).color(0f, 0f, 0f, 1f).endVertex()

        builder.pos(0.5, 0, -0.5).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(-0.5, 0, -0.5).color(0f, 0f, 0f, 1f).endVertex()

        builder.pos(0.25, 0, -0.5).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(0.25, 0, 0.5).color(0f, 0f, 0f, 1f).endVertex()

        builder.pos(-0.25, 0, -0.5).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(-0.25, 0, 0.5).color(0f, 0f, 0f, 1f).endVertex()

        builder.pos(-0.5, 0, 0.25).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(0.5, 0, 0.25).color(0f, 0f, 0f, 1f).endVertex()

        builder.pos(-0.5, 0, -0.25).color(0f, 0f, 0f, 1f).endVertex()
        builder.pos(0.5, 0, -0.25).color(0f, 0f, 0f, 1f).endVertex()
    }

    override def getHitSlot(vhit: Vector3, side: Int): Int = {
        val s1 = (side + 2) % 6
        val s2 = (side + 4) % 6
        val u = vhit.copy.add(-0.5, -0.5, -0.5).scalarProject(Rotation.axes(s1))
        val v = vhit.copy.add(-0.5, -0.5, -0.5).scalarProject(Rotation.axes(s2))

        if (Math.abs(u) < 4 / 16D && Math.abs(v) < 4 / 16D) {
            return -1
        }

        if (Math.abs(u) > 4 / 16D && Math.abs(v) > 4 / 16D) {
            return PartMap.edgeBetween(if (u > 0) s1 else s1 ^ 1, if (v > 0) s2 else s2 ^ 1)
        }

        val s = if (Math.abs(u) > Math.abs(v)) {
            if (u > 0) s1 else s1 ^ 1
        } else if (v > 0) s2 else s2 ^ 1

        PartMap.edgeBetween(side ^ 1, s)
    }
}
