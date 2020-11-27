package codechicken.microblock

import java.util.Collections

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.lib.raytracer.VoxelShapeCache
import codechicken.lib.render.CCRenderState
import codechicken.lib.vec.{Cuboid6, Vector3}
import codechicken.microblock.MicroMaterialRegistry._
import codechicken.microblock.api.{TMicroOcclusion, TMicroOcclusionClient}
import codechicken.multipart._
import codechicken.multipart.api.part.{TIconHitEffectsPart, TMultiPart, TPartialOcclusionPart, TSlottedPart}
import codechicken.multipart.util.PartRayTraceResult
import net.minecraft.client.renderer.RenderType
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.client.model.ModelLoader

import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._

abstract class Microblock(var material: Int = 0) extends TMultiPart //with TCuboidPart
{
    var shape: Byte = 0

    def microFactory: MicroblockFactory

    def getType = microFactory.getType

    override def getStrength(player: PlayerEntity, hit: PartRayTraceResult) = getIMaterial match {
        case null => super.getStrength(player, hit)
        case mat => mat.getStrength(player)
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

    def getIMaterial = MicroMaterialRegistry.getMaterial(material)

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
                items += ItemMicroBlock.createStack(m, itemFactoryID, s, getMaterialName(material))
            }
        }
        items.asJava
    }

    override def pickItem(hit: PartRayTraceResult): ItemStack = {
        val size = getSize
        for (s <- Seq(4, 2, 1))
            if (size % s == 0 && size / s >= 1) {
                return ItemMicroBlock.create(itemFactoryID, size, getMaterialName(material))
            }

        ItemStack.EMPTY //unreachable
    }

    override def writeDesc(packet: MCDataOutput) {
        packet.writeVarInt(material) //read by IDynamicPartFactory
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
        tag.putString("material", getMaterialName(material).toString)
    }

    override def load(tag: CompoundNBT) {
        shape = tag.getByte("shape")
        material = getMaterialID(tag.getString("material"))
    }

    def isTransparent = getIMaterial.isTransparent

    override def getLightValue = getIMaterial.getLightValue

    override def getExplosionResistance(entity: Entity) = getIMaterial.explosionResistance(entity) * microFactory.getResistanceFactor
}

trait MicroblockClient extends Microblock with TIconHitEffectsPart {
    @OnlyIn(Dist.CLIENT)
    override def getBreakingIcon(hit: PartRayTraceResult) = getBrokenIcon(hit.getFace.ordinal)

    @OnlyIn(Dist.CLIENT)
    def getBrokenIcon(side: Int) = getIMaterial match {
        case null => ModelLoader.White.instance()
        case mat => mat.getBreakingIcon(side)
    }

    override def renderStatic(pos: Vector3, layer: RenderType, ccrs: CCRenderState) = {
        if (layer != null && getIMaterial.canRenderInLayer(layer)) {
            render(pos, layer, ccrs)
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
    def render(pos: Vector3, layer: RenderType, ccrs: CCRenderState): Unit
}

trait CommonMicroblockClient extends CommonMicroblock with MicroblockClient with TMicroOcclusionClient {
    override def render(pos: Vector3, layer: RenderType, ccrs: CCRenderState) {
        if (layer == null) {
            MicroblockRender.renderCuboid(pos, ccrs, getIMaterial, layer, getBounds, 0)
        } else {
            MicroblockRender.renderCuboid(pos, ccrs, getIMaterial, layer, renderBounds, renderMask)
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
