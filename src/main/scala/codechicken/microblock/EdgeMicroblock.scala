package codechicken.microblock

import codechicken.lib.data.MCDataInput
import codechicken.lib.render.CCRenderState
import codechicken.lib.vec.Rotation._
import codechicken.lib.vec.Vector3._
import codechicken.lib.vec._
import codechicken.multipart._
import net.minecraft.util.{BlockRenderLayer, ResourceLocation}

import scala.collection.JavaConversions._

object EdgePlacement extends PlacementProperties {

    import codechicken.multipart.PartMap._

    def microFactory = EdgeMicroFactory

    def placementGrid = EdgePlacementGrid

    def opposite(slot: Int, side: Int): Int = {
        if (slot < 0) //custom placement
        {
            return slot
        }
        val e = slot - 15
        15 + packEdgeBits(e, unpackEdgeBits(e) ^ (1 << (side >> 1)))
    }

    override def customPlacement(pmt: MicroblockPlacement): ExecutablePlacement = {
        if (pmt.size % 2 == 1) return null

        val part = PostMicroFactory.create(pmt.world.isRemote, pmt.material)
        part.setShape(pmt.size, pmt.hit.sideHit.ordinal >> 1)
        if (pmt.doExpand) {
            val hpart = pmt.htile.partList(pmt.hit.asInstanceOf[PartRayTraceResult].partIndex)
            if (hpart.getType == PostMicroFactory.getName) {
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
            return pmt.internalPlacement(pmt.htile.asInstanceOf[TileMultipart], part)
        }

        pmt.externalPlacement(part)
    }
}

object EdgeMicroFactory extends CommonMicroFactory {
    var aBounds: Array[Cuboid6] = new Array(256)

    for (s <- 0 until 12) {
        val rx = if ((s & 2) != 0) -1 else 1
        val rz = if ((s & 1) != 0) -1 else 1
        val transform = new TransformationList(new Scale(new Vector3(rx, 1, rz)), AxisCycle.cycles(s >> 2)).at(center)

        for (t <- 1 until 8) {
            val d = t / 8D
            aBounds(t << 4 | s) = new Cuboid6(0, 0, 0, d, 1, d).apply(transform)
        }
    }

    override def itemSlot = 15

    def getName = new ResourceLocation("ccmb:mcr_edge")

    def baseTrait = classOf[EdgeMicroblock]

    def clientTrait = classOf[CommonMicroblockClient]

    def placementProperties = EdgePlacement

    def getResistanceFactor = 0.5F
}

trait EdgeMicroblock extends CommonMicroblock with TEdgePart {
    override def setShape(size: Int, slot: Int) = shape = (size << 4 | (slot - 15)).toByte

    def microFactory = EdgeMicroFactory

    def getBounds = EdgeMicroFactory.aBounds(shape)

    override def getSlot = getShapeSlot + 15
}

object PostMicroFactory extends MicroblockFactory {
    var aBounds: Array[Cuboid6] = new Array(256)

    for (s <- 0 until 3) {
        val transform = sideRotations(s << 1).at(center)
        for (t <- 2 until 8 by 2) {
            val d1 = 0.5 - t / 16D
            val d2 = 0.5 + t / 16D
            aBounds(t << 4 | s) = new Cuboid6(d1, 0, d1, d2, 1, d2).apply(transform)
        }
    }

    def getName = new ResourceLocation("ccmb:mcr_post")

    def baseTrait = classOf[PostMicroblock]

    def clientTrait = classOf[PostMicroblockClient]

    def getResistanceFactor = 0.5F
}

trait PostMicroblockClient extends PostMicroblock with MicroblockClient {
    var renderBounds1: Cuboid6 = _
    var renderBounds2: Cuboid6 = _

    override def render(pos: Vector3, layer: BlockRenderLayer, ccrs: CCRenderState) {
        val mat = getIMaterial
        if (layer == null) {
            MicroblockRender.renderCuboid(pos, ccrs, mat, layer, getBounds, 0)
        } else {
            MicroblockRender.renderCuboid(pos, ccrs, mat, layer, renderBounds1, 0)
            if (renderBounds2 != null) {
                MicroblockRender.renderCuboid(pos, ccrs, mat, layer, renderBounds2, 0)
            }
        }
    }

    override def onPartChanged(part: TMultiPart) {
        recalcBounds()
    }

    override def onAdded() {
        super.onAdded()
        recalcBounds()
    }

    override def read(packet: MCDataInput) {
        super.read(packet)
        recalcBounds()
    }

    def recalcBounds() {
        renderBounds1 = getBounds.copy
        renderBounds2 = null

        shrinkFace(getShapeSlot << 1)
        shrinkFace(getShapeSlot << 1 | 1)

        tile.partList.foreach {
            case post: PostMicroblock if post != this =>
                shrinkPost(post)
            case _ =>
        }
    }

    def shrinkFace(fside: Int) {
        val part = tile.partMap(fside)
        if (part != null && part.isInstanceOf[FaceMicroblock]) {
            MicroOcclusion.shrink(renderBounds1, part.asInstanceOf[CommonMicroblock].getBounds, fside)
        }
    }

    def shrinkPost(post: PostMicroblock) {
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
    def microFactory = PostMicroFactory

    def getBounds = PostMicroFactory.aBounds(shape)

    def getOcclusionBoxes = Seq(getBounds)

    def getPartialOcclusionBoxes = getOcclusionBoxes

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

    override def canPlaceTorchOnTop = getShapeSlot == 0
}
