package codechicken.multipart

import java.util.{HashMap => JHashMap}

import codechicken.lib.asm.ObfMapping
import codechicken.lib.util.ReflectionManager
import codechicken.lib.util.registry.DuplicateValueRegistry
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{IntIdentityHashBiMap, ResourceLocation}
import net.minecraftforge.fml.common.registry.{GameData, LegacyNamespacedRegistry}

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

    val wrapped: LegacyNamespacedRegistry[Class[_ <: TileEntity]] = GameData.getTileEntityRegistry
    val INSTANCE = new DuplicateValueRegistry(wrapped)


    def init(): Unit = {
        val gd = "net/minecraftforge/fml/common/registry/GameData"
        val te = "net/minecraft/tileentity/TileEntity"
        val lnsr = "net/minecraftforge/fml/common/registry/LegacyNamespacedRegistry"
        val rns = "net/minecraft/util/registry/RegistryNamespaced"
        val rs = "net/minecraft/util/registry/RegistrySimple"

        var mapping = new ObfMapping(gd, "getMain", s"()L$gd;")

        val gameData: GameData = ReflectionManager.callMethod(mapping, classOf[GameData], null)

        mapping = new ObfMapping(rs, "field_82596_a")
        ReflectionManager.setField(mapping, INSTANCE, ReflectionManager.getField(mapping, wrapped, classOf[Map[ResourceLocation, Class[_ <: TileEntity]]]))
        mapping = new ObfMapping(rs, "field_186802_b")
        ReflectionManager.setField(mapping, INSTANCE, null)
        mapping = new ObfMapping(rns, "field_148759_a")
        ReflectionManager.setField(mapping, INSTANCE, ReflectionManager.getField(mapping, wrapped, classOf[IntIdentityHashBiMap[Class[_ <: TileEntity]]]))
        mapping = new ObfMapping(rns, "field_148758_b")
        ReflectionManager.setField(mapping, INSTANCE, ReflectionManager.getField(mapping, wrapped, classOf[Map[Class[_ <: TileEntity], ResourceLocation]]))
        mapping = new ObfMapping(lnsr, "legacy_names")
        ReflectionManager.setField(mapping, INSTANCE, ReflectionManager.getField(mapping, wrapped, classOf[Map[ResourceLocation, ResourceLocation]]))
        mapping = new ObfMapping(gd, "iTileEntityRegistry")
        ReflectionManager.setField(mapping, gameData, INSTANCE)
        mapping = new ObfMapping(te, "field_190562_f")
        ReflectionManager.setField(mapping, null, INSTANCE)
    }

    def registerMapping(clazz: Class[_ <: TileEntity], key: ResourceLocation): Unit = {
        INSTANCE.addMapping(clazz, key)
    }
}
