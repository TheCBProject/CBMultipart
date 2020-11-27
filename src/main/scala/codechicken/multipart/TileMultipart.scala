package codechicken.multipart

import java.lang.{Iterable => JIterable}
import java.util.{Random, ArrayList => JArrayList, Collection => JCollection, LinkedList => JLinkedList, List => JList}

import codechicken.lib.capability.CapabilityCache
import codechicken.lib.data.{MCByteStream, MCDataInput, MCDataOutput}
import codechicken.lib.raytracer.{MergedVoxelShapeHolder, VoxelShapeCache}
import codechicken.lib.render.{CCRenderState, RenderUtils}
import codechicken.lib.vec.{Cuboid6, Matrix4, Vector3}
import codechicken.lib.world.IChunkLoadTile
import codechicken.multipart.api.part.{TFacePart, TMultiPart}
import codechicken.multipart.capability.CapHolder
import codechicken.multipart.init.{ModContent, MultiPartRegistries}
import codechicken.multipart.network.MultipartSPH
import codechicken.multipart.util.{MultiPartGenerator, MultiPartHelper, MultipartVoxelShape, PartRayTraceResult}
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.ParticleManager
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.{ActiveRenderInfo, IRenderTypeBuffer, RenderType}
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.item.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.{BlockItemUseContext, ItemStack, ItemUseContext}
import net.minecraft.nbt.{CompoundNBT, ListNBT}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{ActionResultType, Direction, Hand}
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}

import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._

class TileMultipart extends TileEntity(ModContent.tileMultipartType) with IChunkLoadTile {
    /**
     * List of parts in this tile space
     */
    var partList = Seq[TMultiPart]()

    /**
     * Implicit java conversion of part list
     */
    def jPartList: JList[TMultiPart] = partList.asJava

    private[multipart] def from(that: TileMultipart) {
        copyFrom(that)
        loadFrom(that)
        that.loadTo(this)
    }

    //region *** Trait Overrides ***

    /**
     * This method should be used for copying all the data from the fields in that container tile.
     * This method will be automatically generated on java tile traits with fields if it is not overridden.
     */
    def copyFrom(that: TileMultipart) {
        partList = that.partList
        capParts = that.capParts
        resetCapCache()
        markShapeChange()
    }

    /**
     * Used to load the newly accuired data from copyFrom.
     */
    def loadFrom(that: TileMultipart) {
        partList.foreach(_.bind(this))
    }

    /**
     * Called after a tile conversion on the old tile. At the time of this call, this tile is no longer valid.
     * This is called before receiveFrom is called on the new tile.
     *
     * Provided for trait overrides, do not call externally.
     *
     * @param that The new tile
     */
    def loadTo(that: TileMultipart) {}

    /**
     * Remove all parts from internal cache.
     *
     * Provided for trait overrides, do not call externally.
     */
    def clearParts() {
        partList = Seq()
        capParts = new JLinkedList[ICapabilityProvider]()
        resetCapCache()
        markShapeChange()
    }

    /**
     * Bind this part to an internal cache.
     *
     * Provided for trait overrides, do not call externally.
     */
    def bindPart(part: TMultiPart) {}

    /**
     * Called when a part is added (placement).
     *
     * Provided for trait overrides, do not call externally.
     */
    def partAdded(part: TMultiPart) {}

    /**
     * Remove this part from internal cache.
     *
     * Provided for trait overrides, do not call externally.
     */
    def partRemoved(part: TMultiPart, p: Int) {}

    /**
     * Blank implementation
     *
     * Overriden by TTileChangeTile
     */
    def getWeakChanges = false

    /**
     * Blank implementation.
     *
     * Overriden by TTileChangeTile
     */
    def onNeighborTileChange(neighborPos: BlockPos) {}

    /**
     * Blank implementation.
     *
     * Overidden in TSlottedTile
     */
    def partMap(slot: Int): TMultiPart = null

    def operate(f: TMultiPart => Unit) {
        val it = partList.iterator
        while (it.hasNext) {
            val p = it.next()
            if (p.tile != null) f(p)
        }
    }

    //endregion

    //region *** Tile Save/Load ***

    final override def write(tag: CompoundNBT) = {
        super.write(tag)
        val taglist = new ListNBT
        partList.foreach { part =>
            taglist.add(MultiPartRegistries.savePart(new CompoundNBT, part))
        }
        tag.put("parts", taglist)
        tag
    }

    override def getUpdateTag = {
        val tag = super.getUpdateTag
        val desc = new MCByteStream()
        writeDesc(desc)
        tag.putByteArray("data", desc.getBytes)
        tag
    }

    //endregion

    //region *** Networking ***

    /**
     * Writes the description of this tile, and all parts composing it, to packet
     */
    def writeDesc(packet: MCDataOutput) {
        packet.writeByte(partList.size)
        partList.foreach { part =>
            MultiPartRegistries.writePart(packet, part)
        }
    }

    //endregion

    //region *** Adding/Removing parts ***

    /**
     * Returns true if part can be added to this space
     */
    def canAddPart(part: TMultiPart) =
        !partList.contains(part) && occlusionTest(jPartList, part)

    /**
     * Returns true if opart can be replaced with npart (note opart and npart may be the exact same object)
     *
     * This function should be used for testing if a part can change it's shape (eg. rotation, expansion, cable connection)
     * For example, to test whether a cable part can connect to it's neighbor:
     *  1. Set the cable part's bounding boxes as if the connection is established
     *     2. Call canReplacePart(part, part)
     *     3. If canReplacePart succeeds, perform connection, else, revert bounding box
     */
    def canReplacePart(opart: TMultiPart, npart: TMultiPart): Boolean = {
        val olist = partList.filterNot(_ == opart)
        if (olist.contains(npart)) {
            return false
        }

        occlusionTest(olist.asJava, npart)
    }

    /**
     * Returns true if parts do not occlude npart
     */
    def occlusionTest(parts: JList[TMultiPart], npart: TMultiPart): Boolean = {
        parts.stream.allMatch(part => part.occlusionTest(npart) && npart.occlusionTest(part))
    }

    private[multipart] def addPart_impl(part: TMultiPart) {
        if (!world.isRemote) MultipartSPH.sendAddPart(this, part)

        addPart_do(part)
        part.onAdded()
        partAdded(part)
        notifyPartChange(part)
        notifyTileChange()
        markDirty()
        markRender()
    }

    private[multipart] def addPart_do(part: TMultiPart) {
        assert(partList.size < 64, "Tried to add more than 250 parts to the one tile. You're doing it wrong")

        partList = partList :+ part
        bindPart(part)
        part match {
            case p: ICapabilityProvider =>
                capParts.add(p)
                resetCapCache()
            case _ =>
        }
        markShapeChange()
        part.bind(this)
    }

    /**
     * Removes part from this tile. Note that due to the operation sync, the part may not be removed until the call stack has been passed to all other parts in the space.
     */
    def remPart(part: TMultiPart): TileMultipart = {
        assert(!world.isRemote, "Cannot remove multi parts from a client tile")
        remPart_impl(part)
    }

    private[multipart] def remPart_impl(part: TMultiPart): TileMultipart = {
        remPart_do(part, !world.isRemote)

        if (!isRemoved) {
            val tile = MultiPartHelper.partRemoved(this)
            tile.notifyPartChange(part)
            tile.markDirty()
            tile.markRender()
            return tile
        }

        null
    }

    private def remPart_do(part: TMultiPart, sendPacket: Boolean): Int = {
        val r = partList.indexOf(part)
        if (r < 0) {
            throw new IllegalArgumentException("Tried to remove a non-existant part")
        }

        part.preRemove()
        partList = partList.filterNot(_ == part)

        if (sendPacket) MultipartSPH.sendRemPart(this, r)

        partRemoved(part, r)
        part match {
            case p: ICapabilityProvider =>
                capParts.remove(p)
                resetCapCache()
            case _ =>
        }
        part.onRemoved()
        part.tile = null
        markShapeChange()

        if (partList.isEmpty) world.removeBlock(pos, false)
        r
    }

    private[multipart] def loadParts(parts: Iterable[TMultiPart]) {
        clearParts()
        parts.foreach(p => addPart_do(p))
        if (world != null) {
            if (world.isRemote) {
                operate(_.onWorldJoin())
            }
            notifyPartChange(null)
        }
    }

    final def setValid(b: Boolean) {
        if (b) {
            super.validate()
        } else {
            super.remove()
        }
    }

    override def remove() {
        if (!isRemoved) {
            super.remove()
            if (world != null) {
                partList.foreach(_.onWorldSeparate())
            }
        }
    }

    //endregion

    //region *** Internal callbacks ***

    private val outlineShapeHolder = new MergedVoxelShapeHolder[TMultiPart]()
        .setExtractor(_.getOutlineShape)
        .setPostProcessHook(new MultipartVoxelShape(_, this))
    private val collisionShapeHolder = new MergedVoxelShapeHolder[TMultiPart]()
        .setExtractor(_.getCollisionShape)
        .setPostProcessHook(new MultipartVoxelShape(_, this))
    private val cullingShapeHolder = new MergedVoxelShapeHolder[TMultiPart]()
        .setExtractor(_.getCullingShape)
        .setPostProcessHook(new MultipartVoxelShape(_, this))
    private val rayTraceShapeHolder = new MergedVoxelShapeHolder[TMultiPart]()
        .setExtractor(_.getRayTraceShape)
        .setPostProcessHook(new MultipartVoxelShape(_, this))

    def getOutlineShape = outlineShapeHolder.update(partList.asJavaCollection)

    def getCollisionShape = collisionShapeHolder.update(partList.asJavaCollection)

    def getCullingShape = cullingShapeHolder.update(partList.asJavaCollection)

    def getRayTraceShape = rayTraceShapeHolder.update(partList.asJavaCollection)

    /**
     * Drop and remove part at index (internal mining callback)
     */
    def harvestPart(hit: PartRayTraceResult, player: PlayerEntity) =
        partList(hit.partIndex) match {
            case null =>
            case part => part.harvest(player, hit)
        }

    def getDrops: JArrayList[ItemStack] = {
        val list = new JArrayList[ItemStack]()
        partList.foreach { _.getDrops.forEach { e => list.add(e) } }
        list
    }

    def getPickBlock(hit: PartRayTraceResult) =
        partList(hit.partIndex) match {
            case null => null
            case part => part.pickItem(hit)
        }

    def isSolid(side: Int) = partMap(side) match {
        case face: TFacePart => face.solid(side)
        case _ => false
    }

    def canPlaceTorchOnTop = partList.exists(_.canPlaceTorchOnTop) || isSolid(1)

    def getExplosionResistance(entity: Entity) = partList.view.map(_.getExplosionResistance(entity)).max

    def getLightValue = partList.view.map(_.getLightValue).max

    def getPlayerRelativeBlockHardness(player: PlayerEntity, hit: PartRayTraceResult): Float = {
        if (hit == null) return 1 / 100F
        partList(hit.partIndex) match {
            case null => 1 / 100F
            case part => part.getStrength(player, hit)
        }
    }

    override def onChunkUnloaded() {
        operate(_.onChunkUnload())
    }

    override def onChunkLoad() {
        operate(_.onChunkLoad())
    }

    def onMoved() {
        capabilityCache.setWorldPos(getWorld, getPos)
        operate(_.onMoved())
    }

    def onBlockActivated(player: PlayerEntity, hit: PartRayTraceResult, hand: Hand): ActionResultType = {
        if (hit == null) return ActionResultType.FAIL
        partList(hit.partIndex) match {
            case null => ActionResultType.FAIL
            case part => part.activate(player, hit, player.getHeldItem(hand), hand)
        }
    }

    def onBlockClicked(player: PlayerEntity, hit: PartRayTraceResult) {
        if (hit != null) {
            partList(hit.partIndex) match {
                case null =>
                case part => part.click(player, hit, player.getHeldItemMainhand)
            }
        }
    }

    override def setWorldAndPos(world: World, pos: BlockPos) {
        super.setWorldAndPos(world, pos)
        capabilityCache.setWorldPos(world, pos)
    }

    /**
     * Internal callback
     */
    def onEntityCollision(entity: Entity) {
        operate(_.onEntityCollision(entity))
    }

    /**
     * Internal callback
     */
    def onEntityStanding(entity: Entity) {
        operate(_.onEntityStanding(entity))
    }

    def onNeighborBlockChanged(pos: BlockPos) {
        capabilityCache.onNeighborChanged(pos)
        operate(_.onNeighborBlockChanged(pos))
    }

    /**
     * Internal callback, overriden in TRedstoneTile
     */
    def canConnectRedstone(side: Int) = false

    /**
     * Internal callback, overriden in TRedstoneTile
     */
    def strongPowerLevel(side: Int) = 0

    /**
     * Internal callback, overriden in TRedstoneTile
     */
    def weakPowerLevel(side: Int) = 0

    override def getRenderBoundingBox = {
        val c = Cuboid6.full.copy
        partList.foreach(p => c.enclose(p.getRenderBounds))
        c.add(pos).aabb
    }

    //endregion

    //region *** Utility Functions ***

    /**
     * Notifies neighboring blocks that this tile has changed
     */
    def notifyTileChange() {
        world.notifyNeighborsOfStateChange(pos, ModContent.blockMultipart)
    }

    /**
     * Called by parts when they have changed in some form that affects the world.
     * Notifies neighbor blocks, the world and parts that share this host and recalculates lighting
     */
    def notifyPartChange(part: TMultiPart) {
        internalPartChange(part)

        world.notifyBlockUpdate(pos, ModContent.blockMultipart.getDefaultState, ModContent.blockMultipart.getDefaultState, 3)
        world.notifyNeighborsOfStateChange(pos, ModContent.blockMultipart)
        world.getChunkProvider.getLightManager.checkBlock(pos)
    }

    /**
     * Notifies parts sharing this host of a change
     */
    def internalPartChange(part: TMultiPart) {
        operate(p => if (part != p) p.onPartChanged(part))
    }

    /**
     * Notifies all parts not in the passed collection of a change from all the parts in the collection
     */
    def multiPartChange(parts: JCollection[TMultiPart]) {
        operate(p => if (!parts.contains(p)) parts.forEach(p.onPartChanged))
    }

    /**
     * Callback for parts to mark the chunk as needs saving
     */
    override def markDirty() {
        world.markChunkDirty(pos, this)
    }

    /**
     * Mark this block space for a render update.
     */
    def markRender() {}

    def recalcLight(sky: Boolean, block: Boolean) {
        val lm = world.getChunkProvider.getLightManager
        if (sky && lm.skyLight != null) {
            lm.skyLight.checkLight(pos)
        }
        if (block && lm.blockLight != null) {
            lm.blockLight.checkLight(pos)
        }
    }

    def markShapeChange() {
        outlineShapeHolder.clear()
        collisionShapeHolder.clear()
        cullingShapeHolder.clear()
        rayTraceShapeHolder.clear()
    }

    /**
     * Helper function for calling a second level notify on a side (eg indirect power from a lever)
     */
    def notifyNeighborChange(side: Int) {
        world.notifyNeighborsOfStateChange(getPos.offset(Direction.byIndex(side)), ModContent.blockMultipart)
    }

    /**
     * Utility function for dropping items around the center of this space
     */
    def dropItems(items: JIterable[ItemStack]) {
        val pos = Vector3.fromTileCenter(this)
        items.forEach(item => TileMultipart.dropItem(item, world, pos))
    }

    //endregion

    //region *** Capability handling ***

    private val capabilityCache = new CapabilityCache()

    private var capParts = new JLinkedList[ICapabilityProvider]()
    private var calculatedCaps = Set[Capability[_]]()
    private var capMap = Map.empty[Capability[_], CapHolder[_]]

    /**
     * Gets a global [[CapabilityCache]] instance, usable by all parts in this block space.
     * Note: [[CapabilityCache]] might not properly handle wait ticks, as this multipart
     * might not be tickable without a tickable part attached.
     *
     * @return The CapabilityCache instance.
     */
    def getCapCache = capabilityCache


    def resetCapCache() {
        capMap = Map()
        calculatedCaps = Set()
    }

    private def calculateCap(cap: Capability[_]) {
        //        if (calculatedCaps.contains(cap)) {
        //            return
        //        }
        //        calculatedCaps += cap
        //        val holder = new CapHolder[Any]
        //        val genericValid = capParts.filter(_.hasCapability(cap, null))
        //        if (genericValid.nonEmpty) {
        //            holder.generic = MultipartCapRegistry.merge(cap, genericValid.map(_.getCapability(cap, null).asInstanceOf[Object]))
        //        }
        //        for (side <- EnumFacing.VALUES) {
        //            val sidedValid = capParts.filter(_.hasCapability(cap, side))
        //            if (sidedValid.nonEmpty) {
        //                holder.sided += side -> MultipartCapRegistry.merge(cap, sidedValid.map(_.getCapability(cap, side).asInstanceOf[Object]))
        //            }
        //        }
        //        if (holder.generic != null || holder.sided.nonEmpty) {
        //            capMap += cap -> holder
        //        }
    }

    //    override final def hasCapability(capability: Capability[_], facing: EnumFacing) = {
    //        calculateCap(capability)
    //        capMap.get(capability) match {
    //            case Some(holder) => (facing == null && holder.generic != null) || (facing != null && holder.sided.contains(facing))
    //            case None => super.hasCapability(capability, facing)
    //        }
    //    }
    //
    //    override final def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
    //        val cap = capability.asInstanceOf[Capability[Any]]
    //        calculateCap(cap)
    //        capMap.get(capability) match {
    //            case Some(holder) =>
    //                if (facing == null) {
    //                    cap.cast(holder.generic)
    //                } else if (holder.sided.contains(facing)) {
    //                    cap.cast(holder.sided(facing))
    //                } else {
    //                    cap.cast(null)
    //                }
    //            case None => super.getCapability(capability, facing)
    //        }
    //    }
    //endregion

}

trait TileMultipartClient extends TileMultipart {

    def renderStatic(pos: Vector3, layer: RenderType, ccrs: CCRenderState) = partList.count(_.renderStatic(pos, layer, ccrs)) > 0

    def renderDamage(pos: Vector3, texture: TextureAtlasSprite, ccrs: CCRenderState) {
        Minecraft.getInstance.objectMouseOver match {
            case hit: PartRayTraceResult if partList.isDefinedAt(hit.partIndex) =>
                partList(hit.partIndex).renderBreaking(pos, texture, ccrs)
            case _ =>
        }
    }

    def animateTick(random: Random) {}

    def drawHighlight(hit: PartRayTraceResult, info: ActiveRenderInfo, mStack: MatrixStack, getter: IRenderTypeBuffer, partialTicks: Float): Boolean =
        partList(hit.partIndex) match {
            case null => false
            case part =>
                if (!part.drawHighlight(hit, info, mStack, getter, partialTicks)) {
                    val mat = new Matrix4(mStack)
                    mat.translate(hit.getPos)
                    RenderUtils.bufferHitbox(mat, getter, info, VoxelShapeCache.getCuboid(part.getOutlineShape))
                }
                true
        }

    def addHitEffects(hit: PartRayTraceResult, manager: ParticleManager) {
        partList(hit.partIndex) match {
            case null =>
            case part => part.addHitEffects(hit, manager)
        }
    }

    def addDestroyEffects(hit: PartRayTraceResult, manager: ParticleManager) {
        partList(hit.partIndex) match {
            case null =>
            case part => part.addDestroyEffects(hit, manager)
        }
    }

    override def markRender() {
        getWorld match {
            case world: ClientWorld =>
                world.worldRenderer.markBlockRangeForRenderUpdate(getPos.getX, getPos.getY, getPos.getZ, getPos.getX, getPos.getY, getPos.getZ)
        }
    }
}

/**
 * Static class with multipart manipulation helper functions
 */
object TileMultipart {

    /**
     * Gets the multipart tile instance at pos, or null if it doesn't exist or is not a multipart tile
     */
    def getTile(world: World, pos: BlockPos) =
        world.getTileEntity(pos) match {
            case t: TileMultipart => t
            case _ => null
        }

    def checkNoEntityCollision(world: World, pos: BlockPos, part: TMultiPart) =
        world.checkNoEntityCollision(null, part.getCollisionShape.withOffset(pos.getX, pos.getY, pos.getZ))

    /**
     * Returns whether part can be added to the space at pos. Will do conversions as necessary.
     * This function is the recommended way to add parts to the world.
     */
    def canPlacePart(useContext: ItemUseContext, part: TMultiPart): Boolean = {
        val world = useContext.getWorld
        val pos = useContext.getPos.offset(useContext.getFace)

        if (!checkNoEntityCollision(world, pos, part)) {
            return false
        }

        val t = MultiPartHelper.getOrConvertTile(world, pos)
        if (t != null) {
            return t.canAddPart(part)
        }

        if (!replaceable(world, pos, useContext)) return false

        true
    }

    /**
     * Returns if the block at pos is replaceable (air, vines etc)
     */
    def replaceable(world: World, pos: BlockPos, useContext: ItemUseContext): Boolean = {
        val state = world.getBlockState(pos)
        val block = state.getBlock
        block.isAir(state, world, pos) || state.isReplaceable(new BlockItemUseContext(useContext))
    }

    /**
     * Adds a part to a block space. canPlacePart should always be called first.
     * The addition of parts on the client is handled internally.
     */
    def addPart(world: World, pos: BlockPos, part: TMultiPart): TileMultipart = {
        assert(!world.isRemote, "Cannot add multi parts to a client tile.")
        MultiPartHelper.addPart(world, pos, part)
    }

    /**
     * Constructs this tile and its parts from a desc packet
     */
    def handleDescPacket(world: World, pos: BlockPos, packet: MCDataInput) {
        val nparts = packet.readUByte
        val parts = new ListBuffer[TMultiPart]()
        for (_ <- 0 until nparts) {
            parts += MultiPartRegistries.readPart(packet)
        }

        if (parts.isEmpty) return

        val t = world.getTileEntity(pos)
        val tilemp = MultiPartGenerator.INSTANCE.generateCompositeTile(t, parts.asJava, true)
        if (tilemp != t) {
            world.setBlockState(pos, ModContent.blockMultipart.getDefaultState)
            MultiPartHelper.silentAddTile(world, pos, tilemp)
        }

        tilemp.loadParts(parts)
        tilemp.notifyTileChange()
        tilemp.markRender()
    }

    /**
     * Creates this tile from an NBT tag
     */
    def createFromNBT(tag: CompoundNBT): TileMultipart = {
        val partList = tag.getList("parts", 10)
        val parts = ListBuffer[TMultiPart]()

        for (i <- 0 until partList.size) {
            val part = MultiPartRegistries.loadPart(partList.getCompound(i))
            if (part != null) {
                parts += part
            }
        }

        if (parts.isEmpty) return null

        val tmb = MultiPartGenerator.INSTANCE.generateCompositeTile(null, parts.asJava, false)
        tmb.read(tag)
        tmb.loadParts(parts)
        tmb
    }

    /**
     * Drops an item around pos
     */
    //TODO CCL
    def dropItem(stack: ItemStack, world: World, pos: Vector3) {
        val item = new ItemEntity(world, pos.x, pos.y, pos.z, stack)
        item.setMotion(world.rand.nextGaussian() * 0.05, world.rand.nextGaussian() * 0.05 + 0.2, world.rand.nextGaussian() * 0.05)
        item.setPickupDelay(10)
        world.addEntity(item)
    }
}
