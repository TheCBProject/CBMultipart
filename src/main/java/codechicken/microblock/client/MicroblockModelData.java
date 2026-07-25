package codechicken.microblock.client;

import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.util.MaskedCuboid;
import com.google.common.collect.ImmutableSet;
import net.neoforged.neoforge.model.data.ModelProperty;

/**
 * Created by covers1624 on 7/20/26.
 */
public record MicroblockModelData(
        // TODO does material need its own model data?
        MicroMaterial material,
        ImmutableSet<MaskedCuboid> renderCuboids
) {

    public static final ModelProperty<MicroblockModelData> TYPE = new ModelProperty<>();
}
