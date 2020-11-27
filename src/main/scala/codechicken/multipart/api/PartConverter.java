package codechicken.multipart.api;

import codechicken.multipart.api.part.TMultiPart;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by covers1624 on 4/17/20.
 */
public abstract class PartConverter extends ForgeRegistryEntry<PartConverter> {

    private static final ActionResult<Collection<TMultiPart>> EMPTY_LIST = new ActionResult<>(ActionResultType.PASS, Collections.emptyList());
    private static final ActionResult<TMultiPart> EMPTY = new ActionResult<>(ActionResultType.PASS, null);

    /**
     * Convert the block / tile at the given position in world, to a {@link Collection}
     * of {@link TMultiPart} instances.
     * It should be noted, that a mod can choose to delete the block at the position by
     * returning an {@link ActionResult} with type {@link ActionResultType#SUCCESS}, but
     * specifying an empty {@link Collection}. The conversion system, does not treat the
     * result of the conversion any differently if its empty. Conversion only checks
     * the {@link ActionResultType} provided.
     *
     * Use of {@link ActionResultType#FAIL} will be treated as {@link ActionResultType#PASS}.
     *
     * @param world The world.
     * @param pos   The pos.
     * @param state The state at pos in world.
     * @return An {@link ActionResult} specifying {@link ActionResultType#SUCCESS} if the block / tile
     * was converted, and {@link ActionResultType#PASS} if no conversion was performed.
     */
    public ActionResult<Collection<TMultiPart>> convert(IWorld world, BlockPos pos, BlockState state) {
        return emptyResultList();
    }

    /**
     * Convert an {@link ItemStack} about to be placed into a MultiPart, into a {@link Collection}
     * of {@link TMultiPart} instances.
     * Same rules as {@link #convert(IWorld, BlockPos, BlockState)} apply here.
     *
     * @param context The {@link BlockItemUseContext} for the placement.
     * @return An {@link ActionResult} specifying {@link ActionResultType#SUCCESS} if the block / tile
     * was converted, and {@link ActionResultType#PASS} if no conversion was performed.
     */
    public ActionResult<TMultiPart> convert(BlockItemUseContext context) {
        return emptyResult();
    }

    public static ActionResult<Collection<TMultiPart>> emptyResultList() {
        return EMPTY_LIST;
    }

    public static ActionResult<TMultiPart> emptyResult() {
        return EMPTY;
    }
}
