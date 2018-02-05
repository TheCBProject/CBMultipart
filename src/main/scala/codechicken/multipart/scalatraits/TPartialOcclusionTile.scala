package codechicken.multipart.scalatraits

import codechicken.multipart.{PartialOcclusionTest, TMultiPart, TPartialOcclusionPart, TileMultipart}

/**
 * Implementation for the partial occlusion test.
 */
class TPartialOcclusionTile extends TileMultipart {
    override def occlusionTest(parts: Seq[TMultiPart], npart: TMultiPart): Boolean = {
        if (npart.isInstanceOf[TPartialOcclusionPart] && !partialOcclusionTest(parts :+ npart)) {
            return false
        }

        super.occlusionTest(parts, npart)
    }

    def partialOcclusionTest(parts: Seq[TMultiPart]): Boolean = {
        val test = new PartialOcclusionTest(parts.length)
        var i = 0
        while (i < parts.length) {
            val part = parts(i)
            if (part.isInstanceOf[TPartialOcclusionPart]) {
                test.fill(i, part.asInstanceOf[TPartialOcclusionPart])
            }
            i += 1
        }
        test()
    }
}
