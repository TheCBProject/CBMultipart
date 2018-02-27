package codechicken.multipart.scalatraits

import java.util.{LinkedList => JLinkedList}

import codechicken.lib.render.CCRenderState
import codechicken.lib.vec.{Cuboid6, Vector3}
import codechicken.multipart._

import scala.collection.JavaConversions._


trait TTESRRenderTile extends TileMultipartClient {

    var fastRenderParts = new JLinkedList[TFastRenderPart]
    var dynamicRenderParts = new JLinkedList[TDynamicRenderPart]

    override def copyFrom(that: TileMultipart) {
        super.copyFrom(that)
        that match {
            case tile: TTESRRenderTile =>
                fastRenderParts = tile.fastRenderParts
                dynamicRenderParts = tile.dynamicRenderParts
            case _ =>
        }
    }

    override def bindPart(part: TMultiPart) {
        super.bindPart(part)
        part match {
            case fastPart: TFastRenderPart => fastRenderParts += fastPart
            case _ =>
        }
        part match {
            case dynPart: TDynamicRenderPart => dynamicRenderParts += dynPart
            case _ =>
        }
    }

    override def partRemoved(part: TMultiPart, p: Int) {
        super.partRemoved(part, p)
        part match {
            case fastPart: TFastRenderPart => fastRenderParts -= fastPart
            case _ =>
        }
        part match {
            case dynPart: TDynamicRenderPart => dynamicRenderParts -= dynPart
            case _ =>
        }
    }

    override def clearParts() {
        super.clearParts()
        fastRenderParts.clear()
        dynamicRenderParts.clear()
    }

    def renderFast(pos: Vector3, pass: Int, frameDelta: Float, ccrs: CCRenderState) {
        fastRenderParts.filter(_.canRenderFast(pass)).foreach(_.renderFast(ccrs, pos, pass, frameDelta))
    }

    def renderDynamic(pos: Vector3, pass: Int, frameDelta: Float) {
        dynamicRenderParts.filter(_.canRenderDynamic(pass)).foreach(_.renderDynamic(pos, pass, frameDelta))
    }

    //Only fast render when we have no dyn parts. We can emulate and "obtain" the fast buffer.
    override def hasFastRenderer = fastRenderParts.nonEmpty && dynamicRenderParts.isEmpty

    override def shouldRenderInPass(pass: Int) = fastRenderParts.exists(_.canRenderFast(pass)) || dynamicRenderParts.exists(_.canRenderDynamic(pass))

    override def getRenderBoundingBox = {
        val c = Cuboid6.full.copy

        def process(part: TTESRPart) = c.enclose(part.getRenderBounds)

        fastRenderParts.foreach(process)
        dynamicRenderParts.foreach(process)
        c.add(getPos).aabb
    }
}
