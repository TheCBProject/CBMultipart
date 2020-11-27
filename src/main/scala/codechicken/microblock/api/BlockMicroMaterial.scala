package codechicken.microblock.api

import java.util.Random

import codechicken.lib.render.CCRenderState
import codechicken.lib.render.pipeline.IVertexOperation
import codechicken.lib.texture.TextureUtils
import codechicken.lib.util.SneakyUtils.unsafeCast
import codechicken.lib.vec.uv.{IconTransformation, MultiIconTransformation}
import codechicken.lib.vec.{Cuboid6, Vector3}
import codechicken.microblock.{MaterialRenderHelper, logger}
import net.minecraft.block.{Block, BlockState}
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.{RenderType, RenderTypeLookup}
import net.minecraft.client.renderer.model.BakedQuad
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{Direction, ResourceLocation}
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._

/**
 * Standard micro material class suitable for most blocks.
 */
class BlockMicroMaterial(val state: BlockState) extends MicroMaterial {
    setRegistryName(BlockMicroMaterial.makeMaterialKey(state))

    @OnlyIn(Dist.CLIENT)
    var icont: MultiIconTransformation = _

    @OnlyIn(Dist.CLIENT)
    var pIconT: IconTransformation = _

    @OnlyIn(Dist.CLIENT)
    override def loadIcons(): Unit = {
        @OnlyIn(Dist.CLIENT)
        def getSideIcon(state: BlockState, s: Int): TextureAtlasSprite = {
            val side = Direction.BY_INDEX(s)
            val model = Minecraft.getInstance.getBlockRendererDispatcher.getModelForState(state)
            var winner = if (model.getParticleTexture == null) TextureUtils.getMissingSprite else model.getParticleTexture
            if (model != null) {
                val quads = new ListBuffer[BakedQuad]
                quads.addAll(model.getQuads(state, side, new Random(0)).asScala)
                quads.addAll(model.getQuads(state, null, new Random(0)).asScala.filter((quad: BakedQuad) => quad.getFace eq side))
                if (quads.nonEmpty) {
                    val list = new ListBuffer[TextureAtlasSprite]

                    for (quad <- quads) {
                        val sprite: TextureAtlasSprite = quad.func_187508_a()
                        list += sprite
                    }
                    if (list.nonEmpty) winner = list.head
                }
            }
            winner
        }

        try {
            icont = new MultiIconTransformation(Array.tabulate(6)(side => getSideIcon(state, side)): _*)
            pIconT = new IconTransformation(TextureUtils.getParticleIconForBlock(state))
        } catch {
            case e: RuntimeException =>
                logger.error(s"unable to load microblock icons for block ${state.getBlock} with state $state")
        }
    }

    override def getMicroRenderOps(pos: Vector3, side: Int, layer: RenderType, bounds: Cuboid6): Seq[Seq[IVertexOperation]] = {
        Seq(MaterialRenderHelper.instance.start(layer, icont).blockColour(getColour(layer)).lighting().result())
    }

    def getColour(layer: RenderType) = {
        layer match {
            case null =>
                Minecraft.getInstance.getBlockColors.getColor(state, null, null, 0) << 8 | 0xFF
            case _ =>
                Minecraft.getInstance.getBlockColors.getColor(state,
                    CCRenderState.instance().lightMatrix.access, CCRenderState.instance().lightMatrix.pos, 0) << 8 | 0xFF
        }
    }

    override def canRenderInLayer(layer: RenderType) = RenderTypeLookup.canRenderInLayer(state, layer)

    override def getRenderLayer = RenderTypeLookup.getRenderType(state)

    @OnlyIn(Dist.CLIENT)
    def getBreakingIcon(side: Int) = pIconT.icon

    def getItem = new ItemStack(Item.getItemFromBlock(state.getBlock), 1)

    def getLocalizedName = getItem.getDisplayName

    def getStrength(player: PlayerEntity) =
        state.getPlayerRelativeBlockHardness(player, player.world, new BlockPos(0, -1, 0))

    def isTransparent = false //!state.isOpaqueCube

    def getLightValue = state.getLightValue

    def toolClasses = Seq("axe", "pickaxe", "shovel")

    def getCutterStrength = state.getBlock.getHarvestLevel(state)

    def getSound = state.getBlock.getSoundType(state)

    def explosionResistance(entity: Entity): Float = 2 //state.getBlock.getExplosionResistance(entity)
}

/**
 * Utility functions for cleaner registry code
 */
object BlockMicroMaterial {

    /**
     * With the flattening, BlockStates are a LOT less likely to have any properties,
     * but in the event that there are we need to format it properly for a registry
     * name. Due to the limited characters available in a ResourceLocation path,
     * the following is the format for registry keys.
     *
     * "mod_id:block_name//property1.value1/property2.value2/property3.value3"
     *
     * "minecraft:redstone_ore//lit.false"
     * "minecraft:redstone_ore//lit.true"
     *
     * @param state The [[BlockState]] to create a registry key for.
     * @return The registry key.
     */
    def makeMaterialKey(state: BlockState): ResourceLocation = {
        val block = state.getBlock
        var path = block.getRegistryName.getPath
        if (state.getProperties.size() > 0) {
            path += state.getValues.asScala
                .map(e => s"${e._1.getName}.${e._1.getName(unsafeCast(e._2))}")
                .mkString("//", "/", "")
        }
        new ResourceLocation(block.getRegistryName.getNamespace, path)
    }

    def apply(block: Block) = new BlockMicroMaterial(block.getDefaultState)

    def apply(state: BlockState) = new BlockMicroMaterial(state)
}
