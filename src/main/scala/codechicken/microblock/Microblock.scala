package codechicken.microblock

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.lib.raytracer.CuboidRayTraceResult
import codechicken.lib.render.CCRenderState
import codechicken.lib.vec.Vector3
import codechicken.microblock.MicroMaterialRegistry._
import codechicken.multipart._
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.BlockRenderLayer
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

abstract class Microblock(var material: Int = 0) extends TMultiPart with TCuboidPart {
    var shape: Byte = 0

    def microFactory: MicroblockFactory

    def getType = microFactory.getName

    override def getStrength(player: EntityPlayer, hit: CuboidRayTraceResult) = getIMaterial match {
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
                items += ItemMicroPart.createStack(m, ItemMicroPart.damage(itemFactoryID, s), MicroMaterialRegistry.materialName(material))
            }
        }
        items
    }

    override def pickItem(hit: CuboidRayTraceResult): ItemStack = {
        val size = getSize
        for (s <- Seq(4, 2, 1))
            if (size % s == 0 && size / s >= 1) {
                return ItemMicroPart.create(itemFactoryID, size, MicroMaterialRegistry.materialName(material))
            }

        null //unreachable
    }

    override def writeDesc(packet: MCDataOutput) {
        writeMaterialID(packet, material) //read by IDynamicPartFactory
        packet.writeByte(shape)
    }

    override def readDesc(packet: MCDataInput) {
        //matrialID writtin in writeDesc is read by the factory, no need to read it here
        shape = packet.readByte
    }

    def sendShapeUpdate() {
        getWriteStream.writeByte(shape)
    }

    override def read(packet: MCDataInput) {
        super.read(packet)
        tile.notifyPartChange(this)
    }

    override def save(tag: NBTTagCompound) {
        tag.setByte("shape", shape)
        tag.setString("material", materialName(material))
    }

    override def load(tag: NBTTagCompound) {
        shape = tag.getByte("shape")
        material = materialID(tag.getString("material"))
    }

    def isTransparent = getIMaterial.isTransparent

    override def getLightValue = getIMaterial.getLightValue

    override def getExplosionResistance(entity: Entity) = getIMaterial.explosionResistance(entity) * microFactory.getResistanceFactor
}

trait MicroblockClient extends Microblock with TIconHitEffectsPart with IMicroMaterialRender {
    @SideOnly(Side.CLIENT)
    override def getBreakingIcon(hit: CuboidRayTraceResult) = getBrokenIcon(hit.sideHit.ordinal)

    @SideOnly(Side.CLIENT)
    def getBrokenIcon(side: Int) = getIMaterial match {
        case null => ModelLoader.White.INSTANCE
        case mat => mat.getBreakingIcon(side)
    }

    override def renderStatic(pos: Vector3, layer: BlockRenderLayer, ccrs: CCRenderState) = {
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
    def render(pos: Vector3, layer: BlockRenderLayer, ccrs: CCRenderState)

    override def getRenderBounds = getBounds
}

trait CommonMicroblockClient extends CommonMicroblock with MicroblockClient with TMicroOcclusionClient {
    override def render(pos: Vector3, layer: BlockRenderLayer, ccrs: CCRenderState) {
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

    def getPartialOcclusionBoxes = Seq(getBounds)

    override def itemFactoryID = microFactory.getFactoryID
}
