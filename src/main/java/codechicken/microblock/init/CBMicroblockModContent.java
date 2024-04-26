package codechicken.microblock.init;

import codechicken.microblock.CBMicroblock;
import codechicken.microblock.api.BlockMicroMaterial;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.item.ItemMicroBlock;
import codechicken.microblock.item.SawItem;
import codechicken.microblock.part.StandardMicroFactory;
import codechicken.microblock.part.corner.CornerMicroFactory;
import codechicken.microblock.part.edge.EdgeMicroFactory;
import codechicken.microblock.part.edge.PostMicroblockFactory;
import codechicken.microblock.part.face.FaceMicroFactory;
import codechicken.microblock.part.hollow.HollowMicroFactory;
import codechicken.microblock.recipe.MicroRecipe;
import codechicken.microblock.util.MicroMaterialRegistry;
import codechicken.multipart.CBMultipart;
import codechicken.multipart.api.MultipartType;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;

/**
 * Created by covers1624 on 26/6/22.
 */
@ApiStatus.Internal
public class CBMicroblockModContent {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final CrashLock LOCK = new CrashLock("Already initialized.");
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CBMicroblock.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CBMicroblock.MOD_ID);
    private static final DeferredRegister<MultipartType<?>> MULTIPART_TYPES = DeferredRegister.create(new ResourceLocation(CBMultipart.MOD_ID, "multipart_types"), CBMicroblock.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CBMicroblock.MOD_ID);

    public static final RegistryObject<ItemMicroBlock> MICRO_BLOCK_ITEM = ITEMS.register("microblock", () -> new ItemMicroBlock(new Item.Properties()));

    public static final RegistryObject<Item> STONE_ROD_ITEM = ITEMS.register("stone_rod", () -> new Item(new Item.Properties()));

    public static final RegistryObject<SawItem> STONE_SAW = ITEMS.register("stone_saw", () -> new SawItem(Tiers.STONE, new Item.Properties().setNoRepair()));
    public static final RegistryObject<SawItem> IRON_SAW = ITEMS.register("iron_saw", () -> new SawItem(Tiers.IRON, new Item.Properties().setNoRepair()));
    public static final RegistryObject<SawItem> DIAMOND_SAW = ITEMS.register("diamond_saw", () -> new SawItem(Tiers.DIAMOND, new Item.Properties().setNoRepair()));

    public static final RegistryObject<CreativeModeTab> MICRO_TAB = TABS.register("microblocks", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.cb_microblock"))
            .icon(() -> ItemMicroBlock.create(1, 2, MicroMaterialRegistry.getMaterial(BlockMicroMaterial.makeMaterialKey(Blocks.GRASS_BLOCK.defaultBlockState()))))
            .displayItems((p, o) -> {
                for (StandardMicroFactory factory : StandardMicroFactory.FACTORIES.values()) {
                    for (int size : new int[] { 1, 2, 4 }) {
                        for (MicroMaterial material : MicroMaterialRegistry.MICRO_MATERIALS) {
                            o.accept(ItemMicroBlock.create(factory.factoryId, size, material));
                        }
                    }
                }
            })
            .withSearchBar()
            .build()
    );

    public static final RegistryObject<FaceMicroFactory> FACE_MICROBLOCK_PART = MULTIPART_TYPES.register("face", FaceMicroFactory::new);
    public static final RegistryObject<HollowMicroFactory> HOLLOW_MICROBLOCK_PART = MULTIPART_TYPES.register("hollow", HollowMicroFactory::new);
    public static final RegistryObject<CornerMicroFactory> CORNER_MICROBLOCK_PART = MULTIPART_TYPES.register("corner", CornerMicroFactory::new);
    public static final RegistryObject<EdgeMicroFactory> EDGE_MICROBLOCK_PART = MULTIPART_TYPES.register("edge", EdgeMicroFactory::new);
    public static final RegistryObject<PostMicroblockFactory> POST_MICROBLOCK_PART = MULTIPART_TYPES.register("post", PostMicroblockFactory::new);

    public static final RegistryObject<SimpleCraftingRecipeSerializer<?>> MICRO_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("microblock", () -> new SimpleCraftingRecipeSerializer<>(MicroRecipe::new));

    @Nullable
    public static Tier MAX_SAW_TIER;

    public static void init() {
        LOCK.lock();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        TABS.register(bus);
        MULTIPART_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
        bus.addListener(CBMicroblockModContent::onRegisterMicroMaterials);
        bus.addListener(CBMicroblockModContent::onCommonSetup);
        bus.addListener(CBMicroblockModContent::onProcessIMC);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        Tier tier = null;
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            Tier found = SawItem.getSawTier(item);
            if (found != null) {
                if (tier == null || SawItem.isTierGTEQ(found, tier)) {
                    tier = found;
                }
            }
        }
        MAX_SAW_TIER = tier;
    }

    private static void onProcessIMC(InterModProcessEvent event) {
        processIMC(event);
        MicroMaterialConfig.parse(Paths.get("config", "custom-micromaterials.cfg"));
    }

    private static void onRegisterMicroMaterials(RegisterEvent event) {
        // Note: Intentionally kept in same order as Blocks class
        event.register(MicroMaterialRegistry.MICRO_MATERIALS.getRegistryKey(), r -> {
            registerMaterial(r, new BlockMicroMaterial(Blocks.STONE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.GRANITE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.POLISHED_GRANITE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DIORITE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.POLISHED_DIORITE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.ANDESITE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.POLISHED_ANDESITE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.GRASS_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DIRT));
            registerMaterial(r, new BlockMicroMaterial(Blocks.COARSE_DIRT));
            registerMaterial(r, new BlockMicroMaterial(Blocks.PODZOL));
            registerMaterial(r, new BlockMicroMaterial(Blocks.COBBLESTONE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.OAK_PLANKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.SPRUCE_PLANKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BIRCH_PLANKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.JUNGLE_PLANKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.ACACIA_PLANKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CHERRY_PLANKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DARK_OAK_PLANKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.MANGROVE_PLANKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BAMBOO_PLANKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BAMBOO_MOSAIC));
            registerMaterial(r, new BlockMicroMaterial(Blocks.SAND)); //TODO Gravity?
            registerMaterial(r, new BlockMicroMaterial(Blocks.RED_SAND)); //TODO Gravity?
            registerMaterial(r, new BlockMicroMaterial(Blocks.GRAVEL));
            registerMaterial(r, new BlockMicroMaterial(Blocks.GOLD_ORE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DEEPSLATE_GOLD_ORE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.IRON_ORE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DEEPSLATE_IRON_ORE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.COAL_ORE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DEEPSLATE_COAL_ORE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.NETHER_GOLD_ORE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.OAK_LOG));
            registerMaterial(r, new BlockMicroMaterial(Blocks.SPRUCE_LOG));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BIRCH_LOG));
            registerMaterial(r, new BlockMicroMaterial(Blocks.JUNGLE_LOG));
            registerMaterial(r, new BlockMicroMaterial(Blocks.ACACIA_LOG));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CHERRY_LOG));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DARK_OAK_LOG));
            registerMaterial(r, new BlockMicroMaterial(Blocks.MANGROVE_LOG));
            registerMaterial(r, new BlockMicroMaterial(Blocks.MANGROVE_ROOTS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.MUDDY_MANGROVE_ROOTS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.STRIPPED_SPRUCE_LOG));
            registerMaterial(r, new BlockMicroMaterial(Blocks.STRIPPED_BIRCH_LOG));
            registerMaterial(r, new BlockMicroMaterial(Blocks.STRIPPED_JUNGLE_LOG));
            registerMaterial(r, new BlockMicroMaterial(Blocks.STRIPPED_ACACIA_LOG));
            registerMaterial(r, new BlockMicroMaterial(Blocks.STRIPPED_CHERRY_LOG));
            registerMaterial(r, new BlockMicroMaterial(Blocks.STRIPPED_DARK_OAK_LOG));
            registerMaterial(r, new BlockMicroMaterial(Blocks.STRIPPED_OAK_LOG));
            registerMaterial(r, new BlockMicroMaterial(Blocks.STRIPPED_MANGROVE_LOG));
            registerMaterial(r, new BlockMicroMaterial(Blocks.STRIPPED_BAMBOO_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.OAK_WOOD));
            registerMaterial(r, new BlockMicroMaterial(Blocks.SPRUCE_WOOD));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BIRCH_WOOD));
            registerMaterial(r, new BlockMicroMaterial(Blocks.JUNGLE_WOOD));
            registerMaterial(r, new BlockMicroMaterial(Blocks.ACACIA_WOOD));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CHERRY_WOOD));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DARK_OAK_WOOD));
            registerMaterial(r, new BlockMicroMaterial(Blocks.MANGROVE_WOOD));
            registerMaterial(r, new BlockMicroMaterial(Blocks.STRIPPED_OAK_WOOD));
            registerMaterial(r, new BlockMicroMaterial(Blocks.STRIPPED_SPRUCE_WOOD));
            registerMaterial(r, new BlockMicroMaterial(Blocks.STRIPPED_BIRCH_WOOD));
            registerMaterial(r, new BlockMicroMaterial(Blocks.STRIPPED_JUNGLE_WOOD));
            registerMaterial(r, new BlockMicroMaterial(Blocks.STRIPPED_ACACIA_WOOD));
            registerMaterial(r, new BlockMicroMaterial(Blocks.STRIPPED_CHERRY_WOOD));
            registerMaterial(r, new BlockMicroMaterial(Blocks.STRIPPED_DARK_OAK_WOOD));
            registerMaterial(r, new BlockMicroMaterial(Blocks.STRIPPED_MANGROVE_WOOD));
            registerMaterial(r, new BlockMicroMaterial(Blocks.OAK_LEAVES));
            registerMaterial(r, new BlockMicroMaterial(Blocks.SPRUCE_LEAVES));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BIRCH_LEAVES));
            registerMaterial(r, new BlockMicroMaterial(Blocks.JUNGLE_LEAVES));
            registerMaterial(r, new BlockMicroMaterial(Blocks.ACACIA_LEAVES));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CHERRY_LEAVES));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DARK_OAK_LEAVES));
            registerMaterial(r, new BlockMicroMaterial(Blocks.MANGROVE_LEAVES));
            registerMaterial(r, new BlockMicroMaterial(Blocks.AZALEA_LEAVES));
            registerMaterial(r, new BlockMicroMaterial(Blocks.FLOWERING_AZALEA_LEAVES));
            registerMaterial(r, new BlockMicroMaterial(Blocks.SPONGE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.WET_SPONGE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.GLASS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.LAPIS_ORE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DEEPSLATE_LAPIS_ORE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.LAPIS_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.SANDSTONE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CHISELED_SANDSTONE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CUT_SANDSTONE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.WHITE_WOOL));
            registerMaterial(r, new BlockMicroMaterial(Blocks.ORANGE_WOOL));
            registerMaterial(r, new BlockMicroMaterial(Blocks.MAGENTA_WOOL));
            registerMaterial(r, new BlockMicroMaterial(Blocks.LIGHT_BLUE_WOOL));
            registerMaterial(r, new BlockMicroMaterial(Blocks.YELLOW_WOOL));
            registerMaterial(r, new BlockMicroMaterial(Blocks.LIME_WOOL));
            registerMaterial(r, new BlockMicroMaterial(Blocks.PINK_WOOL));
            registerMaterial(r, new BlockMicroMaterial(Blocks.GRAY_WOOL));
            registerMaterial(r, new BlockMicroMaterial(Blocks.LIGHT_GRAY_WOOL));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CYAN_WOOL));
            registerMaterial(r, new BlockMicroMaterial(Blocks.PURPLE_WOOL));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BLUE_WOOL));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BROWN_WOOL));
            registerMaterial(r, new BlockMicroMaterial(Blocks.GREEN_WOOL));
            registerMaterial(r, new BlockMicroMaterial(Blocks.RED_WOOL));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BLACK_WOOL));
            registerMaterial(r, new BlockMicroMaterial(Blocks.GOLD_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.IRON_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BRICKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.TNT)); //TODO, make explode?
            registerMaterial(r, new BlockMicroMaterial(Blocks.BOOKSHELF));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CHISELED_BOOKSHELF));
            registerMaterial(r, new BlockMicroMaterial(Blocks.MOSSY_COBBLESTONE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.OBSIDIAN));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DIAMOND_ORE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DEEPSLATE_DIAMOND_ORE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DIAMOND_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CRAFTING_TABLE)); //TODO Actually function?
            registerMaterial(r, new BlockMicroMaterial(Blocks.REDSTONE_ORE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DEEPSLATE_REDSTONE_ORE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.ICE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.SNOW_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CLAY));
            registerMaterial(r, new BlockMicroMaterial(Blocks.PUMPKIN));
            registerMaterial(r, new BlockMicroMaterial(Blocks.NETHERRACK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.SOUL_SAND));
            registerMaterial(r, new BlockMicroMaterial(Blocks.SOUL_SOIL));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BASALT));
            registerMaterial(r, new BlockMicroMaterial(Blocks.POLISHED_BASALT));
            registerMaterial(r, new BlockMicroMaterial(Blocks.GLOWSTONE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CARVED_PUMPKIN));
            registerMaterial(r, new BlockMicroMaterial(Blocks.JACK_O_LANTERN));
            registerMaterial(r, new BlockMicroMaterial(Blocks.WHITE_STAINED_GLASS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.ORANGE_STAINED_GLASS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.MAGENTA_STAINED_GLASS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.LIGHT_BLUE_STAINED_GLASS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.YELLOW_STAINED_GLASS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.LIME_STAINED_GLASS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.PINK_STAINED_GLASS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.GRAY_STAINED_GLASS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.LIGHT_GRAY_STAINED_GLASS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CYAN_STAINED_GLASS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.PURPLE_STAINED_GLASS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BLUE_STAINED_GLASS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BROWN_STAINED_GLASS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.GREEN_STAINED_GLASS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.RED_STAINED_GLASS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BLACK_STAINED_GLASS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.STONE_BRICKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.MOSSY_STONE_BRICKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CRACKED_STONE_BRICKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CHISELED_STONE_BRICKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.PACKED_MUD));
            registerMaterial(r, new BlockMicroMaterial(Blocks.MUD_BRICKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BROWN_MUSHROOM_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.RED_MUSHROOM_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.MUSHROOM_STEM));
            registerMaterial(r, new BlockMicroMaterial(Blocks.MELON));
            registerMaterial(r, new BlockMicroMaterial(Blocks.MYCELIUM));
            registerMaterial(r, new BlockMicroMaterial(Blocks.NETHER_BRICKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.END_STONE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.REDSTONE_LAMP.defaultBlockState().setValue(RedstoneLampBlock.LIT, true))); //?
            registerMaterial(r, new BlockMicroMaterial(Blocks.EMERALD_ORE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DEEPSLATE_EMERALD_ORE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.EMERALD_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.REDSTONE_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.NETHER_QUARTZ_ORE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.QUARTZ_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CHISELED_QUARTZ_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.QUARTZ_PILLAR));
            registerMaterial(r, new BlockMicroMaterial(Blocks.WHITE_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.ORANGE_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.MAGENTA_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.LIGHT_BLUE_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.YELLOW_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.LIME_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.PINK_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.GRAY_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.LIGHT_GRAY_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CYAN_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.PURPLE_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BLUE_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BROWN_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.GREEN_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.RED_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BLACK_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.SLIME_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.PRISMARINE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.PRISMARINE_BRICKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DARK_PRISMARINE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.SEA_LANTERN));
            registerMaterial(r, new BlockMicroMaterial(Blocks.HAY_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.COAL_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.PACKED_ICE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.RED_SANDSTONE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CHISELED_RED_SANDSTONE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CUT_RED_SANDSTONE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.SMOOTH_STONE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.SMOOTH_SANDSTONE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.SMOOTH_QUARTZ));
            registerMaterial(r, new BlockMicroMaterial(Blocks.SMOOTH_RED_SANDSTONE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.PURPUR_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.PURPUR_PILLAR));
            registerMaterial(r, new BlockMicroMaterial(Blocks.END_STONE_BRICKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DIRT_PATH));
            registerMaterial(r, new BlockMicroMaterial(Blocks.MAGMA_BLOCK)); //TODO Burn?
            registerMaterial(r, new BlockMicroMaterial(Blocks.NETHER_WART_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.RED_NETHER_BRICKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BONE_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.WHITE_GLAZED_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.ORANGE_GLAZED_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.MAGENTA_GLAZED_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.YELLOW_GLAZED_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.LIME_GLAZED_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.PINK_GLAZED_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.GRAY_GLAZED_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CYAN_GLAZED_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.PURPLE_GLAZED_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BLUE_GLAZED_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BROWN_GLAZED_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.GREEN_GLAZED_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.RED_GLAZED_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BLACK_GLAZED_TERRACOTTA));
            registerMaterial(r, new BlockMicroMaterial(Blocks.WHITE_CONCRETE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.ORANGE_CONCRETE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.MAGENTA_CONCRETE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.LIGHT_BLUE_CONCRETE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.YELLOW_CONCRETE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.LIME_CONCRETE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.PINK_CONCRETE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.GRAY_CONCRETE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.LIGHT_GRAY_CONCRETE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CYAN_CONCRETE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.PURPLE_CONCRETE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BLUE_CONCRETE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BROWN_CONCRETE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.GREEN_CONCRETE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.RED_CONCRETE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BLACK_CONCRETE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.WHITE_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
            registerMaterial(r, new BlockMicroMaterial(Blocks.ORANGE_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
            registerMaterial(r, new BlockMicroMaterial(Blocks.MAGENTA_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
            registerMaterial(r, new BlockMicroMaterial(Blocks.LIGHT_BLUE_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
            registerMaterial(r, new BlockMicroMaterial(Blocks.YELLOW_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
            registerMaterial(r, new BlockMicroMaterial(Blocks.LIME_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
            registerMaterial(r, new BlockMicroMaterial(Blocks.PINK_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
            registerMaterial(r, new BlockMicroMaterial(Blocks.GRAY_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
            registerMaterial(r, new BlockMicroMaterial(Blocks.LIGHT_GRAY_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
            registerMaterial(r, new BlockMicroMaterial(Blocks.CYAN_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
            registerMaterial(r, new BlockMicroMaterial(Blocks.PURPLE_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
            registerMaterial(r, new BlockMicroMaterial(Blocks.BLUE_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
            registerMaterial(r, new BlockMicroMaterial(Blocks.BROWN_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
            registerMaterial(r, new BlockMicroMaterial(Blocks.GREEN_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
            registerMaterial(r, new BlockMicroMaterial(Blocks.RED_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
            registerMaterial(r, new BlockMicroMaterial(Blocks.BLACK_CONCRETE_POWDER)); //TODO Gravity -> Concrete?
            registerMaterial(r, new BlockMicroMaterial(Blocks.DRIED_KELP_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DEAD_TUBE_CORAL_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DEAD_BRAIN_CORAL_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DEAD_BUBBLE_CORAL_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DEAD_FIRE_CORAL_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DEAD_HORN_CORAL_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.TUBE_CORAL_BLOCK)); //TODO Dies out of water
            registerMaterial(r, new BlockMicroMaterial(Blocks.BRAIN_CORAL_BLOCK)); //TODO Dies out of water
            registerMaterial(r, new BlockMicroMaterial(Blocks.BUBBLE_CORAL_BLOCK)); //TODO Dies out of water
            registerMaterial(r, new BlockMicroMaterial(Blocks.FIRE_CORAL_BLOCK)); //TODO Dies out of water
            registerMaterial(r, new BlockMicroMaterial(Blocks.HORN_CORAL_BLOCK)); //TODO Dies out of water
            registerMaterial(r, new BlockMicroMaterial(Blocks.BLUE_ICE)); //TODO speed
            registerMaterial(r, new BlockMicroMaterial(Blocks.WARPED_STEM));
            registerMaterial(r, new BlockMicroMaterial(Blocks.STRIPPED_WARPED_STEM));
            registerMaterial(r, new BlockMicroMaterial(Blocks.WARPED_HYPHAE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.STRIPPED_WARPED_HYPHAE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.WARPED_NYLIUM));
            registerMaterial(r, new BlockMicroMaterial(Blocks.WARPED_WART_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CRIMSON_STEM));
            registerMaterial(r, new BlockMicroMaterial(Blocks.STRIPPED_CRIMSON_STEM));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CRIMSON_HYPHAE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.STRIPPED_CRIMSON_HYPHAE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CRIMSON_NYLIUM));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CRIMSON_PLANKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.WARPED_PLANKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.HONEY_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.HONEYCOMB_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.NETHERITE_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.ANCIENT_DEBRIS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CRYING_OBSIDIAN));
            registerMaterial(r, new BlockMicroMaterial(Blocks.LODESTONE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.BLACKSTONE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.POLISHED_BLACKSTONE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.POLISHED_BLACKSTONE_BRICKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CHISELED_POLISHED_BLACKSTONE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.GILDED_BLACKSTONE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CHISELED_NETHER_BRICKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CRACKED_NETHER_BRICKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.QUARTZ_BRICKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.AMETHYST_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.TUFF));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CALCITE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.TINTED_GLASS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.OXIDIZED_COPPER));
            registerMaterial(r, new BlockMicroMaterial(Blocks.WEATHERED_COPPER));
            registerMaterial(r, new BlockMicroMaterial(Blocks.EXPOSED_COPPER));
            registerMaterial(r, new BlockMicroMaterial(Blocks.COPPER_BLOCK)); //TODO Oxidization (normal -> exposed -> weathered -> oxidized)
            registerMaterial(r, new BlockMicroMaterial(Blocks.COPPER_ORE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DEEPSLATE_COPPER_ORE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.OXIDIZED_CUT_COPPER));
            registerMaterial(r, new BlockMicroMaterial(Blocks.WEATHERED_CUT_COPPER));
            registerMaterial(r, new BlockMicroMaterial(Blocks.EXPOSED_CUT_COPPER));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CUT_COPPER)); //TODO Oxidization (normal -> exposed -> weathered -> oxidized)
            registerMaterial(r, new BlockMicroMaterial(Blocks.WAXED_COPPER_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.WAXED_WEATHERED_COPPER));
            registerMaterial(r, new BlockMicroMaterial(Blocks.WAXED_EXPOSED_COPPER));
            registerMaterial(r, new BlockMicroMaterial(Blocks.WAXED_OXIDIZED_COPPER));
            registerMaterial(r, new BlockMicroMaterial(Blocks.WAXED_OXIDIZED_CUT_COPPER));
            registerMaterial(r, new BlockMicroMaterial(Blocks.WAXED_WEATHERED_CUT_COPPER));
            registerMaterial(r, new BlockMicroMaterial(Blocks.WAXED_EXPOSED_CUT_COPPER));
            registerMaterial(r, new BlockMicroMaterial(Blocks.WAXED_CUT_COPPER));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DRIPSTONE_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.ROOTED_DIRT));
            registerMaterial(r, new BlockMicroMaterial(Blocks.MUD));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DEEPSLATE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.COBBLED_DEEPSLATE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.POLISHED_DEEPSLATE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DEEPSLATE_TILES));
            registerMaterial(r, new BlockMicroMaterial(Blocks.DEEPSLATE_BRICKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CHISELED_DEEPSLATE));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CRACKED_DEEPSLATE_BRICKS));
            registerMaterial(r, new BlockMicroMaterial(Blocks.CRACKED_DEEPSLATE_TILES));
            registerMaterial(r, new BlockMicroMaterial(Blocks.SMOOTH_BASALT));
            registerMaterial(r, new BlockMicroMaterial(Blocks.RAW_IRON_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.RAW_COPPER_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.RAW_GOLD_BLOCK));
            registerMaterial(r, new BlockMicroMaterial(Blocks.OCHRE_FROGLIGHT));
            registerMaterial(r, new BlockMicroMaterial(Blocks.VERDANT_FROGLIGHT));
            registerMaterial(r, new BlockMicroMaterial(Blocks.PEARLESCENT_FROGLIGHT));
            registerMaterial(r, new BlockMicroMaterial(Blocks.REINFORCED_DEEPSLATE));
        });
    }

    private static void registerMaterial(RegisterEvent.RegisterHelper<MicroMaterial> r, BlockMicroMaterial material) {
        r.register(BlockMicroMaterial.makeMaterialKey(material.state), material);
    }

    private static void processIMC(InterModProcessEvent event) {
        ForgeRegistry<MicroMaterial> registry = (ForgeRegistry<MicroMaterial>) MicroMaterialRegistry.MICRO_MATERIALS;
        registry.unfreeze();
        event.getIMCStream().forEach(e -> {
            if (!e.method().equals("micro_material")) return;

            String sender = e.senderModId();
            Object sent = e.messageSupplier().get();
            BlockMicroMaterial material;
            if (sent instanceof Block b) {
                material = new BlockMicroMaterial(b);
            } else if (sent instanceof BlockState s) {
                material = new BlockMicroMaterial(s);
            } else {
                LOGGER.error(
                        "Mod {} tried to register a MicroMaterial with an invalid message. Object: '{}', Class: '{}'. IMC only supports Block or BlockState messages.",
                        sender,
                        sent,
                        sent != null ? sent.getClass().getName() : null
                );
                return;
            }

            ResourceLocation key = BlockMicroMaterial.makeMaterialKey(material.state);
            if (registry.containsKey(key)) {
                LOGGER.warn("Mod '{}' tried to register a duplicate MicroMaterial. '{}'. Ignoring.", sender, key);
                return;
            }
            registry.register(key, material);

        });
        registry.freeze();
    }
}
