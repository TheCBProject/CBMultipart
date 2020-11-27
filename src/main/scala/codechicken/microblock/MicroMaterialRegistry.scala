package codechicken.microblock

import codechicken.lib.util.SneakyUtils.unsafeCast
import codechicken.microblock.api.MicroMaterial
import codechicken.microblock.handler.MicroblockMod
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.{BlockRayTraceResult, RayTraceResult}
import net.minecraft.util.{Hand, ResourceLocation}
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.{ForgeRegistries, ForgeRegistry, RegistryBuilder}

import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._


/**
 * Interface for overriding the default micro placement highlight handler to show the effect of placement on a certain block/part
 */
trait IMicroHighlightRenderer {
    /**
     * Return true if a custom highlight was rendered and the default should be skipped
     */
    def renderHighlight(player: PlayerEntity, hand: Hand, hit: RayTraceResult, mcrFactory: CommonMicroFactory, size: Int, material: Int, mStack: MatrixStack, getter: IRenderTypeBuffer, partialTicks: Float): Boolean
}

object MicroMaterialRegistry {

    var MICRO_MATERIALS: ForgeRegistry[MicroMaterial] = _

    private val highlightRenderers = ListBuffer[IMicroHighlightRenderer]()
    private lazy val maxCuttingStrength = ForgeRegistries.ITEMS.iterator.asScala.flatMap {
        case saw: Saw => Some(saw.getMaxCuttingStrength)
        case _ => None
    }.max

    private var iconsLoaded = false

    private[microblock] def init(eventBus: IEventBus) {
        eventBus.addListener(createRegistries)
    }

    private def createRegistries(event: RegistryEvent.NewRegistry) {
        MICRO_MATERIALS = unsafeCast(
            new RegistryBuilder[MicroMaterial]
                .setName(new ResourceLocation(MicroblockMod.modId, "micro_materials"))
                .setType(classOf[MicroMaterial])
                .disableSaving()
                .create()
        )
    }

    /**
     * Registers a highlight renderer
     */
    def registerHighlightRenderer(handler: IMicroHighlightRenderer) {
        highlightRenderers += handler
    }

    def markIconReload() {
        iconsLoaded = false
    }

    private[microblock] def loadIcons() {
        if (!iconsLoaded) {
            MICRO_MATERIALS.forEach(e => e.loadIcons())
            iconsLoaded = true
        }
    }

    def getMaxCuttingStrength = maxCuttingStrength

    def getMaterial(id: Int): MicroMaterial = MICRO_MATERIALS.getValue(id)

    def getMaterial(name: String): MicroMaterial = getMaterial(new ResourceLocation(name))

    def getMaterial(name: ResourceLocation): MicroMaterial = MICRO_MATERIALS.getValue(name)

    def getMaterialID(name: String): Int = getMaterialID(new ResourceLocation(name))

    def getMaterialID(name: ResourceLocation): Int = MICRO_MATERIALS.getID(name)

    def getMaterialName(id: Int): ResourceLocation = MICRO_MATERIALS.getValue(id) match {
        case mat: MicroMaterial => mat.getRegistryName
        case _ => null //TODO?
    }

    def renderHighlight(player: PlayerEntity, hand: Hand, hit: BlockRayTraceResult, mcrClass: CommonMicroFactory, size: Int, material: Int, mStack: MatrixStack, getter: IRenderTypeBuffer, partialTicks: Float): Boolean = {
        val overridden = highlightRenderers.find(_.renderHighlight(player, hand, hit, mcrClass, size, material, mStack, getter, partialTicks))
        if (overridden.isDefined) {
            return true
        }

        MicroblockRender.renderHighlight(player, hand, hit, mcrClass, size, material, mStack, getter, partialTicks)
        true
    }
}
