package codechicken.microblock

import codechicken.lib.config.ConfigTag
import net.minecraft.item.{Item, ItemGroup, ItemStack}

/**
 * Interface for items that are 'saws'
 */
trait Saw extends Item {
    /**
     * The maximum harvest level that some version of this saw is capable of cutting
     */
    def getMaxCuttingStrength: Int = getCuttingStrength(new ItemStack(this))

    /**
     * The harvest level this saw is capable of cutting
     */
    def getCuttingStrength(item: ItemStack): Int
}

class ItemSaw(sawTag: ConfigTag, val harvestLevel: Int) extends Item({
    val maxDamage = sawTag.getTag("durability").setDefaultInt(1 << harvestLevel + 8).getInt
    val prop = new Item.Properties()
        .group(ItemGroup.TOOLS)
        .setNoRepair()
    if (maxDamage > 0) {
        prop.maxDamage(maxDamage)
    }
    prop
}) with Saw {

    override def hasContainerItem(stack: ItemStack) = true

    override def getContainerItem(stack: ItemStack) =
        if (isDamageable) {
            val newStack = new ItemStack(stack.getItem, 1)
            newStack.setDamage(stack.getDamage + 1)
            newStack
        } else {
            stack
        }

    def getCuttingStrength(item: ItemStack) = harvestLevel
}

//object ItemSawRenderer extends IItemRenderer {
//    val models = OBJParser.parseModels(new ResourceLocation("microblockcbe", "models/saw.obj"), 7, new SwapYZ())
//    val handle = models.get("Handle")
//    val holder = models.get("BladeSupport")
//    val blade = models.get("Blade")
//
//    //    def handleRenderType(item:ItemStack, renderType:ItemRenderType) =
//    //        !MicroblockProxy.useSawIcons || TextureUtils.isMissing(item.getIconIndex, TextureMap.locationItemsTexture)
//    //
//    //    def shouldUseRenderHelper(renderType:ItemRenderType, item:ItemStack, helper:ItemRendererHelper) = true
//
//    override def isAmbientOcclusion = true
//
//    override def isGui3d = true
//
//    override def getTransforms = TransformUtils.DEFAULT_BLOCK
//
//    //    override def renderItem(item: ItemStack, transformType: TransformType) {
//    //        //        val t = renderType match {
//    //        //            case INVENTORY => new TransformationList(new Scale(1.8), new Translation(0, 0, -0.6), new Rotation(-pi/4, 1, 0, 0), new Rotation(pi*3/4, 0, 1, 0))
//    //        //            case ENTITY => new TransformationList(new Scale(1), new Translation(0, 0, -0.25), new Rotation(-pi/4, 1, 0, 0))
//    //        //            case EQUIPPED_FIRST_PERSON => new TransformationList(new Scale(1.5), new Rotation(-pi/3, 1, 0, 0), new Rotation(pi*3/4, 0, 1, 0), new Translation(0.5, 0.5, 0.5))
//    //        //            case EQUIPPED => new TransformationList(new Scale(1.5), new Rotation(-pi/5, 1, 0, 0), new Rotation(-pi*3/4, 0, 1, 0), new Translation(0.75, 0.5, 0.75))
//    //        //            case _ => return
//    //        //        }
//    //        val ccrs = CCRenderState.instance()
//    //        ccrs.reset()
//    //        //CCRenderState.useNormals = true
//    //        ccrs.pullLightmap()
//    //        TextureUtils.changeTexture("microblock:textures/items/saw.png")
//    //        ccrs.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.ITEM)
//    //        handle.render(ccrs) //(t)
//    //        holder.render(ccrs) //(t)
//    //        ccrs.draw()
//    //        GL11.glDisable(GL11.GL_CULL_FACE)
//    //        ccrs.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.ITEM)
//    //        blade.render(ccrs,
//    //
//    //            /** t, **/
//    //            new UVTranslation(0, (item.getItem.asInstanceOf[Saw].getCuttingStrength(item) - 1) * 4 / 64D))
//    //        ccrs.draw()
//    //        GL11.glEnable(GL11.GL_CULL_FACE)
//    //    }
//    override def renderItem(stack: ItemStack, transformType: TransformType, mStack: MatrixStack, getter: IRenderTypeBuffer, packedLight: Int, packedOverlay: Int): Unit = ???
//
//    override def func_230044_c_(): Boolean = ???
//}
