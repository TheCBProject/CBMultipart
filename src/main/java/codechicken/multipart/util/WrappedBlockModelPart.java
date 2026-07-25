package codechicken.multipart.util;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.TriState;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Created by covers1624 on 7/18/26.
 */
public class WrappedBlockModelPart implements BlockModelPart {

    private final BlockModelPart other;
    private final BlockState state;

    public WrappedBlockModelPart(BlockModelPart other, BlockState state) {
        this.other = other;
        this.state = state;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable Direction direction) {
        return other.getQuads(direction);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return other.useAmbientOcclusion();
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return other.particleIcon();
    }

    @Override
    public ChunkSectionLayer getRenderType(BlockState state) {
        return other.getRenderType(this.state);
    }

    @Override
    public TriState ambientOcclusion() {
        return other.ambientOcclusion();
    }
}
