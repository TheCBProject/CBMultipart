package codechicken.microblock

import codechicken.microblock.BlockMicroMaterial._
import net.minecraft.block._
import net.minecraft.init.Blocks._
import MicroMaterialRegistry._

object DefaultContent
{
    def load()
    {
        FaceMicroFactory.register(0)
        HollowMicroFactory.register(1)
        CornerMicroFactory.register(2)
        EdgeMicroFactory.register(3)
        PostMicroFactory.register()

        createAndRegister(STONE, 0 to 6)
        registerMaterial(new GrassMicroMaterial, materialKey(GRASS))
        createAndRegister(DIRT, 0 to 1)
        registerMaterial(new TopMicroMaterial(DIRT.getStateFromMeta(2)), materialKey(DIRT.getStateFromMeta(2)))
        createAndRegister(COBBLESTONE)
        createAndRegister(PLANKS, 0 to 5)
        createAndRegister(SAND, 0 to 1)
        createAndRegister(GRAVEL)
        createAndRegister(GOLD_ORE)
        createAndRegister(IRON_ORE)
        createAndRegister(COAL_ORE)

        createAndRegister(BlockPlanks.EnumType.values.take(4).map {
            LOG.getDefaultState.withProperty(BlockOldLog.VARIANT, _)
        })

        createAndRegister(BlockPlanks.EnumType.values.take(4).map {
            LEAVES.getDefaultState.withProperty(BlockOldLeaf.VARIANT, _)
        })

        createAndRegister(SPONGE)
        createAndRegister(GLASS)
        createAndRegister(LAPIS_ORE)
        createAndRegister(LAPIS_BLOCK)
        createAndRegister(SANDSTONE, 0 to 2)
        createAndRegister(WOOL, 0 to 15)
        createAndRegister(GOLD_BLOCK)
        createAndRegister(IRON_BLOCK)
        createAndRegister(BRICK_BLOCK)
        createAndRegister(BOOKSHELF)
        createAndRegister(MOSSY_COBBLESTONE)
        createAndRegister(OBSIDIAN)
        createAndRegister(DIAMOND_ORE)
        createAndRegister(DIAMOND_BLOCK)
        createAndRegister(REDSTONE_ORE)
        createAndRegister(ICE)
        createAndRegister(SNOW)
        createAndRegister(CLAY)
        createAndRegister(NETHERRACK)
        createAndRegister(SOUL_SAND)
        createAndRegister(GLOWSTONE)
        createAndRegister(STAINED_GLASS, 0 to 15)
        createAndRegister(STONEBRICK, 0 to 3)
        createAndRegister(NETHER_BRICK)
        registerMaterial(new TopMicroMaterial(MYCELIUM), materialKey(MYCELIUM))
        createAndRegister(END_STONE)
        createAndRegister(EMERALD_ORE)
        createAndRegister(EMERALD_BLOCK)
        createAndRegister(REDSTONE_BLOCK)
        createAndRegister(QUARTZ_ORE)
        createAndRegister(QUARTZ_BLOCK, 0 to 2)
        createAndRegister(STAINED_HARDENED_CLAY, 0 to 15)

        createAndRegister(BlockPlanks.EnumType.values.drop(4).map {
            LEAVES2.getDefaultState.withProperty(BlockNewLeaf.VARIANT, _)
        })

        createAndRegister(BlockPlanks.EnumType.values().drop(4).map {
            LOG2.getDefaultState.withProperty(BlockNewLog.VARIANT, _)
        })

        createAndRegister(PRISMARINE, 0 to 2)
        createAndRegister(HARDENED_CLAY)
        createAndRegister(COAL_BLOCK)
        createAndRegister(PACKED_ICE)
        createAndRegister(RED_SANDSTONE, 0 to 2)
        createAndRegister(PURPUR_BLOCK)
        createAndRegister(PURPUR_PILLAR)
        createAndRegister(END_BRICKS)
        createAndRegister(MAGMA)
        createAndRegister(NETHER_WART_BLOCK)
        createAndRegister(RED_NETHER_BRICK)
    }
}
