package codechicken.multipart.api.part;

import codechicken.lib.render.particle.CustomParticleHandler;
import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for parts which want easy custom hit/breaking particles.
 * <p>
 * Created by covers1624 on 6/6/22.
 */
public interface TIconHitEffectsPart extends TMultiPart {

    Cuboid6 getBounds();

    @OnlyIn (Dist.CLIENT)
    TextureAtlasSprite getBreakingIcon(PartRayTraceResult hit);

    @OnlyIn (Dist.CLIENT)
    TextureAtlasSprite getBrokenIcon(int side);

    @Override
    @OnlyIn (Dist.CLIENT)
    default void addHitEffects(PartRayTraceResult hit, ParticleEngine engine) {
        addHitEffects(this, hit, engine);
    }

    @Override
    @OnlyIn (Dist.CLIENT)
    default void addDestroyEffects(PartRayTraceResult hit, ParticleEngine engine) {
        addDestroyEffects(this, engine);
    }

    @OnlyIn (Dist.CLIENT)
    static void addHitEffects(TIconHitEffectsPart part, PartRayTraceResult hit, ParticleEngine engine) {
        CustomParticleHandler.addBlockHitEffects(
                part.level(),
                part.getBounds().copy().add(part.pos()),
                hit.getDirection(),
                part.getBreakingIcon(hit),
                engine
        );
    }

    @OnlyIn (Dist.CLIENT)
    static void addDestroyEffects(TIconHitEffectsPart part, ParticleEngine engine) {
        addDestroyEffects(part, engine, true);
    }

    @OnlyIn (Dist.CLIENT)
    static void addDestroyEffects(TIconHitEffectsPart part, ParticleEngine engine, boolean scaleDensity) {
        List<TextureAtlasSprite> icons = new ArrayList<>(6);
        for (int i = 0; i < 6; i++) {
            icons.add(i, part.getBrokenIcon(i));
        }
        Cuboid6 bounds = scaleDensity ? part.getBounds() : Cuboid6.full;

        CustomParticleHandler.addBlockDestroyEffects(part.level(), bounds.copy().add(part.pos()), icons, engine);
    }
}
