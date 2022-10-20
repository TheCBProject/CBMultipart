package codechicken.multipart.block;

import codechicken.lib.capability.CapabilityCache;
import codechicken.lib.data.MCDataByteBuf;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.lib.world.IChunkLoadTile;
import codechicken.multipart.api.part.AbstractMultiPart;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.init.CBMultipartModContent;
import codechicken.multipart.init.MultiPartRegistries;
import codechicken.multipart.network.MultiPartSPH;
import codechicken.multipart.util.*;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * The host tile, capable of containing {@link TMultiPart} instances.
 */
public class TileMultiPart extends BlockEntity implements IChunkLoadTile {

    private List<TMultiPart> partList = new CopyOnWriteArrayList<>();
    private CapabilityCache capabilityCache = new CapabilityCache();

    private final MergedVoxelShapeHolder<TMultiPart> outlineShapeHolder = new MergedVoxelShapeHolder<TMultiPart>()
            .setPostProcessHook(e -> new MultipartVoxelShape(e, this));

    private final MergedVoxelShapeHolder<TMultiPart> collisionShapeHolder = new MergedVoxelShapeHolder<TMultiPart>()
            .setPostProcessHook(e -> new MultipartVoxelShape(e, this));

    private final MergedVoxelShapeHolder<TMultiPart> occlusionShapeHolder = new MergedVoxelShapeHolder<TMultiPart>()
            .setPostProcessHook(e -> new MultipartVoxelShape(e, this));

    private final MergedVoxelShapeHolder<TMultiPart> interactionShapeHolder = new MergedVoxelShapeHolder<TMultiPart>()
            .setPostProcessHook(e -> new MultipartVoxelShape(e, this));

    private final MergedVoxelShapeHolder<TMultiPart> supportShapeHolder = new MergedVoxelShapeHolder<TMultiPart>()
            .setPostProcessHook(e -> new MultipartVoxelShape(e, this));

    private final MergedVoxelShapeHolder<TMultiPart> visualShapeHolder = new MergedVoxelShapeHolder<TMultiPart>()
            .setPostProcessHook(e -> new MultipartVoxelShape(e, this));

    public TileMultiPart(BlockPos pos, BlockState state) {
        super(CBMultipartModContent.tileMultipartType, pos, state);
    }

    // TODO Mixin compiler needs to support ctors with arguments, provided they are identical to the base class ctor.
    protected TileMultiPart() {
        this(null, null);
        throw new UnsupportedOperationException("Exists for traits.");
    }

    public List<TMultiPart> getPartList() {
        return partList;
    }

    public void from(TileMultiPart that) {
        copyFrom(that);
        loadFrom(that);
        that.loadTo(this);
    }

    //region *** Trait Overrides ***

    /**
     * This method should be used for copying all the data from the fields in that container tile.
     * This method will be automatically generated on java tile traits with fields if it is not overridden.
     */
    public void copyFrom(TileMultiPart that) {
        partList = that.partList;
        markShapeChange();
    }

    /**
     * Used to load the newly accuired data from copyFrom.
     */
    public void loadFrom(TileMultiPart that) {
        partList.forEach(e -> ((AbstractMultiPart) e).bind(this));
    }

    /**
     * Called after a tile conversion on the old tile. At the time of this call, this tile is no longer valid.
     * This is called before receiveFrom is called on the new tile.
     * <p>
     * Provided for trait overrides, do not call externally.
     *
     * @param that The new tile
     */
    public void loadTo(TileMultiPart that) { }

    /**
     * Remove all parts from internal cache.
     * <p>
     * Provided for trait overrides, do not call externally.
     */
    public void clearParts() {
        partList = new CopyOnWriteArrayList<>();
        markShapeChange();
    }

    /**
     * Bind this part to an internal cache.
     * <p>
     * Provided for trait overrides, do not call externally.
     */
    public void bindPart(TMultiPart part) { }

    /**
     * Called when a part is added (placement).
     * <p>
     * Provided for trait overrides, do not call externally.
     */
    public void partAdded(TMultiPart part) { }

    /**
     * Remove this part from internal cache.
     * <p>
     * Provided for trait overrides, do not call externally.
     */
    public void partRemoved(TMultiPart part, int p) { }

    /**
     * Blank implementation
     * <p>
     * Overriden by {@link codechicken.multipart.trait.TTileChangeTile}
     */
    public boolean getWeakChanges() { return false; }

    /**
     * Blank implementation.
     * <p>
     * Overriden by {@link codechicken.multipart.trait.TTileChangeTile}
     */
    public void onNeighborTileChange(BlockPos neighborPos) { }

    /**
     * Blank implementation.
     * <p>
     * Overriden by {@link codechicken.multipart.trait.TSlottedTile}
     */
    public TMultiPart getSlottedPart(int slot) { return null; }

    /**
     * Called when the Tile is marked as removed via {@link #setRemoved()}.
     * <p>
     * Provided for trait overrides, do not call externally.
     */
    public void onRemoved() { }

    public void operate(Consumer<TMultiPart> cons) {
        for (TMultiPart part : partList) {
            if (part.tile() != null) {
                cons.accept(part);
            }
        }
    }
    //endregion

    //region *** Tile Save/Load ***

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag parts = new ListTag();
        for (TMultiPart part : partList) {
            parts.add(MultiPartRegistries.savePart(new CompoundTag(), part));
        }
        tag.put("parts", parts);
    }

    @Override
    public final CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        MCDataByteBuf desc = new MCDataByteBuf();
        writeDesc(desc);
        desc.writeToNBT(tag, "data");
        return tag;
    }
    //endregion

    //region *** Networking ***

    /**
     * Writes the description of this tile, and all parts composing it, to packet
     */
    public void writeDesc(MCDataOutput packet) {
        packet.writeByte(partList.size());
        for (TMultiPart part : partList) {
            MultiPartRegistries.writePart(packet, part);
        }
    }
    //endregion

    //region *** Adding/Removing parts ***

    public boolean canAddPart(TMultiPart part) {
        return !partList.contains(part) && occlusionTest(partList, part);
    }

    /**
     * Returns true if oPart can be replaced with nPart (note oPart and nPart may be the exact same object)
     * <p>
     * This function should be used for testing if a part can change it's shape (eg. rotation, expansion, cable connection)
     * For example, to test whether a cable part can connect to it's neighbor:
     * 1. Set the cable part's bounding boxes as if the connection is established
     * 2. Call canReplacePart(part, part)
     * 3. If canReplacePart succeeds, perform connection, else, revert bounding box
     */
    public boolean canReplacePart(TMultiPart oPart, TMultiPart nPart) {
        if (oPart != nPart && partList.contains(nPart)) {
            return false;
        }
        return occlusionTest(new FilteredCollectionView<>(partList, e -> e != oPart), nPart);
    }

    public boolean occlusionTest(Collection<TMultiPart> parts, TMultiPart nPart) {
        return parts.stream().allMatch(part -> part.occlusionTest(nPart) && nPart.occlusionTest(part));
    }

    public void addPart_impl(TMultiPart part) {
        if (!level.isClientSide) MultiPartSPH.sendAddPart(this, part);

        addPart_do(part);
        part.onAdded();
        partAdded(part);
        notifyPartChange(part);
        notifyTileChange();
        setChanged();
        markRender();
    }

    public void addPart_do(TMultiPart part) {

        partList.add(part);
        bindPart(part);
        markShapeChange();
        ((AbstractMultiPart) part).bind(this);
    }

    public TileMultiPart remPart(TMultiPart part) {
        Preconditions.checkArgument(!level.isClientSide, "Cannot remove MultiParts from a client tile.");
        return remPart_impl(part);
    }

    public TileMultiPart remPart_impl(TMultiPart part) {
        remPart_do(part, !level.isClientSide);

        if (!isRemoved()) {
            TileMultiPart tile = partRemoved(this);
            tile.notifyPartChange(part);
            tile.setChanged();
            tile.markRender();
            return tile;

        }
        return null;
    }

    private int remPart_do(TMultiPart part, boolean sendPacket) {
        int idx = partList.indexOf(part);
        if (idx < 0) {
            throw new IllegalArgumentException("Tried to remove a non-existent part");
        }

        part.preRemove();
        partList.removeIf(e -> e == part);

        if (sendPacket) MultiPartSPH.sendRemPart(this, idx);

        partRemoved(part, idx);
        part.onRemoved();
        ((AbstractMultiPart) part).bind(null);
        markShapeChange();
        recalcLight(false, true);

        if (partList.isEmpty()) level.removeBlock(worldPosition, false);
        return idx;
    }

    private void loadParts(Iterable<TMultiPart> parts) {
        clearParts();
        parts.forEach(this::addPart_do);
        if (level != null) {
            if (level.isClientSide) {
                operate(TMultiPart::onWorldJoin);
            }
            notifyPartChange(null);
        }
    }

    public final void setValid(boolean b) {
        if (b) {
            clearRemoved();
        } else {
            setRemoved();
        }
    }

    @Override
    public void setRemoved() {
        if (!isRemoved()) {
            super.setRemoved();
            onRemoved();
            if (level != null) {
                partList.forEach(TMultiPart::onWorldSeparate);
            }
        }
    }

    //Empty method here fixes trait inheritance issue.
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return super.getCapability(cap, side);
    }

    //endregion

    //region *** Internal callbacks ***
    public VoxelShape getShape(CollisionContext context) { return outlineShapeHolder.update(partList, part -> part.getShape(context)); }

    public VoxelShape getCollisionShape(CollisionContext context) { return collisionShapeHolder.update(partList, part -> part.getCollisionShape(context)); }

    public VoxelShape getRenderOcclusionShape() { return occlusionShapeHolder.update(partList, TMultiPart::getRenderOcclusionShape); }

    public VoxelShape getInteractionShape() { return interactionShapeHolder.update(partList, TMultiPart::getInteractionShape); }

    public VoxelShape getBlockSupportShape() { return supportShapeHolder.update(partList, TMultiPart::getBlockSupportShape); }

    public VoxelShape getVisualShape(CollisionContext context) { return visualShapeHolder.update(partList, part -> part.getVisualShape(context)); }

    public void harvestPart(PartRayTraceResult hit, Player player) {
        hit.part.harvest(player, hit);
    }

    public List<ItemStack> getDrops() {
        return partList.stream()
                .map(TMultiPart::getDrops)
                .flatMap(e -> StreamSupport.stream(e.spliterator(), false))
                .collect(Collectors.toList());
    }

    public ItemStack getPickBlock(PartRayTraceResult hit) {
        return hit.part.pickItem(hit);
    }

    public float getExplosionResistance(Explosion explosion) {
        return (float) partList.stream()
                .mapToDouble(e -> e.getExplosionResistance(explosion))
                .max()
                .orElse(0);
    }

    public int getLightValue() {
        return partList.stream()
                .mapToInt(TMultiPart::getLightValue)
                .max()
                .orElse(0);
    }

    public float getDestroyProgress(Player player, PartRayTraceResult hit) {
        return hit.part.getStrength(player, hit);
    }

    @Override
    public void onChunkUnloaded() {
        operate(TMultiPart::onChunkUnload);
    }

    @Override
    public void onChunkLoad(LevelChunk chunk) {
        operate(e -> e.onChunkLoad(chunk));
    }

    public void onMoved() {
        capabilityCache.setWorldPos(level, worldPosition);
        operate(TMultiPart::onMoved);
    }

    public InteractionResult use(Player player, PartRayTraceResult hit, InteractionHand hand) {
        return hit.part.activate(player, hit, player.getItemInHand(hand), hand);
    }

    public void attack(Player player, PartRayTraceResult hit) {
        hit.part.click(player, hit, player.getMainHandItem());
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        capabilityCache.setWorldPos(level, getBlockPos());
    }

    public void entityInside(Entity entity) {
        operate(e -> e.onEntityCollision(entity));
    }

    public void stepOn(Entity entity) {
        operate(e -> e.onEntityStanding(entity));
    }

    public void onNeighborBlockChanged(BlockPos pos) {
        capabilityCache.onNeighborChanged(pos);
        operate(e -> e.onNeighborBlockChanged(pos));
    }

    public boolean canConnectRedstone(int side) { return false; }

    public int getDirectSignal(int side) { return 0; }

    public int getSignal(int side) { return 0; }

    @Override
    public AABB getRenderBoundingBox() {
        Cuboid6 c = Cuboid6.full.copy();
        operate(e -> c.enclose(e.getRenderBounds()));
        return c.add(worldPosition).aabb();
    }

    public void animateTick(Random random) { }

    public boolean isClientTile() { return false; }

    //endregion

    //region *** Utility Functions ***

    /**
     * Notifies neighboring blocks that this tile has changed
     */
    public void notifyTileChange() {
        if (level != null) {
            level.updateNeighborsAt(worldPosition, CBMultipartModContent.blockMultipart);
        }
    }

    /**
     * Called by parts when they have changed in some form that affects the world.
     * Notifies neighbor blocks, the world and parts that share this host and recalculates lighting
     */
    public void notifyPartChange(TMultiPart part) {
        internalPartChange(part);

        BlockState state = CBMultipartModContent.blockMultipart.defaultBlockState();
        level.sendBlockUpdated(worldPosition, state, state, 3);
        level.updateNeighborsAt(worldPosition, CBMultipartModContent.blockMultipart);
        level.getChunkSource().getLightEngine().checkBlock(worldPosition);
    }

    /**
     * Notifies parts sharing this host of a change
     */
    public void internalPartChange(TMultiPart part) {
        operate(e -> {
            if (e != part) {
                e.onPartChanged(part);
            }
        });
    }

    /**
     * Notifies all parts not in the passed collection of a change from all the parts in the collection
     */
    public void multiPartChange(Collection<TMultiPart> parts) {
        operate(p -> {
            if (!parts.contains(p)) {
                parts.forEach(p::onPartChanged);
            }
        });
    }

    /**
     * Callback for parts to mark the chunk as needs saving
     */
    @Override
    public void setChanged() {
        super.setChanged();
    }

    /**
     * Mark this block space for a render update.
     */
    public void markRender() { }

    public void recalcLight(boolean sky, boolean block) {
        LevelLightEngine lm = level.getLightEngine();
        if (sky && lm.skyEngine != null) {
            lm.skyEngine.checkBlock(worldPosition);
        }
        if (block && lm.blockEngine != null) {
            lm.blockEngine.checkBlock(worldPosition);
        }
    }

    public void markShapeChange() {
        outlineShapeHolder.clear();
        collisionShapeHolder.clear();
        occlusionShapeHolder.clear();
        interactionShapeHolder.clear();
    }

    /**
     * Helper function for calling a second level notify on a side (eg indirect power from a lever)
     */
    public void notifyNeighborChange(Direction side) {
        level.updateNeighborsAt(worldPosition.relative(side), CBMultipartModContent.blockMultipart);
    }

    public void notifyNeighborChange(int side) {
        notifyNeighborChange(Direction.from3DDataValue(side));
    }

    /**
     * Utility function for dropping items around the center of this space
     */
    public void dropItems(Iterable<ItemStack> items) {
        Vector3 pos = Vector3.fromTileCenter(this);
        items.forEach(e -> dropItem(e, level, pos));
    }

    public CapabilityCache getCapCache() {
        return capabilityCache;
    }
    //endregion

    public static boolean canPlacePart(UseOnContext context, TMultiPart part) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (!isUnobstructed(level, pos, part)) {
            return false;
        }

        TileMultiPart tile = MultiPartHelper.getOrConvertTile(level, pos);
        if (tile != null) {
            return tile.canAddPart(part);
        }

        if (!replaceable(level, pos, context)) return false;

        return true;
    }

    public static boolean isUnobstructed(Level world, BlockPos pos, TMultiPart part) {
        return world.isUnobstructed(null, part.getCollisionShape(CollisionContext.empty()).move(pos.getX(), pos.getY(), pos.getZ()));
    }

    /**
     * Returns if the block at pos is replaceable (air, vines etc)
     */
    public static boolean replaceable(Level world, BlockPos pos, UseOnContext context) {
        BlockState state = world.getBlockState(pos);
        return state.isAir() || state.canBeReplaced(new BlockPlaceContext(context));
    }

    /**
     * Adds a part to a block space. canPlacePart should always be called first.
     * The addition of parts on the client is handled internally.
     */
    public static TileMultiPart addPart(Level world, BlockPos pos, TMultiPart part) {
        return MultiPartHelper.addPart(world, pos, part);
    }

    /**
     * Constructs this tile and its parts from a desc packet
     */
    public static void handleDescPacket(Level world, BlockPos pos, MCDataInput packet) {
        List<TMultiPart> parts = new ArrayList<>();
        int nParts = packet.readUByte();
        for (int i = 0; i < nParts; i++) {
            parts.add(MultiPartRegistries.readPart(packet));
        }
        if (parts.isEmpty()) return;

        BlockEntity t = world.getBlockEntity(pos);
        TileMultiPart tile = MultiPartGenerator.INSTANCE.generateCompositeTile(t, pos, parts, true);
        if (tile != t) {
            world.setBlockAndUpdate(pos, CBMultipartModContent.blockMultipart.defaultBlockState());
            MultiPartHelper.silentAddTile(world, pos, tile);
        }

        tile.loadParts(parts);
        tile.notifyTileChange();
        tile.markRender();
    }

    /**
     * Creates this tile from an NBT tag
     */
    public static TileMultiPart fromNBT(CompoundTag tag, BlockPos pos) {
        ListTag partList = tag.getList("parts", 10);
        List<TMultiPart> parts = new ArrayList<>();

        for (int i = 0; i < partList.size(); i++) {
            TMultiPart part = MultiPartRegistries.loadPart(partList.getCompound(i));
            if (part != null) {
                parts.add(part);
            }
        }
        if (parts.isEmpty()) return null;

        TileMultiPart tile = MultiPartGenerator.INSTANCE.generateCompositeTile(null, pos, parts, false);
        tile.load(tag);
        tile.loadParts(parts);
        return tile;
    }

    public static void dropItem(ItemStack stack, Level level, Vector3 pos) {
        ItemEntity item = new ItemEntity(level, pos.x, pos.y, pos.z, stack);
        item.setDeltaMovement(level.random.nextGaussian() * 0.05, level.random.nextGaussian() * 0.05 + 0.2, level.random.nextGaussian() * 0.05);
        item.setPickUpDelay(10);
        level.addFreshEntity(item);
    }

    private static TileMultiPart partRemoved(TileMultiPart tile) {
        TileMultiPart newTile = MultiPartGenerator.INSTANCE.generateCompositeTile(tile, tile.getBlockPos(), tile.getPartList(), tile.getLevel().isClientSide);
        if (tile != newTile) {
            tile.setValid(false);
            MultiPartHelper.silentAddTile(tile.getLevel(), tile.getBlockPos(), newTile);
            newTile.from(tile);
            newTile.notifyTileChange();
        }
        return newTile;
    }
}