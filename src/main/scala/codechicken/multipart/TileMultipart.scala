package codechicken.multipart

import java.lang.{Iterable => JIterable}
import java.util.{Random, ArrayList => JArrayList, Collection => JCollection, LinkedList => JLinkedList, List => JList}

import codechicken.lib.data.MCDataOutput
import codechicken.lib.packet.PacketCustom
import codechicken.lib.raytracer.{CuboidRayTraceResult, DistanceRayTraceResult}
import codechicken.lib.render.{CCRenderState, RenderUtils}
import codechicken.lib.vec.{Cuboid6, Vector3}
import codechicken.lib.world.IChunkLoadTile
import codechicken.multipart.capability.CapHolder
import codechicken.multipart.handler.{MultipartCompatiblity, MultipartProxy, MultipartSPH}
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.ParticleManager
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.{AxisAlignedBB, BlockPos, Vec3d}
import net.minecraft.util.{BlockRenderLayer, EnumFacing, EnumHand, ResourceLocation}
import net.minecraft.world.{EnumSkyBlock, World}
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

class TileMultipart extends TileEntity with IChunkLoadTile {
    /**
     * List of parts in this tile space
     */
    var partList = Seq[TMultiPart]()

    /**
     * Implicit java conversion of part list
     */
    def jPartList: JList[TMultiPart] = partList

    private[multipart] def from(that: TileMultipart) {
        copyFrom(that)
        loadFrom(that)
        that.loadTo(this)
    }

    /** ** Trait Overrides ****/

    /**
     * This method should be used for copying all the data from the fields in that container tile.
     * This method will be automatically generated on java tile traits with fields if it is not overridden.
     */
    def copyFrom(that: TileMultipart) {
        partList = that.partList
        capParts = that.capParts
        resetCapCache()
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

    def operate(f: (TMultiPart) => Unit) {
        val it = partList.iterator
        while (it.hasNext) {
            val p = it.next()
            if (p.tile != null) f(p)
        }
    }

    /** ** Tile Save/Load ****/

    final override def writeToNBT(tag: NBTTagCompound) = {
        super.writeToNBT(tag)
        val taglist = new NBTTagList
        partList.foreach { part =>
            val parttag = new NBTTagCompound
            parttag.setString("id", part.getType.toString)
            part.save(parttag)
            taglist.appendTag(parttag)
        }
        tag.setTag("parts", taglist)
        tag
    }

    /** ** Networking ****/

    /**
     * Writes the description of this tile, and all parts composing it, to packet
     */
    def writeDesc(packet: MCDataOutput) {
        packet.writeByte(partList.size)
        partList.foreach { part =>
            MultiPartRegistry.writePartID(packet, part)
            part.writeDesc(packet)
        }
    }

    /**
     * Get the write stream for updates to part
     */
    def getWriteStream(part: TMultiPart): MCDataOutput = getWriteStream.writeByte(partList.indexOf(part))

    private def getWriteStream = MultipartSPH.getTileStream(world, getPos)

    /** ** Adding/Removing parts ****/

    /**
     * Returns true if part can be added to this space
     */
    def canAddPart(part: TMultiPart) =
        MultipartCompatiblity.canAddPart(world, pos) &&
            !partList.contains(part) &&
            occlusionTest(partList, part)

    /**
     * Returns true if opart can be replaced with npart (note opart and npart may be the exact same object)
     *
     * This function should be used for testing if a part can change it's shape (eg. rotation, expansion, cable connection)
     * For example, to test whether a cable part can connect to it's neighbor:
     *  1. Set the cable part's bounding boxes as if the connection is established
     *  2. Call canReplacePart(part, part)
     *  3. If canReplacePart succeeds, perform connection, else, revert bounding box
     */
    def canReplacePart(opart: TMultiPart, npart: TMultiPart): Boolean = {
        val olist = partList.filterNot(_ == opart)
        if (olist.contains(npart)) {
            return false
        }

        occlusionTest(olist, npart)
    }

    /**
     * Returns true if parts do not occlude npart
     */
    def occlusionTest(parts: Seq[TMultiPart], npart: TMultiPart): Boolean = {
        parts.forall(part => part.occlusionTest(npart) && npart.occlusionTest(part))
    }

    private[multipart] def addPart_impl(part: TMultiPart) {
        if (!world.isRemote) writeAddPart(part)

        addPart_do(part)
        part.onAdded()
        partAdded(part)
        notifyPartChange(part)
        notifyTileChange()
        markDirty()
        markRender()
    }

    private[multipart] def writeAddPart(part: TMultiPart) {
        val stream = getWriteStream.writeByte(253)
        MultiPartRegistry.writePartID(stream, part)
        part.writeDesc(stream)
    }

    private[multipart] def addPart_do(part: TMultiPart) {
        assert(partList.size < 250, "Tried to add more than 250 parts to the one tile. You're doing it wrong")

        partList = partList :+ part
        bindPart(part)
        part match {
            case p: ICapabilityProvider =>
                capParts += p
                resetCapCache()
            case _ =>
        }
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

        if (!isInvalid) {
            val tile = MultipartGenerator.partRemoved(this)
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

        if (sendPacket) getWriteStream.writeByte(254).writeByte(r)

        partRemoved(part, r)
        part match {
            case p: ICapabilityProvider =>
                capParts -= p
                resetCapCache()
            case _ =>
        }
        part.onRemoved()
        part.tile = null

        if (partList.isEmpty) world.setBlockToAir(pos)
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
            super.invalidate()
        }
    }

    override def invalidate() {
        if (!isInvalid) {
            super.invalidate()
            if (world != null) {
                partList.foreach(_.onWorldSeparate())
                if (world.isRemote) {
                    TileCache.remove(this)
                }
            }
        }
    }

    override def validate() {
        super.validate()
        if (world != null && world.isRemote) {
            TileCache.add(this)
        }
    }

    /** ** Internal Block callbacks ****/

    /**
     * Internal block callback to obtain entity collision boxes
     */
    def addCollisionBoxToList(entityBox: AxisAlignedBB, list: JList[AxisAlignedBB]) {
        val mask = new Cuboid6(entityBox).subtract(pos) //get entityBox in zero-space
        partList.foreach {
            _.getCollisionBoxes.foreach { c =>
                if (c.intersects(mask)) list.add(c.aabb().offset(pos))
            }
        }
    }

    /**
     * Perform a raytrace returning the nearest intersecting part
     */
    def collisionRayTrace(start: Vec3d, end: Vec3d): PartRayTraceResult = rayTraceAll(start, end).headOption.orNull

    /**
     * Perform a raytrace returning all intersecting parts sorted nearest to farthest
     */
    def rayTraceAll(start: Vec3d, end: Vec3d): JIterable[PartRayTraceResult] = {
        var list = ListBuffer[PartRayTraceResult]()
        for ((p, i) <- partList.view.zipWithIndex)
            p.collisionRayTrace(start, end) match {
                case crtr: CuboidRayTraceResult =>
                    val partMOP = new PartRayTraceResult(i, crtr)
                    list += partMOP
                case _ =>
            }

        list.asInstanceOf[ListBuffer[DistanceRayTraceResult]].sorted.asInstanceOf[ListBuffer[PartRayTraceResult]]
    }

    /**
     * Drop and remove part at index (internal mining callback)
     */
    def harvestPart(hit: PartRayTraceResult, player: EntityPlayer) =
        partList(hit.partIndex) match {
            case null =>
            case part => part.harvest(player, hit)
        }

    def getDrops: JArrayList[ItemStack] = {
        val list = new JArrayList[ItemStack]()
        partList.foreach { _.getDrops.foreach { list.add } }
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

    def getPlayerRelativeBlockHardness(player: EntityPlayer, hit: PartRayTraceResult): Float = {
        if (hit == null) return 1 / 100F
        partList(hit.partIndex) match {
            case null => 1 / 100F
            case part => part.getStrength(player, hit)
        }
    }

    override def onChunkUnload() {
        operate(_.onChunkUnload())
    }

    override def onChunkLoad() {
        operate(_.onChunkLoad())
    }

    override def setWorldCreate(worldIn: World) = setWorld(worldIn)

    def onMoved() {
        operate(_.onMoved())
    }

    def onBlockActivated(player: EntityPlayer, hit: PartRayTraceResult, hand: EnumHand): Boolean = {
        if (hit == null) return false
        partList(hit.partIndex) match {
            case null => false
            case part => part.activate(player, hit, player.getHeldItem(hand), hand)
        }
    }

    def onBlockClicked(player: EntityPlayer, hit: PartRayTraceResult) {
        if (hit != null) {
            partList(hit.partIndex) match {
                case null =>
                case part => part.click(player, hit, player.getHeldItemMainhand)
            }
        }
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

    @Deprecated
    def onNeighborBlockChange() {
        operate(_.onNeighborChanged())
    }

    def onNeighborBlockChanged(pos: BlockPos) {
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

    /** ** Utility Functions ****/

    /**
     * Notifies neighboring blocks that this tile has changed
     */
    def notifyTileChange() {
        world.notifyNeighborsOfStateChange(pos, MultipartProxy.block, true)
    }

    /**
     * Called by parts when they have changed in some form that affects the world.
     * Notifies neighbor blocks, the world and parts that share this host and recalculates lighting
     */
    def notifyPartChange(part: TMultiPart) {
        internalPartChange(part)

        world.notifyBlockUpdate(pos, MultipartProxy.block.getDefaultState, MultipartProxy.block.getDefaultState, 3)
        world.notifyNeighborsOfStateChange(pos, MultipartProxy.block, true)
        world.checkLight(pos)
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
        operate(p => if (!parts.contains(p)) parts.foreach(p.onPartChanged))
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
    def markRender() {
        world.markBlockRangeForRenderUpdate(pos, pos)
    }

    def recalcLight(sky: Boolean, block: Boolean) {
        if (sky && !world.provider.isNether) {
            world.checkLightFor(EnumSkyBlock.SKY, pos)
        }
        if (block) {
            world.checkLightFor(EnumSkyBlock.BLOCK, pos)
        }
    }

    /**
     * Helper function for calling a second level notify on a side (eg indirect power from a lever)
     */
    def notifyNeighborChange(side: Int) {
        world.notifyNeighborsOfStateChange(getPos.offset(EnumFacing.values()(side)), MultipartProxy.block, true)
    }

    /**
     * Utility function for dropping items around the center of this space
     */
    def dropItems(items: JIterable[ItemStack]) {
        val pos = Vector3.fromTileCenter(this)
        items.foreach(item => TileMultipart.dropItem(item, world, pos))
    }

    override def shouldRefresh(world: World, pos: BlockPos, oldState: IBlockState, newState: IBlockState) = oldState.getBlock != newState.getBlock

    /** Capability handling */

    private var capParts = new JLinkedList[ICapabilityProvider]()
    private var calculatedCaps = Set[Capability[_]]()
    private var capMap = Map.empty[Capability[_], CapHolder[_]]

    def resetCapCache() {
        capMap = Map()
        calculatedCaps = Set()
    }

    private def calculateCap(cap: Capability[_]) {
        if (calculatedCaps.contains(cap)) {
            return
        }
        calculatedCaps += cap
        val holder = new CapHolder[Any]
        val genericValid = capParts.filter(_.hasCapability(cap, null))
        if (genericValid.nonEmpty) {
            holder.generic = MultipartCapRegistry.merge(cap, genericValid.map(_.getCapability(cap, null).asInstanceOf[Object]))
        }
        for (side <- EnumFacing.VALUES) {
            val sidedValid = capParts.filter(_.hasCapability(cap, side))
            if (sidedValid.nonEmpty) {
                holder.sided += side -> MultipartCapRegistry.merge(cap, sidedValid.map(_.getCapability(cap, side).asInstanceOf[Object]))
            }
        }
        if (holder.generic != null || holder.sided.nonEmpty) {
            capMap += cap -> holder
        }
    }

    override final def hasCapability(capability: Capability[_], facing: EnumFacing) = {
        calculateCap(capability)
        capMap.get(capability) match {
            case Some(holder) => (facing == null && holder.generic != null) || (facing != null && holder.sided.contains(facing))
            case None => super.hasCapability(capability, facing)
        }
    }

    override final def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
        val cap = capability.asInstanceOf[Capability[Any]]
        calculateCap(cap)
        capMap.get(capability) match {
            case Some(holder) =>
                if (facing == null) {
                    cap.cast(holder.generic)
                } else if (holder.sided.contains(facing)) {
                    cap.cast(holder.sided(facing))
                } else {
                    cap.cast(null)
                }
            case None => super.getCapability(capability, facing)
        }
    }

}

trait TileMultipartClient extends TileMultipart {

    def renderStatic(pos: Vector3, layer: BlockRenderLayer, ccrs: CCRenderState) = partList.count(_.renderStatic(pos, layer, ccrs)) > 0

    def renderDamage(pos: Vector3, texture: TextureAtlasSprite, ccrs: CCRenderState) {
        Minecraft.getMinecraft.objectMouseOver match {
            case hit: PartRayTraceResult if partList.isDefinedAt(hit.partIndex) =>
                partList(hit.partIndex).renderBreaking(pos, texture, ccrs)
            case _ =>
        }
    }

    def randomDisplayTick(random: Random) {}

    def drawHighlight(player: EntityPlayer, hit: PartRayTraceResult, frame: Float): Boolean =
        partList(hit.partIndex) match {
            case null => false
            case part =>
                if (!part.drawHighlight(player, hit, frame)) {
                    RenderUtils.renderHitBox(player, hit.cuboid6.copy.add(Vector3.fromBlockPos(getPos)), frame)
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
}

/**
 * Static class with multipart manipulation helper functions
 */
object TileMultipart {
    /**
     * Gets a multipart tile instance at pos, converting if necessary.
     */
    def getOrConvertTile(world: World, pos: BlockPos) = getOrConvertTile2(world, pos)._1

    /**
     * Gets a multipart tile instance at pos, converting if necessary.
     * Note converted tiles are merely a structure formality,
     * they do not actually exist in world until they are required to by the addition of another multipart to their space.
     *
     * @return (The tile or null if there was none, true if the tile is a result of a conversion)
     */
    def getOrConvertTile2(world: World, pos: BlockPos): (TileMultipart, Boolean) = {
        val t = world.getTileEntity(pos)
        if (t.isInstanceOf[TileMultipart]) {
            return (t.asInstanceOf[TileMultipart], false)
        }

        val p = MultiPartRegistry.convertBlock(world, pos, world.getBlockState(pos))
        if (p.nonEmpty) {
            val t = MultipartGenerator.generateCompositeTile(null, p, world.isRemote)
            t.setPos(pos)
            t.setWorld(world)
            p.foreach(t.addPart_do)
            return (t, true)
        }
        (null, false)
    }

    /**
     * Gets the multipart tile instance at pos, or null if it doesn't exist or is not a multipart tile
     */
    def getTile(world: World, pos: BlockPos) =
        world.getTileEntity(pos) match {
            case t: TileMultipart => t
            case _ => null
        }

    def checkNoEntityCollision(world: World, pos: BlockPos, part: TMultiPart) =
        part.getCollisionBoxes.forall(b => world.checkNoEntityCollision(b.aabb.offset(pos)))

    /**
     * Returns whether part can be added to the space at pos. Will do conversions as necessary.
     * This function is the recommended way to add parts to the world.
     */
    def canPlacePart(world: World, pos: BlockPos, part: TMultiPart): Boolean = {
        if (!checkNoEntityCollision(world, pos, part)) {
            return false
        }

        val t = getOrConvertTile(world, pos)
        if (t != null) {
            return t.canAddPart(part)
        } else if (!MultipartCompatiblity.canAddPart(world, pos)) {
            return false
        }

        if (!replaceable(world, pos)) return false

        true
    }

    /**
     * Returns if the block at pos is replaceable (air, vines etc)
     */
    def replaceable(world: World, pos: BlockPos): Boolean = {
        val state = world.getBlockState(pos)
        val block = state.getBlock
        block.isAir(state, world, pos) || block.isReplaceable(world, pos)
    }

    /**
     * Adds a part to a block space. canPlacePart should always be called first.
     * The addition of parts on the client is handled internally.
     */
    def addPart(world: World, pos: BlockPos, part: TMultiPart): TileMultipart = {
        assert(!world.isRemote, "Cannot add multi parts to a client tile.")
        MultipartGenerator.addPart(world, pos, part)
    }

    /**
     * Constructs this tile and its parts from a desc packet
     */
    def handleDescPacket(world: World, pos: BlockPos, packet: PacketCustom) {
        val nparts = packet.readUByte
        val parts = new ListBuffer[TMultiPart]()
        for (i <- 0 until nparts) {
            val part = MultiPartRegistry.readPart(packet)
            part.readDesc(packet)
            parts += part
        }

        if (parts.isEmpty) return

        val t = world.getTileEntity(pos)
        val tilemp = MultipartGenerator.generateCompositeTile(t, parts, true)
        if (tilemp != t) {
            world.setBlockState(pos, MultipartProxy.block.getDefaultState)
            MultipartGenerator.silentAddTile(world, pos, tilemp)
        }

        tilemp.loadParts(parts)
        tilemp.notifyTileChange()
        tilemp.markRender()
    }

    /**
     * Handles an update packet, addition, removal and otherwise
     */
    def handlePacket(pos: BlockPos, world: World, i: Int, packet: PacketCustom) {
        def tilemp = TileCache.findTile(world, pos)

        i match {
            case 253 =>
                val part = MultiPartRegistry.readPart(packet)
                part.readDesc(packet)
                MultipartGenerator.addPart(world, pos, part)
            case 254 => tilemp.remPart_impl(tilemp.partList(packet.readUByte))
            case _ => tilemp.partList(i).read(packet)
        }
    }

    /**
     * Creates this tile from an NBT tag
     */
    def createFromNBT(tag: NBTTagCompound): TileMultipart = {
        val partList = tag.getTagList("parts", 10)
        val parts = ListBuffer[TMultiPart]()

        for (i <- 0 until partList.tagCount) {
            val partTag = partList.getCompoundTagAt(i)
            val partID = new ResourceLocation(partTag.getString("id"))
            val part = MultiPartRegistry.loadPart(partID, partTag)
            if (part != null) {
                part.load(partTag)
                parts += part
            }
        }

        if (parts.isEmpty) return null

        val tmb = MultipartGenerator.generateCompositeTile(null, parts, false)
        tmb.readFromNBT(tag)
        tmb.loadParts(parts)
        tmb
    }

    /**
     * Drops an item around pos
     */
    //TODO CCL
    def dropItem(stack: ItemStack, world: World, pos: Vector3) {
        val item = new EntityItem(world, pos.x, pos.y, pos.z, stack)
        item.motionX = world.rand.nextGaussian() * 0.05
        item.motionY = world.rand.nextGaussian() * 0.05 + 0.2
        item.motionZ = world.rand.nextGaussian() * 0.05
        item.setPickupDelay(10)
        world.spawnEntity(item)
    }
}
