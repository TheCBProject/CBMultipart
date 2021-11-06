package codechicken.microblock.handler

import codechicken.lib.config.{ConfigTag, StandardConfigFile}
import codechicken.microblock.handler.MicroblockMod._
import codechicken.microblock.{ConfigContent, MicroMaterialRegistry}
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.event.lifecycle._
import net.minecraftforge.scorge.lang.ScorgeModLoadingContext

import java.io.File
import java.nio.file.Paths

object MicroblockMod {
    final val modId = "cb_microblock"
    private var instance: MicroblockMod = _

    def config = instance.config

    def proxy = instance.proxy
}

class MicroblockMod {

    instance = this

    final val config: ConfigTag = new StandardConfigFile(Paths.get("config", "CBMicroblock.cfg")).load()
    final val proxy: MicroblockProxy = DistExecutor.runForDist(
        () => () => new MicroblockProxyClient().asInstanceOf[MicroblockProxy],
        () => () => new MicroblockProxyServer().asInstanceOf[MicroblockProxy]
    )

    MicroMaterialRegistry.init(ScorgeModLoadingContext.get.getModEventBus)
    ScorgeModLoadingContext.get.getModEventBus.register(this)
    ScorgeModLoadingContext.get.getModEventBus.register(MicroblockModContent)
    ScorgeModLoadingContext.get.getModEventBus.register(DataGenerators)
    MinecraftForge.EVENT_BUS.register(this)


    @SubscribeEvent
    def onCommonSetup(event: FMLCommonSetupEvent): Unit = {
        proxy.commonSetup(event)
        ConfigContent.parse(new File("./config"))
        //ConfigContent.load();
    }

    @SubscribeEvent
    def onClientSetup(event: FMLClientSetupEvent): Unit = {
        proxy.clientSetup(event)
    }

    @SubscribeEvent
    def onServerSetup(event: FMLDedicatedServerSetupEvent): Unit = {
        proxy.serverSetup(event)
    }

    @SubscribeEvent
    def onLoadComplete(event: FMLLoadCompleteEvent): Unit = {
        proxy.loadComplete(event)
    }

    @SubscribeEvent
    def onProcessIMC(event: InterModProcessEvent): Unit = {
        MicroblockModContent.processIMC(MicroMaterialRegistry.MICRO_MATERIALS, event.getIMCStream)
    }
}
