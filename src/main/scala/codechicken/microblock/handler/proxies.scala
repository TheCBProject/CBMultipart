package codechicken.microblock.handler

import codechicken.lib.config.{ConfigTag}
import codechicken.lib.model.ModelRegistryHelper
import codechicken.microblock._
import codechicken.microblock.handler.MicroblockModContent.itemMicroBlock
import net.minecraft.client.renderer.model.ModelResourceLocation
import net.minecraft.item.Item
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.event.lifecycle.{FMLClientSetupEvent, FMLCommonSetupEvent, FMLDedicatedServerSetupEvent, FMLLoadCompleteEvent}
import net.minecraftforge.scorge.lang.ScorgeModLoadingContext
import org.apache.logging.log4j.{LogManager, Logger}

import scala.collection.mutable

object MicroblockProxy {
    var logger: Logger = LogManager.getLogger("ForgeMicroBlockCBE")

    var useSawIcons: Boolean = _
    var showAllMicroparts: Boolean = _
}

trait MicroblockProxy {

    def commonSetup(event: FMLCommonSetupEvent) {}

    def clientSetup(event: FMLClientSetupEvent) {}

    def serverSetup(event: FMLDedicatedServerSetupEvent) {}

    def loadComplete(event: FMLLoadCompleteEvent) {}

}

class MicroblockProxyServer extends MicroblockProxy {

    protected var saws = mutable.ListBuffer[Item]()
    FaceMicroFactory.register(0)
    HollowMicroFactory.register(1)
    CornerMicroFactory.register(2)
    EdgeMicroFactory.register(3)
    PostMicroFactory.register()

    override def commonSetup(event: FMLCommonSetupEvent) {
        MicroBlockGenerator.loadAnnotations()

        //        sawStone = createSaw(config, "saw_stone", 1)
        //        sawIron = createSaw(config, "saw_iron", 2)
        //        sawDiamond = createSaw(config, "saw_diamond", 3)
        //stoneRod = new Item().setUnlocalizedName("microblockcbe:stone_rod").setCreativeTab(CreativeTabs.MATERIALS)
        //ForgeRegistries.ITEMS.register(stoneRod.setRegistryName("stone_rod"))

        //OreDictionary.registerOre("rodStone", stoneRod)
        MicroblockNetwork.init()

        MinecraftForge.EVENT_BUS.register(MicroblockEventHandler)

        //        useSawIcons = config.getTag("useSawIcons").setComment("Set to true to use mc style icons for the saw instead of the 3D model").getBooleanValue(false)
        //        showAllMicroparts = config.getTag("showAllMicroparts").setComment("Set this to true to show all MicroParts in JEI. By default only Stone is shown.").getBooleanValue(false)
    }

    override def loadComplete(event: FMLLoadCompleteEvent) {
        //MicroMaterialRegistry.calcMaxCuttingStrength()
    }

    def createSaw(config: ConfigTag, name: String, strength: Int) = {
        val saw = new ItemSaw(config.getTag(name), strength)
        //            .setUnlocalizedName("microblockcbe:" + name)
        //        ForgeRegistries.ITEMS.register(saw.setRegistryName(name))
        saws += saw
        saw
    }

    //    def addSawRecipe(saw: Item, blade: Item) {
    //                CraftingManager.getInstance.getRecipeList.add(
    //                    new ShapedOreRecipe(new ItemStack(saw),
    //                        "srr",
    //                        "sbr",
    //                        's': Character, "stickWood",
    //                        'r': Character, "rodStone",
    //                        'b': Character, blade))
    //    }

    //    def registerRecipes(registry: IForgeRegistry[IRecipe]) {
    //        registry.register(MicroRecipe.setRegistryName("micro_recipe"))
    //    }

    //    def init() {
    //                CraftingManager.getInstance.getRecipeList.add(MicroRecipe)
    //                CraftingManager.getInstance.addRecipe(new ItemStack(stoneRod, 4), "s", "s", 's': Character, Blocks.STONE)
    //                addSawRecipe(sawStone, Items.FLINT)
    //                addSawRecipe(sawIron, Items.IRON_INGOT)
    //                addSawRecipe(sawDiamond, Items.DIAMOND)
    //    }
}

class MicroblockProxyClient extends MicroblockProxyServer {

    val modelHelper = new ModelRegistryHelper(ScorgeModLoadingContext.get.getModEventBus)

    override def clientSetup(event: FMLClientSetupEvent) {
        super.clientSetup(event)
        modelHelper.register(new ModelResourceLocation(itemMicroBlock.getRegistryName, "inventory"), ItemMicroBlockRenderer)
    }

    //    @OnlyIn(Dist.CLIENT)
    //    def preInit() {
    //
    //        ModelRegistryHelper.registerItemRenderer(itemMicro, ItemMicroPartRenderer)
    //        registerFMPItemModel(stoneRod)
    //        saws.foreach(registerFMPItemModel)
    //        saws.foreach(ModelRegistryHelper.registerItemRenderer(_, ItemSawRenderer))
    //    }

    //    @SideOnly(Side.CLIENT)
    //    def registerFMPItemModel(item: Item) {
    //        val loc = item.getRegistryName
    //        val mLoc = new ModelResourceLocation("microblockcbe:items", s"type=${loc.getResourcePath}")
    //        ModelLoader.setCustomModelResourceLocation(item, 0, mLoc)
    //        ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition {
    //            override def getModelLocation(stack: ItemStack) = mLoc
    //        })
    //    }
}
