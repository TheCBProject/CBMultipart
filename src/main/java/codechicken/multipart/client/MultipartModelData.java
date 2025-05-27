package codechicken.multipart.client;

import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.block.TileMultipart;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Created by covers1624 on 2/8/25.
 */
public record MultipartModelData(
        TileMultipart tile,
        IdentityHashMap<MultiPart, ModelData> partsAndData
) {
    public static final ModelProperty<MultipartModelData> DATA = new ModelProperty<>();
}
