package codechicken.multipart.minecraft;

import codechicken.lib.util.ArrayUtils;
import codechicken.multipart.api.MultiPartType;
import codechicken.multipart.api.PartConverter;
import codechicken.multipart.api.SimpleMultiPartType;
import codechicken.multipart.api.part.TMultiPart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResultHolder;
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
//@Mod.EventBusSubscriber (modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD) //TODO JavaFML Loader
public class ModContent {

    @ObjectHolder ("torch")
    public static MultiPartType<TorchPart> torchPartType;

    @ObjectHolder ("redstone_torch")
    public static MultiPartType<RedstoneTorchPart> redstoneTorchPartType;

    @ObjectHolder ("lever")
    public static MultiPartType<LeverPart> leverPartType;

    @ObjectHolder ("stone_button")
    public static MultiPartType<ButtonPart.StoneButtonPart> stoneButtonPartType;

    @ObjectHolder ("oak_button")
    public static MultiPartType<ButtonPart.OakButtonPart> oakButtonPartType;
    @ObjectHolder ("spruce_button")
    public static MultiPartType<ButtonPart.SpruceButtonPart> spruceButtonPartType;
    @ObjectHolder ("birch_button")
    public static MultiPartType<ButtonPart.BirchButtonPart> birchButtonPartType;
    @ObjectHolder ("jungle_button")
    public static MultiPartType<ButtonPart.JungleButtonPart> jungleButtonPartType;
    @ObjectHolder ("acacia_button")
    public static MultiPartType<ButtonPart.AcaciaButtonPart> acaciaButtonPartType;
    @ObjectHolder ("dark_oak_button")
    public static MultiPartType<ButtonPart.DarkOakButtonPart> darkOakButtonPartType;

    @SubscribeEvent
    public static void onRegisterMultiParts(RegistryEvent.Register<MultiPartType<?>> event) {
        IForgeRegistry<MultiPartType<?>> r = event.getRegistry();
        ModContainer container = ModLoadingContext.get().getActiveContainer();
        ModLoadingContext.get().setActiveContainer(null);

        r.register(new SimpleMultiPartType<>(e -> new TorchPart()).setRegistryName("torch"));
        r.register(new SimpleMultiPartType<>(e -> new RedstoneTorchPart()).setRegistryName("redstone_torch"));
        r.register(new SimpleMultiPartType<>(e -> new LeverPart()).setRegistryName("lever"));

        r.register(new SimpleMultiPartType<>(e -> new ButtonPart.StoneButtonPart()).setRegistryName("stone_button"));

        r.register(new SimpleMultiPartType<>(e -> new ButtonPart.OakButtonPart()).setRegistryName("oak_button"));
        r.register(new SimpleMultiPartType<>(e -> new ButtonPart.SpruceButtonPart()).setRegistryName("spruce_button"));
        r.register(new SimpleMultiPartType<>(e -> new ButtonPart.BirchButtonPart()).setRegistryName("birch_button"));
        r.register(new SimpleMultiPartType<>(e -> new ButtonPart.JungleButtonPart()).setRegistryName("jungle_button"));
        r.register(new SimpleMultiPartType<>(e -> new ButtonPart.AcaciaButtonPart()).setRegistryName("acacia_button"));
        r.register(new SimpleMultiPartType<>(e -> new ButtonPart.DarkOakButtonPart()).setRegistryName("dark_oak_button"));

        ModLoadingContext.get().setActiveContainer(container);
    }

    @SubscribeEvent
    public static void onRegisterMultiPartConverters(RegistryEvent.Register<PartConverter> event) {
        IForgeRegistry<PartConverter> r = event.getRegistry();

        ModContainer container = ModLoadingContext.get().getActiveContainer();
        ModLoadingContext.get().setActiveContainer(null);

        r.register(new Converter<>(TorchPart::new, TorchPart::new, Items.TORCH, Blocks.TORCH, Blocks.WALL_TORCH).setRegistryName("torch"));
        r.register(new Converter<>(RedstoneTorchPart::new, RedstoneTorchPart::new, Items.REDSTONE_TORCH, Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH).setRegistryName("redstone_torch"));
        r.register(new Converter<>(LeverPart::new, LeverPart::new, Items.LEVER, Blocks.LEVER).setRegistryName("lever"));

        r.register(new Converter<>(ButtonPart.StoneButtonPart::new, ButtonPart.StoneButtonPart::new, Items.STONE_BUTTON, Blocks.STONE_BUTTON).setRegistryName("stone_button"));

        r.register(new Converter<>(ButtonPart.OakButtonPart::new, ButtonPart.OakButtonPart::new, Items.OAK_BUTTON, Blocks.OAK_BUTTON).setRegistryName("oak_button"));
        r.register(new Converter<>(ButtonPart.SpruceButtonPart::new, ButtonPart.SpruceButtonPart::new, Items.SPRUCE_BUTTON, Blocks.SPRUCE_BUTTON).setRegistryName("spruce_button"));
        r.register(new Converter<>(ButtonPart.BirchButtonPart::new, ButtonPart.BirchButtonPart::new, Items.BIRCH_BUTTON, Blocks.BIRCH_BUTTON).setRegistryName("birch_button"));
        r.register(new Converter<>(ButtonPart.JungleButtonPart::new, ButtonPart.JungleButtonPart::new, Items.JUNGLE_BUTTON, Blocks.JUNGLE_BUTTON).setRegistryName("jungle_button"));
        r.register(new Converter<>(ButtonPart.AcaciaButtonPart::new, ButtonPart.AcaciaButtonPart::new, Items.ACACIA_BUTTON, Blocks.ACACIA_BUTTON).setRegistryName("acacia_button"));
        r.register(new Converter<>(ButtonPart.DarkOakButtonPart::new, ButtonPart.DarkOakButtonPart::new, Items.DARK_OAK_BUTTON, Blocks.DARK_OAK_BUTTON).setRegistryName("dark_oak_button"));

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
        public InteractionResultHolder<Collection<TMultiPart>> convert(LevelAccessor world, BlockPos pos, BlockState state) {
            if (blocks.length == 0) {
                return PartConverter.emptyResultList();
            }
            if (ArrayUtils.contains(blocks, state.getBlock())) {
                return InteractionResultHolder.success(Collections.singleton(blockFactory.apply(state)));
            }
            return super.convert(world, pos, state);
        }

        @Override
        public InteractionResultHolder<TMultiPart> convert(UseOnContext context) {
            if (context.getItemInHand().getItem() != item) {
                return emptyResult();
            }
            TMultiPart result = factory.get().setStateOnPlacement(new BlockPlaceContext(context));
            if (result != null) {
                return InteractionResultHolder.success(result);
            }
            return super.convert(context);
        }
    }
}
