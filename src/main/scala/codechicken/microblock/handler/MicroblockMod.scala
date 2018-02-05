package codechicken.microblock.handler

import codechicken.lib.CodeChickenLib
import codechicken.microblock.{ConfigContent, DefaultContent, MicroMaterialRegistry}
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent, FMLServerAboutToStartEvent}

import scala.collection.JavaConversions._

@Mod(modid = "microblockcbe", acceptedMinecraftVersions = CodeChickenLib.MC_VERSION_DEP, dependencies = "required-after:forgemultipartcbe", modLanguage = "scala")
object MicroblockMod {
    @EventHandler
    def preInit(event: FMLPreInitializationEvent) {
        MicroblockProxy.preInit()
        DefaultContent.load()
        ConfigContent.parse(event.getModConfigurationDirectory)
    }

    @EventHandler
    def init(event: FMLInitializationEvent) {
        MicroblockProxy.init()
        ConfigContent.load()
    }

    @EventHandler
    def postInit(event: FMLPostInitializationEvent) {
        MicroMaterialRegistry.setupIDMap()
        MicroblockProxy.postInit()
    }

    @EventHandler
    def beforeServerStart(event: FMLServerAboutToStartEvent) {
        MicroMaterialRegistry.setupIDMap()
    }

    @EventHandler
    def handleIMC(event: IMCEvent) {
        ConfigContent.handleIMC(event.getMessages)
    }
}
