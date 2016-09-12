package codechicken.microblock

import codechicken.microblock.BlockMicroMaterial._
import net.minecraft.block.{BlockNewLog, BlockOldLog, BlockPlanks}
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

        createAndRegister(STONE)
        createAndRegister {
            import net.minecraft.block.BlockDirt._
            val default = DIRT.getDefaultState
            Seq(
                default.withProperty(VARIANT, DirtType.DIRT),
                default.withProperty(VARIANT, DirtType.COARSE_DIRT),
                default.withProperty(VARIANT, DirtType.PODZOL)
            )
        }
        createAndRegister(COBBLESTONE)
        createAndRegister {
            import net.minecraft.block.BlockPlanks._
            val default = PLANKS.getDefaultState
            Seq(
                default.withProperty(VARIANT, EnumType.OAK),
                default.withProperty(VARIANT, EnumType.SPRUCE),
                default.withProperty(VARIANT, EnumType.BIRCH),
                default.withProperty(VARIANT, EnumType.ACACIA),
                default.withProperty(VARIANT, EnumType.DARK_OAK)
            )
        }

        createAndRegister(
            BlockPlanks.EnumType.values().slice(0, 4).map { logType =>
                LOG.getDefaultState.withProperty(BlockOldLog.VARIANT, logType)
            }
        )

        createAndRegister(
            BlockPlanks.EnumType.values().slice(4, 6).map { logType =>
                LOG2.getDefaultState.withProperty(BlockNewLog.VARIANT, logType)
            }
        )

//        createAndRegister(leaves, 0 to 3)
//        createAndRegister(leaves2, 0 to 1, "tile.leaves2")
//        createAndRegister(sponge)
//        createAndRegister(glass)
//        createAndRegister(lapis_block)
//        createAndRegister(sandstone, 0 to 2)
//        createAndRegister(wool, 0 to 15)
//        createAndRegister(gold_block)
//        createAndRegister(iron_block)
//        createAndRegister(brick_block)
//        createAndRegister(bookshelf)
//        createAndRegister(mossy_cobblestone)
//        createAndRegister(obsidian)
//        createAndRegister(diamond_block)
//        createAndRegister(ice)
//        createAndRegister(snow)
//        createAndRegister(clay)
//        createAndRegister(netherrack)
//        createAndRegister(soul_sand)
//        createAndRegister(glowstone)
//        createAndRegister(stonebrick, 0 to 3)
//        createAndRegister(nether_brick)
//        createAndRegister(end_stone)
//        createAndRegister(emerald_block)
//        createAndRegister(redstone_block)
//        createAndRegister(quartz_block)
//        createAndRegister(stained_hardened_clay, 0 to 15)
//        createAndRegister(hardened_clay)
//        createAndRegister(coal_block)
//        createAndRegister(packed_ice)
//        createAndRegister(stained_glass, 0 to 15)

        registerMaterial(new GrassMicroMaterial, materialKey(GRASS))

//        MicroMaterialRegistry.remapName(oldKey(grass), materialKey(grass))
//        registerMaterial(new GrassMicroMaterial, materialKey(grass))
//        MicroMaterialRegistry.remapName(oldKey(mycelium), materialKey(mycelium))
//        registerMaterial(new TopMicroMaterial(mycelium), materialKey(mycelium))
    }
}
