package codechicken.microblock

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.lib.raytracer.VoxelShapeCache
import codechicken.lib.render.CCRenderState
import codechicken.lib.vec.Cuboid6
import codechicken.microblock.api.{MicroMaterial, TMicroOcclusion, TMicroOcclusionClient}
import codechicken.multipart.api.part.{TIconHitEffectsPart, TMultiPart, TPartialOcclusionPart, TSlottedPart}
import codechicken.multipart.util.PartRayTraceResult
import net.minecraft.client.renderer.RenderType
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.world.Explosion
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.client.model.ModelLoader

import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._

abstract class Microblock(var material: MicroMaterial) extends TMultiPart {
    var shape: Byte = 0

    def microFactory: MicroblockFactory

    def getType = microFactory.getType

    override def getStrength(player: PlayerEntity, hit: PartRayTraceResult) = getMaterial match {
        case mat => mat.getStrength(player)
        case null => super.getStrength(player, hit)
    }

    def getSize = shape >> 4

    def getShapeSlot = shape & 0xF

    /**
     * General purpose microblock description value. These values are only used by
     * subclass overrides in this class, so they can be whatever you would like.
     *
     * @param size A 28 bit value representing the current size
     * @param slot A 4 bit value representing the current slot
     */
    def setShape(size: Int, slot: Int) {
        shape = (size << 4 | slot).toByte
    }

    def getMaterial = material

    def getBounds: Cuboid6

    /**
     * The factory ID that will be put into the ItemStack damage value
     */
    def itemFactoryID: Int

    override def getDrops = {
        var size = getSize
        val items = ListBuffer[ItemStack]()
        for (s <- Seq(4, 2, 1)) {
            val m = size / s
            size -= m * s
            if (m > 0) {
                items += ItemMicroBlock.createStack(m, itemFactoryID, s, material)
            }
        }
        items.asJava
    }

    override def pickItem(hit: PartRayTraceResult): ItemStack = {
        val size = getSize
        for (s <- Seq(4, 2, 1))
            if (size % s == 0 && size / s >= 1) {
                return ItemMicroBlock.create(itemFactoryID, size, material)
            }

        ItemStack.EMPTY //unreachable
    }

    override def writeDesc(packet: MCDataOutput) {
        packet.writeRegistryIdUnsafe(MicroMaterialRegistry.MICRO_MATERIALS, material)
        packet.writeByte(shape)
    }

    override def readDesc(packet: MCDataInput) {
        //materialID written in writeDesc is read by the factory, no need to read it here
        shape = packet.readByte
    }

    def sendShapeUpdate() {
        sendUpdate(p => p.writeByte(shape))
    }

    override def readUpdate(packet: MCDataInput) {
        super.readUpdate(packet)
        tile.notifyPartChange(this)
    }

    override def save(tag: CompoundNBT) {
        tag.putByte("shape", shape)
        tag.putString("material", material.getRegistryName.toString)
    }

    override def load(tag: CompoundNBT) {
        shape = tag.getByte("shape")
        material = MicroMaterialRegistry.getMaterial(tag.getString("material"))
    }

    def isTransparent = getMaterial.isTransparent

    override def getLightValue = getMaterial.getLightValue

    override def getExplosionResistance(entity: Entity, explosion: Explosion) = getMaterial.explosionResistance(world, pos, entity, explosion) * microFactory.getResistanceFactor
}

trait MicroblockClient extends Microblock with TIconHitEffectsPart {
    @OnlyIn(Dist.CLIENT)
    override def getBreakingIcon(hit: PartRayTraceResult) = getBrokenIcon(hit.getFace.ordinal)

    @OnlyIn(Dist.CLIENT)
    def getBrokenIcon(side: Int) = getMaterial match {
        case mat => mat.getBreakingIcon(side)
        case null => ModelLoader.White.instance()
    }

    override def renderStatic(layer: RenderType, ccrs: CCRenderState) = {
        if (layer == null || getMaterial.canRenderInLayer(layer)) {
            render(layer, ccrs)
            true
        } else {
            false
        }
    }

    /**
     * Called to add the vertecies of this part to the CCRenderState
     *
     * @param pos   The position of this part
     * @param layer The block layer, null for inventory rendering
     * @param ccrs  The CCRenderState to add the verts to
     */
    def render(layer: RenderType, ccrs: CCRenderState): Unit
}

trait CommonMicroblockClient extends CommonMicroblock with MicroblockClient with TMicroOcclusionClient {
    override def render(layer: RenderType, ccrs: CCRenderState) {
        if (layer == null) {
            MicroblockRender.renderCuboid(ccrs, getMaterial, layer, getBounds, 0)
        } else {
            MicroblockRender.renderCuboid(ccrs, getMaterial, layer, renderBounds, renderMask)
        }
    }
}

trait CommonMicroblock extends Microblock with TPartialOcclusionPart with TMicroOcclusion with TSlottedPart {
    def microFactory: CommonMicroFactory

    def getSlot = getShapeSlot

    def getSlotMask = 1 << getSlot

    override def getOutlineShape = VoxelShapeCache.getShape(getBounds)

    override def getPartialOcclusionShape = getOutlineShape

    override def itemFactoryID = microFactory.getFactoryID
}
