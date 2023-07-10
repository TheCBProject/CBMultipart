package codechicken.multipart.minecraft;

import codechicken.lib.util.ArrayUtils;
import codechicken.multipart.api.MultipartType;
import codechicken.multipart.api.PartConverter;
import codechicken.multipart.api.SimpleMultipartType;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.init.MultiPartRegistries;
import codechicken.multipart.util.MultipartPlaceContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegisterEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by covers1624 on 1/9/20.
 */
public class ModContent {

    @ObjectHolder (registryName = "cb_multipart:multipart_types", value = "minecraft:torch")
    public static MultipartType<TorchPart> torchPartType;

    @ObjectHolder (registryName = "cb_multipart:multipart_types", value = "minecraft:soul_torch")
    public static MultipartType<SoulTorchPart> soulTorchPartType;

    @ObjectHolder (registryName = "cb_multipart:multipart_types", value = "minecraft:redstone_torch")
    public static MultipartType<RedstoneTorchPart> redstoneTorchPartType;

    @ObjectHolder (registryName = "cb_multipart:multipart_types", value = "minecraft:lever")
    public static MultipartType<LeverPart> leverPartType;

    @ObjectHolder (registryName = "cb_multipart:multipart_types", value = "minecraft:stone_button")
    public static MultipartType<ButtonPart.StoneButtonPart> stoneButtonPartType;

    @ObjectHolder (registryName = "cb_multipart:multipart_types", value = "minecraft:polished_blackstone_button")
    public static MultipartType<ButtonPart.PolishedBlackstoneButtonPart> polishedBlackstoneButtonPartType;
    @ObjectHolder (registryName = "cb_multipart:multipart_types", value = "minecraft:oak_button")
    public static MultipartType<ButtonPart.OakButtonPart> oakButtonPartType;
    @ObjectHolder (registryName = "cb_multipart:multipart_types", value = "minecraft:spruce_button")
    public static MultipartType<ButtonPart.SpruceButtonPart> spruceButtonPartType;
    @ObjectHolder (registryName = "cb_multipart:multipart_types", value = "minecraft:birch_button")
    public static MultipartType<ButtonPart.BirchButtonPart> birchButtonPartType;
    @ObjectHolder (registryName = "cb_multipart:multipart_types", value = "minecraft:jungle_button")
    public static MultipartType<ButtonPart.JungleButtonPart> jungleButtonPartType;
    @ObjectHolder (registryName = "cb_multipart:multipart_types", value = "minecraft:acacia_button")
    public static MultipartType<ButtonPart.AcaciaButtonPart> acaciaButtonPartType;
    @ObjectHolder (registryName = "cb_multipart:multipart_types", value = "minecraft:dark_oak_button")
    public static MultipartType<ButtonPart.DarkOakButtonPart> darkOakButtonPartType;
    @ObjectHolder (registryName = "cb_multipart:multipart_types", value = "minecraft:crimson_button")
    public static MultipartType<ButtonPart.CrimsonButtonPart> crimsonButtonPartType;
    @ObjectHolder (registryName = "cb_multipart:multipart_types", value = "minecraft:warped_button")
    public static MultipartType<ButtonPart.WarpedButtonPart> warpedButtonPartType;

    @SubscribeEvent
    public static void onRegisterMultiParts(RegisterEvent event) {
        event.register(MultiPartRegistries.MULTIPART_TYPES.getRegistryKey(), r -> {
            ModContainer container = ModLoadingContext.get().getActiveContainer();
            ModLoadingContext.get().setActiveContainer(null);

            r.register("torch", new SimpleMultipartType<>(e -> new TorchPart()));
            r.register("soul_torch", new SimpleMultipartType<>(e -> new SoulTorchPart()));
            r.register("redstone_torch", new SimpleMultipartType<>(e -> new RedstoneTorchPart()));
            r.register("lever", new SimpleMultipartType<>(e -> new LeverPart()));

            r.register("stone_button", new SimpleMultipartType<>(e -> new ButtonPart.StoneButtonPart()));

            r.register("polished_blackstone_button", new SimpleMultipartType<>(e -> new ButtonPart.PolishedBlackstoneButtonPart()));
            r.register("oak_button", new SimpleMultipartType<>(e -> new ButtonPart.OakButtonPart()));
            r.register("spruce_button", new SimpleMultipartType<>(e -> new ButtonPart.SpruceButtonPart()));
            r.register("birch_button", new SimpleMultipartType<>(e -> new ButtonPart.BirchButtonPart()));
            r.register("jungle_button", new SimpleMultipartType<>(e -> new ButtonPart.JungleButtonPart()));
            r.register("acacia_button", new SimpleMultipartType<>(e -> new ButtonPart.AcaciaButtonPart()));
            r.register("dark_oak_button", new SimpleMultipartType<>(e -> new ButtonPart.DarkOakButtonPart()));
            r.register("crimson_button", new SimpleMultipartType<>(e -> new ButtonPart.CrimsonButtonPart()));
            r.register("warped_button", new SimpleMultipartType<>(e -> new ButtonPart.WarpedButtonPart()));

            ModLoadingContext.get().setActiveContainer(container);
        });
    }

    @SubscribeEvent
    public static void onRegisterMultiPartConverters(RegisterEvent event) {

        event.register(MultiPartRegistries.PART_CONVERTERS.getRegistryKey(), r -> {
            ModContainer container = ModLoadingContext.get().getActiveContainer();
            ModLoadingContext.get().setActiveContainer(null);

            r.register("torch", new Converter<>(TorchPart::new, TorchPart::new, Items.TORCH, Blocks.TORCH, Blocks.WALL_TORCH));
            r.register("soul_torch", new Converter<>(SoulTorchPart::new, SoulTorchPart::new, Items.SOUL_TORCH, Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH));
            r.register("redstone_torch", new Converter<>(RedstoneTorchPart::new, RedstoneTorchPart::new, Items.REDSTONE_TORCH, Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH));
            r.register("lever", new Converter<>(LeverPart::new, LeverPart::new, Items.LEVER, Blocks.LEVER));

            r.register("stone_button", new Converter<>(ButtonPart.StoneButtonPart::new, ButtonPart.StoneButtonPart::new, Items.STONE_BUTTON, Blocks.STONE_BUTTON));

            r.register("polished_blackstone_button", new Converter<>(ButtonPart.PolishedBlackstoneButtonPart::new, ButtonPart.PolishedBlackstoneButtonPart::new, Items.POLISHED_BLACKSTONE_BUTTON, Blocks.POLISHED_BLACKSTONE_BUTTON));
            r.register("oak_button", new Converter<>(ButtonPart.OakButtonPart::new, ButtonPart.OakButtonPart::new, Items.OAK_BUTTON, Blocks.OAK_BUTTON));
            r.register("spruce_button", new Converter<>(ButtonPart.SpruceButtonPart::new, ButtonPart.SpruceButtonPart::new, Items.SPRUCE_BUTTON, Blocks.SPRUCE_BUTTON));
            r.register("birch_button", new Converter<>(ButtonPart.BirchButtonPart::new, ButtonPart.BirchButtonPart::new, Items.BIRCH_BUTTON, Blocks.BIRCH_BUTTON));
            r.register("jungle_button", new Converter<>(ButtonPart.JungleButtonPart::new, ButtonPart.JungleButtonPart::new, Items.JUNGLE_BUTTON, Blocks.JUNGLE_BUTTON));
            r.register("acacia_button", new Converter<>(ButtonPart.AcaciaButtonPart::new, ButtonPart.AcaciaButtonPart::new, Items.ACACIA_BUTTON, Blocks.ACACIA_BUTTON));
            r.register("dark_oak_button", new Converter<>(ButtonPart.DarkOakButtonPart::new, ButtonPart.DarkOakButtonPart::new, Items.DARK_OAK_BUTTON, Blocks.DARK_OAK_BUTTON));
            r.register("crimson_button", new Converter<>(ButtonPart.CrimsonButtonPart::new, ButtonPart.CrimsonButtonPart::new, Items.CRIMSON_BUTTON, Blocks.CRIMSON_BUTTON));
            r.register("warped_button", new Converter<>(ButtonPart.WarpedButtonPart::new, ButtonPart.WarpedButtonPart::new, Items.WARPED_BUTTON, Blocks.WARPED_BUTTON));

            ModLoadingContext.get().setActiveContainer(container);
        });

    }

    private static class Converter<T extends McStatePart> extends PartConverter {

        private final Supplier<T> factory;
        private final Function<BlockState, T> blockFactory;
        private final Item item;
        private final Block[] blocks;

        private Converter(Supplier<T> factory, Function<BlockState, T> blockFactory, Item item, Block... blocks) {
            this.factory = factory;
            this.blockFactory = blockFactory;
            this.item = item;
            this.blocks = blocks;
        }

        @Override
        public ConversionResult<Collection<MultiPart>> convert(LevelAccessor world, BlockPos pos, BlockState state) {
            if (blocks.length == 0) {
                return PartConverter.emptyResultList();
            }
            if (ArrayUtils.contains(blocks, state.getBlock())) {
                return ConversionResult.success(Collections.singleton(blockFactory.apply(state)));
            }
            return super.convert(world, pos, state);
        }

        @Override
        public ConversionResult<MultiPart> convert(MultipartPlaceContext context) {
            if (context.getItemInHand().getItem() != item) {
                return emptyResult();
            }
            MultiPart result = factory.get().setStateOnPlacement(context);
            if (result != null) {
                return ConversionResult.success(result);
            }
            return super.convert(context);
        }
    }
}
