package codechicken.microblock

import codechicken.lib.render.pipeline.IVertexOperation
import codechicken.lib.texture.TextureUtils
import codechicken.lib.vec.uv.{IconTransformation, UVTranslation}
import codechicken.lib.vec.{Cuboid6, Vector3}
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.util.BlockRenderLayer

class GrassMicroMaterial extends BlockMicroMaterial(Blocks.GRASS.getDefaultState)
{
    var sideIconT:IconTransformation = _

    override def loadIcons()
    {
        super.loadIcons()
        sideIconT = new IconTransformation(TextureUtils.getIconsForBlock(Blocks.GRASS.getDefaultState, 2)(1))
    }

    override def getMicroRenderOps(pos:Vector3, side:Int, layer:BlockRenderLayer, bounds:Cuboid6) =
    {
        val list = Seq.newBuilder[Seq[IVertexOperation]]

        if (side == 1)
            list += MaterialRenderHelper.start(pos, layer, icont).blockColour(getColour(layer)).lighting().result()
        else
            list += MaterialRenderHelper.start(pos, layer, icont).lighting().result()

        if(side > 1)
            list += MaterialRenderHelper.start(pos, layer, new UVTranslation(0, bounds.max.y-1) ++ sideIconT)
                .blockColour(getColour(layer)).lighting().result()

        list.result()
    }
}

class TopMicroMaterial($state:IBlockState) extends BlockMicroMaterial($state)
{
    def this(b:Block) = this(b.getDefaultState)

    override def getMicroRenderOps(pos:Vector3, side:Int, layer:BlockRenderLayer, bounds:Cuboid6) =
    {
        val list = Seq.newBuilder[Seq[IVertexOperation]]

        if (side <= 1)
            list += MaterialRenderHelper.start(pos, layer, icont).blockColour(getColour(layer)).lighting().result()
        else
            list += MaterialRenderHelper.start(pos, layer, new UVTranslation(0, bounds.max.y-1) ++ icont)
                        .blockColour(getColour(layer)).lighting().result()

        list.result()
    }
}
