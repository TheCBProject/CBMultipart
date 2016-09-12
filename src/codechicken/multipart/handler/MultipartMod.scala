package codechicken.multipart.handler

import codechicken.multipart.MultiPartRegistry
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event._

@Mod(modid = MultipartMod.modID, acceptedMinecraftVersions = "[1.10]", modLanguage = "scala")
object MultipartMod
{
    final val modID = "forgemultipartcbe"

    @EventHandler
    def preInit(event:FMLPreInitializationEvent) {
        MultipartProxy.preInit(event.getModConfigurationDirectory, event.getModLog)
    }

    @EventHandler
    def init(event:FMLInitializationEvent) {
        MultipartProxy.init()
    }

    @EventHandler
    def postInit(event:FMLPostInitializationEvent) {
        if (MultiPartRegistry.required) {
            MultiPartRegistry.postInit()
            MultipartProxy.postInit()
        }
    }

    @EventHandler
    def beforeServerStart(event:FMLServerAboutToStartEvent) {
        MultiPartRegistry.beforeServerStart()
    }
}