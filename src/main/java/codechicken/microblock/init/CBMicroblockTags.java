package codechicken.microblock.init;

import codechicken.microblock.CBMicroblock;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Created by covers1624 on 22/10/22.
 */
public class CBMicroblockTags {

    public static class Items {

        public static final TagKey<Item> STONE_ROD = common("rods/stone");

        private static TagKey<Item> common(String path) {
            return ItemTags.create(Identifier.fromNamespaceAndPath("c", path));
        }

        private static TagKey<Item> mod(String path) {
            return ItemTags.create(Identifier.fromNamespaceAndPath(CBMicroblock.MOD_ID, path));
        }
    }
}
