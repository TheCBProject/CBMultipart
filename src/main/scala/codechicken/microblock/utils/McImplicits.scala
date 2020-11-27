package codechicken.microblock.utils

import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.{IForgeRegistry, IForgeRegistryEntry}

import scala.language.implicitConversions

/**
 * Created by covers1624 on 4/15/20.
 */
trait McImplicits {

    implicit class ForgeRegistryImplicits[T <: IForgeRegistryEntry[T]](self: IForgeRegistry[T]) {
        def register(thing: T, name: String): T = register(thing, new ResourceLocation(name))

        def register(thing: T, name: ResourceLocation): T = {
            self.register(thing.setRegistryName(name))
            thing
        }
    }

}
