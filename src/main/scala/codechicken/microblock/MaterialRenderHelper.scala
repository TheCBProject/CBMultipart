package codechicken.microblock

import codechicken.lib.render.CCRenderState
import codechicken.lib.render.pipeline.{ColourMultiplier, IVertexOperation}
import codechicken.lib.vec.uv.UVTransformation
import net.minecraft.client.renderer.RenderType

/**
 * Created by covers1624 on 4/18/20.
 */
class MaterialRenderHelper {
    private var layer: RenderType = null
    private var builder = Seq.newBuilder[IVertexOperation]

    def start(layer: RenderType, uvt: UVTransformation) = {
        this.layer = layer
        builder.clear()
        builder += uvt
        this
    }

    def blockColour(colour: Int) = {
        builder += ColourMultiplier.instance(colour)
        this
    }

    def lighting() = {
        if (layer != null) {
            builder += CCRenderState.instance().lightMatrix
        }
        this
    }

    def result() = builder.result()
}

object MaterialRenderHelper {
    private val instances = new ThreadLocal[MaterialRenderHelper] {
        override def initialValue() = new MaterialRenderHelper
    }

    def instance = instances.get()
}
