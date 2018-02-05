package codechicken.multipart

import java.lang.Iterable

import codechicken.lib.raytracer.IndexedCuboid6
import codechicken.lib.render.{BlockRenderer, CCRenderState}
import codechicken.lib.vec.uv.IconTransformation
import codechicken.lib.vec.{Cuboid6, Vector3}
import net.minecraft.client.renderer.texture.TextureAtlasSprite

import scala.collection.JavaConversions._

/**
 * Trait for parts that are simply a cuboid, having one bounding box. Overrides TMultiPart functions to this effect.
 *
 * If using in Java, manually copy the implementation from below.
 */
trait TCuboidPart extends TMultiPart {
    /**
     * Return the bounding Cuboid6 for this part.
     */
    def getBounds: Cuboid6

    override def getSubParts: Iterable[IndexedCuboid6] = Seq(new IndexedCuboid6(0, getBounds))

    override def getCollisionBoxes: Iterable[Cuboid6] = Seq(getBounds)

    override def renderBreaking(pos: Vector3, texture: TextureAtlasSprite, ccrs: CCRenderState) {
        ccrs.setPipeline(pos.translation(), new IconTransformation(texture))
        BlockRenderer.renderCuboid(ccrs, getBounds, 0)
    }
}
