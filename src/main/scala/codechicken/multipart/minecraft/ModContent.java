package codechicken.multipart.minecraft;

import codechicken.lib.util.ArrayUtils;
import codechicken.multipart.api.MultiPartType;
import codechicken.multipart.api.PartConverter;
import codechicken.multipart.api.SimpleMultiPartType;
import codechicken.multipart.api.part.TMultiPart;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
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

    @SubscribeEvent
    public static void onRegisterMultiParts(RegistryEvent.Register<MultiPartType<?>> event) {
        IForgeRegistry<MultiPartType<?>> r = event.getRegistry();
        ModContainer container = ModLoadingContext.get().getActiveContainer();
        Object ext = ModLoadingContext.get().extension();
        ModLoadingContext.get().setActiveContainer(null, null);

        r.register(new SimpleMultiPartType<>(e -> new TorchPart()).setRegistryName("torch"));

        ModLoadingContext.get().setActiveContainer(container, ext);
    }

    @SubscribeEvent
    public static void onRegisterMultiPartConverters(RegistryEvent.Register<PartConverter> event) {
        IForgeRegistry<PartConverter> r = event.getRegistry();

        ModContainer container = ModLoadingContext.get().getActiveContainer();
        Object ext = ModLoadingContext.get().extension();
        ModLoadingContext.get().setActiveContainer(null, null);

        r.register(new Converter<>(TorchPart::new, TorchPart::new, Items.TORCH, Blocks.TORCH, Blocks.WALL_TORCH).setRegistryName("torch"));

        ModLoadingContext.get().setActiveContainer(container, ext);
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
        public ActionResult<Collection<TMultiPart>> convert(IWorld world, BlockPos pos, BlockState state) {
            if (blocks.length == 0) {
                return PartConverter.emptyResultList();
            }
            if (ArrayUtils.contains(blocks, state.getBlock())) {
                return ActionResult.resultSuccess(Collections.singleton(blockFactory.apply(state)));
            }
            return super.convert(world, pos, state);
        }

        @Override
        public ActionResult<TMultiPart> convert(BlockItemUseContext context) {
            if (context.getItem().getItem() != item) {
                return emptyResult();
            }
            TMultiPart result = factory.get().setStateOnPlacement(context);
            if (result != null) {
                return ActionResult.resultSuccess(result);
            }
            return super.convert(context);
        }
    }
}
