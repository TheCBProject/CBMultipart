package codechicken.microblock

import codechicken.lib.render.pipeline.IVertexOperation
import codechicken.lib.vec.uv.{IconTransformation, UVTranslation}
import codechicken.lib.vec.{Cuboid6, Vector3}
import codechicken.microblock.api.BlockMicroMaterial
import net.minecraft.block.{Block, BlockState, Blocks}
import net.minecraft.client.renderer.RenderType

class GrassMicroMaterial extends BlockMicroMaterial(Blocks.GRASS_BLOCK.getDefaultState) {
    var sideIconT: IconTransformation = _

    override def loadIcons() {
        super.loadIcons()
        //sideIconT = new IconTransformation(TextureUtils.getIconsForBlock(Blocks.GRASS.getDefaultState, 2)(1))
    }

    override def getMicroRenderOps(pos: Vector3, side: Int, layer: RenderType, bounds: Cuboid6) = {
        val list = Seq.newBuilder[Seq[IVertexOperation]]

        if (side == 1) {
            list += MaterialRenderHelper.instance.start(layer, icont).blockColour(getColour(layer)).lighting().result()
        } else {
            list += MaterialRenderHelper.instance.start(layer, icont).lighting().result()
        }

        if (side > 1) {
            list += MaterialRenderHelper.instance.start(layer, new UVTranslation(0, bounds.max.y - 1) ++ sideIconT)
                .blockColour(getColour(layer)).lighting().result()
        }

        list.result()
    }
}

class TopMicroMaterial($state: BlockState) extends BlockMicroMaterial($state) {
    def this(b: Block) = this(b.getDefaultState)

    override def getMicroRenderOps(pos: Vector3, side: Int, layer: RenderType, bounds: Cuboid6) = {
        val list = Seq.newBuilder[Seq[IVertexOperation]]

        if (side <= 1) {
            list += MaterialRenderHelper.instance.start(layer, icont).blockColour(getColour(layer)).lighting().result()
        } else {
            list += MaterialRenderHelper.instance.start(layer, new UVTranslation(0, bounds.max.y - 1) ++ icont)
                .blockColour(getColour(layer)).lighting().result()
        }

        list.result()
    }
}
