package codechicken.microblock

import codechicken.lib.raytracer.RayTracer
import codechicken.lib.render.CCRenderState
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

    override def getName(stack: ItemStack) = {
        val material = getMaterialFromStack(stack)
        val mcrFactory = getFactory(stack)
        val size = getSize(stack)
        if (material == null || mcrFactory == null) {
            new StringTextComponent("Unnamed")
        } else {
            new TranslationTextComponent(s"item.${mcrFactory.getType.getRegistryName.toString.replace(":", ".")}.$size", material.getLocalizedName)
        }
    }

    override def fillItemCategory(group: ItemGroup, list: NonNullList[ItemStack]) {
        if (allowdedIn(group)) {
            for (factoryID <- factories.indices) {
                val factory = factories(factoryID)
                if (factory != null) {
                    for (size <- Seq(1, 2, 4))
                        MicroMaterialRegistry.MICRO_MATERIALS.forEach(e => list.add(create(factoryID, size, e)))
                }
            }
        }
    }

    override def useOn(context: ItemUseContext): ActionResultType = {
        val player = context.getPlayer
        val world = context.getLevel
        val stack = player.getItemInHand(context.getHand)
        val material = getMaterialFromStack(stack)
        val mcrFactory = getFactory(stack)
        val size = getSize(stack)
        if (material == null || mcrFactory == null) {
            return ActionResultType.FAIL
        }

        val hit = RayTracer.retraceBlock(world, player, context.getClickedPos)
        if (hit != null) {
            val placement = MicroblockPlacement(player, context.getHand, hit, size, material, !player.abilities.instabuild, mcrFactory.placementProperties)
            if (placement == null) {
                return ActionResultType.FAIL
            }

            if (!world.isClientSide) {
                placement.place(world, player, stack)
                if (!player.abilities.instabuild) {
                    placement.consume(world, player, stack)
                }
                val sound = material.getSound
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

    def create(factoryID: Int, size: Int, material: MicroMaterial): ItemStack = create(factoryID, size, material.getRegistryName)

    def create(factoryID: Int, size: Int, material: ResourceLocation): ItemStack = createStack(1, factoryID, size, material)

    def createStack(amount: Int, factoryId: Int, size: Int, material: MicroMaterial): ItemStack = createStack(amount, factoryId, size, material.getRegistryName)

    def createStack(amount: Int, factoryId: Int, size: Int, material: ResourceLocation): ItemStack = {
        val stack = new ItemStack(MicroblockModContent.itemMicroBlock, amount)
        stack.getOrCreateTag()
        stack.getTag.putInt("factory_id", factoryId)
        stack.getTag.putInt("size", size)
        stack.getTag.putString("mat", material.toString)
        stack
    }

    /** Itemstack getters * */

    def getFactoryID(stack: ItemStack): Int = {
        stack.getOrCreateTag()
        if (!stack.getTag.contains("factory_id")) {
            //Wut..
            -2000
        } else {
            stack.getTag.getInt("factory_id")
        }
    }

    def getFactory(stack: ItemStack): CommonMicroFactory =  {
        val factoryId = getFactoryID(stack)
        if (factoryId < 0 || factoryId > CommonMicroFactory.factories.length) {
            return null
        }
        CommonMicroFactory.factories(factoryId)
    }

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

    def getMaterialFromStack(stack: ItemStack): MicroMaterial = {
        stack.getOrCreateTag()
        if (!stack.getTag.contains("mat")) {
            return null
        }

        MicroMaterialRegistry.getMaterial(stack.getTag.getString("mat"))
    }
}

object ItemMicroBlockRenderer extends IItemRenderer {

    override def useAmbientOcclusion = true

    override def isGui3d = true

    override def getModelTransform = TransformUtils.DEFAULT_BLOCK

    override def renderItem(stack: ItemStack, transformType: TransformType, mStack: MatrixStack, buffers: IRenderTypeBuffer, packedLight: Int, packedOverlay: Int) {
        val material = getMaterialFromStack(stack)
        val factory = getFactory(stack)
        val size = getSize(stack)
        if (material == null || factory == null) {
            return
        }
        val part = factory.create(true, getMaterialFromStack(stack)).asInstanceOf[MicroblockClient]
        part.setShape(size, factory.itemSlot)

        val mat = new Matrix4(mStack)
        mat.translate(new Vector3(0.5, 0.5, 0.5).subtract(part.getBounds.center))

        val ccrs = CCRenderState.instance()
        ccrs.reset()
        ccrs.brightness = packedLight
        ccrs.overlay = packedOverlay

        if (material.renderItem(ccrs, stack, transformType, mStack, buffers, mat, part)) return

        ccrs.bind(part.getMaterial.getRenderLayer, buffers, mat)
        part.render(null, ccrs)
    }

    def renderHighlight(player: PlayerEntity, hand: Hand, stack: ItemStack, hit: BlockRayTraceResult, mStack: MatrixStack, getter: IRenderTypeBuffer, partialTicks: Float): Boolean = {
        val material = getMaterialFromStack(stack)
        val mcrClass = getFactory(stack)
        val size = getSize(stack)

        if (material == null || mcrClass == null) {
            return false
        }

        MicroMaterialRegistry.renderHighlight(player, hand, hit, mcrClass, size, material, mStack, getter, partialTicks)
    }

    override def usesBlockLight(): Boolean = false
}
