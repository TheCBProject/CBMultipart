package codechicken.multipart.api.part;

import codechicken.lib.capability.CapabilityCache;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.api.MultipartType;
import codechicken.multipart.api.PartConverter;
import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.network.MultiPartSPH;
import codechicken.multipart.util.PartRayTraceResult;
import codechicken.multipart.util.TickScheduler;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a Multipart.
 * <p>
 * All Multipart implementations are expected to extend from {@link BaseMultipart}.
 * <p>
 * Created by covers1624 on 6/6/22.
 */
public interface MultiPart {

    /**
     * Get the underlying {@link TileMultipart} this part is inside.
     * <p>
     * The implementation of this function is INTERNAL, it is expected that
     * all Multiparts extend from {@link BaseMultipart}.
     *
     * @return The {@link TileMultipart}.
     */
    @Contract (pure = true)
    TileMultipart tile();

    /**
     * @return If this part has an attached {@link TileMultipart}.
     */
    boolean hasTile();

    /**
     * Get the {@link Level} the container {@link TileMultipart} is inside.
     *
     * @return The {@link Level}.
     */
    @Contract (pure = true)
    default Level level() {
        assert tile().getLevel() != null;
        return tile().getLevel();
    }

    /**
     * @return If this part has a {@link Level} attached.
     */
    default boolean hasLevel() {
        return tile().hasLevel();
    }

    /**
     * The {@link BlockPos} of the container {@link TileMultipart}.
     *
     * @return The {@link BlockPos}.
     */
    @Contract (pure = true)
    default BlockPos pos() {
        return tile().getBlockPos();
    }

    /**
     * Get the global {@link CapabilityCache} for this block.
     *
     * @return The {@link CapabilityCache}.
     */
    @Contract (pure = true)
    default CapabilityCache capCache() {
        return tile().getCapCache();
    }

    /**
     * Returns the {@link MultipartType} registry entry for this part.
     *
     * @return The {@link MultipartType}.
     * @see MultipartType
     */
    @Contract (pure = true)
    MultipartType<?> getType();

    /**
     * Write all the data required to describe a client version of this part to the packet.
     * <p>
     * Called server-side, when a client loads a part for the first time.
     *
     * @param packet The packet to write to.
     */
    default void writeDesc(MCDataOutput packet) { }

    /**
     * Fill out this part with the description information contained in {@code packet}.
     * <p>
     * Companion method to {@link #writeDesc(MCDataOutput)}.
     * <p>
     * Called client-side when the client loads this part for the first time.
     *
     * @param packet The packet to read from.
     */
    default void readDesc(MCDataInput packet) { }

    /**
     * Save this part to a {@link CompoundTag}.
     * <p>
     * Only called server-side.
     *
     * @param tag The tag to write to.
     */
    default void save(CompoundTag tag) { }

    /**
     * Load this part from a {@link CompoundTag}.
     * <p>
     * Only called server-side.
     *
     * @param tag The tag to read from.
     */
    default void load(CompoundTag tag) { }

    /**
     * Send a packet to this part's client-side counterpart.
     *
     * @param func The callback to write the packet data.
     */
    default void sendUpdate(Consumer<MCDataOutput> func) {
        MultiPartSPH.dispatchPartUpdate(this, func);
    }

    /**
     * Read a packet sent via {@link #sendUpdate}.
     *
     * @param packet THe packet to read.
     */
    default void readUpdate(MCDataInput packet) {
        readDesc(packet);
        tile().markRender();
    }

    /**
     * Perform an occlusion test to determine weather this part and {@code npart} can 'fit' into this block space.
     *
     * @param npart The part to run the test against.
     * @return {@code true} if both this part and {@code npart} are able to share this block space.
     */
    default boolean occlusionTest(MultiPart npart) {
        return true;
    }

    default VoxelShape getShape(CollisionContext context) {
        return Shapes.empty();
    }

    default VoxelShape getCollisionShape(CollisionContext context) {
        return getShape(context);
    }

    default VoxelShape getRenderOcclusionShape() {
        return getShape(CollisionContext.empty());
    }

    default VoxelShape getInteractionShape() {
        return getShape(CollisionContext.empty());
    }

    default VoxelShape getBlockSupportShape() {
        return getShape(CollisionContext.empty());
    }

    default VoxelShape getVisualShape(CollisionContext context) {
        return getShape(context);
    }

    /**
     * Harvest this part, removing it from the container {@link TileMultipart}
     * and dropping any items if necessary.
     *
     * @param player The {@link Player} harvesting the part.
     * @param hit    The {@link PartRayTraceResult} hit result.
     */
    default void harvest(Player player, PartRayTraceResult hit) {
        if (!player.getAbilities().instabuild) {
            tile().dropItems(getDrops());
        }
        tile().remPart(this);
    }

    /**
     * Return a list of {@link ItemStack}s that should be dropped when this part is destroyed.
     *
     * @return The {@link ItemStack}s.
     */
    default Iterable<ItemStack> getDrops() {
        return List.of();
    }

    /**
     * Return the {@link ItemStack} for pick-block(usually middle click) function.
     *
     * @param hit The {@link PartRayTraceResult} hit result.
     * @return The {@link ItemStack} pick result.
     */
    default ItemStack getCloneStack(PartRayTraceResult hit) {
        return ItemStack.EMPTY;
    }

    /**
     * Get the explosion resistance for this part.
     * <p>
     * The explosion resistance for the host {@link TileMultipart} is the
     * maximum explosion resistance for all contained parts.
     *
     * @param explosion The {@link Explosion}.
     * @return The resistance.
     */
    default float getExplosionResistance(Explosion explosion) {
        return 0F;
    }

    /**
     * The light level emitted by this part.
     *
     * @return The light level.
     */
    default int getLightEmission() {
        return 0;
    }

    /**
     * Return a value indicating how hard this part is to break.
     * <p>
     * By default, MC calculates this as (sudo code):
     * {@code player.digSpeedZeroToOne / block.hardness / canHarvest ? 30 : 100}
     *
     * @param player The player breaking the block.
     * @param hit    The {@link PartRayTraceResult} hit result.
     * @return The block strength.
     */
    default float getStrength(Player player, PartRayTraceResult hit) {
        return 1 / 30F;
    }

    /**
     * Used to get the sound for this part when placed.
     *
     * @param context The placement context.
     * @return The sound, or {@code null} for no sound.
     */
    @Nullable
    default SoundType getPlacementSound(UseOnContext context) {
        return null;
    }

    /**
     * Called when this part is added to the block space.
     */
    default void onAdded() {
        onWorldJoin();
    }

    /**
     * Called just before this part is actually removed from the container tile.
     */
    default void preRemove() { }

    /**
     * Called when this part is removed from the block space.
     */
    default void onRemoved() {
        onWorldSeparate();
    }

    /**
     * Called when the containing chunk is loaded on the server.
     *
     * @param chunk The chunk.
     */
    default void onChunkLoad(LevelChunk chunk) {
        onWorldJoin();
    }

    /**
     * Called when the containing chunk is unloaded on the server.
     */
    default void onChunkUnload() {
    }

    /**
     * Called when this part separates from the world.
     * <p>
     * This may be due to removal, chunk unload, etc.
     * <p>
     * Use this to sync with external data structures.
     * <p>
     * Called client and server side.
     */
    default void onWorldSeparate() { }

    /**
     * Called when this part joins the world.
     * <p>
     * This may be due to placement, chunk load, frames, etc.
     * <p>
     * Use this to sync with external data structures.
     * <p>
     * Called client and server side.
     */
    default void onWorldJoin() { }

    /**
     * Called when this part is converted from a normal block or tile.
     * <p>
     * Only applicable if a converter has been registered.
     *
     * @see PartConverter
     */
    default void onConverted() {
        onAdded();
    }

    /**
     * Called when this part is converted from a normal block or tile,
     * before the original tile has been replaced.
     * <p>
     * Use this to clear inventories, etc, from the old tile.
     * <p>
     * Only applicable if a converter has been registered.
     */
    default void invalidateConvertedTile() { }

    /**
     * Called when this part has been moved without a save/load.
     */
    default void onMoved() {
        onWorldJoin();
    }

    /**
     * Called on block right-click.
     * <p>
     * This should not modify the part client-side.
     *
     * @param player The player that right-clicked the part.
     * @param hit    The {@link PartRayTraceResult} hit result.
     * @param stack  The {@link ItemStack} held by the player.
     * @param hand   The {@link InteractionHand} the player is using.
     * @return The {@link InteractionResult}.
     */
    default InteractionResult activate(Player player, PartRayTraceResult hit, ItemStack stack, InteractionHand hand) {
        return InteractionResult.FAIL;
    }

    /**
     * Called on block left click.
     *
     * @param player The player who clicked on this part.
     * @param hit    The {@link PartRayTraceResult} hit result.
     * @param stack  The {@link ItemStack} held by the player.
     */
    default void click(Player player, PartRayTraceResult hit, ItemStack stack) {
    }

    /**
     * Called when an entity is within this block space.
     * <p>
     * The entity may not actually be colliding with this part.
     *
     * @param entity The {@link Entity}.
     */
    default void onEntityCollision(Entity entity) { }

    /**
     * Called when an entity is standing on this block space.
     * <p>
     * The entity may not actually be standing on this part.
     *
     * @param entity The {@link Entity}.
     */
    default void onEntityStanding(Entity entity) { }

    /**
     * Called when a neighbor block changes.
     *
     * @param neighbor The neighbor block.
     */
    default void onNeighborBlockChanged(BlockPos neighbor) { }

    /**
     * Called when a part is added or removed from this block space.
     *
     * @param part The part which changed. May be {@code null} if multiple parts are changed.
     */
    default void onPartChanged(@Nullable MultiPart part) { }

    /**
     * Called when a scheduled tick is executed.
     *
     * @see #scheduleTick(int)
     */
    default void scheduledTick() { }

    /**
     * Set a {@link #scheduledTick()} callback for this part {@code ticks} in the future.
     * <p>
     * {@code ticks} is a world time offset. If the chunk containing this
     * part is not loaded at the time of this scheduled tick, it may fire immediately on load.
     *
     * @param ticks The number of ticks in the future to fire the callback.
     */
    default void scheduleTick(int ticks) {
        TickScheduler.scheduleTick(this, ticks);
    }

    /**
     * Add particles and other effects when a player is mining this part.
     *
     * @param hit    The {@link PartRayTraceResult} hit result.
     * @param engine The {@link ParticleEngine} to spawn particles.
     */
    @OnlyIn (Dist.CLIENT)
    default void addHitEffects(PartRayTraceResult hit, ParticleEngine engine) { }

    /**
     * Add particles and other effects when a player finishes breaking this part.
     *
     * @param hit    The {@link PartRayTraceResult} hit result.
     * @param engine The {@link ParticleEngine} to spawn particles.
     */
    @OnlyIn (Dist.CLIENT)
    default void addDestroyEffects(PartRayTraceResult hit, ParticleEngine engine) { }

    /**
     * Add particles and other effects when a player lands on this part.
     *
     * @param hit               The hit directly bellow the entities feet.
     * @param entity            The position of the entity.
     * @param numberOfParticles The number of particles to spawn.
     */
    @OnlyIn (Dist.CLIENT)
    default void addLandingEffects(PartRayTraceResult hit, Vector3 entity, int numberOfParticles) { }

    /**
     * Add particles and other effects when a player runs over this part.
     * <p>
     * This is called on both the client and the server.
     *
     * @param hit    The hit directly bellow the players feet.
     * @param entity The entity running.
     */
    @OnlyIn (Dist.CLIENT)
    default void addRunningEffects(PartRayTraceResult hit, Entity entity) { }

    /**
     * Gets the bounds of this part for Frustum culling.
     * Bounds are relative to the current part's coordinates.
     *
     * @return The bounds.
     */
    default Cuboid6 getRenderBounds() {
        return Cuboid6.full;
    }
}
