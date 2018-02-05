package codechicken.multipart.asm

import java.util.{BitSet => JBitSet}

object ASMImplicits {

    implicit class ExtBitSet(val bitset: JBitSet) extends AnyVal {
        def set(b: JBitSet) = {
            bitset.clear()
            bitset.or(b)
            bitset
        }

        def copy = new JBitSet().set(bitset)
    }

    def nodeName(name: String) = if (name == null) null else name.replace('.', '/')

    implicit class ExtClass(val clazz: Class[_]) extends AnyVal {
        def nodeName = ASMImplicits.nodeName(clazz.getName)
    }

}
