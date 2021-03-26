package codechicken.microblock

import codechicken.microblock.MicroMaterialRegistry._
import codechicken.microblock.MicroRecipe._
import codechicken.microblock.api.{BlockMicroMaterial, MicroMaterial}
import codechicken.microblock.handler.MicroblockModContent
import codechicken.microblock.handler.MicroblockModContent._
import net.minecraft.block.Blocks
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.SpecialRecipe
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World

class MicroRecipe(id: ResourceLocation) extends SpecialRecipe(id) {

    override def getSerializer = MicroblockModContent.microRecipeSerializer

    override def getResultItem = ItemMicroBlock.create(1, 1, BlockMicroMaterial.makeMaterialKey(Blocks.STONE.defaultBlockState))

    override def matches(icraft: CraftingInventory, world: World) = !assemble(icraft).isEmpty

    override def canCraftInDimensions(width: Int, height: Int): Boolean = width >= 3 && height >= 3

    override def assemble(icraft: CraftingInventory): ItemStack = {
        var res = getHollowResult(icraft)
        if (!res.isEmpty) return res
        res = getGluingResult(icraft)
        if (!res.isEmpty) return res
        res = getThinningResult(icraft)
        if (!res.isEmpty) return res
        res = getSplittingResult(icraft)
        if (!res.isEmpty) return res
        res = getHollowFillResult(icraft)
        res
    }

    def getHollowResult(icraft: CraftingInventory): ItemStack = {
        if (!getStackRowCol(icraft, 1, 1).isEmpty) return ItemStack.EMPTY

        val first = getStackRowCol(icraft, 0, 0)
        val factory = microFactory(first)
        val size = microSize(first)
        val material = microMaterial(first)
        if (first.isEmpty || first.getItem != itemMicroBlock || factory != 0) return ItemStack.EMPTY

        for (i <- 1 to 8 if i != 4) {
            val item = icraft.getItem(i)
            if (item.isEmpty || item.getItem != itemMicroBlock ||
                microMaterial(item) != material || microFactory(item) != factory || microSize(item) != size) {
                return ItemStack.EMPTY
            }
        }
        return create(8, 1, size, material)
    }

    def getGluingResult(icraft: CraftingInventory): ItemStack = {
        var size = 0
        var count = 0
        var smallest = 0
        var mcrFactory = 0
        var material: MicroMaterial = null
        for (i <- 0 until 9) {
            val item = icraft.getItem(i)
            if (!item.isEmpty) {
                if (item.getItem != itemMicroBlock) return ItemStack.EMPTY
                if (count == 0) {
                    size = microSize(item)
                    mcrFactory = microFactory(item)
                    material = microMaterial(item)
                    count = 1
                    smallest = size
                }
                else if (microFactory(item) != mcrFactory || microMaterial(item) != material) {
                    return ItemStack.EMPTY
                } else if (mcrFactory >= 2 && microSize(item) != smallest) {
                    return ItemStack.EMPTY
                } else {
                    smallest = Math.min(smallest, microSize(item))
                    count += 1
                    size += microSize(item)
                }
            }
        }

        if (count <= 1) return ItemStack.EMPTY

        mcrFactory match {
            case 3 => count match {
                case 2 => create(1, 0, smallest, material)
                case _ => ItemStack.EMPTY
            }
            case 2 => count match {
                case 2 => create(1, 3, smallest, material)
                case 4 => create(1, 0, smallest, material)
                case _ => ItemStack.EMPTY
            }
            case 1 | 0 =>
                val base = Seq(1, 2, 4).find(s => (s & size) != 0)
                if (base.isEmpty) {
                    create(size / 8, 0, 8, material)
                } else if (base.get <= smallest) {
                    ItemStack.EMPTY
                } else {
                    create(size / base.get, mcrFactory, base.get, material)
                }
            case _ => ItemStack.EMPTY
        }
    }

    def getSaw(icraft: CraftingInventory): (Saw, Int, Int) = {
        for (r <- 0 until 3)
            for (c <- 0 until 3) {
                val item = getStackRowCol(icraft, c, r)
                if (!item.isEmpty && item.getItem.isInstanceOf[Saw]) {
                    return (item.getItem.asInstanceOf[Saw], r, c)
                }
            }
        return (null, 0, 0)
    }

    def getThinningResult(icraft: CraftingInventory): ItemStack = {
        val (saw, row, col) = getSaw(icraft)
        if (saw == null) {
            return ItemStack.EMPTY
        }

        val item = getStackRowCol(icraft, col, row + 1)
        if (item.isEmpty) {
            return ItemStack.EMPTY
        }

        val size = microSize(item)
        val material = microMaterial(item)
        val mcrClass = microFactory(item)
        if (size == 1 || material == null || !canCut(saw, getStackRowCol(icraft, col, row), material)) {
            return ItemStack.EMPTY
        }

        for (r <- 0 until 3)
            for (c <- 0 until 3)
                if ((c != col || r != row && r != row + 1) &&
                    !getStackRowCol(icraft, c, r).isEmpty) {
                    return ItemStack.EMPTY
                }

        return create(2, mcrClass, size / 2, material)
    }

    val splitMap = Map(0 -> 3, 1 -> 3, 3 -> 2)

    def getSplittingResult(icraft: CraftingInventory): ItemStack = {
        val (saw, row, col) = getSaw(icraft)
        if (saw == null) return ItemStack.EMPTY
        val item = getStackRowCol(icraft, col + 1, row)
        if (item.isEmpty || item.getItem != itemMicroBlock) return ItemStack.EMPTY
        val mcrClass = microFactory(item)
        val material = microMaterial(item)
        if (!canCut(saw, getStackRowCol(icraft, col, row), material)) return ItemStack.EMPTY
        val split = splitMap.get(mcrClass)
        if (split.isEmpty) return ItemStack.EMPTY

        for (r <- 0 until 3)
            for (c <- 0 until 3)
                if ((r != row || c != col && c != col + 1) &&
                    !getStackRowCol(icraft, c, r).isEmpty) {
                    return ItemStack.EMPTY
                }

        return create(2, split.get, microSize(item), material)
    }

    def getHollowFillResult(icraft: CraftingInventory): ItemStack = {
        var cover: ItemStack = ItemStack.EMPTY
        for (i <- 0 until 9) {
            val item = icraft.getItem(i)
            if (!item.isEmpty) {
                if (item.getItem != itemMicroBlock || !cover.isEmpty || microFactory(item) != 1) {
                    return ItemStack.EMPTY
                } else {
                    cover = item
                }
            }
        }
        if (cover.isEmpty) return ItemStack.EMPTY
        return create(1, 0, microSize(cover), microMaterial(cover))
    }
}

object MicroRecipe {
    def create(amount: Int, factoryID: Int, size: Int, material: MicroMaterial): ItemStack = {
        if (size == 8) {
            val item = material.getItem.copy
            item.setCount(amount)
            return item
        }
        ItemMicroBlock.createStack(amount, factoryID, size, material)
    }

    def canCut(saw: Saw, sawItem: ItemStack, material: MicroMaterial): Boolean = {
        val sawStrength = saw.getCuttingStrength(sawItem)
        val matStrength = material.getCutterStrength
        sawStrength >= matStrength || sawStrength == MicroMaterialRegistry.getMaxCuttingStrength
    }

    def getStackRowCol(inv: CraftingInventory, row: Int, col: Int): ItemStack = inv.getItem(row + col * inv.getWidth)
}
