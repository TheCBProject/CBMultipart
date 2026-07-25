package codechicken.multipart.client;

import codechicken.multipart.api.MultipartClientRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import java.util.List;

import static codechicken.multipart.CBMultipart.MOD_ID;

/**
 * Created by covers1624 on 1/21/25.
 */
public class MultipartTileBakedModel implements DynamicBlockStateModel {

    private final TextureAtlasSprite particle;

    public MultipartTileBakedModel(TextureAtlasSprite particle) {
        this.particle = particle;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
        var data = level.getModelData(pos);
        var tileData = data.get(MultipartModelData.DATA);
        if (tileData == null) return;

        for (var partAndData : tileData.parts()) {
            var renderer = MultipartClientRegistry.getRenderer(partAndData.type());
            if (renderer != null) {
                renderer.collectParts(partAndData.data(), level, pos, random, parts);
            }
        }
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return particle;
    }

    public static final class Unbaked implements CustomUnbakedBlockStateModel {

        public static final Identifier TYPE = Identifier.fromNamespaceAndPath(MOD_ID, "tile");
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            var missingSprite = baker.sprites().get(
                    new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation()),
                    () -> "MultipartTileBakedModel.Unbaked"
            );
            return new MultipartTileBakedModel(missingSprite);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
        }

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return CODEC;
        }
    }
}
