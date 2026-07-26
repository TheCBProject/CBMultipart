package codechicken.multipart.api;

import codechicken.multipart.CBMultipart;
import codechicken.multipart.api.part.MultiPart;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;

/**
 * Created by covers1624 on 6/5/25.
 */
public class MultipartLootContextParams {

    public static final ContextKey<MultiPart> MULTI_PART = new ContextKey<>(Identifier.fromNamespaceAndPath(CBMultipart.MOD_ID, "multipart"));
}
