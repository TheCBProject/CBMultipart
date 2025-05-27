package codechicken.multipart.client;

import codechicken.lib.texture.TextureUtils;
import codechicken.multipart.api.MultipartClientRegistry;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.util.ConcatenatedListView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by covers1624 on 1/21/25.
 */
public class MultipartTileBakedModel implements IDynamicBakedModel {

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        var multipartData = data.get(MultipartModelData.DATA);
        if (multipartData == null) return List.of();

        if (renderType == null && Minecraft.getInstance().hitResult instanceof PartRayTraceResult hit) {
            return getPartQuads(
                    hit.part,
                    side,
                    rand,
                    multipartData.partsAndData().getOrDefault(hit.part, ModelData.EMPTY),
                    null
            );
        }

        var parts = multipartData.tile().getPartList();
        List<List<BakedQuad>> quads = new ArrayList<>(parts.size());
        for (MultiPart part : parts) {
            var partQuads = getPartQuads(part, side, rand, multipartData.partsAndData().getOrDefault(part, ModelData.EMPTY), renderType);
            if (!partQuads.isEmpty()) {
                quads.add(partQuads);
            }
        }

        return ConcatenatedListView.of(quads);
    }

    private static List<BakedQuad> getPartQuads(MultiPart part, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        var renderer = MultipartClientRegistry.getRenderer(part.getType());
        if (renderer != null) {
            return renderer.getQuads(part, side, rand, data, renderType);
        }

        return List.of();
    }

    // @formatter:off
    @Override public boolean useAmbientOcclusion() { return true; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean usesBlockLight() { return true; }
    @Override public boolean isCustomRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleIcon() { return TextureUtils.getMissingSprite(); }
    @Override public ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }
    // @formatter:on
}
