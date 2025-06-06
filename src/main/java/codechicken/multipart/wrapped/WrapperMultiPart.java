package codechicken.multipart.wrapped;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.multipart.api.MultipartType;
import codechicken.multipart.api.part.BaseMultipart;
import codechicken.multipart.api.part.ModelRenderPart;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.api.part.NormalOcclusionPart;
import codechicken.multipart.init.CBMultipartModContent;
import codechicken.multipart.util.PartRayTraceResult;
import codechicken.multipart.wrapped.level.WrapperBlockProvider;
import codechicken.multipart.wrapped.level.WrapperLevelFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Created by covers1624 on 6/1/25.
 */
public class WrapperMultiPart extends BaseMultipart implements NormalOcclusionPart, ModelRenderPart {

    private @Nullable BlockState state;

    private @Nullable Level wrapperLevel;

    public WrapperMultiPart() {
    }

    public WrapperMultiPart(BlockState state) {
        this.state = state;
    }

    private Level wrapperLevel() {
        if (wrapperLevel == null) {
            wrapperLevel = WrapperLevelFactory.makeLevel(level(), new WrapperBlockProvider() {
                @Override
                public BlockState getState(Level wrapped, BlockPos pos) {
                    if (pos().equals(pos)) {
                        return state();
                    }
                    return wrapped.getBlockState(pos);
                }

                @Override
                public boolean setState(Level wrapped, BlockPos pos, BlockState state, int flags, int recursionLeft) {
                    if (pos().equals(pos)) {
                        return WrapperMultiPart.this.setState(state);
                    }
                    return wrapped.setBlock(pos, state, flags, recursionLeft);
                }
            });
        }
        return wrapperLevel;
    }

    public BlockState state() {
        return requireNonNull(state, "State not yet set.");
    }

    public boolean setState(BlockState state) {
        // TODO drop previous state as item.
        if (!state.is(CBMultipartModContent.ALLOW_MULTIPART_WRAPPING_TAG)) {
            if (!level().isClientSide) {
                tile().remPart(this);
            }
            return false;
        }

        this.state = state;
        if (state.isAir()) {
            if (!level().isClientSide) {
                tile().remPart(this);
            }
        } else {
            tile().notifyPartChange(this);
            tile().notifyShapeChange();
            tile().setChanged();
            tile().markRender();
        }
        return true;
    }

    @Override
    public MultipartType<?> getType() {
        return CBMultipartModContent.WRAPPED_PART.get();
    }

    @Override
    public void writeDesc(MCDataOutput packet) {
        packet.writeCompoundNBT(NbtUtils.writeBlockState(state()));
    }

    @Override
    public void readDesc(MCDataInput packet) {
        state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), packet.readCompoundNBT());
    }

    @Override
    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("state", NbtUtils.writeBlockState(state()));
    }

    @Override
    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("state"));
    }

    @Override
    public VoxelShape getShape(CollisionContext context) {
        return state().getShape(wrapperLevel(), pos(), context);
    }

    @Override
    public VoxelShape getCollisionShape(CollisionContext context) {
        return state().getCollisionShape(wrapperLevel(), pos(), context);
    }

    @Override
    public VoxelShape getRenderOcclusionShape() {
        return state().getOcclusionShape(wrapperLevel(), pos());
    }

    @Override
    public VoxelShape getInteractionShape() {
        return state().getInteractionShape(wrapperLevel(), pos());
    }

    @Override
    public VoxelShape getBlockSupportShape() {
        return state().getBlockSupportShape(wrapperLevel(), pos());
    }

    @Override
    public VoxelShape getVisualShape(CollisionContext context) {
        return state().getVisualShape(wrapperLevel(), pos(), context);
    }

    @Override
    public VoxelShape getOcclusionShape() {
        // Use the collision shape for occlusion, fallback to the regular shape if it's not available.
        VoxelShape cShape = state().getCollisionShape(wrapperLevel(), pos());
        return cShape.isEmpty() ? state().getShape(wrapperLevel(), pos()) : cShape;
    }

    @Override
    public ItemStack getCloneStack(PartRayTraceResult hit, Player player) {
        return state().getCloneItemStack(hit.hit, wrapperLevel(), pos(), player);
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, Player player, PartRayTraceResult hit, InteractionHand hand) {
        return state().useItemOn(stack, wrapperLevel(), player, hand, hit.hit);
    }

    @Override
    public InteractionResult useWithoutItem(Player player, PartRayTraceResult hit) {
        return state().useWithoutItem(wrapperLevel(), player, hit.hit);
    }

    @Override
    public Iterable<ItemStack> getDrops(LootParams.Builder builder) {
        return state().getDrops(builder);
    }

    @Override
    public boolean occlusionTest(MultiPart nPart) {
        if (!NormalOcclusionPart.super.occlusionTest(nPart)) return false;
        if (nPart instanceof WrapperMultiPart wrapperPart) {
            return state().getBlock() != wrapperPart.state().getBlock();
        }
        return true;

    }

    @Override
    public BlockState getCurrentState() {
        return state();
    }
}
