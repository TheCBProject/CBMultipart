package codechicken.multipart.api;

import codechicken.multipart.CBMultipart;
import codechicken.multipart.api.part.MultiPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

/**
 * Created by covers1624 on 6/5/25.
 */
public class MultipartLootContextParams {

    public static final LootContextParam<MultiPart> MULTI_PART = new LootContextParam<>(ResourceLocation.fromNamespaceAndPath(CBMultipart.MOD_ID, "multipart"));
}
