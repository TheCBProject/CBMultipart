package codechicken.multipart.capability

import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

/**
 * Created by covers1624 on 6/10/18.
 */
class CapHolder[T >: Any] {

    var cap: Capability[T] = _
    var generic: T = _
    var sided = Map.empty[EnumFacing, T]
}
