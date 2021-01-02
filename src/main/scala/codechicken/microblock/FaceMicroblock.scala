package codechicken.microblock

import codechicken.lib.raytracer.VoxelShapeCache
import codechicken.lib.render.CCRenderState
import codechicken.lib.vec.Rotation._
import codechicken.lib.vec.Vector3._
import codechicken.lib.vec.{Cuboid6, Vector3}
import codechicken.microblock.handler.MicroblockModContent
import codechicken.multipart.api.part.TFacePart
import net.minecraft.client.renderer.RenderType
import net.minecraft.util.math.shapes.VoxelShape

object FacePlacement extends PlacementProperties {
    override def microFactory = FaceMicroFactory

    override def placementGrid = FacePlacementGrid

    override def opposite(slot: Int, side: Int) = slot ^ 1

    override def expand(slot: Int, side: Int) = sneakOpposite(slot, side)

    override def sneakOpposite(slot: Int, side: Int) = slot == (side ^ 1)
}

class FaceMicroFactory

object FaceMicroFactory extends CommonMicroFactory {
    var aBounds: Array[Cuboid6] = new Array(256)
    var aShapes: Array[VoxelShape] = new Array(256)

    for (s <- 0 until 6) {
        val transform = sideRotations(s).at(CENTER)
        for (t <- 1 until 8) {
            val d = t / 8D
            aBounds(t << 4 | s) = new Cuboid6(0, 0, 0, 1, d, 1).apply(transform)
            aShapes(t << 4 | s) = VoxelShapeCache.getShape(aBounds(t << 4 | s))
        }
    }

    override def getType = MicroblockModContent.faceMultiPartType

    override def itemSlot = 3

    override def baseTrait = classOf[FaceMicroblock]

    override def clientTrait = classOf[FaceMicroblockClient]

    override def placementProperties = FacePlacement

    override def getResistanceFactor = 1
}

trait FaceMicroblockClient extends CommonMicroblockClient {
    override def render(layer: RenderType, ccrs: CCRenderState) {
        if (layer == null) {
            MicroblockRender.renderCuboid(ccrs, getMaterial, layer, getBounds, 0)
        } else if (isTransparent) {
            MicroblockRender.renderCuboid(ccrs, getMaterial, layer, renderBounds, renderMask)
        } else {
            MicroblockRender.renderCuboid(ccrs, getMaterial, layer, renderBounds, renderMask | 1 << getSlot)
            MicroblockRender.renderCuboid(ccrs, getMaterial, layer, Cuboid6.full, ~(1 << getSlot))
        }
    }
}

trait FaceMicroblock extends CommonMicroblock with TFacePart {
    override def microFactory = FaceMicroFactory

    override def getBounds = FaceMicroFactory.aBounds(shape)
}
