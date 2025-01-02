package codechicken.microblock.recipe;

import codechicken.lib.util.ItemUtils;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.init.CBMicroblockTags;
import codechicken.microblock.item.ItemMicroBlock;
import codechicken.microblock.item.MicroMaterialComponent;
import codechicken.microblock.util.MicroMaterialRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import static codechicken.microblock.init.CBMicroblockModContent.MICRO_BLOCK_ITEM;

/**
 * Created by covers1624 on 22/10/22.
 */
public class MicroRecipe extends CustomRecipe {

    private static final int[] splitMap = { 3, 3, -1, 2 };

    public MicroRecipe() {
        super(CraftingBookCategory.MISC);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CBMicroblockModContent.MICRO_RECIPE_SERIALIZER.get();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !getAssemblyResult(input).isEmpty();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {
        return getAssemblyResult(inv);
    }

    private ItemStack getAssemblyResult(CraftingInput inv) {
        var res = getHollowResult(inv);
        if (!res.isEmpty()) return res;

        res = getGluingResult(inv);
        if (!res.isEmpty()) return res;

        res = getThinningResult(inv);
        if (!res.isEmpty()) return res;

        res = getSplittingResult(inv);
        if (!res.isEmpty()) return res;

        return getHollowFillResult(inv);
    }

    private static ItemStack getHollowResult(CraftingInput inv) {
        if (inv.width() != 3 || inv.height() != 3) return ItemStack.EMPTY;

        if (!inv.getItem(1, 1).isEmpty()) return ItemStack.EMPTY;

        ItemStack first = inv.getItem(0, 0);
        if (!first.is(MICRO_BLOCK_ITEM.get())) return ItemStack.EMPTY;

        MicroMaterialComponent materialComponent = MicroMaterialComponent.getComponent(first);
        if (materialComponent == null) return ItemStack.EMPTY;

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                if (x == 0 && y == 0 || x == 1 && y == 1) continue;

                ItemStack stack = inv.getItem(x, y);
                if (!stack.is(MICRO_BLOCK_ITEM.get())) return ItemStack.EMPTY;

                MicroMaterialComponent comp = MicroMaterialComponent.getComponent(stack);
                if (comp == null) return ItemStack.EMPTY;

                if (!comp.equals(materialComponent)) return ItemStack.EMPTY;
            }
        }

        return create(8, 1, materialComponent.size(), materialComponent.material());
    }

    private static ItemStack getGluingResult(CraftingInput inv) {
        int size = 0;
        int count = 0;
        int smallest = 0;
        int mcrFactory = 0;
        MicroMaterial material = null;
        for (ItemStack stack : inv.items()) {
            if (!stack.isEmpty()) {
                if (!stack.is(MICRO_BLOCK_ITEM.get())) return ItemStack.EMPTY;

                MicroMaterialComponent component = MicroMaterialComponent.getComponent(stack);
                if (component == null) return ItemStack.EMPTY;

                if (count == 0) {
                    size = component.size();
                    mcrFactory = component.factoryId();
                    material = component.material();
                    count = 1;
                    smallest = size;
                    continue;
                }

                if (component.factoryId() != mcrFactory || component.material() != material) return ItemStack.EMPTY;
                if (mcrFactory >= 2 && component.size() != smallest) return ItemStack.EMPTY;

                smallest = Math.min(smallest, component.size());
                count += 1;
                size += component.size();
            }
        }

        if (material == null) return ItemStack.EMPTY;
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

                if (base == -1) yield create(size / 8, 0, 8, material);
                if (base <= smallest) yield ItemStack.EMPTY;
                yield create(size / base, mcrFactory, base, material);
            }
            default -> ItemStack.EMPTY;
        };
    }

    private static ItemStack getThinningResult(CraftingInput inv) {
        if (inv.items().size() != 2 || inv.width() != 2) return ItemStack.EMPTY;

        SawResult saw = findSaw(inv);
        if (saw == null) return ItemStack.EMPTY;

        ItemStack item = inv.getItem(saw.x == 0 ? 1 : 0, saw.y);
        if (item.isEmpty()) return ItemStack.EMPTY;

        int factory;
        int size;
        MicroMaterial material;

        MicroMaterialComponent comp = MicroMaterialComponent.getComponent(item);
        if (comp != null) {
            factory = comp.factoryId();
            size = comp.size();
            material = comp.material();
        } else {
            factory = 0;
            size = 8;
            material = findMaterial(item);
        }

        if (size == 1 || material == null) return ItemStack.EMPTY;

        return create(2, factory, size / 2, material);
    }

    private static ItemStack getSplittingResult(CraftingInput inv) {
        if (inv.items().size() != 2 || inv.height() != 2) return ItemStack.EMPTY;

        SawResult saw = findSaw(inv);
        if (saw == null) return ItemStack.EMPTY;

        ItemStack item = inv.getItem(saw.x, saw.y == 0 ? 1 : 0);
        if (!item.is(MICRO_BLOCK_ITEM)) return ItemStack.EMPTY;

        MicroMaterialComponent comp = MicroMaterialComponent.getComponent(item);
        if (comp == null) return ItemStack.EMPTY;

        int split = splitMap[comp.factoryId()];
        if (split == -1) return ItemStack.EMPTY;

        return create(2, split, comp.size(), comp.material());
    }

    private static ItemStack getHollowFillResult(CraftingInput inv) {
        if (inv.items().size() != 1) return ItemStack.EMPTY;

        ItemStack only = inv.items().getFirst();
        if (!only.is(CBMicroblockModContent.MICRO_BLOCK_ITEM)) return ItemStack.EMPTY;

        MicroMaterialComponent component = MicroMaterialComponent.getComponent(only);
        if (component == null || component.factoryId() != 1) return ItemStack.EMPTY;

        return create(1, 0, component.size(), component.material());
    }

    private static @Nullable SawResult findSaw(CraftingInput inv) {
        for (int x = 0; x < inv.width(); x++) {
            for (int y = 0; y < inv.height(); y++) {
                ItemStack item = inv.getItem(x, y);
                if (item.is(CBMicroblockTags.Items.TOOL_SAW)) {
                    return new SawResult(item, x, y);
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

    @Nullable
    public static MicroMaterial findMaterial(ItemStack stack) {
        if (stack.isEmpty()) return null;
        for (MicroMaterial material : MicroMaterialRegistry.microMaterials()) {
            ItemStack mStack = material.getItem();
            if (ItemStack.isSameItemSameComponents(mStack, stack)) {
                return material;
            }
        }
        return null;
    }

    private record SawResult(ItemStack stack, int x, int y) { }
}
