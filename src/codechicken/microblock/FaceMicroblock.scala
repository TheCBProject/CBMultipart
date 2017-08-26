package codechicken.microblock

import codechicken.lib.render.CCRenderState
import codechicken.lib.vec.Rotation._
import codechicken.lib.vec.{Cuboid6, Rotation, Vector3}
import codechicken.lib.vec.Vector3._
import codechicken.multipart.TFacePart
import net.minecraft.util.{BlockRenderLayer, ResourceLocation}

object FacePlacement extends PlacementProperties
{
    def microFactory = FaceMicroFactory

    def placementGrid = FacePlacementGrid

    def opposite(slot:Int, side:Int) = slot^1

    override def expand(slot:Int, side:Int) = sneakOpposite(slot, side)

    override def sneakOpposite(slot:Int, side:Int) = slot == (side^1)
}

class FaceMicroFactory

object FaceMicroFactory extends CommonMicroFactory
{
    var aBounds:Array[Cuboid6] = new Array(256)

    for(s <- 0 until 6)
    {
        val transform = sideRotations(s).at(center)
        for(t <- 1 until 8)
        {
            val d = t/8D
            aBounds(t<<4|s) = new Cuboid6(0, 0, 0, 1, d, 1).apply(transform)
        }
    }

    def getName = new ResourceLocation("ccmb:mcr_face")

    def itemSlot = 3

    def baseTrait = classOf[FaceMicroblock]
    def clientTrait = classOf[FaceMicroblockClient]

    def placementProperties = FacePlacement

    def getResistanceFactor = 1
}

trait FaceMicroblockClient extends CommonMicroblockClient
{
    override def render(pos:Vector3, layer:BlockRenderLayer, ccrs:CCRenderState) {
        if(layer == null)
            MicroblockRender.renderCuboid(pos, ccrs, getIMaterial, layer, getBounds, 0)
        else if(isTransparent)
            MicroblockRender.renderCuboid(pos, ccrs, getIMaterial, layer, renderBounds, renderMask)
        else {
            val mat = getIMaterial
            MicroblockRender.renderCuboid(pos, ccrs, mat, layer, renderBounds, renderMask | 1<<getSlot)
            MicroblockRender.renderCuboid(pos, ccrs, mat, layer, Cuboid6.full, ~(1<<getSlot))
        }
    }
}

trait FaceMicroblock extends CommonMicroblock with TFacePart
{
    def microFactory = FaceMicroFactory

    def getBounds = FaceMicroFactory.aBounds(shape)

    override def solid(side:Int) = getIMaterial.isSolid
}
