package codechicken.multipart.asm

import java.util.{BitSet => JBitSet}

trait ScratchBitSet {
    private val bitSets = new ThreadLocal[JBitSet]

    def getBitSet = bitSets.get match {
        case null =>
            val bitset = new JBitSet
            bitSets.set(bitset)
            bitset
        case bitset => bitset
    }

    def freshBitSet = {
        val bitset = getBitSet
        bitset.clear()
        bitset
    }
}
