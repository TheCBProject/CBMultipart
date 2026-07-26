package codechicken.multipart.api.part.render;

import codechicken.lib.render.particle.CustomParticleHandler;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A simple and standard implementation of {@link PartParticleHandler}.
 * <p>
 * Created by covers1624 on 7/26/26.
 */
public interface StandardPartParticleHandler<P extends MultiPart> extends PartParticleHandler<P> {

    /**
     * Get the bounds of the part.
     * <p>
     * For breaking, places the crumbling particles on the faces of the supplied cuboid.
     * <p>
     * For broken, enables spawning particles based on the size/location of the cuboid.
     *
     * @param part The part.
     * @return The bounds.
     */
    Cuboid6 getBounds(P part);

    /**
     * When spawning breaking particles, if the number/locations of particles spawned should be based on the volume of your part.
     *
     * @param part The part.
     * @return If volumetric breaking particles should be used.
     */
    default boolean enableVolumetricBreaking(P part) {
        return true;
    }

    /**
     * Get the texture to use when breaking/running/landing on a part.
     * <p>
     * In the case of running/landing a hit result will be spawned from the players feet
     * to hit your part.
     *
     * @param part The part.
     * @param hit  The current hit result.
     * @return The texture. May be {@code null} to disable.
     */
    @Nullable TextureAtlasSprite getParticleTexture(P part, PartRayTraceResult hit);

    /**
     * Get all the textures to use when your part is broken.
     * <p>
     * The textures will be randomly mixed together.
     *
     * @param part The part.
     * @return The textures.
     */
    List<TextureAtlasSprite> getBrokenTextures(P part);

    @Override
    default void addHitEffects(P part, PartRayTraceResult hit, ParticleEngine engine) {
        var texture = getParticleTexture(part, hit);
        if (texture == null) return;

        CustomParticleHandler.addBlockHitEffects(
                part.level(),
                getBounds(part).copy().add(part.pos()),
                hit.getDirection(),
                texture,
                engine
        );
    }

    @Override
    default void addDestroyEffects(P part, PartRayTraceResult hit, ParticleEngine engine) {
        Cuboid6 bounds = enableVolumetricBreaking(part) ? getBounds(part) : Cuboid6.full;
        CustomParticleHandler.addBlockDestroyEffects(
                part.level(),
                bounds.copy().add(part.pos()),
                getBrokenTextures(part),
                engine
        );
    }

    @Override
    default void addLandingEffects(P part, PartRayTraceResult hit, Vector3 entity, int numberOfParticles) {
        var texture = getParticleTexture(part, hit);
        if (texture == null) return;

        CustomParticleHandler.addLandingEffects(
                part.level(),
                entity,
                texture,
                numberOfParticles
        );
    }

    @Override
    default void addRunningEffects(P part, PartRayTraceResult hit, Entity entity) {
        var texture = getParticleTexture(part, hit);
        if (texture == null) return;

        CustomParticleHandler.addRunningEffects(
                part.level(),
                entity,
                texture
        );
    }
}
