package codechicken.multipart

import java.util.{HashMap => JHashMap}

import codechicken.lib.reflect.{ObfMapping, ReflectionManager}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraft.util.registry.RegistryNamespaced

/**
 * Created by covers1624 on 8/04/2017.
 *
 * Soo, THIS IS SOME BULL SHIT, NEVER DO IT EVER!
 *
 * The reason this exists is due to how Tiles are mapped from ResourceLocation to Class in 1.11.
 * Pre 1.11, there was a separate map for ID > Class and Class > ID
 * This allowed FMP to register the id of "savedMultipart" to a holder tile called TileNBTContainer and there was no reverse mapping.
 * FMP then registered all of its auto generated classes to the id of "savedMultipart".
 * So in the registry we had:
 *
 * TileMultipart_cmp$$0 > "savedMultipart"
 * TileMultipart_cmp$$1 > "savedMultipart"
 * TileMultipart_cmp$$2 > "savedMultipart"
 * TileMultipart_cmp$$3 > "savedMultipart"
 * TileMultipart_cmp$$4 > "savedMultipart"
 *
 * "savedMultipart" > TileNBTContainer
 *
 * Then on ChunkDataLoadEvent FMP iterated over all the tiles in search for TileNBTContainers.
 * If found, it grabbed the stored NBT inside TileNBTContainer and constructed a proper multipart tile with the correct traits applied.
 *
 * This is all still true to 1.11, except for the fact that there is no way to do it without wrapping the tile registry,
 * We can then control what the registry returns for getNameForObject allowing us to do what we did before.
 * If someone has a better idea, Please contact us.
 */
object WrappedTileEntityRegistry {

    private val tileRegistry = new ObfMapping("net/minecraft/tileentity/TileEntity", "field_190562_f")

    val wrapped: RegistryNamespaced[ResourceLocation, Class[_ <: TileEntity]] =
        ReflectionManager.getField(tileRegistry, null, classOf[RegistryNamespaced[ResourceLocation, Class[_ <: TileEntity]]])
    val INSTANCE = new DuplicateValueRegistry(wrapped)


    def init() {
        ReflectionManager.setField(tileRegistry, null, INSTANCE)
    }

    def registerMapping(clazz: Class[_ <: TileEntity], key: ResourceLocation) {
        INSTANCE.addMapping(clazz, key)
    }
}
