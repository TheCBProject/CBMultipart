package codechicken.multipart.scalatraits

import codechicken.lib.math.MathHelper
import codechicken.multipart.TMultiPart
import codechicken.multipart.INeighborTileChangePart
import codechicken.multipart.TileMultipart
import net.minecraft.util.math.{BlockPos, Vec3i}

/**
 * Mixin implementation for INeighborTileChange
 *
 * Reduces unnecessary computation
 */
trait TTileChangeTile extends TileMultipart
{
    var weakTileChanges = false

    override def copyFrom(that:TileMultipart)
    {
        super.copyFrom(that)
        if(that.isInstanceOf[TTileChangeTile])
            weakTileChanges = that.asInstanceOf[TTileChangeTile].weakTileChanges
    }

    override def bindPart(part:TMultiPart)
    {
        super.bindPart(part)
        if(part.isInstanceOf[INeighborTileChangePart])
            weakTileChanges |= part.asInstanceOf[INeighborTileChangePart].weakTileChanges
    }

    override def clearParts()
    {
        super.clearParts()
        weakTileChanges = false
    }

    override def partRemoved(part:TMultiPart, p:Int)
    {
        super.partRemoved(part, p)
        weakTileChanges = partList.exists(p => p.isInstanceOf[INeighborTileChangePart] && p.asInstanceOf[INeighborTileChangePart].weakTileChanges)
    }

    override def onNeighborTileChange(neighborPos:BlockPos)
    {
        super.onNeighborTileChange(neighborPos)
        val offset = new BlockPos(neighborPos).subtract(new Vec3i(getPos.getX, getPos.getY, getPos.getZ))
        val diff = MathHelper.absSum(offset)
        val side = MathHelper.toSide(offset)

        if(side < 0 || diff <= 0 || diff > 2) return

        val weak = diff == 2
        operate { p =>
            if(p.isInstanceOf[INeighborTileChangePart])
                p.asInstanceOf[INeighborTileChangePart].onNeighborTileChanged(side, weak)
        }
    }

    override def getWeakChanges = weakTileChanges
}
