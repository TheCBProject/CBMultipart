package codechicken.microblock

import java.util.{List => JList}

import codechicken.lib.raytracer.RayTracer
import codechicken.lib.render.CCRenderState
import codechicken.lib.render.item.IItemRenderer
import codechicken.lib.texture.TextureUtils
import codechicken.lib.util.TransformUtils
import codechicken.lib.vec.Vector3
import codechicken.microblock.CommonMicroFactory._
import codechicken.microblock.ItemMicroPart._
import codechicken.microblock.handler.MicroblockProxy
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util._
import net.minecraft.util.math.{BlockPos, RayTraceResult}
import net.minecraft.util.text.translation.I18n
import net.minecraft.world.World
import net.minecraftforge.common.model.IModelState
import org.lwjgl.opengl.GL11

class ItemMicroPart extends Item {
    setUnlocalizedName("microblock")
    setHasSubtypes(true)

    override def getItemStackDisplayName(stack: ItemStack): String = {
        val material = getMaterial(stack)
        val mcrFactory = getFactory(stack)
        val size = getSize(stack)
        if (material == null || mcrFactory == null) {
            return "Unnamed"
        }

        I18n.translateToLocalFormatted(mcrFactory.getName.getResourcePath + "." + size + ".name", material.getLocalizedName)
    }

    override def getSubItems(tab: CreativeTabs, list: NonNullList[ItemStack]) {
        if (MicroMaterialRegistry.getIdMap != null && isInCreativeTab(tab)) {
            for (factoryID <- factories.indices) {
                val factory = factories(factoryID)
                if (factory != null) {
                    for (size <- Seq(1, 2, 4))
                        MicroMaterialRegistry.getIdMap.foreach(e => list.add(create(factoryID, size, e._1)))
                }
            }
        }
    }

    override def onItemUse(player: EntityPlayer, world: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult = {
        val stack = player.getHeldItem(hand)
        val material = getMaterialID(stack)
        val mcrFactory = getFactory(stack)
        val size = getSize(stack)
        if (material < 0 || mcrFactory == null) {
            return EnumActionResult.FAIL
        }

        val hit = RayTracer.retraceBlock(world, player, pos)
        if (hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK) {
            val placement = MicroblockPlacement(player, hit, size, material, !player.capabilities.isCreativeMode, mcrFactory.placementProperties)
            if (placement == null) {
                return EnumActionResult.FAIL
            }

            if (!world.isRemote) {
                placement.place(world, player, stack)
                if (!player.capabilities.isCreativeMode) {
                    placement.consume(world, player, stack)
                }
                val sound = MicroMaterialRegistry.getMaterial(material).getSound
                if (sound != null) {
                    world.playSound(null, placement.pos.getX + 0.5D, placement.pos.getY + 0.5D, placement.pos.getZ + 0.5D, sound.getPlaceSound, SoundCategory.BLOCKS, (sound.getVolume + 1.0F) / 2.0F, sound.getPitch * 0.8F)
                }

            }

            return EnumActionResult.SUCCESS
        }

        EnumActionResult.FAIL
    }
}

object ItemMicroPart {
    def checkTagCompound(stack: ItemStack) {
        if (!stack.hasTagCompound) {
            stack.setTagCompound(new NBTTagCompound())
        }
    }

    /**
     * Creates an ItemStack damage by packing the following information:
     *
     * @param factoryID The id of the factory for this part.
     * @param size      The size of this microblock. Valid values are 1, 2, and 4 (representing 1/8th, 2/8th, and 4/8th respectively)
     * @return The packed damage value
     */
    def damage(factoryID: Int, size: Int): Int = factoryID << 8 | size & 0xFF

    /**
     * Unpacks the damage value from the ItemStack and returns the factory ID
     */
    def factoryID(damage: Int) = damage >> 8

    /**
     * Unpacks the damage value from the ItemStack and returns the size in eigths
     */
    def size(damage: Int) = damage & 0xFF

    def create(factoryID: Int, size: Int, material: Int): ItemStack = create(damage(factoryID, size), material)

    def create(factoryID: Int, size: Int, material: String): ItemStack = create(damage(factoryID, size), material)

    def create(damage: Int, material: Int): ItemStack = create(damage, MicroMaterialRegistry.materialName(material))

    def create(damage: Int, material: String): ItemStack = createStack(1, damage, material)

    def createStack(amount: Int, damage: Int, material: String): ItemStack = {
        val stack = new ItemStack(MicroblockProxy.itemMicro, amount, damage)
        checkTagCompound(stack)
        stack.getTagCompound.setString("mat", material)
        stack
    }

    /** Itemstack getters **/

    def getFactoryID(stack: ItemStack): Int = factoryID(stack.getItemDamage)

    def getFactory(stack: ItemStack) = CommonMicroFactory.factories(getFactoryID(stack))

    def getSize(stack: ItemStack): Int = size(stack.getItemDamage)

    def getMaterialID(stack: ItemStack): Int = {
        checkTagCompound(stack)
        if (!stack.getTagCompound.hasKey("mat")) {
            return 0
        }

        MicroMaterialRegistry.materialID(stack.getTagCompound.getString("mat"))
    }

    def getMaterial(stack: ItemStack): IMicroMaterial = {
        checkTagCompound(stack)
        if (!stack.getTagCompound.hasKey("mat")) {
            return null
        }

        MicroMaterialRegistry.getMaterial(stack.getTagCompound.getString("mat"))
    }
}

object ItemMicroPartRenderer extends IItemRenderer {
    val modelResLoc = new ModelResourceLocation("forgemicroblocks:microblock", "inventory")

    override def isAmbientOcclusion = true

    override def isGui3d = true

    override def getTransforms: IModelState = TransformUtils.DEFAULT_BLOCK

    override def renderItem(item: ItemStack, transformType: TransformType) {
        val material = getMaterial(item)
        val factory = getFactory(item)
        val size = getSize(item)
        if (material == null || factory == null) {
            return
        }

        TextureUtils.bindBlockTexture()
        val ccrs = CCRenderState.instance()
        ccrs.reset()
        ccrs.pullLightmap()
        ccrs.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.ITEM)

        val part = factory.create(true, getMaterialID(item)).asInstanceOf[MicroblockClient]
        part.setShape(size, factory.itemSlot)
        part.render(new Vector3(0.5, 0.5, 0.5).subtract(part.getBounds.center), null, ccrs)

        ccrs.draw()
    }

    def renderHighlight(player: EntityPlayer, stack: ItemStack, hit: RayTraceResult): Boolean = {
        val material = getMaterialID(stack)
        val mcrClass = getFactory(stack)
        val size = getSize(stack)

        if (material < 0 || mcrClass == null) {
            return false
        }

        MicroMaterialRegistry.renderHighlight(player, hit, mcrClass, size, material)
    }
}
