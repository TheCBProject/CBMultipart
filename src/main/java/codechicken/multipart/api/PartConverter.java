package codechicken.multipart.api;

import codechicken.multipart.api.part.MultiPart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by covers1624 on 4/17/20.
 */
public abstract class PartConverter extends ForgeRegistryEntry<PartConverter> {

    private static final ConversionResult<Collection<MultiPart>> EMPTY_LIST = new ConversionResult<>(Collections.emptyList(), false);
    private static final ConversionResult<MultiPart> EMPTY = new ConversionResult<>(null, false);

    /**
     * Convert the block / tile at the given position in world, to a {@link Collection}
     * of {@link MultiPart} instances.
     * It should be noted, that a mod can choose to delete the block at the position by
     * returning a failed {@link ConversionResult}.
     *
     * @param world The world.
     * @param pos   The pos.
     * @param state The state at pos in world.
     * @return A {@link ConversionResult}, providing a {@link Collection} of {@link MultiPart} instances
     * if conversion was successful.
     */
    public ConversionResult<Collection<MultiPart>> convert(LevelAccessor world, BlockPos pos, BlockState state) {
        return emptyResultList();
    }

    /**
     * Convert an {@link ItemStack} about to be placed into a {@link MultiPart} instance.
     *
     * @param context The {@link UseOnContext} for the placement.
     * @return A {@link ConversionResult}, providing the {@link MultiPart} instance if conversion
     * was successful.
     */
    public ConversionResult<MultiPart> convert(UseOnContext context) {
        return emptyResult();
    }

    public static ConversionResult<Collection<MultiPart>> emptyResultList() {
        return EMPTY_LIST;
    }

    public static ConversionResult<MultiPart> emptyResult() {
        return EMPTY;
    }

    public record ConversionResult<T>(@Nullable T result, boolean success) {

        public static <T> ConversionResult<T> success(T thing) {
            return new ConversionResult<>(thing, true);
        }
    }
}
