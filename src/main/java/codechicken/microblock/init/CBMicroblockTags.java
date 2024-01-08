package codechicken.microblock.init;

import codechicken.microblock.CBMicroblock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Created by covers1624 on 22/10/22.
 */
public class CBMicroblockTags {

    public static class Items {

        public static final TagKey<Item> STONE_ROD = forge("rods/stone");

        public static final TagKey<Item> TOOL_SAW = mod("tools/saw");

        private static TagKey<Item> forge(String path) {
            return ItemTags.create(new ResourceLocation("forge", path));
        }

        private static TagKey<Item> mod(String path) {
            return ItemTags.create(new ResourceLocation(CBMicroblock.MOD_ID, path));
        }
    }
}
