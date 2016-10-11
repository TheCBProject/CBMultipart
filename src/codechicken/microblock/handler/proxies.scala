package codechicken.microblock.handler

import java.util.{List => JList}

import codechicken.lib.config.ConfigFile
import codechicken.lib.model.ModelRegistryHelper
import codechicken.lib.packet.PacketCustom
import codechicken.microblock._
import codechicken.multipart.handler.MultipartProxy._
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.crafting.CraftingManager
import net.minecraft.item.{Item, ItemStack}
import net.minecraftforge.client.event.ModelBakeEvent
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import net.minecraftforge.oredict.{OreDictionary, ShapedOreRecipe}
import org.apache.logging.log4j.Logger

import scala.collection.mutable

class MicroblockProxy_serverImpl
{
    var logger: Logger = _

    var itemMicro: ItemMicroPart = _
    var sawStone: Item = _
    var sawIron: Item = _
    var sawDiamond: Item = _
    var stoneRod: Item = _

    var useSawIcons: Boolean = _

    def preInit(logger:Logger) {
        this.logger = logger
        itemMicro = new ItemMicroPart
        sawStone = createSaw(config, "sawStone", 1)
        sawIron = createSaw(config, "sawIron", 2)
        sawDiamond = createSaw(config, "sawDiamond", 3)
        stoneRod = new Item().setUnlocalizedName("stoneRod")//.setTextureName("microblock:stoneRod")
        GameRegistry.register(stoneRod.setRegistryName("stoneRod"))

        OreDictionary.registerOre("rodStone", stoneRod)

        MinecraftForge.EVENT_BUS.register(MicroblockEventHandler)

        useSawIcons = config.getTag("useSawIcons").setComment("Set to true to use mc style icons for the saw instead of the 3D model").getBooleanValue(false)
    }

    protected var saws = mutable.MutableList[Item]()
    def createSaw(config: ConfigFile, name: String, strength: Int) = {
        val saw = new ItemSaw(config.getTag(name).useBraces(), strength)
            .setUnlocalizedName(name)//.setTextureName("microblock:" + name)
        GameRegistry.register(saw.setRegistryName(name))
        saws+=saw
        saw
    }

    def addSawRecipe(saw: Item, blade: Item) {
        CraftingManager.getInstance.getRecipeList.add(
            new ShapedOreRecipe(new ItemStack(saw),
                "srr",
                "sbr",
                's': Character, "stickWood",
                'r': Character, "rodStone",
                'b': Character, blade))
    }

    def init() {
        CraftingManager.getInstance.getRecipeList.add(MicroRecipe)
        CraftingManager.getInstance.addRecipe(new ItemStack(stoneRod, 4), "s", "s", 's': Character, Blocks.STONE)
        addSawRecipe(sawStone, Items.FLINT)
        addSawRecipe(sawIron, Items.IRON_INGOT)
        addSawRecipe(sawDiamond, Items.DIAMOND)
    }

    def postInit() {
        MicroMaterialRegistry.calcMaxCuttingStrength()
        PacketCustom.assignHandshakeHandler(MicroblockSPH.registryChannel, MicroblockSPH)
    }
}

class MicroblockProxy_clientImpl extends MicroblockProxy_serverImpl
{
    @SideOnly(Side.CLIENT)
    override def preInit(logger:Logger)
    {
        super.preInit(logger)

        ModelRegistryHelper.registerItemRenderer(itemMicro, ItemMicroPartRenderer)
        saws.foreach(ModelRegistryHelper.registerItemRenderer(_, ItemSawRenderer))
    }

    @SideOnly(Side.CLIENT)
    override def postInit()
    {
        super.postInit()
        PacketCustom.assignHandler(MicroblockCPH.registryChannel, MicroblockCPH)
    }

    @SideOnly(Side.CLIENT)
    override def init()
    {
        super.init()
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    def onModelBakeEvent(event:ModelBakeEvent)
    {
        MicroMaterialRegistry.loadIcons() //Theres probably a better place for this
    }
}

object MicroblockProxy extends MicroblockProxy_clientImpl
{
}
