package codechicken.multipart.api.part

import java.lang.{Iterable => JIterable}
import java.util.Collections
import java.util.function.Consumer
import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.lib.render.CCRenderState
import codechicken.lib.vec.{Cuboid6, Vector3}
import codechicken.multipart.api.MultiPartType
import codechicken.multipart.block.TileMultiPart
import codechicken.multipart.network.MultiPartSPH
import codechicken.multipart.util.{PartRayTraceResult, TickScheduler}
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.block.SoundType
import net.minecraft.client.particle.ParticleManager
import net.minecraft.client.renderer.texture.{OverlayTexture, TextureAtlasSprite}
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.client.renderer.{ActiveRenderInfo, IRenderTypeBuffer, LightTexture, RenderType}
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.{ItemStack, ItemUseContext}
import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.shapes.{VoxelShape, VoxelShapes}
import net.minecraft.util.{ActionResultType, Hand}
import net.minecraft.world.Explosion
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

abstract class TMultiPart {
    /**
     * Reference to the container TileMultipart instance
     */
    var tile: TileMultiPart = _

    /**
     * Getter for tile.worldObj
     */
    def world = if (tile == null) null else tile.getLevel

    /**
     * Short getter for blockPos
     */
    def pos = tile.getBlockPos

    def capCache = tile.getCapCache

    /**
     * Called when the container tile instance is changed to update reference
     */
    def bind(t: TileMultiPart) {
        tile = t
    }

    /**
     * Returns the type [[MultiPartType]] for this part, Similar to how
     * [[TileEntityType]] works. see [[MultiPartType]]s documentation for
     * more information.
     */
    def getType: MultiPartType[_]

    /**
     * Write all the data required to describe a client version of this part to the packet.
     * Called serverside, when a client loads this part for the first time.
     */
    def writeDesc(packet: MCDataOutput) {}

    /**
     * Fill out this part with the description information contained in packet. Will be exactly as written from writeDesc.
     * Called clientside when a client loads this part for the first time.
     */
    def readDesc(packet: MCDataInput) {}

    /**
     * Save part to NBT (only called serverside)
     */
    def save(tag: CompoundNBT) {}

    /**
     * Load part from NBT (only called serverside)
     */
    def load(tag: CompoundNBT) {}

    /**
     * Allows this part to send a packet to its client-side counterpart.
     *
     * @param func The function to evaluate to write packet data.
     */
    def sendUpdate(func: Consumer[MCDataOutput]) {
        MultiPartSPH.dispatchPartUpdate(this, func)
    }

    /**
     * Reads a packet sent via [[sendUpdate]]
     *
     * @param packet The packet to read.
     */
    def readUpdate(packet: MCDataInput) = {
        readDesc(packet)
        tile.markRender()
    }

    /**
     * Perform an occlusion test to determine whether this and npart can 'fit' in this block space.
     *
     * @param npart The part to run the test against.
     * @return True if both this part and npart are able to share this block space
     */
    def occlusionTest(npart: TMultiPart): Boolean = true

    def getOutlineShape: VoxelShape = VoxelShapes.empty()

    //TODO pass through ISelectionContext
    def getCollisionShape: VoxelShape = getOutlineShape

    def getRenderOcclusionShape: VoxelShape = getOutlineShape

    def getInteractionShape: VoxelShape = getOutlineShape

    /**
     * Harvest this part, removing it from the container tile and dropping items if necessary.
     *
     * @param hit    An instance of ExtendedMOP from collisionRayTrace
     * @param player The player harvesting the part
     */
    def harvest(player: PlayerEntity, hit: PartRayTraceResult) {
        if (!player.abilities.instabuild) {
            tile.dropItems(getDrops)
        }
        tile.remPart(this)
    }

    /**
     * Return a list of items that should be dropped when this part is destroyed.
     */
    def getDrops: JIterable[ItemStack] = Collections.emptySet()

    /**
     * Return the itemstack for the middle click pick-block function.
     */
    def pickItem(hit: PartRayTraceResult): ItemStack = ItemStack.EMPTY

    /**
     * Explosion resistance of the host tile is the maximum explosion resistance of the contained parts
     *
     * @param explosion The explosion.
     * @return The resistance of this part the the explosion
     */
    def getExplosionResistance(explosion: Explosion) = 0F

    /**
     * The light level emitted by this part
     */
    def getLightValue = 0

    /**
     * Return a value indicating how hard this part is to break
     *
     * By default, MC calculates as:
     *
     * {Player dig speed 0-1} / {block hardness} / {if can harvest 30 else 100}
     *
     * @param hit An instance of ExtendedMOP from collisionRayTrace
     */
    def getStrength(player: PlayerEntity, hit: PartRayTraceResult): Float = 1 / 30f

    /**
     * Used to get the sound on placement.
     *
     * Weird place for this, but handles conversion placement
     * and direct placement.
     *
     * @param context The ItemUseContext used when placing the part.
     * @return The sound
     */
    def getPlacementSound(context: ItemUseContext): SoundType = null

    /**
     * Called when this part is added to the block space
     */
    def onAdded() = onWorldJoin()

    /**
     * Called just before this part is actually removed from the container tile
     */
    def preRemove() {}

    /**
     * Called when this part is removed from the block space
     */
    def onRemoved() = onWorldSeparate()

    /**
     * Called when the containing chunk is loaded on the server.
     */
    def onChunkLoad() = onWorldJoin()

    /**
     * Called when the containing chunk is unloaded on the server.
     */
    def onChunkUnload() = onWorldSeparate()

    /**
     * Called when this part separates from the world (due to removal, chunk unload or other). Use this to sync with external data structures. Called on both client and server
     */
    def onWorldSeparate() {}

    /**
     * Called when this part joins the world (due to placement, chunkload or frame move etc). Use this to sync with external data structures. Called on both client and server
     */
    def onWorldJoin() {}

    /**
     * Called when this part is converted from a normal block/tile (only applicable if a converter has been registered)
     */
    def onConverted() = onAdded()

    /**
     * Called when this part is converted from a normal block/tile (only applicable if a converter has been registered) before the original tile has been replaced
     * Use this to clear out things like inventory from the old tile.
     */
    def invalidateConvertedTile() {}

    /**
     * Called when this part has been moved without a save/load.
     */
    def onMoved() = onWorldJoin()

    /**
     * Called on block right click.
     * This should not modify the part client side. If the client call returns false, the server will not call this function.
     *
     * @param player The player that right-clicked on the part
     * @param hit    An instance of CuboidRayTraceResult from collisionRayTrace
     * @param item   The item held by the player
     * @param hand   The hand that the player is holding the item in
     */
    def activate(player: PlayerEntity, hit: PartRayTraceResult, item: ItemStack, hand: Hand) = ActionResultType.FAIL

    /**
     * Called on block left click.
     *
     * @param player The player who clicked on this part
     * @param hit    An instance of CuboidRayTraceResult from collisionRayTrace
     * @param item   The item held by the player
     */
    def click(player: PlayerEntity, hit: PartRayTraceResult, item: ItemStack) {}

    /**
     * Called when an entity is within this block space. May not actually collide with this part.
     */
    def onEntityCollision(entity: Entity) {}

    /**
     * Called when an entity is standing on this block space. May not actyally collide with this part.
     */
    def onEntityStanding(entity: Entity) {}

    /**
     * Called when a neighbor block changed
     */
    def onNeighborBlockChanged(from: BlockPos) {}

    /**
     * Called when a part is added or removed from this block space.
     * The part parameter may be null if several things have changed.
     */
    def onPartChanged(part: TMultiPart) {}

    /**
     * Called when a scheduled tick is executed.
     */
    def scheduledTick() {}

    /**
     * Sets a scheduledTick callback for this part ticks in the future. This is a world time value, so if the chunk is unloaded and reloaded some time later, the tick may fire immediately.
     */
    def scheduleTick(ticks: Int) = TickScheduler.scheduleTick(this, ticks)

    /**
     * Add particles and other effects when a player is mining this part
     *
     * @param hit An instance of ExtendedMOP from collisionRayTrace
     */
    @OnlyIn(Dist.CLIENT)
    def addHitEffects(hit: PartRayTraceResult, manager: ParticleManager) {}

    /**
     * Add particles and other effects when a player broke this part
     *
     * @param hit An instance of ExtendedMOP from collisionRayTrace
     */
    @OnlyIn(Dist.CLIENT)
    def addDestroyEffects(hit: PartRayTraceResult, manager: ParticleManager) {}

    /**
     * Render the static, unmoving faces of this part into the world renderer.
     * The given CCRenderState is set up as follows should you wish to use it:
     *  - CCRenderState.reset() has been called
     *  - The current buffer is bound
     *  - The light matrix is located
     *
     * Otherwise an instance of the VertexBuffer can be retrieved from
     * CCRenderState via CCRenderState.getBuffer()
     *
     * NOTE: The tessellator is already drawing. DO NOT make draw calls or
     * mess with the GL state
     *
     * This may be called on a ChunkBatching thread. Please make sure
     * Everything you do is Thread Safe.
     *
     * @param layer The render layer
     * @param ccrs  An instance of CCRenderState to use.
     * @return true if vertices were added to the buffer
     */
    @OnlyIn(Dist.CLIENT)
    def renderStatic(layer: RenderType, ccrs: CCRenderState) = false

    /**
     * Override how your part displays its breaking progress,
     * By default this method will delegate to renderStatic with a null RenderType.
     * You shouldn't need to override this in _most_ cases.
     *
     * CCRenderState is set up as follows should you wish to use it:
     *  - CCRenderState.reset() has been called
     *  - The current buffer is bound
     *  - LightMatrix has been located.
     *
     * Otherwise an instance of the [[com.mojang.blaze3d.vertex.IVertexBuilder]] and [[net.minecraft.client.renderer.vertex.VertexFormat]] can be retrieved from
     * CCRenderState via CCRenderState.getConsumer and CCRenderState.getVertexFormat
     *
     * NOTE: The tessellator is already drawing. DO NOT make draw calls or
     * mess with the GL state
     *
     * This may be called on a ChunkBatching thread. Please make sure
     * Everything you do is Thread Safe.
     *
     * @param ccrs An instance of CCRenderState to use.
     */
    @OnlyIn(Dist.CLIENT)
    def renderBreaking(ccrs: CCRenderState) {
        renderStatic(null, ccrs)
    }

    /**
     * The bounds of this part for Frustum culling.
     * Bound are relative to the current part's coordinates.
     *
     * @return The bounds.
     */
    def getRenderBounds = Cuboid6.full

    /**
     * Render the dynamic, changing faces of this part and other gfx.
     * This is equivalent to a [[TileEntityRenderer]].
     *
     * @param mStack        The MatrixStack to apply.
     * @param buffers       The Buffer storage.
     * @param packedLight   The Packed LightMap value to use, see [[LightTexture]]
     * @param packedOverlay The Packed Overlay value to use, see [[OverlayTexture]]
     * @param partialTicks  The partial ticks.
     */
    @OnlyIn(Dist.CLIENT)
    def renderDynamic(mStack: MatrixStack, buffers: IRenderTypeBuffer, packedLight: Int, packedOverlay: Int, partialTicks: Float) {}

    /**
     * Override the drawing of the selection box around this part.
     *
     * @param hit An instance of ExtendedMOP from collisionRayTrace
     * @return true if highlight rendering was overridden.
     */
    @OnlyIn(Dist.CLIENT)
    def drawHighlight(hit: PartRayTraceResult, info: ActiveRenderInfo, mStack: MatrixStack, getter: IRenderTypeBuffer, partialTicks: Float): Boolean = false
}
