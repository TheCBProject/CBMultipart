package codechicken.microblock.api

import codechicken.lib.render.pipeline.IVertexOperation
import codechicken.lib.vec.{Cuboid6, Vector3}
import codechicken.microblock.{MicroBlockGenerator, Microblock}
import net.minecraft.block.SoundType
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.{Explosion, IWorldReader}
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.InterModComms
import net.minecraftforge.registries.{ForgeRegistryEntry, IForgeRegistry}

/**
 * A MicroMaterial! Used to define the type of a Microblock, can control various aspects
 * of how the MicroMaterial behaves. MixinTraits are supported, Please see [[MicroBlockGenerator]]
 * if you require advanced functionality on the base [[codechicken.multipart.api.part.TMultiPart]] instance.
 *
 * To Register a [[MicroMaterial]] simply subscribe to the [[RegistryEvent.Register]] event with the generic if [[MicroMaterial]],
 * A default implementation of [[MicroMaterial]], [[BlockMicroMaterial]] exits for your convenience.
 * RegistryNames for [[MicroMaterial]] instances should follow the format laid out in [[BlockMicroMaterial.makeMaterialKey()]],
 * registry overrides are supported by [[MicroMaterial]], so you can feel free to add extra functionality at will.
 *
 *
 * TODO, Many of these methods need extra context for forge overloaded methods.
 * Interface for defining a micro material
 */
abstract class MicroMaterial extends ForgeRegistryEntry[MicroMaterial] {

    /**
     * The icon to be used for breaking particles on side
     */
    @OnlyIn(Dist.CLIENT)
    def getBreakingIcon(side: Int): TextureAtlasSprite

    /**
     * Callback to load icons from the underlying block/etc
     */
    @OnlyIn(Dist.CLIENT)
    def loadIcons() {}

    /**
     * This function must return a list of vertex operations, one set for each
     * rendering pass on the preloaded model.
     *
     * @param side   The side that is being rendered as EnumFacing indexes
     * @param layer  The current render layer, null for inventory rendering
     * @param bounds The cuboid bounds of the face being rendered
     */
    @OnlyIn(Dist.CLIENT)
    def getMicroRenderOps(side: Int, layer: RenderType, bounds: Cuboid6): Seq[Seq[IVertexOperation]]

    /**
     * Get the render pass for which this material renders in.
     */
    def canRenderInLayer(layer: RenderType) = layer == RenderType.getSolid


    /**
     * Gets the render pass for which this material renders as an item.
     */
    def getRenderLayer: RenderType = RenderType.getSolid

    /**
     * Return true if this material is not opaque (glass, ice).
     */
    def isTransparent: Boolean

    /**
     * Return the light level emitted by this material (glowstone)
     */
    def getLightValue: Int

    /**
     * Return the strength of this material
     */
    def getStrength(player: PlayerEntity): Float

    /**
     * Return the localised name of this material (normally the block name)
     */
    def getLocalizedName: ITextComponent

    /**
     * Get the item that this material is cut from (full block -> slabs)
     */
    def getItem: ItemStack

    /**
     * Get the strength of saw requried to cut this material
     */
    def getCutterStrength: Int

    /**
     * Get the breaking/walking sound
     */
    def getSound: SoundType

    /**
     * Get the explosion resistance of this part to an explosion caused by entity
     */
    def explosionResistance(world:IWorldReader, pos:BlockPos, entity: Entity, explosion: Explosion): Float
}
