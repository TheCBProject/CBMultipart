package codechicken.microblock.handler

import codechicken.lib.config.ConfigTag
import codechicken.lib.gui.SimpleItemGroup
import codechicken.microblock._
import codechicken.microblock.api.{BlockMicroMaterial, MicroMaterial}
import codechicken.multipart.api.MultiPartType
import net.minecraft.block.Blocks
import net.minecraft.item.crafting.{IRecipeSerializer, SpecialRecipeSerializer}
import net.minecraft.item.{Item, ItemGroup}
import net.minecraft.tags.ITag.INamedTag
import net.minecraft.tags.{ItemTags, Tag}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.ModLoadingContext

/**
 * Created by covers1624 on 23/12/19.
 */
object MicroblockModContent {

    var microTab = new SimpleItemGroup("cb_microblock", () => ItemMicroBlock.create(0, 1, BlockMicroMaterial.makeMaterialKey(Blocks.STONE.defaultBlockState)))

    var itemMicroBlock: ItemMicroBlock = _
    var itemStoneRod: Item = _
    var itemStoneSaw: Item = _
    var itemIronSaw: Item = _
    var itemDiamondSaw: Item = _

    var stoneRodTag: INamedTag[Item] = ItemTags.bind("forge:rods/stone")

    var microRecipeSerializer: SpecialRecipeSerializer[_] = _

    var faceMultiPartType: MultiPartType[_] = _
    var hollowMultiPartType: MultiPartType[_] = _
    var cornerMultiPartType: MultiPartType[_] = _
    var edgeMultiPartType: MultiPartType[_] = _
    var postMultiPartType: MultiPartType[_] = _

    @SubscribeEvent
    def onRegisterItems(event: RegistryEvent.Register[Item]) {
        val registry = event.getRegistry
        itemMicroBlock = new ItemMicroBlock(new Item.Properties().tab(microTab))
        registry.register(itemMicroBlock.setRegistryName("microblock"))

        itemStoneRod = new Item(new Item.Properties().tab(ItemGroup.TAB_MATERIALS))
        registry.register(itemStoneRod.setRegistryName("stone_rod"))

        val sawsTag = MicroblockMod.config.getTag("saws")
        itemStoneSaw = createSaw(sawsTag, "stone_saw", 1)
        itemIronSaw = createSaw(sawsTag, "iron_saw", 2)
        itemDiamondSaw = createSaw(sawsTag, "diamond_saw", 3)

        registry.register(itemStoneSaw)
        registry.register(itemIronSaw)
        registry.register(itemDiamondSaw)
    }

    @SubscribeEvent
    def onRegisterRecipeSerializers(event: RegistryEvent.Register[IRecipeSerializer[_]]) {
        val registry = event.getRegistry

        microRecipeSerializer = new SpecialRecipeSerializer(e => new MicroRecipe(e))
        registry.register(microRecipeSerializer.setRegistryName("microblock"))
    }

    @SubscribeEvent
    def onRegisterMultiParts(event: RegistryEvent.Register[MultiPartType[_]]) {
        val registry = event.getRegistry
        faceMultiPartType = registry.register(FaceMicroFactory, MicroblockMod.modId + ":face")
        hollowMultiPartType = registry.register(HollowMicroFactory, MicroblockMod.modId + ":hollow")
        cornerMultiPartType = registry.register(CornerMicroFactory, MicroblockMod.modId + ":corner")
        edgeMultiPartType = registry.register(EdgeMicroFactory, MicroblockMod.modId + ":edge")
        postMultiPartType = registry.register(PostMicroFactory, MicroblockMod.modId + ":post")
    }

    @SubscribeEvent
    def onRegisterMicroMaterials(event: RegistryEvent.Register[MicroMaterial]) {
        val r = event.getRegistry
        val container = ModLoadingContext.get().getActiveContainer
        val ext: Any = ModLoadingContext.get().extension()
        ModLoadingContext.get().setActiveContainer(null, null)
        r.register(BlockMicroMaterial(Blocks.STONE))
        r.register(BlockMicroMaterial(Blocks.GRANITE))
        r.register(BlockMicroMaterial(Blocks.POLISHED_GRANITE))
        r.register(BlockMicroMaterial(Blocks.DIORITE))
        r.register(BlockMicroMaterial(Blocks.POLISHED_DIORITE))
        r.register(BlockMicroMaterial(Blocks.ANDESITE))
        r.register(BlockMicroMaterial(Blocks.POLISHED_ANDESITE))
        r.register(new GrassMicroMaterial())
        r.register(BlockMicroMaterial(Blocks.DIRT))
        r.register(BlockMicroMaterial(Blocks.COARSE_DIRT))
        r.register(BlockMicroMaterial(Blocks.PODZOL))
        r.register(BlockMicroMaterial(Blocks.COBBLESTONE))
        r.register(BlockMicroMaterial(Blocks.OAK_PLANKS))
        r.register(BlockMicroMaterial(Blocks.SPRUCE_PLANKS))
        r.register(BlockMicroMaterial(Blocks.BIRCH_PLANKS))
        r.register(BlockMicroMaterial(Blocks.JUNGLE_PLANKS))
        r.register(BlockMicroMaterial(Blocks.ACACIA_PLANKS))
        r.register(BlockMicroMaterial(Blocks.DARK_OAK_PLANKS))
        r.register(BlockMicroMaterial(Blocks.SAND)) //TODO Gravity?
        r.register(BlockMicroMaterial(Blocks.RED_SAND)) //TODO Gravity?
        r.register(BlockMicroMaterial(Blocks.GRAVEL))
        r.register(BlockMicroMaterial(Blocks.GOLD_ORE))
        r.register(BlockMicroMaterial(Blocks.IRON_ORE))
        r.register(BlockMicroMaterial(Blocks.COAL_ORE))
        r.register(BlockMicroMaterial(Blocks.OAK_LOG))
        r.register(BlockMicroMaterial(Blocks.SPRUCE_LOG))
        r.register(BlockMicroMaterial(Blocks.BIRCH_LOG))
        r.register(BlockMicroMaterial(Blocks.JUNGLE_LOG))
        r.register(BlockMicroMaterial(Blocks.ACACIA_LOG))
        r.register(BlockMicroMaterial(Blocks.DARK_OAK_LOG))
        r.register(BlockMicroMaterial(Blocks.STRIPPED_OAK_LOG))
        r.register(BlockMicroMaterial(Blocks.STRIPPED_SPRUCE_LOG))
        r.register(BlockMicroMaterial(Blocks.STRIPPED_BIRCH_LOG))
        r.register(BlockMicroMaterial(Blocks.STRIPPED_JUNGLE_LOG))
        r.register(BlockMicroMaterial(Blocks.STRIPPED_ACACIA_LOG))
        r.register(BlockMicroMaterial(Blocks.STRIPPED_DARK_OAK_LOG))
        r.register(BlockMicroMaterial(Blocks.OAK_WOOD))
        r.register(BlockMicroMaterial(Blocks.SPRUCE_WOOD))
        r.register(BlockMicroMaterial(Blocks.BIRCH_WOOD))
        r.register(BlockMicroMaterial(Blocks.JUNGLE_WOOD))
        r.register(BlockMicroMaterial(Blocks.ACACIA_WOOD))
        r.register(BlockMicroMaterial(Blocks.DARK_OAK_WOOD))
        r.register(BlockMicroMaterial(Blocks.STRIPPED_OAK_WOOD))
        r.register(BlockMicroMaterial(Blocks.STRIPPED_SPRUCE_WOOD))
        r.register(BlockMicroMaterial(Blocks.STRIPPED_BIRCH_WOOD))
        r.register(BlockMicroMaterial(Blocks.STRIPPED_JUNGLE_WOOD))
        r.register(BlockMicroMaterial(Blocks.STRIPPED_ACACIA_WOOD))
        r.register(BlockMicroMaterial(Blocks.STRIPPED_DARK_OAK_WOOD))
        r.register(BlockMicroMaterial(Blocks.OAK_LEAVES))
        r.register(BlockMicroMaterial(Blocks.SPRUCE_LEAVES))
        r.register(BlockMicroMaterial(Blocks.BIRCH_LEAVES))
        r.register(BlockMicroMaterial(Blocks.JUNGLE_LEAVES))
        r.register(BlockMicroMaterial(Blocks.ACACIA_LEAVES))
        r.register(BlockMicroMaterial(Blocks.DARK_OAK_LEAVES))
        r.register(BlockMicroMaterial(Blocks.SPONGE))
        r.register(BlockMicroMaterial(Blocks.WET_SPONGE))
        r.register(BlockMicroMaterial(Blocks.GLASS))
        r.register(BlockMicroMaterial(Blocks.LAPIS_ORE))
        r.register(BlockMicroMaterial(Blocks.LAPIS_BLOCK))
        r.register(BlockMicroMaterial(Blocks.SANDSTONE))
        r.register(BlockMicroMaterial(Blocks.CHISELED_SANDSTONE))
        r.register(BlockMicroMaterial(Blocks.CUT_SANDSTONE))
        r.register(BlockMicroMaterial(Blocks.WHITE_WOOL))
        r.register(BlockMicroMaterial(Blocks.ORANGE_WOOL))
        r.register(BlockMicroMaterial(Blocks.MAGENTA_WOOL))
        r.register(BlockMicroMaterial(Blocks.LIGHT_BLUE_WOOL))
        r.register(BlockMicroMaterial(Blocks.YELLOW_WOOL))
        r.register(BlockMicroMaterial(Blocks.LIME_WOOL))
        r.register(BlockMicroMaterial(Blocks.PINK_WOOL))
        r.register(BlockMicroMaterial(Blocks.GRAY_WOOL))
        r.register(BlockMicroMaterial(Blocks.LIGHT_GRAY_WOOL))
        r.register(BlockMicroMaterial(Blocks.CYAN_WOOL))
        r.register(BlockMicroMaterial(Blocks.PURPLE_WOOL))
        r.register(BlockMicroMaterial(Blocks.BLUE_WOOL))
        r.register(BlockMicroMaterial(Blocks.BROWN_WOOL))
        r.register(BlockMicroMaterial(Blocks.GREEN_WOOL))
        r.register(BlockMicroMaterial(Blocks.RED_WOOL))
        r.register(BlockMicroMaterial(Blocks.BLACK_WOOL))
        r.register(BlockMicroMaterial(Blocks.GOLD_BLOCK))
        r.register(BlockMicroMaterial(Blocks.IRON_BLOCK))
        r.register(BlockMicroMaterial(Blocks.BRICKS))
        r.register(BlockMicroMaterial(Blocks.TNT)) //TODO, make explode?
        r.register(BlockMicroMaterial(Blocks.BOOKSHELF))
        r.register(BlockMicroMaterial(Blocks.MOSSY_COBBLESTONE))
        r.register(BlockMicroMaterial(Blocks.OBSIDIAN))
        r.register(BlockMicroMaterial(Blocks.DIAMOND_ORE))
        r.register(BlockMicroMaterial(Blocks.DIAMOND_BLOCK))
        r.register(BlockMicroMaterial(Blocks.CRAFTING_TABLE)) //TODO Actually function?
        r.register(BlockMicroMaterial(Blocks.REDSTONE_ORE))
        r.register(BlockMicroMaterial(Blocks.ICE))
        r.register(BlockMicroMaterial(Blocks.SNOW_BLOCK))
        r.register(BlockMicroMaterial(Blocks.CLAY))
        r.register(BlockMicroMaterial(Blocks.PUMPKIN))
        r.register(BlockMicroMaterial(Blocks.NETHERRACK))
        r.register(BlockMicroMaterial(Blocks.SOUL_SAND))
        r.register(BlockMicroMaterial(Blocks.GLOWSTONE))
        r.register(BlockMicroMaterial(Blocks.CARVED_PUMPKIN))
        r.register(BlockMicroMaterial(Blocks.JACK_O_LANTERN))
        r.register(BlockMicroMaterial(Blocks.WHITE_STAINED_GLASS))
        r.register(BlockMicroMaterial(Blocks.ORANGE_STAINED_GLASS))
        r.register(BlockMicroMaterial(Blocks.MAGENTA_STAINED_GLASS))
        r.register(BlockMicroMaterial(Blocks.LIGHT_BLUE_STAINED_GLASS))
        r.register(BlockMicroMaterial(Blocks.YELLOW_STAINED_GLASS))
        r.register(BlockMicroMaterial(Blocks.LIME_STAINED_GLASS))
        r.register(BlockMicroMaterial(Blocks.PINK_STAINED_GLASS))
        r.register(BlockMicroMaterial(Blocks.GRAY_STAINED_GLASS))
        r.register(BlockMicroMaterial(Blocks.LIGHT_GRAY_STAINED_GLASS))
        r.register(BlockMicroMaterial(Blocks.CYAN_STAINED_GLASS))
        r.register(BlockMicroMaterial(Blocks.PURPLE_STAINED_GLASS))
        r.register(BlockMicroMaterial(Blocks.BLUE_STAINED_GLASS))
        r.register(BlockMicroMaterial(Blocks.BROWN_STAINED_GLASS))
        r.register(BlockMicroMaterial(Blocks.GREEN_STAINED_GLASS))
        r.register(BlockMicroMaterial(Blocks.RED_STAINED_GLASS))
        r.register(BlockMicroMaterial(Blocks.BLACK_STAINED_GLASS))
        r.register(BlockMicroMaterial(Blocks.STONE_BRICKS))
        r.register(BlockMicroMaterial(Blocks.MOSSY_STONE_BRICKS))
        r.register(BlockMicroMaterial(Blocks.CRACKED_STONE_BRICKS))
        r.register(BlockMicroMaterial(Blocks.CHISELED_STONE_BRICKS))
        r.register(BlockMicroMaterial(Blocks.BROWN_MUSHROOM_BLOCK))
        r.register(BlockMicroMaterial(Blocks.RED_MUSHROOM_BLOCK))
        r.register(BlockMicroMaterial(Blocks.MUSHROOM_STEM))
        r.register(BlockMicroMaterial(Blocks.MELON))
        r.register(new TopMicroMaterial(Blocks.MYCELIUM))
        r.register(BlockMicroMaterial(Blocks.NETHER_BRICKS))
        r.register(BlockMicroMaterial(Blocks.END_STONE))
        r.register(BlockMicroMaterial(Blocks.EMERALD_ORE))
        r.register(BlockMicroMaterial(Blocks.EMERALD_BLOCK))
        r.register(BlockMicroMaterial(Blocks.REDSTONE_BLOCK))
        r.register(BlockMicroMaterial(Blocks.NETHER_QUARTZ_ORE))
        r.register(BlockMicroMaterial(Blocks.QUARTZ_BLOCK))
        r.register(BlockMicroMaterial(Blocks.CHISELED_QUARTZ_BLOCK))
        r.register(BlockMicroMaterial(Blocks.QUARTZ_PILLAR))
        r.register(BlockMicroMaterial(Blocks.WHITE_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.ORANGE_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.MAGENTA_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.LIGHT_BLUE_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.YELLOW_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.LIME_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.PINK_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.GRAY_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.LIGHT_GRAY_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.CYAN_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.PURPLE_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.BLUE_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.BROWN_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.GREEN_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.RED_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.BLACK_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.SLIME_BLOCK))
        r.register(BlockMicroMaterial(Blocks.PRISMARINE))
        r.register(BlockMicroMaterial(Blocks.PRISMARINE_BRICKS))
        r.register(BlockMicroMaterial(Blocks.DARK_PRISMARINE))
        r.register(BlockMicroMaterial(Blocks.SEA_LANTERN))
        r.register(BlockMicroMaterial(Blocks.HAY_BLOCK))
        r.register(BlockMicroMaterial(Blocks.TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.COAL_BLOCK))
        r.register(BlockMicroMaterial(Blocks.PACKED_ICE))
        r.register(BlockMicroMaterial(Blocks.RED_SANDSTONE))
        r.register(BlockMicroMaterial(Blocks.CHISELED_RED_SANDSTONE))
        r.register(BlockMicroMaterial(Blocks.CUT_RED_SANDSTONE))
        r.register(BlockMicroMaterial(Blocks.SMOOTH_STONE))
        r.register(BlockMicroMaterial(Blocks.SMOOTH_SANDSTONE))
        r.register(BlockMicroMaterial(Blocks.SMOOTH_QUARTZ))
        r.register(BlockMicroMaterial(Blocks.SMOOTH_RED_SANDSTONE))
        r.register(BlockMicroMaterial(Blocks.PURPUR_BLOCK))
        r.register(BlockMicroMaterial(Blocks.END_STONE_BRICKS))
        r.register(BlockMicroMaterial(Blocks.MAGMA_BLOCK)) //TODO Burn?
        r.register(BlockMicroMaterial(Blocks.NETHER_WART_BLOCK))
        r.register(BlockMicroMaterial(Blocks.RED_NETHER_BRICKS))
        r.register(BlockMicroMaterial(Blocks.BONE_BLOCK))
        r.register(BlockMicroMaterial(Blocks.WHITE_GLAZED_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.ORANGE_GLAZED_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.MAGENTA_GLAZED_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.YELLOW_GLAZED_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.LIME_GLAZED_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.PINK_GLAZED_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.GRAY_GLAZED_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.CYAN_GLAZED_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.PURPLE_GLAZED_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.BLUE_GLAZED_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.BROWN_GLAZED_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.GREEN_GLAZED_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.RED_GLAZED_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.BLACK_GLAZED_TERRACOTTA))
        r.register(BlockMicroMaterial(Blocks.WHITE_CONCRETE))
        r.register(BlockMicroMaterial(Blocks.ORANGE_CONCRETE))
        r.register(BlockMicroMaterial(Blocks.MAGENTA_CONCRETE))
        r.register(BlockMicroMaterial(Blocks.LIGHT_BLUE_CONCRETE))
        r.register(BlockMicroMaterial(Blocks.YELLOW_CONCRETE))
        r.register(BlockMicroMaterial(Blocks.LIME_CONCRETE))
        r.register(BlockMicroMaterial(Blocks.PINK_CONCRETE))
        r.register(BlockMicroMaterial(Blocks.GRAY_CONCRETE))
        r.register(BlockMicroMaterial(Blocks.LIGHT_GRAY_CONCRETE))
        r.register(BlockMicroMaterial(Blocks.CYAN_CONCRETE))
        r.register(BlockMicroMaterial(Blocks.PURPLE_CONCRETE))
        r.register(BlockMicroMaterial(Blocks.BLUE_CONCRETE))
        r.register(BlockMicroMaterial(Blocks.BROWN_CONCRETE))
        r.register(BlockMicroMaterial(Blocks.GREEN_CONCRETE))
        r.register(BlockMicroMaterial(Blocks.RED_CONCRETE))
        r.register(BlockMicroMaterial(Blocks.BLACK_CONCRETE))
        r.register(BlockMicroMaterial(Blocks.WHITE_CONCRETE_POWDER)) //TODO Gravity -> Concrete?
        r.register(BlockMicroMaterial(Blocks.ORANGE_CONCRETE_POWDER)) //TODO Gravity -> Concrete?
        r.register(BlockMicroMaterial(Blocks.MAGENTA_CONCRETE_POWDER)) //TODO Gravity -> Concrete?
        r.register(BlockMicroMaterial(Blocks.LIGHT_BLUE_CONCRETE_POWDER)) //TODO Gravity -> Concrete?
        r.register(BlockMicroMaterial(Blocks.YELLOW_CONCRETE_POWDER)) //TODO Gravity -> Concrete?
        r.register(BlockMicroMaterial(Blocks.LIME_CONCRETE_POWDER)) //TODO Gravity -> Concrete?
        r.register(BlockMicroMaterial(Blocks.PINK_CONCRETE_POWDER)) //TODO Gravity -> Concrete?
        r.register(BlockMicroMaterial(Blocks.GRAY_CONCRETE_POWDER)) //TODO Gravity -> Concrete?
        r.register(BlockMicroMaterial(Blocks.LIGHT_GRAY_CONCRETE_POWDER)) //TODO Gravity -> Concrete?
        r.register(BlockMicroMaterial(Blocks.CYAN_CONCRETE_POWDER)) //TODO Gravity -> Concrete?
        r.register(BlockMicroMaterial(Blocks.PURPLE_CONCRETE_POWDER)) //TODO Gravity -> Concrete?
        r.register(BlockMicroMaterial(Blocks.BLUE_CONCRETE_POWDER)) //TODO Gravity -> Concrete?
        r.register(BlockMicroMaterial(Blocks.BROWN_CONCRETE_POWDER)) //TODO Gravity -> Concrete?
        r.register(BlockMicroMaterial(Blocks.GREEN_CONCRETE_POWDER)) //TODO Gravity -> Concrete?
        r.register(BlockMicroMaterial(Blocks.RED_CONCRETE_POWDER)) //TODO Gravity -> Concrete?
        r.register(BlockMicroMaterial(Blocks.BLACK_CONCRETE_POWDER)) //TODO Gravity -> Concrete?
        r.register(BlockMicroMaterial(Blocks.DRIED_KELP_BLOCK))
        r.register(BlockMicroMaterial(Blocks.DEAD_TUBE_CORAL_BLOCK))
        r.register(BlockMicroMaterial(Blocks.DEAD_BRAIN_CORAL_BLOCK))
        r.register(BlockMicroMaterial(Blocks.DEAD_BUBBLE_CORAL_BLOCK))
        r.register(BlockMicroMaterial(Blocks.DEAD_FIRE_CORAL_BLOCK))
        r.register(BlockMicroMaterial(Blocks.DEAD_HORN_CORAL_BLOCK))
        r.register(BlockMicroMaterial(Blocks.TUBE_CORAL_BLOCK)) //TODO Dies out of water
        r.register(BlockMicroMaterial(Blocks.BRAIN_CORAL_BLOCK)) //TODO Dies out of water
        r.register(BlockMicroMaterial(Blocks.BUBBLE_CORAL_BLOCK)) //TODO Dies out of water
        r.register(BlockMicroMaterial(Blocks.FIRE_CORAL_BLOCK)) //TODO Dies out of water
        r.register(BlockMicroMaterial(Blocks.HORN_CORAL_BLOCK)) //TODO Dies out of water
        r.register(BlockMicroMaterial(Blocks.BLUE_ICE)) //TODO speed
        ModLoadingContext.get().setActiveContainer(container, ext)
        //processRegistrationMessages(r)
    }

    //    def processRegistrationMessages(r: IForgeRegistry[MicroMaterial]) {
    //        InterModComms.getMessages(MicroblockMod.modId)
    //            .filter(e => e.getMethod == "micro_material")
    //            .map(e => (e.getSenderModId, e.getMessageSupplier.get()))
    //            .forEach(e => {
    //                val (sender, obj) = e
    //                val mat = obj match {
    //                    case state: BlockState => BlockMicroMaterial(state.asInstanceOf[BlockState])
    //                    case block: Block => BlockMicroMaterial(block.asInstanceOf[Block])
    //                    case e => logger.error(s"Mod '$sender' tried to register MicroMaterial of invalid " +
    //                        s"type '${if (e != null) e.toString else null}', class '${if (e != null) e.getClass else null}', please use the registry directly.")
    //                        null
    //                }
    //                if (mat != null) {
    //                    if (r.containsKey(mat.getRegistryName)) {
    //                        logger.warn(s"Mod '$sender' tried to register duplicate MicroMaterial '${mat.getRegistryName}'. Ignoring.")
    //                    } else {
    //                        r.register(mat)
    //                    }
    //                }
    //            })
    //    }

    private def createSaw(config: ConfigTag, name: String, strength: Int) = {
        val saw = new ItemSaw(config.getTag(name), strength)
        saw.setRegistryName(name)
        saw
    }
}
