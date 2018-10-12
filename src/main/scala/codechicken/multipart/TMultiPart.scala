package codechicken.multipart

import java.lang.{Iterable => JIterable}
import java.util.{EnumSet => JEnumSet}

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.lib.raytracer.{CuboidRayTraceResult, IndexedCuboid6, RayTracer}
import codechicken.lib.render.CCRenderState
import codechicken.lib.vec.{Cuboid6, Vector3}
import net.minecraft.block.SoundType
import net.minecraft.client.particle.ParticleManager
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.{BlockPos, Vec3d}
import net.minecraft.util.{BlockRenderLayer, EnumHand, ResourceLocation}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.JavaConversions._

abstract class TMultiPart {
    /**
     * Reference to the container TileMultipart instance
     */
    var tile: TileMultipart = _

    /**
     * Getter for tile.worldObj
     */
    def world = if (tile == null) null else tile.getWorld

    /**
     * Short getter for blockPos
     */
    def pos = tile.getPos

    /**
     * Called when the container tile instance is changed to update reference
     */
    def bind(t: TileMultipart) {
        tile = t
    }

    /**
     * Returns a unique identifier for the part. Convention dictates that this be formatted as:
     * "[modid]:[unique name]"
     */
    def getType: ResourceLocation

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
    def save(tag: NBTTagCompound) {}

    /**
     * Load part from NBT (only called serverside)
     */
    def load(tag: NBTTagCompound) {}

    /**
     * Gets a MCDataOutput instance for writing update data to clients with this part loaded.
     * The write stream functions as a buffer which is flushed in a compressed databurst packet at the end of the tick.
     */
    def getWriteStream: MCDataOutput = tile.getWriteStream(this)

    /**
     * Read and operate on data written to getWriteStream. Ensure all data this part wrote is read even if it's not going to be used.
     * The default implementation assumes a call to sendDescUpdate as the only use of getWriteStream.
     */
    def read(packet: MCDataInput) {
        readDesc(packet)
        tile.markRender()
    }

    /**
     * Quick and easy method to re-describe the whole part on the client. This will call read on the client which calls readDesc unless overriden.
     * Incremental changes should be sent rather than the whole description packet if possible.
     */
    def sendDescUpdate() = writeDesc(getWriteStream)

    /**
     * Perform an occlusion test to determine whether this and npart can 'fit' in this block space.
     *
     * @param npart The part to run the test against.
     * @return True if both this part and npart are able to share this block space
     */
    def occlusionTest(npart: TMultiPart): Boolean = true

    /**
     * Return a list of entity collision boxes.
     * Note all Cuboid6's returned by methods in TMultiPart should be within (0,0,0)->(1,1,1)
     */
    def getCollisionBoxes: JIterable[Cuboid6] = Seq()

    /**
     * Perform a raytrace of this part. The default implementation does a Cuboid6 ray trace on bounding boxes returned from getSubParts.
     * This should only be overridden if you need special ray-tracing capabilities such as triangular faces.
     * The returned CuboidRayTraceResult will be passed to methods such as 'activate' so it is recommended to use the data field to indicate information about the hit area.
     */
    def collisionRayTrace(start: Vec3d, end: Vec3d): CuboidRayTraceResult = {
        val boxes = getSubParts.map { _.copy }
        RayTracer.rayTraceCuboidsClosest(start, end, tile.getPos, boxes.toList)
    }

    /**
     * For the default collisionRayTrace implementation, returns a list of indexed bounding boxes. The data field of ExtendedMOP will be set to the index of the cuboid the raytrace hit.
     */
    def getSubParts: JIterable[IndexedCuboid6] = Seq()

    /**
     * Harvest this part, removing it from the container tile and dropping items if necessary.
     *
     * @param hit    An instance of ExtendedMOP from collisionRayTrace
     * @param player The player harvesting the part
     */
    def harvest(player: EntityPlayer, hit: CuboidRayTraceResult) {
        if (!player.capabilities.isCreativeMode) {
            tile.dropItems(getDrops)
        }
        tile.remPart(this)
    }

    /**
     * Return a list of items that should be dropped when this part is destroyed.
     */
    def getDrops: JIterable[ItemStack] = Seq()

    /**
     * Return the itemstack for the middle click pick-block function.
     */
    def pickItem(hit: CuboidRayTraceResult): ItemStack = ItemStack.EMPTY

    /**
     * If any part returns true for this, torches can be placed. Vanilla hacks...
     */
    def canPlaceTorchOnTop = false

    /**
     * Explosion resistance of the host tile is the maximum explosion resistance of the contained parts
     *
     * @param entity The entity responsible for this explosion
     * @return The resistance of this part the the explosion
     */
    def getExplosionResistance(entity: Entity) = 0F

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
    def getStrength(player: EntityPlayer, hit: CuboidRayTraceResult): Float = 1 / 30f

    /**
     * Used to get the sound on placement.
     * YOUR PART MAY NOT BE IN THE WORLD YET!
     *
     * Weird place for this, but handles conversion placement
     * and direct placement.
     *
     * @param stack The stack used to place the part.
     * @return The sound
     */
    def getPlacementSound(stack:ItemStack):SoundType = {
        stack.getItem match {
            case i:TItemMultiPart => i.getPlacementSound(stack)
            case _ => null
        }
    }

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
    def activate(player: EntityPlayer, hit: CuboidRayTraceResult, item: ItemStack, hand: EnumHand) = false

    /**
     * Called on block left click.
     *
     * @param player The player who clicked on this part
     * @param hit    An instance of CuboidRayTraceResult from collisionRayTrace
     * @param item   The item held by the player
     */
    def click(player: EntityPlayer, hit: CuboidRayTraceResult, item: ItemStack) {}

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
    @Deprecated//Use pos sensitive version bellow, onNeighborBlockChanged.
    def onNeighborChanged() {}

    def onNeighborBlockChanged(from:BlockPos) {}

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
    @SideOnly(Side.CLIENT)
    def addHitEffects(hit: CuboidRayTraceResult, manager: ParticleManager) {}

    /**
     * Add particles and other effects when a player broke this part
     *
     * @param hit An instance of ExtendedMOP from collisionRayTrace
     */
    @SideOnly(Side.CLIENT)
    def addDestroyEffects(hit: CuboidRayTraceResult, manager: ParticleManager) {}

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
     * @param pos   The position to render at. Use this instead of the actual tile position
     *              (Although they will be the same in almost all cases).
     * @param layer The render layer
     * @param ccrs  An instance of CCRenderState that is in use
     * @return true if vertices were added to the buffer
     */
    @SideOnly(Side.CLIENT)
    def renderStatic(pos: Vector3, layer: BlockRenderLayer, ccrs: CCRenderState) = false

    /**
     * Render the static, unmoving faces of this part into the world renderer.
     *
     * CCRenderState is set up as follows should you wish to use it:
     *  - CCRenderState.reset() has been called
     *  - The current buffer is bound
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
     * @param pos     The position to render at. Use this instead of the actual tile position
     *                (Although they will be the same in almost all cases)
     * @param ccrs    An instance of CCRenderState that is in use
     * @param texture The current f overlay texture
     */
    @SideOnly(Side.CLIENT)
    def renderBreaking(pos: Vector3, texture: TextureAtlasSprite, ccrs: CCRenderState) {}

    /**
     * Override the drawing of the selection box around this part.
     *
     * @param hit An instance of ExtendedMOP from collisionRayTrace
     * @return true if highlight rendering was overridden.
     */
    @SideOnly(Side.CLIENT)
    def drawHighlight(player: EntityPlayer, hit: CuboidRayTraceResult, frame: Float): Boolean = false
}
