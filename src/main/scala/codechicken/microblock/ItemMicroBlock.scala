package codechicken.microblock

import codechicken.lib.raytracer.RayTracer
import codechicken.lib.render.CCRenderState
import codechicken.lib.render.buffer.TransformingVertexBuilder
import codechicken.lib.render.item.IItemRenderer
import codechicken.lib.util.TransformUtils
import codechicken.lib.vec.{Matrix4, Vector3}
import codechicken.microblock.CommonMicroFactory._
import codechicken.microblock.ItemMicroBlock._
import codechicken.microblock.api.MicroMaterial
import codechicken.microblock.handler.MicroblockModContent
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.{Item, ItemGroup, ItemStack, ItemUseContext}
import net.minecraft.util._
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.text.{StringTextComponent, TranslationTextComponent}

class ItemMicroBlock(properties: Item.Properties) extends Item(properties) {

    override def getDisplayName(stack: ItemStack) = {
        val material = getMaterial(stack)
        val mcrFactory = getFactory(stack)
        val size = getSize(stack)
        if (material == null || mcrFactory == null) {
            new StringTextComponent("Unnamed")
        } else {
            new TranslationTextComponent(s"item.${mcrFactory.getType.getRegistryName.toString.replace(":", ".")}.$size", material.getLocalizedName)
        }
    }

    override def fillItemGroup(group: ItemGroup, list: NonNullList[ItemStack]) {
        if (isInGroup(group)) {
            for (factoryID <- factories.indices) {
                val factory = factories(factoryID)
                if (factory != null) {
                    for (size <- Seq(1, 2, 4))
                        MicroMaterialRegistry.MICRO_MATERIALS.forEach(e => list.add(create(factoryID, size, e)))
                }
            }
        }
    }

    override def onItemUse(context: ItemUseContext): ActionResultType = {
        val player = context.getPlayer
        val world = context.getWorld
        val stack = player.getHeldItem(context.getHand)
        val material = getMaterialID(stack)
        val mcrFactory = getFactory(stack)
        val size = getSize(stack)
        if (material < 0 || mcrFactory == null) {
            return ActionResultType.FAIL
        }

        val hit = RayTracer.retraceBlock(world, player, context.getPos)
        if (hit != null) {
            val placement = MicroblockPlacement(player, context.getHand, hit, size, material, !player.abilities.isCreativeMode, mcrFactory.placementProperties)
            if (placement == null) {
                return ActionResultType.FAIL
            }

            if (!world.isRemote) {
                placement.place(world, player, stack)
                if (!player.abilities.isCreativeMode) {
                    placement.consume(world, player, stack)
                }
                val sound = MicroMaterialRegistry.getMaterial(material).getSound
                if (sound != null) {
                    world.playSound(null, placement.pos.getX + 0.5D, placement.pos.getY + 0.5D, placement.pos.getZ + 0.5D, sound.getPlaceSound, SoundCategory.BLOCKS, (sound.getVolume + 1.0F) / 2.0F, sound.getPitch * 0.8F)
                }

            }

            return ActionResultType.SUCCESS
        }

        ActionResultType.FAIL
    }
}

object ItemMicroBlock {

    def create(factoryID: Int, size: Int, material: Int): ItemStack = create(factoryID, size, MicroMaterialRegistry.getMaterialName(material))

    def create(factoryID: Int, size: Int, material: MicroMaterial): ItemStack = create(factoryID, size, material.getRegistryName)

    def create(factoryID: Int, size: Int, material: ResourceLocation): ItemStack = createStack(1, factoryID, size, material)

    def createStack(amount: Int, factoryId: Int, size: Int, material: ResourceLocation): ItemStack = {
        val stack = new ItemStack(MicroblockModContent.itemMicroBlock, amount)
        stack.getOrCreateTag()
        stack.getTag.putInt("factory_id", factoryId)
        stack.getTag.putInt("size", size)
        stack.getTag.putString("mat", material.toString)
        stack
    }

    /** Itemstack getters **/

    def getFactoryID(stack: ItemStack): Int = {
        stack.getOrCreateTag()
        if (!stack.getTag.contains("factory_id")) {
            //Wut..
            logger.error("Found stack with no factory_id tag? {}", stack)
            -2000
        } else {
            stack.getTag.getInt("factory_id")
        }
    }

    def getFactory(stack: ItemStack) = CommonMicroFactory.factories(getFactoryID(stack))

    def getSize(stack: ItemStack): Int = {
        stack.getOrCreateTag()
        if (!stack.getTag.contains("size")) {
            //Wut..
            logger.error("Found stack with no size tag? {}", stack)
            -2000
        } else {
            stack.getTag.getInt("size")
        }
    }

    def getMaterialID(stack: ItemStack): Int = {
        stack.getOrCreateTag()
        if (!stack.getTag.contains("mat")) {
            return 0
        }

        MicroMaterialRegistry.getMaterialID(stack.getTag.getString("mat"))
    }

    def getMaterial(stack: ItemStack): MicroMaterial = {
        stack.getOrCreateTag()
        if (!stack.getTag.contains("mat")) {
            return null
        }

        MicroMaterialRegistry.getMaterial(stack.getTag.getString("mat"))
    }
}

object ItemMicroBlockRenderer extends IItemRenderer {

    override def isAmbientOcclusion = true

    override def isGui3d = true

    override def getTransforms = TransformUtils.DEFAULT_BLOCK

    override def renderItem(stack: ItemStack, transformType: TransformType, mStack: MatrixStack, getter: IRenderTypeBuffer, packedLight: Int, packedOverlay: Int) {
        val material = getMaterial(stack)
        val factory = getFactory(stack)
        val size = getSize(stack)
        if (material == null || factory == null) {
            return
        }
        val mat = new Matrix4(mStack)
        val part = factory.create(true, getMaterialID(stack)).asInstanceOf[MicroblockClient]
        val ccrs = CCRenderState.instance()
        part.setShape(size, factory.itemSlot)
        mat.translate(new Vector3(0.5, 0.5, 0.5).subtract(part.getBounds.center))

        ccrs.reset()
        ccrs.bind(part.getIMaterial.getRenderLayer, getter)
        ccrs.r = new TransformingVertexBuilder(ccrs.r, mat)
        ccrs.brightness = packedLight
        ccrs.overlay = packedOverlay
        part.render(Vector3.ZERO, null, ccrs)
    }

    def renderHighlight(player: PlayerEntity, hand: Hand, stack: ItemStack, hit: BlockRayTraceResult, mStack: MatrixStack, getter: IRenderTypeBuffer, partialTicks: Float): Boolean = {
        val material = getMaterialID(stack)
        val mcrClass = getFactory(stack)
        val size = getSize(stack)

        if (material < 0 || mcrClass == null) {
            return false
        }

        MicroMaterialRegistry.renderHighlight(player, hand, hit, mcrClass, size, material, mStack, getter, partialTicks)
    }

    override def func_230044_c_(): Boolean = true
}
