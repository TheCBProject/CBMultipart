package codechicken.multipart.api;

import codechicken.multipart.api.part.TMultiPart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by covers1624 on 4/17/20.
 */
// TODO, don't use InteractionResultHolder and InteractionResult anymore.
public abstract class PartConverter extends ForgeRegistryEntry<PartConverter> {

    private static final InteractionResultHolder<Collection<TMultiPart>> EMPTY_LIST = new InteractionResultHolder<>(InteractionResult.PASS, Collections.emptyList());
    private static final InteractionResultHolder<TMultiPart> EMPTY = new InteractionResultHolder<>(InteractionResult.PASS, null);

    /**
     * Convert the block / tile at the given position in world, to a {@link Collection}
     * of {@link TMultiPart} instances.
     * It should be noted, that a mod can choose to delete the block at the position by
     * returning an {@link InteractionResultHolder} with type {@link InteractionResult#SUCCESS}, but
     * specifying an empty {@link Collection}. The conversion system, does not treat the
     * result of the conversion any differently if its empty. Conversion only checks
     * the {@link InteractionResult} provided.
     * <p>
     * Use of {@link InteractionResult#FAIL} will be treated as {@link InteractionResult#PASS}.
     *
     * @param world The world.
     * @param pos   The pos.
     * @param state The state at pos in world.
     * @return An {@link InteractionResultHolder} specifying {@link InteractionResult#SUCCESS} if the block / tile
     * was converted, and {@link InteractionResult#PASS} if no conversion was performed.
     */
    public InteractionResultHolder<Collection<TMultiPart>> convert(LevelAccessor world, BlockPos pos, BlockState state) {
        return emptyResultList();
    }

    /**
     * Convert an {@link ItemStack} about to be placed into a MultiPart, into a {@link Collection}
     * of {@link TMultiPart} instances.
     * Same rules as {@link #convert(LevelAccessor, BlockPos, BlockState)} apply here.
     *
     * @param context The {@link UseOnContext} for the placement.
     * @return An {@link InteractionResultHolder} specifying {@link InteractionResult#SUCCESS} if the block / tile
     * was converted, and {@link InteractionResult#PASS} if no conversion was performed.
     */
    public InteractionResultHolder<TMultiPart> convert(UseOnContext context) {
        return emptyResult();
    }

    public static InteractionResultHolder<Collection<TMultiPart>> emptyResultList() {
        return EMPTY_LIST;
    }

    public static InteractionResultHolder<TMultiPart> emptyResult() {
        return EMPTY;
    }
}
