package codechicken.multipart.wrapped;

import codechicken.multipart.api.PartConverter;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.block.BlockMultipart;
import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.init.CBMultipartModContent;
import codechicken.multipart.util.MultipartPlaceContext;
import codechicken.multipart.wrapped.level.WrapperBlockProvider;
import codechicken.multipart.wrapped.level.WrapperLevelFactory;
import net.covers1624.quack.collection.FastStream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by covers1624 on 6/4/25.
 */
public class WrapperPartConverter extends PartConverter {

    @Override
    public ConversionResult<Collection<MultiPart>> convert(LevelAccessor world, BlockPos pos, BlockState state) {
        if (state.is(CBMultipartModContent.ALLOW_MULTIPART_WRAPPING_TAG)) {
            // TODO convert the tile.
            return ConversionResult.success(List.of(new WrapperMultiPart(state)));
        }
        return emptyResultList();
    }

    @Override
    public ConversionResult<MultiPart> convert(MultipartPlaceContext context) {
        if (!(context.getItemInHand().getItem() instanceof BlockItem blockItem)) return emptyResult();

        if (!blockItem.getBlock().builtInRegistryHolder().is(CBMultipartModContent.ALLOW_MULTIPART_WRAPPING_TAG)) return emptyResult();

        Map<BlockPos, BlockState> simulation = new HashMap<>();
        TileMultipart tile = BlockMultipart.getTile(context.getLevel(), context.getClickedPos());
        if (tile != null) {
            BlockState existing = FastStream.of(tile.getPartList())
                    .filter(e -> e instanceof WrapperMultiPart)
                    .map(e -> ((WrapperMultiPart) e).state())
                    .filter(e -> e.getBlock().equals(blockItem.getBlock()))
                    .onlyOrDefault();
            if (existing != null) {
                simulation.put(tile.getBlockPos(), existing);
            }
        }

        Level simLevel = WrapperLevelFactory.makeLevel(context.getLevel(), new WrapperBlockProvider() {
            @Override
            public BlockState getState(Level wrapped, BlockPos pos) {
                BlockState state = simulation.get(pos);
                if (state == null) {
                    state = wrapped.getBlockState(pos);
                }
                return state;
            }

            @Override
            public boolean setState(Level wrapped, BlockPos pos, BlockState state, int flags, int recursionLeft) {
                simulation.put(pos.immutable(), state);
                return false;
            }
        });

        BlockPlaceContext simContext = new BlockPlaceContext(simLevel, context.getPlayer(), context.getHand(), context.getItemInHand(), context.getHitResult()) {
            @Override
            public BlockPos getClickedPos() {
                return context.getClickedPos();
            }
        };

        // TODO there are a few fundamental problems with the conversion api here.
        //      - We should pass through the tile (and if it was converted) to this function.
        //        - This can be used to grab existing wrapper parts, or to prevent placement if it would convert under some situations, etc.
        //      - We need a way for the converter to declare that the part should replace another on placement.
        //        - Wrapped candles require this, as their on-placement, upgrades the existing block to the variant with more candles.
        //      - WrapperMultiPart currently requires an active world to perform occlusion checks, so this currently just crashes when doing anything.
        //        - We need a way to assign the world to parts before they are added to the tile, currently they only grab their world from the tile.
        //      - PartConverters probably shouldn't be registered to a game registry, its a bit overkill. They can just be a List or a Map.
        //        - We also need them to be sorted, so mods can override the handling of the wrapper part converter, incase they want special handling
        //          for their blocks.
        BlockState state = blockItem.getBlock().getStateForPlacement(simContext);
        if (state != null) {
            return new ConversionResult<>(new WrapperMultiPart(state), true);
        }

        return emptyResult();
    }
}
