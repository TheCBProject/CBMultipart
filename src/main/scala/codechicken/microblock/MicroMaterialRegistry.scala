package codechicken.microblock

import codechicken.lib.util.SneakyUtils.unsafeCast
import codechicken.microblock.api.MicroMaterial
import codechicken.microblock.handler.MicroblockMod
import codechicken.microblock.handler.MicroblockModContent.itemMicroBlock
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.{Hand, ResourceLocation}
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.{ForgeRegistries, ForgeRegistry, RegistryBuilder}

import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._


/**
 * Interface for overriding the default micro placement highlight handler to show the effect of placement on a certain block/part
 */
// TODO 1.17 Move to API package.
trait IMicroHighlightRenderer {
    /**
     * Return true if a custom highlight was rendered and the default should be skipped
     */
    def renderHighlight(player: PlayerEntity, hand: Hand, hit: BlockRayTraceResult, mcrFactory: CommonMicroFactory, size: Int, material: MicroMaterial, mStack: MatrixStack, getter: IRenderTypeBuffer, partialTicks: Float): Boolean
}

object MicroMaterialRegistry {

    var MICRO_MATERIALS: ForgeRegistry[MicroMaterial] = _

    private val highlightRenderers = ListBuffer[IMicroHighlightRenderer]()
    private lazy val maxCuttingStrength = ForgeRegistries.ITEMS.iterator.asScala.flatMap {
        case saw: Saw => Some(saw.getMaxCuttingStrength)
        case _ => None
    }.max

    private var iconsLoaded = false

    private[microblock] def init(eventBus: IEventBus): Unit = {
        eventBus.addListener(createRegistries)
    }

    private def createRegistries(event: RegistryEvent.NewRegistry): Unit = {
        MICRO_MATERIALS = unsafeCast(
            new RegistryBuilder[MicroMaterial]
                .setName(new ResourceLocation(MicroblockMod.modId, "micro_materials"))
                .setType(classOf[MicroMaterial])
                .disableSaving()
                .allowModification()
                .create()
        )
    }

    /**
     * Registers a highlight renderer
     */
    def registerHighlightRenderer(handler: IMicroHighlightRenderer): Unit = {
        highlightRenderers += handler
    }

    def markIconReload(): Unit = {
        iconsLoaded = false
    }

    private[microblock] def loadIcons(): Unit = {
        if (!iconsLoaded) {
            MICRO_MATERIALS.forEach(e => e.loadIcons())
            iconsLoaded = true
        }
    }

    def getMaxCuttingStrength = maxCuttingStrength

    def getMaterial(name: String): MicroMaterial = getMaterial(new ResourceLocation(name))

    def getMaterial(name: ResourceLocation): MicroMaterial = MICRO_MATERIALS.getValue(name)

    /**
     * Gets the [[MicroMaterial]] from the given ItemStack.
     * If the stack is a [[ItemMicroBlock]] its material will be returned
     * in all other cases attempts to lookup the material from the registry.
     *
     * @param item The [[ItemStack]] to get the material from.
     * @return The material.
     */
    def microMaterial(item: ItemStack) =
        if (item.getItem == itemMicroBlock) {
            ItemMicroBlock.getMaterialFromStack(item)
        } else {
            findMaterial(item)
        }

    /**
     * Gets the MicroFactory ID from the given ItemStack.
     * If the stack is a [[ItemMicroBlock]], its factory is returned
     * in call other cases 0 is returned.
     *
     * @param item The [[ItemStack]] to get the micro factory from.
     * @return The factory id or 0.
     */
    def microFactory(item: ItemStack) =
        if (item.getItem == itemMicroBlock) {
            ItemMicroBlock.getFactoryID(item)
        } else {
            0
        }

    /**
     * Gets the Micro size from the given ItemStack.
     * If the stack is a [[ItemMicroBlock]], its size is returned
     * in call other cases 0 is returned.
     *
     * @param item The [[ItemStack]] to get the micro size from.
     * @return The size or 0.
     */
    def microSize(item: ItemStack) =
        if (item.getItem == itemMicroBlock) {
            ItemMicroBlock.getSize(item)
        } else {
            8
        }

    /**
     * Attempts to find a [[MicroMaterial]] for the given [[ItemStack]]
     *
     * @param item The [[ItemStack]] to find the material for.
     * @return The material or null.
     */
    def findMaterial(item: ItemStack): MicroMaterial =
        MicroMaterialRegistry.MICRO_MATERIALS.asScala.find { m =>
            val mitem = m.getItem
            item.getItem == mitem.getItem &&
                ItemStack.tagMatches(item, mitem)
        }.orNull


    def renderHighlight(player: PlayerEntity, hand: Hand, hit: BlockRayTraceResult, mcrClass: CommonMicroFactory, size: Int, material: MicroMaterial, mStack: MatrixStack, getter: IRenderTypeBuffer, partialTicks: Float): Boolean = {
        val overridden = highlightRenderers.find(_.renderHighlight(player, hand, hit, mcrClass, size, material, mStack, getter, partialTicks))
        if (overridden.isDefined) {
            return true
        }

        MicroblockRender.renderHighlight(player, hand, hit, mcrClass, size, material, mStack, getter, partialTicks)
        true
    }
}
