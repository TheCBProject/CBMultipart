package codechicken.microblock.recipe;

import codechicken.lib.util.ItemUtils;
import codechicken.microblock.api.BlockMicroMaterial;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.item.ItemMicroBlock;
import codechicken.microblock.item.SawItem;
import codechicken.microblock.util.MicroMaterialRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import static codechicken.microblock.init.CBMicroblockModContent.MICRO_BLOCK_ITEM;

/**
 * Created by covers1624 on 22/10/22.
 */
public class MicroRecipe extends CustomRecipe {

    private static final int[] splitMap = { 3, 3, -1, 2 };

    public MicroRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CBMicroblockModContent.MICRO_RECIPE_SERIALIZER.get();
    }

    @Override
    public ItemStack getResultItem() {
        return ItemMicroBlock.create(1, 1, BlockMicroMaterial.makeMaterialKey(Blocks.STONE.defaultBlockState()));
    }

    @Override
    public boolean matches(CraftingContainer cont, Level level) {
        return !assemble(cont).isEmpty();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public ItemStack assemble(CraftingContainer cont) {
        var res = getHollowResult(cont);
        if (!res.isEmpty()) return res;

        res = getGluingResult(cont);
        if (!res.isEmpty()) return res;

        res = getThinningResult(cont);
        if (!res.isEmpty()) return res;

        res = getSplittingResult(cont);
        if (!res.isEmpty()) return res;

        return getHollowFillResult(cont);
    }

    private static ItemStack getHollowResult(CraftingContainer cont) {
        if (!getStack(cont, 1, 1).isEmpty()) return ItemStack.EMPTY;

        ItemStack first = getStack(cont, 0, 0);
        int factory = microFactory(first);
        int size = microSize(first);
        MicroMaterial material = microMaterial(first);
        if (first.isEmpty() || !first.is(MICRO_BLOCK_ITEM.get()) || factory != 0) return ItemStack.EMPTY;

        for (int i = 0; i <= 8; i++) {
            if (i == 4) continue;
            ItemStack item = cont.getItem(i);
            if (item.isEmpty() || !item.is(MICRO_BLOCK_ITEM.get()) ||
                    microMaterial(item) != material || microFactory(item) != factory || microSize(item) != size) {
                return ItemStack.EMPTY;
            }
        }
        return create(8, 1, size, material);
    }

    private static ItemStack getGluingResult(CraftingContainer cont) {
        int size = 0;
        int count = 0;
        int smallest = 0;
        int mcrFactory = 0;
        MicroMaterial material = null;
        for (int i = 0; i < 9; i++) {
            ItemStack item = cont.getItem(i);
            if (!item.isEmpty()) {
                if (!item.is(MICRO_BLOCK_ITEM.get())) return ItemStack.EMPTY;
                if (count == 0) {
                    size = microSize(item);
                    mcrFactory = microFactory(item);
                    material = microMaterial(item);
                    count = 1;
                    smallest = size;
                } else if (microFactory(item) != mcrFactory || microMaterial(item) != material) {
                    return ItemStack.EMPTY;
                } else if (mcrFactory >= 2 && microSize(item) != smallest) {
                    return ItemStack.EMPTY;
                } else {
                    smallest = Math.min(smallest, microSize(item));
                    count += 1;
                    size += microSize(item);
                }
            }
        }

        if (count <= 1) return ItemStack.EMPTY;

        return switch (mcrFactory) {
            case 3 -> count == 2 ? create(1, 0, smallest, material) : ItemStack.EMPTY;
            case 2 -> switch (count) {
                case 2 -> create(1, 3, smallest, material);
                case 3 -> create(1, 0, smallest, material);
                default -> ItemStack.EMPTY;
            };
            case 1, 0 -> {
                int base = -1;
                for (int s : new int[] { 1, 2, 4 }) {
                    if ((s & size) != 0) {
                        base = s;
                        break;
                    }
                }
                if (base == -1) {
                    yield create(size / 8, 0, 8, material);
                } else if (base <= smallest) {
                    yield ItemStack.EMPTY;
                } else {
                    yield create(size / base, mcrFactory, base, material);
                }
            }
            default -> ItemStack.EMPTY;
        };
    }

    private static ItemStack getThinningResult(CraftingContainer cont) {
        SawResult saw = getSaw(cont);
        if (saw == null) return ItemStack.EMPTY;

        ItemStack item = getStack(cont, saw.col, saw.row + 1);
        if (item.isEmpty()) return ItemStack.EMPTY;

        int size = microSize(item);
        MicroMaterial material = microMaterial(item);
        int mcrClass = microFactory(item);
        if (size == 1 || material == null || !canCut(saw.sawTier, material)) {
            return ItemStack.EMPTY;
        }

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if ((c != saw.col || r != saw.row && r != saw.row + 1) && !getStack(cont, c, r).isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }

        return create(2, mcrClass, size / 2, material);
    }

    private static ItemStack getSplittingResult(CraftingContainer cont) {
        SawResult saw = getSaw(cont);
        if (saw == null) return ItemStack.EMPTY;

        ItemStack item = getStack(cont, saw.col + 1, saw.row);
        if (!item.is(MICRO_BLOCK_ITEM.get())) return ItemStack.EMPTY;

        int mcrClass = microFactory(item);
        MicroMaterial material = microMaterial(item);
        if (material == null || !canCut(saw.sawTier, material)) return ItemStack.EMPTY;

        int split = splitMap[mcrClass];
        if (split == -1) return ItemStack.EMPTY;

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if ((r != saw.row || c != saw.col && c != saw.col + 1) && !getStack(cont, c, r).isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return create(2, split, microSize(item), material);
    }

    private static ItemStack getHollowFillResult(CraftingContainer cont) {
        ItemStack cover = ItemStack.EMPTY;
        for (int i = 0; i < 9; i++) {
            ItemStack item = cont.getItem(i);
            if (!item.isEmpty()) {
                if (item.getItem() != MICRO_BLOCK_ITEM.get() || !cover.isEmpty() || microFactory(item) != 1) {
                    return ItemStack.EMPTY;
                }
                cover = item;
            }
        }
        if (cover.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return create(1, 0, microSize(cover), microMaterial(cover));
    }

    @Nullable
    private static SawResult getSaw(CraftingContainer cont) {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                ItemStack item = getStack(cont, r, c);
                Tier tier = SawItem.getSawTier(item.getItem());
                if (tier != null) {
                    return new SawResult(item, tier, r, c);
                }
            }
        }
        return null;
    }

    private static ItemStack create(int amount, int factoryId, int size, MicroMaterial material) {
        if (size == 8) {
            return ItemUtils.copyStack(material.getItem(), amount);
        }
        return ItemMicroBlock.createStack(amount, factoryId, size, material);
    }

    private static boolean canCut(Tier sawTier, MicroMaterial material) {
        // If the material has a tier, saw must be GTEQ, or max tier saw.
        // If the material has no tier, must be max tier saw.
        Tier required = material.getCutterTier();
        return required != null && SawItem.isTierGTEQ(sawTier, required) || sawTier == CBMicroblockModContent.MAX_SAW_TIER;
    }

    private static ItemStack getStack(CraftingContainer cont, int row, int col) {
        return cont.getItem(row + col * cont.getWidth());
    }

    private static int microFactory(ItemStack stack) {
        if (!stack.is(MICRO_BLOCK_ITEM.get())) return 0;

        return ItemMicroBlock.getFactoryID(stack);
    }

    private static int microSize(ItemStack stack) {
        if (!stack.is(MICRO_BLOCK_ITEM.get())) return 8;

        return ItemMicroBlock.getSize(stack);
    }

    @Nullable
    public static MicroMaterial microMaterial(ItemStack stack) {
        if (!stack.is(MICRO_BLOCK_ITEM.get())) return findMaterial(stack);

        return ItemMicroBlock.getMaterialFromStack(stack);
    }

    @Nullable
    public static MicroMaterial findMaterial(ItemStack stack) {
        if (stack.isEmpty()) return null;
        for (MicroMaterial material : MicroMaterialRegistry.MICRO_MATERIALS.getValues()) {
            ItemStack mStack = material.getItem();
            if (mStack.is(stack.getItem()) && ItemStack.tagMatches(mStack, stack)) {
                return material;
            }
        }
        return null;
    }

    private record SawResult(ItemStack stack, Tier sawTier, int row, int col) { }
}
