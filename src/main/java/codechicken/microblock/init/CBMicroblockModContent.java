package codechicken.microblock.init;

import codechicken.lib.gui.SimpleCreativeTab;
import codechicken.microblock.CBMicroblock;
import codechicken.microblock.api.BlockMicroMaterial;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.item.ItemMicroBlock;
import codechicken.microblock.part.corner.CornerMicroFactory;
import codechicken.microblock.part.face.FaceMicroFactory;
import codechicken.microblock.part.hollow.HollowMicroFactory;
import codechicken.multipart.CBMultipart;
import codechicken.multipart.api.MultiPartType;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

/**
 * Created by covers1624 on 26/6/22.
 */
public class CBMicroblockModContent {

    private static final CrashLock LOCK = new CrashLock("Already initialized.");
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registry.ITEM_REGISTRY, CBMicroblock.MOD_ID);
    private static final DeferredRegister<MultiPartType<?>> MULTIPART_TYPES = DeferredRegister.create(new ResourceLocation(CBMultipart.MOD_ID, "multipart_types"), CBMicroblock.MOD_ID);

    public static final SimpleCreativeTab MICRO_TAB = new SimpleCreativeTab("cb_microblock", () -> new ItemStack(Blocks.STONE)) {
        @Override
        public boolean hasSearchBar() {
            return true;
        }
    };

    public static final RegistryObject<ItemMicroBlock> MICRO_BLOCK_ITEM = ITEMS.register("microblock", () -> new ItemMicroBlock(new Item.Properties().tab(MICRO_TAB)));

    public static final RegistryObject<FaceMicroFactory> FACE_MICROBLOCK_PART = MULTIPART_TYPES.register("face", FaceMicroFactory::new);
    public static final RegistryObject<HollowMicroFactory> HOLLOW_MICROBLOCK_PART = MULTIPART_TYPES.register("hollow", HollowMicroFactory::new);
    public static final RegistryObject<CornerMicroFactory> CORNER_MICROBLOCK_PART = MULTIPART_TYPES.register("corner", CornerMicroFactory::new);

    public static void init() {
        LOCK.lock();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        MULTIPART_TYPES.register(bus);
        bus.addGenericListener(MicroMaterial.class, CBMicroblockModContent::onRegisterMicroMaterials);
    }

    private static void onRegisterMicroMaterials(RegistryEvent.Register<MicroMaterial> event) {
        IForgeRegistry<MicroMaterial> r = event.getRegistry();
        r.register(new BlockMicroMaterial(Blocks.STONE));
        r.register(new BlockMicroMaterial(Blocks.GRANITE));
        r.register(new BlockMicroMaterial(Blocks.POLISHED_GRANITE));
        r.register(new BlockMicroMaterial(Blocks.DIORITE));
        r.register(new BlockMicroMaterial(Blocks.POLISHED_DIORITE));
        r.register(new BlockMicroMaterial(Blocks.ANDESITE));
        r.register(new BlockMicroMaterial(Blocks.POLISHED_ANDESITE));
        r.register(new BlockMicroMaterial(Blocks.GRASS_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.DIRT));
        r.register(new BlockMicroMaterial(Blocks.COARSE_DIRT));
        r.register(new BlockMicroMaterial(Blocks.PODZOL));
        r.register(new BlockMicroMaterial(Blocks.COBBLESTONE));
        r.register(new BlockMicroMaterial(Blocks.OAK_PLANKS));
        r.register(new BlockMicroMaterial(Blocks.SPRUCE_PLANKS));
        r.register(new BlockMicroMaterial(Blocks.BIRCH_PLANKS));
        r.register(new BlockMicroMaterial(Blocks.JUNGLE_PLANKS));
        r.register(new BlockMicroMaterial(Blocks.ACACIA_PLANKS));
        r.register(new BlockMicroMaterial(Blocks.DARK_OAK_PLANKS));
        r.register(new BlockMicroMaterial(Blocks.SAND)); //TODO Gravity?
        r.register(new BlockMicroMaterial(Blocks.RED_SAND)); //TODO Gravity?
        r.register(new BlockMicroMaterial(Blocks.GRAVEL));
        r.register(new BlockMicroMaterial(Blocks.GOLD_ORE));
        r.register(new BlockMicroMaterial(Blocks.IRON_ORE));
        r.register(new BlockMicroMaterial(Blocks.COAL_ORE));
        r.register(new BlockMicroMaterial(Blocks.OAK_LOG));
        r.register(new BlockMicroMaterial(Blocks.SPRUCE_LOG));
        r.register(new BlockMicroMaterial(Blocks.BIRCH_LOG));
        r.register(new BlockMicroMaterial(Blocks.JUNGLE_LOG));
        r.register(new BlockMicroMaterial(Blocks.ACACIA_LOG));
        r.register(new BlockMicroMaterial(Blocks.DARK_OAK_LOG));
        r.register(new BlockMicroMaterial(Blocks.STRIPPED_OAK_LOG));
        r.register(new BlockMicroMaterial(Blocks.STRIPPED_SPRUCE_LOG));
        r.register(new BlockMicroMaterial(Blocks.STRIPPED_BIRCH_LOG));
        r.register(new BlockMicroMaterial(Blocks.STRIPPED_JUNGLE_LOG));
        r.register(new BlockMicroMaterial(Blocks.STRIPPED_ACACIA_LOG));
        r.register(new BlockMicroMaterial(Blocks.STRIPPED_DARK_OAK_LOG));
        r.register(new BlockMicroMaterial(Blocks.OAK_WOOD));
        r.register(new BlockMicroMaterial(Blocks.SPRUCE_WOOD));
        r.register(new BlockMicroMaterial(Blocks.BIRCH_WOOD));
        r.register(new BlockMicroMaterial(Blocks.JUNGLE_WOOD));
        r.register(new BlockMicroMaterial(Blocks.ACACIA_WOOD));
        r.register(new BlockMicroMaterial(Blocks.DARK_OAK_WOOD));
        r.register(new BlockMicroMaterial(Blocks.STRIPPED_OAK_WOOD));
        r.register(new BlockMicroMaterial(Blocks.STRIPPED_SPRUCE_WOOD));
        r.register(new BlockMicroMaterial(Blocks.STRIPPED_BIRCH_WOOD));
        r.register(new BlockMicroMaterial(Blocks.STRIPPED_JUNGLE_WOOD));
        r.register(new BlockMicroMaterial(Blocks.STRIPPED_ACACIA_WOOD));
        r.register(new BlockMicroMaterial(Blocks.STRIPPED_DARK_OAK_WOOD));
        r.register(new BlockMicroMaterial(Blocks.OAK_LEAVES));
        r.register(new BlockMicroMaterial(Blocks.SPRUCE_LEAVES));
        r.register(new BlockMicroMaterial(Blocks.BIRCH_LEAVES));
        r.register(new BlockMicroMaterial(Blocks.JUNGLE_LEAVES));
        r.register(new BlockMicroMaterial(Blocks.ACACIA_LEAVES));
        r.register(new BlockMicroMaterial(Blocks.DARK_OAK_LEAVES));
        r.register(new BlockMicroMaterial(Blocks.SPONGE));
        r.register(new BlockMicroMaterial(Blocks.WET_SPONGE));
        r.register(new BlockMicroMaterial(Blocks.GLASS));
        r.register(new BlockMicroMaterial(Blocks.LAPIS_ORE));
        r.register(new BlockMicroMaterial(Blocks.LAPIS_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.SANDSTONE));
        r.register(new BlockMicroMaterial(Blocks.CHISELED_SANDSTONE));
        r.register(new BlockMicroMaterial(Blocks.CUT_SANDSTONE));
        r.register(new BlockMicroMaterial(Blocks.WHITE_WOOL));
        r.register(new BlockMicroMaterial(Blocks.ORANGE_WOOL));
        r.register(new BlockMicroMaterial(Blocks.MAGENTA_WOOL));
        r.register(new BlockMicroMaterial(Blocks.LIGHT_BLUE_WOOL));
        r.register(new BlockMicroMaterial(Blocks.YELLOW_WOOL));
        r.register(new BlockMicroMaterial(Blocks.LIME_WOOL));
        r.register(new BlockMicroMaterial(Blocks.PINK_WOOL));
        r.register(new BlockMicroMaterial(Blocks.GRAY_WOOL));
        r.register(new BlockMicroMaterial(Blocks.LIGHT_GRAY_WOOL));
        r.register(new BlockMicroMaterial(Blocks.CYAN_WOOL));
        r.register(new BlockMicroMaterial(Blocks.PURPLE_WOOL));
        r.register(new BlockMicroMaterial(Blocks.BLUE_WOOL));
        r.register(new BlockMicroMaterial(Blocks.BROWN_WOOL));
        r.register(new BlockMicroMaterial(Blocks.GREEN_WOOL));
        r.register(new BlockMicroMaterial(Blocks.RED_WOOL));
        r.register(new BlockMicroMaterial(Blocks.BLACK_WOOL));
        r.register(new BlockMicroMaterial(Blocks.GOLD_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.IRON_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.BRICKS));
        r.register(new BlockMicroMaterial(Blocks.TNT)); //TODO, make explode?
        r.register(new BlockMicroMaterial(Blocks.BOOKSHELF));
        r.register(new BlockMicroMaterial(Blocks.MOSSY_COBBLESTONE));
        r.register(new BlockMicroMaterial(Blocks.OBSIDIAN));
        r.register(new BlockMicroMaterial(Blocks.DIAMOND_ORE));
        r.register(new BlockMicroMaterial(Blocks.DIAMOND_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.CRAFTING_TABLE)); //TODO Actually function?
        r.register(new BlockMicroMaterial(Blocks.REDSTONE_ORE));
        r.register(new BlockMicroMaterial(Blocks.ICE));
        r.register(new BlockMicroMaterial(Blocks.SNOW_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.CLAY));
        r.register(new BlockMicroMaterial(Blocks.PUMPKIN));
        r.register(new BlockMicroMaterial(Blocks.NETHERRACK));
        r.register(new BlockMicroMaterial(Blocks.SOUL_SAND));
        r.register(new BlockMicroMaterial(Blocks.GLOWSTONE));
        r.register(new BlockMicroMaterial(Blocks.CARVED_PUMPKIN));
        r.register(new BlockMicroMaterial(Blocks.JACK_O_LANTERN));
        r.register(new BlockMicroMaterial(Blocks.WHITE_STAINED_GLASS));
        r.register(new BlockMicroMaterial(Blocks.ORANGE_STAINED_GLASS));
        r.register(new BlockMicroMaterial(Blocks.MAGENTA_STAINED_GLASS));
        r.register(new BlockMicroMaterial(Blocks.LIGHT_BLUE_STAINED_GLASS));
        r.register(new BlockMicroMaterial(Blocks.YELLOW_STAINED_GLASS));
        r.register(new BlockMicroMaterial(Blocks.LIME_STAINED_GLASS));
        r.register(new BlockMicroMaterial(Blocks.PINK_STAINED_GLASS));
        r.register(new BlockMicroMaterial(Blocks.GRAY_STAINED_GLASS));
        r.register(new BlockMicroMaterial(Blocks.LIGHT_GRAY_STAINED_GLASS));
        r.register(new BlockMicroMaterial(Blocks.CYAN_STAINED_GLASS));
        r.register(new BlockMicroMaterial(Blocks.PURPLE_STAINED_GLASS));
        r.register(new BlockMicroMaterial(Blocks.BLUE_STAINED_GLASS));
        r.register(new BlockMicroMaterial(Blocks.BROWN_STAINED_GLASS));
        r.register(new BlockMicroMaterial(Blocks.GREEN_STAINED_GLASS));
        r.register(new BlockMicroMaterial(Blocks.RED_STAINED_GLASS));
        r.register(new BlockMicroMaterial(Blocks.BLACK_STAINED_GLASS));
        r.register(new BlockMicroMaterial(Blocks.STONE_BRICKS));
        r.register(new BlockMicroMaterial(Blocks.MOSSY_STONE_BRICKS));
        r.register(new BlockMicroMaterial(Blocks.CRACKED_STONE_BRICKS));
        r.register(new BlockMicroMaterial(Blocks.CHISELED_STONE_BRICKS));
        r.register(new BlockMicroMaterial(Blocks.BROWN_MUSHROOM_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.RED_MUSHROOM_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.MUSHROOM_STEM));
        r.register(new BlockMicroMaterial(Blocks.MELON));
        r.register(new BlockMicroMaterial(Blocks.MYCELIUM));
        r.register(new BlockMicroMaterial(Blocks.NETHER_BRICKS));
        r.register(new BlockMicroMaterial(Blocks.END_STONE));
        r.register(new BlockMicroMaterial(Blocks.EMERALD_ORE));
        r.register(new BlockMicroMaterial(Blocks.EMERALD_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.REDSTONE_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.NETHER_QUARTZ_ORE));
        r.register(new BlockMicroMaterial(Blocks.QUARTZ_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.CHISELED_QUARTZ_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.QUARTZ_PILLAR));
        r.register(new BlockMicroMaterial(Blocks.WHITE_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.ORANGE_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.MAGENTA_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.LIGHT_BLUE_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.YELLOW_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.LIME_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.PINK_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.GRAY_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.LIGHT_GRAY_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.CYAN_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.PURPLE_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.BLUE_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.BROWN_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.GREEN_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.RED_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.BLACK_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.SLIME_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.PRISMARINE));
        r.register(new BlockMicroMaterial(Blocks.PRISMARINE_BRICKS));
        r.register(new BlockMicroMaterial(Blocks.DARK_PRISMARINE));
        r.register(new BlockMicroMaterial(Blocks.SEA_LANTERN));
        r.register(new BlockMicroMaterial(Blocks.HAY_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.COAL_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.PACKED_ICE));
        r.register(new BlockMicroMaterial(Blocks.RED_SANDSTONE));
        r.register(new BlockMicroMaterial(Blocks.CHISELED_RED_SANDSTONE));
        r.register(new BlockMicroMaterial(Blocks.CUT_RED_SANDSTONE));
        r.register(new BlockMicroMaterial(Blocks.SMOOTH_STONE));
        r.register(new BlockMicroMaterial(Blocks.SMOOTH_SANDSTONE));
        r.register(new BlockMicroMaterial(Blocks.SMOOTH_QUARTZ));
        r.register(new BlockMicroMaterial(Blocks.SMOOTH_RED_SANDSTONE));
        r.register(new BlockMicroMaterial(Blocks.PURPUR_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.END_STONE_BRICKS));
        r.register(new BlockMicroMaterial(Blocks.MAGMA_BLOCK)); //TODO Burn?
        r.register(new BlockMicroMaterial(Blocks.NETHER_WART_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.RED_NETHER_BRICKS));
        r.register(new BlockMicroMaterial(Blocks.BONE_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.WHITE_GLAZED_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.ORANGE_GLAZED_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.MAGENTA_GLAZED_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.YELLOW_GLAZED_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.LIME_GLAZED_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.PINK_GLAZED_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.GRAY_GLAZED_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.CYAN_GLAZED_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.PURPLE_GLAZED_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.BLUE_GLAZED_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.BROWN_GLAZED_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.GREEN_GLAZED_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.RED_GLAZED_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.BLACK_GLAZED_TERRACOTTA));
        r.register(new BlockMicroMaterial(Blocks.WHITE_CONCRETE));
        r.register(new BlockMicroMaterial(Blocks.ORANGE_CONCRETE));
        r.register(new BlockMicroMaterial(Blocks.MAGENTA_CONCRETE));
        r.register(new BlockMicroMaterial(Blocks.LIGHT_BLUE_CONCRETE));
        r.register(new BlockMicroMaterial(Blocks.YELLOW_CONCRETE));
        r.register(new BlockMicroMaterial(Blocks.LIME_CONCRETE));
        r.register(new BlockMicroMaterial(Blocks.PINK_CONCRETE));
        r.register(new BlockMicroMaterial(Blocks.GRAY_CONCRETE));
        r.register(new BlockMicroMaterial(Blocks.LIGHT_GRAY_CONCRETE));
        r.register(new BlockMicroMaterial(Blocks.CYAN_CONCRETE));
        r.register(new BlockMicroMaterial(Blocks.PURPLE_CONCRETE));
        r.register(new BlockMicroMaterial(Blocks.BLUE_CONCRETE));
        r.register(new BlockMicroMaterial(Blocks.BROWN_CONCRETE));
        r.register(new BlockMicroMaterial(Blocks.GREEN_CONCRETE));
        r.register(new BlockMicroMaterial(Blocks.RED_CONCRETE));
        r.register(new BlockMicroMaterial(Blocks.BLACK_CONCRETE));
        r.register(new BlockMicroMaterial(Blocks.WHITE_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
        r.register(new BlockMicroMaterial(Blocks.ORANGE_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
        r.register(new BlockMicroMaterial(Blocks.MAGENTA_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
        r.register(new BlockMicroMaterial(Blocks.LIGHT_BLUE_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
        r.register(new BlockMicroMaterial(Blocks.YELLOW_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
        r.register(new BlockMicroMaterial(Blocks.LIME_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
        r.register(new BlockMicroMaterial(Blocks.PINK_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
        r.register(new BlockMicroMaterial(Blocks.GRAY_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
        r.register(new BlockMicroMaterial(Blocks.LIGHT_GRAY_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
        r.register(new BlockMicroMaterial(Blocks.CYAN_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
        r.register(new BlockMicroMaterial(Blocks.PURPLE_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
        r.register(new BlockMicroMaterial(Blocks.BLUE_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
        r.register(new BlockMicroMaterial(Blocks.BROWN_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
        r.register(new BlockMicroMaterial(Blocks.GREEN_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
        r.register(new BlockMicroMaterial(Blocks.RED_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
        r.register(new BlockMicroMaterial(Blocks.BLACK_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
        r.register(new BlockMicroMaterial(Blocks.DRIED_KELP_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.DEAD_TUBE_CORAL_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.DEAD_BRAIN_CORAL_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.DEAD_BUBBLE_CORAL_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.DEAD_FIRE_CORAL_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.DEAD_HORN_CORAL_BLOCK));
        r.register(new BlockMicroMaterial(Blocks.TUBE_CORAL_BLOCK)); //TODO Dies out of water
        r.register(new BlockMicroMaterial(Blocks.BRAIN_CORAL_BLOCK)); //TODO Dies out of water
        r.register(new BlockMicroMaterial(Blocks.BUBBLE_CORAL_BLOCK)); //TODO Dies out of water
        r.register(new BlockMicroMaterial(Blocks.FIRE_CORAL_BLOCK)); //TODO Dies out of water
        r.register(new BlockMicroMaterial(Blocks.HORN_CORAL_BLOCK)); //TODO Dies out of water
        r.register(new BlockMicroMaterial(Blocks.BLUE_ICE)); //TODO speed
    }
}
