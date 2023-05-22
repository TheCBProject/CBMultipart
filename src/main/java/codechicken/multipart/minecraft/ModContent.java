package codechicken.multipart.minecraft;

import codechicken.lib.util.ArrayUtils;
import codechicken.multipart.api.MultipartType;
import codechicken.multipart.api.PartConverter;
import codechicken.multipart.api.SimpleMultipartType;
import codechicken.multipart.api.part.MultiPart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by covers1624 on 1/9/20.
 */
@ObjectHolder ("minecraft")
public class ModContent {

    @ObjectHolder ("torch")
    public static MultipartType<TorchPart> torchPartType;

    @ObjectHolder ("soul_torch")
    public static MultipartType<SoulTorchPart> soulTorchPartType;

    @ObjectHolder ("redstone_torch")
    public static MultipartType<RedstoneTorchPart> redstoneTorchPartType;

    @ObjectHolder ("lever")
    public static MultipartType<LeverPart> leverPartType;

    @ObjectHolder ("stone_button")
    public static MultipartType<ButtonPart.StoneButtonPart> stoneButtonPartType;

    @ObjectHolder ("polished_blackstone_button")
    public static MultipartType<ButtonPart.PolishedBlackstoneButtonPart> polishedBlackstoneButtonPartType;
    @ObjectHolder ("oak_button")
    public static MultipartType<ButtonPart.OakButtonPart> oakButtonPartType;
    @ObjectHolder ("spruce_button")
    public static MultipartType<ButtonPart.SpruceButtonPart> spruceButtonPartType;
    @ObjectHolder ("birch_button")
    public static MultipartType<ButtonPart.BirchButtonPart> birchButtonPartType;
    @ObjectHolder ("jungle_button")
    public static MultipartType<ButtonPart.JungleButtonPart> jungleButtonPartType;
    @ObjectHolder ("acacia_button")
    public static MultipartType<ButtonPart.AcaciaButtonPart> acaciaButtonPartType;
    @ObjectHolder ("dark_oak_button")
    public static MultipartType<ButtonPart.DarkOakButtonPart> darkOakButtonPartType;
    @ObjectHolder ("crimson_button")
    public static MultipartType<ButtonPart.CrimsonButtonPart> crimsonButtonPartType;
    @ObjectHolder ("warped_button")
    public static MultipartType<ButtonPart.WarpedButtonPart> warpedButtonPartType;

    @SubscribeEvent
    public static void onRegisterMultiParts(RegistryEvent.Register<MultipartType<?>> event) {
        IForgeRegistry<MultipartType<?>> r = event.getRegistry();
        ModContainer container = ModLoadingContext.get().getActiveContainer();
        ModLoadingContext.get().setActiveContainer(null);

        r.register(new SimpleMultipartType<>(e -> new TorchPart()).setRegistryName("torch"));
        r.register(new SimpleMultipartType<>(e -> new SoulTorchPart()).setRegistryName("soul_torch"));
        r.register(new SimpleMultipartType<>(e -> new RedstoneTorchPart()).setRegistryName("redstone_torch"));
        r.register(new SimpleMultipartType<>(e -> new LeverPart()).setRegistryName("lever"));

        r.register(new SimpleMultipartType<>(e -> new ButtonPart.StoneButtonPart()).setRegistryName("stone_button"));

        r.register(new SimpleMultipartType<>(e -> new ButtonPart.PolishedBlackstoneButtonPart()).setRegistryName("polished_blackstone_button"));
        r.register(new SimpleMultipartType<>(e -> new ButtonPart.OakButtonPart()).setRegistryName("oak_button"));
        r.register(new SimpleMultipartType<>(e -> new ButtonPart.SpruceButtonPart()).setRegistryName("spruce_button"));
        r.register(new SimpleMultipartType<>(e -> new ButtonPart.BirchButtonPart()).setRegistryName("birch_button"));
        r.register(new SimpleMultipartType<>(e -> new ButtonPart.JungleButtonPart()).setRegistryName("jungle_button"));
        r.register(new SimpleMultipartType<>(e -> new ButtonPart.AcaciaButtonPart()).setRegistryName("acacia_button"));
        r.register(new SimpleMultipartType<>(e -> new ButtonPart.DarkOakButtonPart()).setRegistryName("dark_oak_button"));
        r.register(new SimpleMultipartType<>(e -> new ButtonPart.CrimsonButtonPart()).setRegistryName("crimson_button"));
        r.register(new SimpleMultipartType<>(e -> new ButtonPart.WarpedButtonPart()).setRegistryName("warped_button"));

        ModLoadingContext.get().setActiveContainer(container);
    }

    @SubscribeEvent
    public static void onRegisterMultiPartConverters(RegistryEvent.Register<PartConverter> event) {
        IForgeRegistry<PartConverter> r = event.getRegistry();

        ModContainer container = ModLoadingContext.get().getActiveContainer();
        ModLoadingContext.get().setActiveContainer(null);

        r.register(new Converter<>(TorchPart::new, TorchPart::new, Items.TORCH, Blocks.TORCH, Blocks.WALL_TORCH).setRegistryName("torch"));
        r.register(new Converter<>(SoulTorchPart::new, SoulTorchPart::new, Items.SOUL_TORCH, Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH).setRegistryName("soul_torch"));
        r.register(new Converter<>(RedstoneTorchPart::new, RedstoneTorchPart::new, Items.REDSTONE_TORCH, Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH).setRegistryName("redstone_torch"));
        r.register(new Converter<>(LeverPart::new, LeverPart::new, Items.LEVER, Blocks.LEVER).setRegistryName("lever"));

        r.register(new Converter<>(ButtonPart.StoneButtonPart::new, ButtonPart.StoneButtonPart::new, Items.STONE_BUTTON, Blocks.STONE_BUTTON).setRegistryName("stone_button"));

        r.register(new Converter<>(ButtonPart.PolishedBlackstoneButtonPart::new, ButtonPart.PolishedBlackstoneButtonPart::new, Items.POLISHED_BLACKSTONE_BUTTON, Blocks.POLISHED_BLACKSTONE_BUTTON).setRegistryName("polished_blackstone_button"));
        r.register(new Converter<>(ButtonPart.OakButtonPart::new, ButtonPart.OakButtonPart::new, Items.OAK_BUTTON, Blocks.OAK_BUTTON).setRegistryName("oak_button"));
        r.register(new Converter<>(ButtonPart.SpruceButtonPart::new, ButtonPart.SpruceButtonPart::new, Items.SPRUCE_BUTTON, Blocks.SPRUCE_BUTTON).setRegistryName("spruce_button"));
        r.register(new Converter<>(ButtonPart.BirchButtonPart::new, ButtonPart.BirchButtonPart::new, Items.BIRCH_BUTTON, Blocks.BIRCH_BUTTON).setRegistryName("birch_button"));
        r.register(new Converter<>(ButtonPart.JungleButtonPart::new, ButtonPart.JungleButtonPart::new, Items.JUNGLE_BUTTON, Blocks.JUNGLE_BUTTON).setRegistryName("jungle_button"));
        r.register(new Converter<>(ButtonPart.AcaciaButtonPart::new, ButtonPart.AcaciaButtonPart::new, Items.ACACIA_BUTTON, Blocks.ACACIA_BUTTON).setRegistryName("acacia_button"));
        r.register(new Converter<>(ButtonPart.DarkOakButtonPart::new, ButtonPart.DarkOakButtonPart::new, Items.DARK_OAK_BUTTON, Blocks.DARK_OAK_BUTTON).setRegistryName("dark_oak_button"));
        r.register(new Converter<>(ButtonPart.CrimsonButtonPart::new, ButtonPart.CrimsonButtonPart::new, Items.CRIMSON_BUTTON, Blocks.CRIMSON_BUTTON).setRegistryName("crimson_button"));
        r.register(new Converter<>(ButtonPart.WarpedButtonPart::new, ButtonPart.WarpedButtonPart::new, Items.WARPED_BUTTON, Blocks.WARPED_BUTTON).setRegistryName("warped_button"));

        ModLoadingContext.get().setActiveContainer(container);
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
        public ConversionResult<MultiPart> convert(UseOnContext context) {
            if (context.getItemInHand().getItem() != item) {
                return emptyResult();
            }
            MultiPart result = factory.get().setStateOnPlacement(new BlockPlaceContext(context));
            if (result != null) {
                return ConversionResult.success(result);
            }
            return super.convert(context);
        }
    }
}
