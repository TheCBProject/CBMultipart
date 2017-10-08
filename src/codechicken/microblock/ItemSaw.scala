package codechicken.microblock

import codechicken.lib.config.ConfigTag
import codechicken.lib.render._
import codechicken.lib.render.item.IItemRenderer
import codechicken.lib.texture.TextureUtils
import codechicken.lib.util.TransformUtils
import codechicken.lib.vec.uv.UVTranslation
import codechicken.lib.vec.SwapYZ
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.model.IModelState
import org.lwjgl.opengl.GL11

/**
 * Interface for items that are 'saws'
 */
trait Saw extends Item
{
    /**
     * The maximum harvest level that some version of this saw is capable of cutting
     */
    def getMaxCuttingStrength:Int = getCuttingStrength(new ItemStack(this))
    /**
     * The harvest level this saw is capable of cutting
     */
    def getCuttingStrength(item:ItemStack):Int
}

class ItemSaw(sawTag:ConfigTag, val harvestLevel:Int) extends Item with Saw
{
    {
        val maxDamage = sawTag.getTag("durability").getIntValue(1<<harvestLevel+8)
        if(maxDamage > 0)
            setMaxDamage(maxDamage)
        setNoRepair()
        setMaxStackSize(1)
        setCreativeTab(CreativeTabs.TOOLS)
    }

    override def hasContainerItem = true

    override def getContainerItem(stack:ItemStack) =
        if(isDamageable)
            new ItemStack(stack.getItem, 1, stack.getItemDamage+1)
        else
            stack

    def getCuttingStrength(item:ItemStack) = harvestLevel
}

object ItemSawRenderer extends IItemRenderer
{
    val models = OBJParser.parseModels(new ResourceLocation("microblockcbe", "models/saw.obj"), 7, new SwapYZ())
    val handle = models.get("Handle")
    val holder = models.get("BladeSupport")
    val blade = models.get("Blade")

//    def handleRenderType(item:ItemStack, renderType:ItemRenderType) =
//        !MicroblockProxy.useSawIcons || TextureUtils.isMissing(item.getIconIndex, TextureMap.locationItemsTexture)
//
//    def shouldUseRenderHelper(renderType:ItemRenderType, item:ItemStack, helper:ItemRendererHelper) = true

    override def isAmbientOcclusion = true
    override def isGui3d = true
    override def getTransforms: IModelState = TransformUtils.DEFAULT_BLOCK
    override def renderItem(item:ItemStack, transformType: TransformType)
    {
//        val t = renderType match {
//            case INVENTORY => new TransformationList(new Scale(1.8), new Translation(0, 0, -0.6), new Rotation(-pi/4, 1, 0, 0), new Rotation(pi*3/4, 0, 1, 0))
//            case ENTITY => new TransformationList(new Scale(1), new Translation(0, 0, -0.25), new Rotation(-pi/4, 1, 0, 0))
//            case EQUIPPED_FIRST_PERSON => new TransformationList(new Scale(1.5), new Rotation(-pi/3, 1, 0, 0), new Rotation(pi*3/4, 0, 1, 0), new Translation(0.5, 0.5, 0.5))
//            case EQUIPPED => new TransformationList(new Scale(1.5), new Rotation(-pi/5, 1, 0, 0), new Rotation(-pi*3/4, 0, 1, 0), new Translation(0.75, 0.5, 0.75))
//            case _ => return
//        }
        val ccrs = CCRenderState.instance()
        ccrs.reset()
        //CCRenderState.useNormals = true
        ccrs.pullLightmap()
        TextureUtils.changeTexture("microblock:textures/items/saw.png")
        ccrs.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.ITEM)
        handle.render(ccrs)//(t)
        holder.render(ccrs)//(t)
        ccrs.draw()
        GL11.glDisable(GL11.GL_CULL_FACE)
        ccrs.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.ITEM)
        blade.render(ccrs,/**t, **/new UVTranslation(0, (item.getItem.asInstanceOf[Saw].getCuttingStrength(item)-1)*4/64D))
        ccrs.draw()
        GL11.glEnable(GL11.GL_CULL_FACE)
    }

}
