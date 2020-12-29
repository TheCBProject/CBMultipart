package codechicken.microblock

import codechicken.lib.raytracer.{IndexedCuboid6, SubHitVoxelShape, VoxelShapeCache}
import codechicken.lib.render.{CCRenderState, RenderUtils}
import codechicken.lib.vec.Rotation._
import codechicken.lib.vec.Vector3._
import codechicken.lib.vec.{Cuboid6, Matrix4, Vector3}
import codechicken.microblock.api.{ISidedHollowConnect, MicroMaterial}
import codechicken.microblock.handler.MicroblockModContent
import codechicken.multipart.api.part.{TFacePart, TNormalOcclusionPart}
import codechicken.multipart.util.PartRayTraceResult
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.{ActiveRenderInfo, IRenderTypeBuffer, RenderType}
import net.minecraft.util.math.shapes.{VoxelShape, VoxelShapes}
import org.lwjgl.opengl.GL11

import scala.jdk.CollectionConverters._

object HollowPlacement extends PlacementProperties {

    object HollowPlacementGrid extends FaceEdgeGrid(3 / 8D)

    def microFactory = HollowMicroFactory

    def placementGrid = HollowPlacementGrid

    def opposite(slot: Int, side: Int) = slot ^ 1

    override def expand(slot: Int, side: Int) = sneakOpposite(slot, side)

    override def sneakOpposite(slot: Int, side: Int) = slot == (side ^ 1)
}

object HollowMicroFactory extends CommonMicroFactory {
    var pBoxes: Array[Seq[Cuboid6]] = new Array(256)
    var pShape: Array[VoxelShape] = new Array(256)
    var occBounds: Array[Cuboid6] = new Array(256)
    for (s <- 0 until 6) {
        val transform = sideRotations(s).at(CENTER)
        for (t <- 1 until 8) {
            val d = t / 8D
            val w1 = 1 / 8D
            val w2 = 3 / 16D
            pBoxes(t << 4 | s) = Seq(
                new Cuboid6(0, 0, 0, w1, d, 1),
                new Cuboid6(1 - w1, 0, 0, 1, d, 1),
                new Cuboid6(w1, 0, 0, 1 - w1, d, w1),
                new Cuboid6(w1, 0, 1 - w1, 1 - w1, d, 1))
                .map(_.apply(transform))
            occBounds(t << 4 | s) = new Cuboid6(1 / 8D, 0, 1 / 8D, 7 / 8D, d, 7 / 8D).apply(transform)
            pShape(t << 4 | s) = pBoxes(t << 4 | s).map(VoxelShapeCache.getShape).reduce(VoxelShapes.or)
        }
    }

    override def getType = MicroblockModContent.hollowMultiPartType

    override def baseTrait = classOf[HollowMicroblock]

    override def clientTrait = classOf[HollowMicroblockClient]

    override def itemSlot = 3

    override def placementProperties = HollowPlacement

    override def getResistanceFactor = 1
}

trait HollowMicroblockClient extends HollowMicroblock with CommonMicroblockClient {
    renderMask |= 8 << 8

    override def recalcBounds() {
        super.recalcBounds()
        renderMask = renderMask & 0xFF | getHollowSize << 8
    }

    override def render(layer: RenderType, ccrs: CCRenderState) {
        if (layer == null) {
            renderHollow(ccrs, layer, getBounds, 0, false, MicroblockRender.renderCuboid)
        } else if (isTransparent) {
            renderHollow(ccrs, layer, renderBounds, renderMask, false, MicroblockRender.renderCuboid)
        } else {
            renderHollow(ccrs, layer, renderBounds, renderMask | 1 << getSlot, false, MicroblockRender.renderCuboid)
            renderHollow(ccrs, layer, Cuboid6.full, ~(1 << getSlot), true, MicroblockRender.renderCuboid)
        }
    }

    def renderHollow(ccrs: CCRenderState, layer: RenderType, c: Cuboid6, sideMask: Int, face: Boolean, f: (CCRenderState, MicroMaterial, RenderType, Cuboid6, Int) => Unit) {
        val mat = getMaterial
        val size = renderMask >> 8
        val d1 = 0.5 - size / 32D
        val d2 = 0.5 + size / 32D
        val x1 = c.min.x
        val x2 = c.max.x
        val y1 = c.min.y
        val y2 = c.max.y
        val z1 = c.min.z
        val z2 = c.max.z

        var iMask = 0
        getSlot match {
            case 0 | 1 =>
                if (face) {
                    iMask = 0x3C
                }
                f(ccrs, mat, layer, new Cuboid6(d1, y1, d2, d2, y2, z2), 0x3B | iMask) //-z internal
                f(ccrs, mat, layer, new Cuboid6(d1, y1, z1, d2, y2, d1), 0x37 | iMask) //+z internal

                f(ccrs, mat, layer, new Cuboid6(d2, y1, d1, x2, y2, d2), sideMask & 0x23 | 0xC | iMask) //-x internal -y+y+x external
                f(ccrs, mat, layer, new Cuboid6(x1, y1, d1, d1, y2, d2), sideMask & 0x13 | 0xC | iMask) //+x internal -y+y-x external

                f(ccrs, mat, layer, new Cuboid6(x1, y1, d2, x2, y2, z2), sideMask & 0x3B | 4 | iMask) //-y+y+z-x+x external
                f(ccrs, mat, layer, new Cuboid6(x1, y1, z1, x2, y2, d1), sideMask & 0x37 | 8 | iMask) //-y+y-z-x+x external
            case 2 | 3 =>
                if (face) {
                    iMask = 0x33
                }
                f(ccrs, mat, layer, new Cuboid6(d2, d1, z1, x2, d2, z2), 0x2F | iMask) //-x internal
                f(ccrs, mat, layer, new Cuboid6(x1, d1, z1, d1, d2, z2), 0x1F | iMask) //+x internal

                f(ccrs, mat, layer, new Cuboid6(d1, d2, z1, d2, y2, z2), sideMask & 0xE | 0x30 | iMask) //-y internal -z+z+y external
                f(ccrs, mat, layer, new Cuboid6(d1, y1, z1, d2, d1, z2), sideMask & 0xD | 0x30 | iMask) //+y internal -z+z-y external

                f(ccrs, mat, layer, new Cuboid6(d2, y1, z1, x2, y2, z2), sideMask & 0x2F | 0x10 | iMask) //-z+z+x-y+y external
                f(ccrs, mat, layer, new Cuboid6(x1, y1, z1, d1, y2, z2), sideMask & 0x1F | 0x20 | iMask) //-z+z-x-y+y external
            case 4 | 5 =>
                if (face) {
                    iMask = 0xF
                }
                f(ccrs, mat, layer, new Cuboid6(x1, d2, d1, x2, y2, d2), 0x3E | iMask) //-y internal
                f(ccrs, mat, layer, new Cuboid6(x1, y1, d1, x2, d1, d2), 0x3D | iMask) //+y internal

                f(ccrs, mat, layer, new Cuboid6(x1, d1, d2, x2, d2, z2), sideMask & 0x38 | 3 | iMask) //-z internal -x+x+z external
                f(ccrs, mat, layer, new Cuboid6(x1, d1, z1, x2, d2, d1), sideMask & 0x34 | 3 | iMask) //+z internal -x+x-z external

                f(ccrs, mat, layer, new Cuboid6(x1, d2, z1, x2, y2, z2), sideMask & 0x3E | 1 | iMask) //-x+x+y-z+z external
                f(ccrs, mat, layer, new Cuboid6(x1, y1, z1, x2, d1, z2), sideMask & 0x3D | 2 | iMask) //-x+x-y-z+z external
        }
    }

    override def drawHighlight(hit: PartRayTraceResult, info: ActiveRenderInfo, mStack: MatrixStack, getter: IRenderTypeBuffer, partialTicks: Float) = {
        val size = getHollowSize
        val d1 = 0.5 - size / 32D
        val d2 = 0.5 + size / 32D
        val t = (shape >> 4) / 8D

        val mat = new Matrix4(mStack)
        mat.translate(hit.getPos)
        mat.translate(-info.getProjectedView.x, -info.getProjectedView.y, -info.getProjectedView.z)
        mat.apply(sideRotations(shape & 0xF).at(Vector3.CENTER))
        RenderUtils.bufferHitBox(mat, getter, new Cuboid6(0, 0, 0, 1, t, 1).expand(0.001))
        RenderUtils.bufferHitBox(mat, getter, new Cuboid6(d1, 0, d1, d2, t, d2).expand(-0.001))
        true
    }
}

trait HollowMicroblock extends CommonMicroblock with TFacePart with TNormalOcclusionPart {
    override def microFactory = HollowMicroFactory

    override def getBounds: Cuboid6 = FaceMicroFactory.aBounds(shape)

    override def getOutlineShape: VoxelShape = getCollisionShape

    override def getPartialOcclusionShape = HollowMicroFactory.pShape(shape)

    def getHollowSize = tile match {
        case null => 8
        case _ => tile.partMap(6) match {
            case part: ISidedHollowConnect => part.getHollowSize(getSlot)
            case _ => 8
        }
    }

    //TODO, Cache.
    def getOcclusionShape = {
        val size = getHollowSize
        val c = HollowMicroFactory.occBounds(shape)
        val d1 = 0.5 - size / 32D
        val d2 = 0.5 + size / 32D
        val x1 = c.min.x
        val x2 = c.max.x
        val y1 = c.min.y
        val y2 = c.max.y
        val z1 = c.min.z
        val z2 = c.max.z

        getSlot match {
            case 0 | 1 =>
                Seq(new Cuboid6(d2, y1, d1, x2, y2, d2),
                    new Cuboid6(x1, y1, d1, d1, y2, d2),
                    new Cuboid6(x1, y1, d2, x2, y2, z2),
                    new Cuboid6(x1, y1, z1, x2, y2, d1))
                    .map(VoxelShapeCache.getShape)
                    .reduce(VoxelShapes.or)
            case 2 | 3 =>
                Seq(new Cuboid6(d1, d2, z1, d2, y2, z2),
                    new Cuboid6(d1, y1, z1, d2, d1, z2),
                    new Cuboid6(d2, y1, z1, x2, y2, z2),
                    new Cuboid6(x1, y1, z1, d1, y2, z2))
                    .map(VoxelShapeCache.getShape)
                    .reduce(VoxelShapes.or)
            case 4 | 5 =>
                Seq(new Cuboid6(x1, d1, d2, x2, d2, z2),
                    new Cuboid6(x1, d1, z1, x2, d2, d1),
                    new Cuboid6(x1, d2, z1, x2, y2, z2),
                    new Cuboid6(x1, y1, z1, x2, d1, z2))
                    .map(VoxelShapeCache.getShape)
                    .reduce(VoxelShapes.or)
        }
    }

    //TODO, Cache.
    def getCollisionBoxes = {
        val size = getHollowSize
        val d1 = 0.5 - size / 32D
        val d2 = 0.5 + size / 32D
        val t = (shape >> 4) / 8D

        val tr = sideRotations(shape & 0xF).at(CENTER)
        Seq(new Cuboid6(0, 0, 0, 1, t, d1),
            new Cuboid6(0, 0, d2, 1, t, 1),
            new Cuboid6(0, 0, d1, d1, t, d2),
            new Cuboid6(d2, 0, d1, 1, t, d2))
            .map(c => c.apply(tr))
    }

    override def getCollisionShape = getCollisionBoxes.map(VoxelShapeCache.getShape).reduce(VoxelShapes.or)

    override def getRayTraceShape = new SubHitVoxelShape(getCollisionShape, getCollisionBoxes.map(c => new IndexedCuboid6(0, c)).asJava)

    override def allowCompleteOcclusion = true

    override def solid(side: Int) = false

    override def redstoneConductionMap = 0x10
}
