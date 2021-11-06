package codechicken.microblock.handler

import codechicken.lib.model.ModelRegistryHelper
import codechicken.microblock._
import codechicken.microblock.handler.MicroblockMod.config
import codechicken.microblock.handler.MicroblockModContent.itemMicroBlock
import codechicken.microblock.handler.MicroblockProxy.showAllMicroblocks
import net.minecraft.client.renderer.model.ModelResourceLocation
import net.minecraft.item.Item
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.event.lifecycle.{FMLClientSetupEvent, FMLCommonSetupEvent}
import net.minecraftforge.scorge.lang.ScorgeModLoadingContext
import org.apache.logging.log4j.{LogManager, Logger}

import scala.collection.mutable

object MicroblockProxy {
    var logger: Logger = LogManager.getLogger("ForgeMicroBlockCBE")

    var showAllMicroblocks: Boolean = _
}

trait MicroblockProxy {

    def commonSetup(event: FMLCommonSetupEvent): Unit = {}

    def clientSetup(event: FMLClientSetupEvent): Unit = {}

}

class MicroblockProxyServer extends MicroblockProxy {

    protected var saws = mutable.ListBuffer[Item]()
    FaceMicroFactory.register(0)
    HollowMicroFactory.register(1)
    CornerMicroFactory.register(2)
    EdgeMicroFactory.register(3)
    PostMicroFactory.register()

    override def commonSetup(event: FMLCommonSetupEvent): Unit = {
        MicroBlockGenerator.loadAnnotations()
        MicroblockNetwork.init()

        MinecraftForge.EVENT_BUS.register(MicroblockEventHandler)

        showAllMicroblocks = config.getTag("showAllMicroblocks")
            .setComment("Set this to true to show all MicroBlocks in JEI. By default only Stone is shown.")
            .setDefaultBoolean(false)
            .getBoolean
        config.save()
    }
}

class MicroblockProxyClient extends MicroblockProxyServer {

    val modelHelper = new ModelRegistryHelper(ScorgeModLoadingContext.get.getModEventBus)

    override def clientSetup(event: FMLClientSetupEvent): Unit = {
        super.clientSetup(event)
        modelHelper.register(new ModelResourceLocation(itemMicroBlock.getRegistryName, "inventory"), ItemMicroBlockRenderer)
    }
}
