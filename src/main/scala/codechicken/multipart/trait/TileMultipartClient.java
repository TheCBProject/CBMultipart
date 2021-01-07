package codechicken.multipart.trait;

import codechicken.multipart.block.TileMultiPart;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Manual trait implemented on every client side TileMultiPart.
 */
public class TileMultipartClient extends TileMultiPart {

    @Override
    public boolean isClientTile() {
        return true;
    }

    @Override
    public void markRender() {
        if (getWorld() instanceof ClientWorld) {
            ClientWorld world = (ClientWorld) getWorld();
            BlockPos pos = getPos();
            world.worldRenderer.markBlockRangeForRenderUpdate(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
        }
    }
}
