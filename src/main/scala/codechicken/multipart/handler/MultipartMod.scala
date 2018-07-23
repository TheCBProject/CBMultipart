package codechicken.multipart.handler

import codechicken.lib.CodeChickenLib
import codechicken.multipart.{MultiPartRegistry, TickScheduler, WrappedTileEntityRegistry}
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event._

@Mod(modid = MultipartMod.modID, dependencies = MultipartMod.deps, acceptedMinecraftVersions = CodeChickenLib.MC_VERSION_DEP, modLanguage = "scala", certificateFingerprint = "f1850c39b2516232a2108a7bd84d1cb5df93b261")
object MultipartMod {
    final val modID = "forgemultipartcbe"
    final val deps = CodeChickenLib.MOD_VERSION_DEP

    @EventHandler
    def preInit(event: FMLPreInitializationEvent) {
        MultipartProxy.preInit(event.getModConfigurationDirectory)
        WrappedTileEntityRegistry.init()
    }

    @EventHandler
    def init(event: FMLInitializationEvent) {
        MultipartProxy.init()
    }

    @EventHandler
    def postInit(event: FMLPostInitializationEvent) {
        if (MultiPartRegistry.required) {
            MultiPartRegistry.postInit()
            MultipartProxy.postInit()
        }
    }

    @EventHandler
    def beforeServerStart(event: FMLServerAboutToStartEvent) {
        TickScheduler.onServerStarting(event.getServer)
        MultiPartRegistry.beforeServerStart()
    }
}
