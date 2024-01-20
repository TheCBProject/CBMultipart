package codechicken.multipart.trait;

import codechicken.multipart.block.TileMultipart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

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
            BlockPos pos = getBlockPos();
            Minecraft.getInstance().levelRenderer.setBlocksDirty(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
        }
    }
}
