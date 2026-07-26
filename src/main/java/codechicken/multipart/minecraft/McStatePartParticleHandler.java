package codechicken.multipart.minecraft;

import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.api.part.render.StandardPartParticleHandler;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.List;

/**
 * Created by covers1624 on 7/26/26.
 */
public class McStatePartParticleHandler implements StandardPartParticleHandler<McStatePart> {

    @Override
    public Cuboid6 getBounds(McStatePart part) {
        return new Cuboid6(part.getShape(CollisionContext.empty()).bounds());
    }

    @Override
    public TextureAtlasSprite getParticleTexture(McStatePart part, PartRayTraceResult hit) {
        return getParticleTexture(part);
    }

    @Override
    public List<TextureAtlasSprite> getBrokenTextures(McStatePart part) {
        return List.of(getParticleTexture(part));
    }

    private static TextureAtlasSprite getParticleTexture(McStatePart part) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper()
                .getBlockModel(part.getCurrentState())
                .particleIcon(); // TODO fake level for model data
    }
}
