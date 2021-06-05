package codechicken.microblock

import java.util.Collections
import codechicken.lib.data.MCDataInput
import codechicken.lib.raytracer.VoxelShapeCache
import codechicken.lib.render.CCRenderState
import codechicken.lib.vec.Rotation._
import codechicken.lib.vec.Vector3._
import codechicken.lib.vec._
import codechicken.microblock.api.MicroOcclusion
import codechicken.microblock.handler.MicroblockModContent
import codechicken.multipart._
import codechicken.multipart.api.part.{TEdgePart, TMultiPart, TNormalOcclusionPart, TPartialOcclusionPart}
import codechicken.multipart.block.TileMultiPart
import codechicken.multipart.util.PartRayTraceResult
import net.minecraft.client.renderer.RenderType
import net.minecraft.util.math.shapes.{ISelectionContext, VoxelShape}

object EdgePlacement extends PlacementProperties {

    import codechicken.multipart.util.PartMap._

    override def microFactory = EdgeMicroFactory

    override def placementGrid = EdgePlacementGrid

    override def opposite(slot: Int, side: Int): Int = {
        if (slot < 0) { //custom placement
            return slot
        }
        val e = slot - 15
        15 + packEdgeBits(e, unpackEdgeBits(e) ^ (1 << (side >> 1)))
    }

    override def customPlacement(pmt: MicroblockPlacement): ExecutablePlacement = {
        if (pmt.size % 2 == 1) return null

        val part = PostMicroFactory.create(pmt.world.isClientSide, pmt.material)
        part.setShape(pmt.size, pmt.hit.getDirection.ordinal >> 1)
        if (pmt.doExpand) {
            val hpart = pmt.hit.asInstanceOf[PartRayTraceResult].part
            if (hpart.getType == PostMicroFactory.getType) {
                val mpart = hpart.asInstanceOf[Microblock]
                if (mpart.material == pmt.material && mpart.getSize + pmt.size < 8) {
                    part.shape = ((mpart.getSize + pmt.size) << 4 | mpart.getShapeSlot).toByte
                    return pmt.expand(mpart, part)
                }
            }
        }

        if (pmt.slot >= 0) {
            return null
        }

        if (pmt.internal && !pmt.oppMod) {
            return pmt.internalPlacement(pmt.htile.asInstanceOf[TileMultiPart], part)
        }

        pmt.externalPlacement(part)
    }
}

object EdgeMicroFactory extends CommonMicroFactory {
    var aBounds: Array[Cuboid6] = new Array(256)

    for (s <- 0 until 12) {
        val rx = if ((s & 2) != 0) -1 else 1
        val rz = if ((s & 1) != 0) -1 else 1
        val transform = new TransformationList(new Scale(new Vector3(rx, 1, rz)), AxisCycle.cycles(s >> 2)).at(CENTER)

        for (t <- 1 until 8) {
            val d = t / 8D
            aBounds(t << 4 | s) = new Cuboid6(0, 0, 0, d, 1, d).apply(transform)
        }
    }

    override def itemSlot = 15

    override def getType = MicroblockModContent.edgeMultiPartType

    override def baseTrait = classOf[EdgeMicroblock]

    override def clientTrait = classOf[CommonMicroblockClient]

    override def placementProperties = EdgePlacement

    override def getResistanceFactor = 0.5F
}

trait EdgeMicroblock extends CommonMicroblock with TEdgePart {
    override def setShape(size: Int, slot: Int) = shape = (size << 4 | (slot - 15)).toByte

    override def microFactory = EdgeMicroFactory

    override def getBounds = EdgeMicroFactory.aBounds(shape)

    override def getSlot = getShapeSlot + 15
}

object PostMicroFactory extends MicroblockFactory {
    var aBounds: Array[Cuboid6] = new Array(256)
    var aShapes: Array[VoxelShape] = new Array(256)

    for (s <- 0 until 3) {
        val transform = sideRotations(s << 1).at(CENTER)
        for (t <- 2 until 8 by 2) {
            val d1 = 0.5 - t / 16D
            val d2 = 0.5 + t / 16D
            aBounds(t << 4 | s) = new Cuboid6(d1, 0, d1, d2, 1, d2).apply(transform)
            aShapes(t << 4 | s) = VoxelShapeCache.getShape(aBounds(t << 4 | s))
        }
    }

    override def getType = MicroblockModContent.postMultiPartType

    override def baseTrait = classOf[PostMicroblock]

    override def clientTrait = classOf[PostMicroblockClient]

    override def getResistanceFactor = 0.5F
}

trait PostMicroblockClient extends PostMicroblock with MicroblockClient {
    var renderBounds1: Cuboid6 = _
    var renderBounds2: Cuboid6 = _

    override def render(layer: RenderType, ccrs: CCRenderState): Unit = {
        if (layer == null) {
            MicroblockRender.renderCuboid(ccrs, getMaterial, layer, getBounds, 0)
        } else {
            MicroblockRender.renderCuboid(ccrs, getMaterial, layer, renderBounds1, 0)
            if (renderBounds2 != null) {
                MicroblockRender.renderCuboid(ccrs, getMaterial, layer, renderBounds2, 0)
            }
        }
    }

    override def onPartChanged(part: TMultiPart): Unit = {
        recalcBounds()
    }

    override def onAdded(): Unit = {
        super.onAdded()
        recalcBounds()
    }

    override def readUpdate(packet: MCDataInput): Unit = {
        super.readUpdate(packet)
        recalcBounds()
    }

    def recalcBounds(): Unit = {
        renderBounds1 = getBounds.copy
        renderBounds2 = null

        shrinkFace(getShapeSlot << 1)
        shrinkFace(getShapeSlot << 1 | 1)

        tile.getPartList.forEach {
            case post: PostMicroblock if post != this =>
                shrinkPost(post)
            case _ =>
        }
    }

    def shrinkFace(fside: Int): Unit = {
        val part = tile.getSlottedPart(fside)
        if (part != null && part.isInstanceOf[FaceMicroblock]) {
            MicroOcclusion.shrink(renderBounds1, part.asInstanceOf[CommonMicroblock].getBounds, fside)
        }
    }

    def shrinkPost(post: PostMicroblock): Unit = {
        if (post == this) {
            return
        }

        if (thisShrinks(post)) {
            if (renderBounds2 == null) {
                renderBounds2 = getBounds.copy
            }
            MicroOcclusion.shrink(renderBounds1, post.getBounds, getShapeSlot << 1 | 1)
            MicroOcclusion.shrink(renderBounds2, post.getBounds, getShapeSlot << 1)
        }
    }

    def thisShrinks(other: PostMicroblock): Boolean = {
        if (getSize != other.getSize) return getSize < other.getSize
        if (isTransparent != other.isTransparent) return isTransparent
        getShapeSlot > other.getShapeSlot
    }
}

trait PostMicroblock extends Microblock with TPartialOcclusionPart with TNormalOcclusionPart {
    override def microFactory = PostMicroFactory

    override def getBounds = PostMicroFactory.aBounds(shape)

    override def getShape(context: ISelectionContext) = PostMicroFactory.aShapes(shape)

    override def getOcclusionShape = PostMicroFactory.aShapes(shape)

    override def getPartialOcclusionShape = getOcclusionShape

    override def itemFactoryID = EdgeMicroFactory.getFactoryID

    override def occlusionTest(npart: TMultiPart): Boolean = {
        if (npart.isInstanceOf[PostMicroblock]) {
            return npart.asInstanceOf[PostMicroblock].getShapeSlot != getShapeSlot
        }

        if (npart.isInstanceOf[FaceMicroblock]) {
            if ((npart.asInstanceOf[CommonMicroblock].getSlot >> 1) == getShapeSlot) {
                return true
            }
        }

        super.occlusionTest(npart)
    }

    def getResistanceFactor = PostMicroFactory.getResistanceFactor
}
