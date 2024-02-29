package codechicken.multipart.minecraft;

import codechicken.lib.util.ArrayUtils;
import codechicken.multipart.api.MultipartType;
import codechicken.multipart.api.PartConverter;
import codechicken.multipart.api.SimpleMultipartType;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.util.MultipartPlaceContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by covers1624 on 1/9/20.
 */
public class MinecraftMultipartModContent {

    private static final DeferredRegister<MultipartType<?>> MULTIPART_TYPES = DeferredRegister.create(MultipartType.MULTIPART_TYPES, "minecraft");
    private static final DeferredRegister<PartConverter> PART_CONVERTERS = DeferredRegister.create(PartConverter.PART_CONVERTERS, "minecraft");

    public static final RegistryObject<MultipartType<TorchPart>> TORCH_PART = MULTIPART_TYPES.register("torch", () -> new SimpleMultipartType<>(e -> new TorchPart()));
    public static final RegistryObject<MultipartType<SoulTorchPart>> SOUL_TORCH_PART = MULTIPART_TYPES.register("soul_torch", () -> new SimpleMultipartType<>(e -> new SoulTorchPart()));
    public static final RegistryObject<MultipartType<RedstoneTorchPart>> REDSTONE_TORCH_PART = MULTIPART_TYPES.register("redstone_torch", () -> new SimpleMultipartType<>(e -> new RedstoneTorchPart()));
    public static final RegistryObject<MultipartType<LeverPart>> LEVER_PART = MULTIPART_TYPES.register("lever", () -> new SimpleMultipartType<>(e -> new LeverPart()));

    public static final RegistryObject<MultipartType<ButtonPart.StoneButtonPart>> STONE_BUTTON_PART = MULTIPART_TYPES.register("stone_button", () -> new SimpleMultipartType<>(e -> new ButtonPart.StoneButtonPart()));
    public static final RegistryObject<MultipartType<ButtonPart.PolishedBlackstoneButtonPart>> POLISHED_BLACKSTONE_BUTTON_PART = MULTIPART_TYPES.register("polished_blackstone_button", () -> new SimpleMultipartType<>(e -> new ButtonPart.PolishedBlackstoneButtonPart()));

    public static final RegistryObject<MultipartType<ButtonPart.OakButtonPart>> OAK_BUTTON_PART = MULTIPART_TYPES.register("oak_button", () -> new SimpleMultipartType<>(e -> new ButtonPart.OakButtonPart()));
    public static final RegistryObject<MultipartType<ButtonPart.SpruceButtonPart>> SPRUCE_BUTTON_PART = MULTIPART_TYPES.register("spruce_button", () -> new SimpleMultipartType<>(e -> new ButtonPart.SpruceButtonPart()));
    public static final RegistryObject<MultipartType<ButtonPart.BirchButtonPart>> BIRCH_BUTTON_PART = MULTIPART_TYPES.register("birch_button", () -> new SimpleMultipartType<>(e -> new ButtonPart.BirchButtonPart()));
    public static final RegistryObject<MultipartType<ButtonPart.JungleButtonPart>> JUNGLE_BUTTON_PART = MULTIPART_TYPES.register("jungle_button", () -> new SimpleMultipartType<>(e -> new ButtonPart.JungleButtonPart()));
    public static final RegistryObject<MultipartType<ButtonPart.AcaciaButtonPart>> ACACIA_BUTTON_PART = MULTIPART_TYPES.register("acacia_button", () -> new SimpleMultipartType<>(e -> new ButtonPart.AcaciaButtonPart()));
    public static final RegistryObject<MultipartType<ButtonPart.DarkOakButtonPart>> DARK_OAK_BUTTON_PART = MULTIPART_TYPES.register("dark_oak_button", () -> new SimpleMultipartType<>(e -> new ButtonPart.DarkOakButtonPart()));
    public static final RegistryObject<MultipartType<ButtonPart.CrimsonButtonPart>> CRIMSON_BUTTON_PART = MULTIPART_TYPES.register("crimson_button", () -> new SimpleMultipartType<>(e -> new ButtonPart.CrimsonButtonPart()));
    public static final RegistryObject<MultipartType<ButtonPart.WarpedButtonPart>> WARPED_BUTTON_PART = MULTIPART_TYPES.register("warped_button", () -> new SimpleMultipartType<>(e -> new ButtonPart.WarpedButtonPart()));

    private static final RegistryObject<PartConverter> TORCH_CONVERTER = PART_CONVERTERS.register("torch", () ->
            new Converter<>(TorchPart::new, TorchPart::new, Items.TORCH, Blocks.TORCH, Blocks.WALL_TORCH)
    );
    private static final RegistryObject<PartConverter> SOUL_TORCH_CONVERTER = PART_CONVERTERS.register("soul_torch", () ->
            new Converter<>(SoulTorchPart::new, SoulTorchPart::new, Items.SOUL_TORCH, Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH)
    );
    private static final RegistryObject<PartConverter> REDSTONE_TORCH_CONVERTER = PART_CONVERTERS.register("redstone_torch", () ->
            new Converter<>(RedstoneTorchPart::new, RedstoneTorchPart::new, Items.REDSTONE_TORCH, Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH)
    );
    private static final RegistryObject<PartConverter> LEVER_CONVERTER = PART_CONVERTERS.register("lever", () ->
            new Converter<>(LeverPart::new, LeverPart::new, Items.LEVER, Blocks.LEVER)
    );

    private static final RegistryObject<PartConverter> STONE_BUTTON_CONVERTER = PART_CONVERTERS.register("stone_button", () ->
            new Converter<>(ButtonPart.StoneButtonPart::new, ButtonPart.StoneButtonPart::new, Items.STONE_BUTTON, Blocks.STONE_BUTTON)
    );
    private static final RegistryObject<PartConverter> POLISHED_BLACKSTONE_BUTTON_CONVERTER = PART_CONVERTERS.register("polished_blackstone_button", () ->
            new Converter<>(ButtonPart.PolishedBlackstoneButtonPart::new, ButtonPart.PolishedBlackstoneButtonPart::new, Items.POLISHED_BLACKSTONE_BUTTON, Blocks.POLISHED_BLACKSTONE_BUTTON)
    );

    private static final RegistryObject<PartConverter> OAK_BUTTON_CONVERTER = PART_CONVERTERS.register("oak_button", () ->
            new Converter<>(ButtonPart.OakButtonPart::new, ButtonPart.OakButtonPart::new, Items.OAK_BUTTON, Blocks.OAK_BUTTON)
    );
    private static final RegistryObject<PartConverter> SPRUCE_BUTTON_CONVERTER = PART_CONVERTERS.register("spruce_button", () ->
            new Converter<>(ButtonPart.SpruceButtonPart::new, ButtonPart.SpruceButtonPart::new, Items.SPRUCE_BUTTON, Blocks.SPRUCE_BUTTON)
    );
    private static final RegistryObject<PartConverter> BIRCH_BUTTON_CONVERTER = PART_CONVERTERS.register("birch_button", () ->
            new Converter<>(ButtonPart.BirchButtonPart::new, ButtonPart.BirchButtonPart::new, Items.BIRCH_BUTTON, Blocks.BIRCH_BUTTON)
    );
    private static final RegistryObject<PartConverter> JUNGLE_BUTTON_CONVERTER = PART_CONVERTERS.register("jungle_button", () ->
            new Converter<>(ButtonPart.JungleButtonPart::new, ButtonPart.JungleButtonPart::new, Items.JUNGLE_BUTTON, Blocks.JUNGLE_BUTTON)
    );
    private static final RegistryObject<PartConverter> ACACIA_BUTTON_CONVERTER = PART_CONVERTERS.register("acacia_button", () ->
            new Converter<>(ButtonPart.AcaciaButtonPart::new, ButtonPart.AcaciaButtonPart::new, Items.ACACIA_BUTTON, Blocks.ACACIA_BUTTON)
    );
    private static final RegistryObject<PartConverter> DARK_OAK_BUTTON_CONVERTER = PART_CONVERTERS.register("dark_oak_button", () ->
            new Converter<>(ButtonPart.DarkOakButtonPart::new, ButtonPart.DarkOakButtonPart::new, Items.DARK_OAK_BUTTON, Blocks.DARK_OAK_BUTTON)
    );
    private static final RegistryObject<PartConverter> CRIMSON_BUTTON_CONVERTER = PART_CONVERTERS.register("crimson_button", () ->
            new Converter<>(ButtonPart.CrimsonButtonPart::new, ButtonPart.CrimsonButtonPart::new, Items.CRIMSON_BUTTON, Blocks.CRIMSON_BUTTON)
    );
    private static final RegistryObject<PartConverter> WARPED_BUTTON_CONVERTER = PART_CONVERTERS.register("warped_button", () ->
            new Converter<>(ButtonPart.WarpedButtonPart::new, ButtonPart.WarpedButtonPart::new, Items.WARPED_BUTTON, Blocks.WARPED_BUTTON)
    );

    public static void init(IEventBus modBus) {
        MULTIPART_TYPES.register(modBus);
        PART_CONVERTERS.register(modBus);
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
