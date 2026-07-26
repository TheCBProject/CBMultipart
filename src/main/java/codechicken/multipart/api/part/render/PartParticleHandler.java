package codechicken.multipart.api.part.render;

import codechicken.lib.vec.Vector3;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.world.entity.Entity;

/**
 * Created by covers1624 on 7/26/26.
 *
 * @see StandardPartParticleHandler
 */
public interface PartParticleHandler<P> {

    /**
     * Add particles and other effects when a player is mining this part.
     *
     * @param hit    The {@link PartRayTraceResult} hit result.
     * @param engine The {@link ParticleEngine} to spawn particles.
     */
    default void addHitEffects(P part, PartRayTraceResult hit, ParticleEngine engine) { }

    /**
     * Add particles and other effects when a player finishes breaking this part.
     *
     * @param hit    The {@link PartRayTraceResult} hit result.
     * @param engine The {@link ParticleEngine} to spawn particles.
     */
    default void addDestroyEffects(P part, PartRayTraceResult hit, ParticleEngine engine) { }

    /**
     * Add particles and other effects when a player lands on this part.
     *
     * @param hit               The hit directly bellow the entities feet.
     * @param entity            The position of the entity.
     * @param numberOfParticles The number of particles to spawn.
     */
    default void addLandingEffects(P part, PartRayTraceResult hit, Vector3 entity, int numberOfParticles) { }

    /**
     * Add particles and other effects when a player runs over this part.
     * <p>
     * This is called on both the client and the server.
     *
     * @param hit    The hit directly bellow the players feet.
     * @param entity The entity running.
     */
    default void addRunningEffects(P part, PartRayTraceResult hit, Entity entity) { }
}
