package codechicken.multipart.trait;

import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.client.MultipartModelData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Manual trait implemented on every client side TileMultiPart.
 */
public class TileMultipartClient extends TileMultipart {

    public TileMultipartClient(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public boolean isClientTile() {
        return true;
    }

    @Override
    public void markRender() {
        if (getLevel() instanceof ClientLevel) {
            requestModelDataUpdate();
            BlockPos pos = getBlockPos();
            Minecraft.getInstance().levelRenderer.setBlocksDirty(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
        }
    }

    @Override
    public ModelData getModelData() {
        IdentityHashMap<MultiPart, ModelData> partData = new IdentityHashMap<>();
        for (MultiPart part : getPartList()) {
            ModelData data = part.getModelData();
            if (data != ModelData.EMPTY) {
                partData.put(part, data);
            }
        }
        return ModelData.of(
                MultipartModelData.DATA,
                new MultipartModelData(
                        tile(),
                        partData
                )
        );
    }
}
