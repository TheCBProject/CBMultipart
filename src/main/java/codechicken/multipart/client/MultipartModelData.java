package codechicken.multipart.client;

import codechicken.multipart.api.MultipartType;
import codechicken.multipart.block.TileMultipart;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;

import java.util.List;

/**
 * Created by covers1624 on 2/8/25.
 */
public record MultipartModelData(
        TileMultipart tile,
        List<PartModelData> parts
) {

    public static final ModelProperty<MultipartModelData> DATA = new ModelProperty<>();

    public record PartModelData(MultipartType<?> type, ModelData data) { }
}
