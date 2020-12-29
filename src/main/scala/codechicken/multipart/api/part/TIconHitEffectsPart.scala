package codechicken.multipart.api.part

import java.util.{ArrayList => JArrayList}

import codechicken.lib.render.particle.CustomParticleHandler
import codechicken.lib.vec.{Cuboid6, Vector3}
import codechicken.multipart.util.PartRayTraceResult
import net.minecraft.client.particle.ParticleManager
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

/**
 * This suite of 3 classes provides simple functions for standard minecraft style hit and break particles.
 *
 * Scala|Java composition setup.
 * Due to the lack of mixin inheritance in Java, the classes are structured to suit both languages as follows.
 * IconHitEffects contains static implementations of the functions that would be overriden in TMultiPart
 * JIconHitEffects is the interface that should be implemented by a Java class,
 * which can then override the functions in TMultipart and call the static methods in IconHitEffects with 'this' as the first parameter
 * TIconHitEffects is a trait for scala implementors that does includes the overrides/static calls that Java programmers need to include themselves.
 */
object IconHitEffects {
    def addHitEffects(part: TIconHitEffectsPart, hit: PartRayTraceResult, manager: ParticleManager) {
        CustomParticleHandler.addBlockHitEffects(part.tile.getWorld,
            part.getBounds.copy.add(Vector3.fromTile(part.tile)),
            hit.getFace, part.getBreakingIcon(hit), manager)
    }

    def addDestroyEffects(part: TIconHitEffectsPart, manager: ParticleManager) {
        addDestroyEffects(part, manager, true)
    }

    def addDestroyEffects(part: TIconHitEffectsPart, manager: ParticleManager, scaleDensity: Boolean) {
        val icons = new JArrayList[TextureAtlasSprite](6)
        for (i <- 0 until 6)
            icons.add(i, part.getBrokenIcon(i))
        val bounds =
            if (scaleDensity) {
                part.getBounds.copy
            } else {
                Cuboid6.full.copy
            }

        CustomParticleHandler.addBlockDestroyEffects(part.tile.getWorld,
            bounds.add(Vector3.fromTile(part.tile)), icons, manager)
    }
}

/**
 * Part interface that contain callbacks for particle rendering. Be sure to
 * manually override addHitEffects and addDestoyEffects as done below if you
 * are using this in Java.
 */
trait TIconHitEffectsPart extends TMultiPart {
    def getBounds: Cuboid6

    @OnlyIn(Dist.CLIENT)
    def getBreakingIcon(hit: PartRayTraceResult): TextureAtlasSprite

    @OnlyIn(Dist.CLIENT)
    def getBrokenIcon(side: Int): TextureAtlasSprite

    @OnlyIn(Dist.CLIENT)
    override def addHitEffects(hit: PartRayTraceResult, manager: ParticleManager) {
        IconHitEffects.addHitEffects(this, hit, manager)
    }

    @OnlyIn(Dist.CLIENT)
    override def addDestroyEffects(hit: PartRayTraceResult, manager: ParticleManager) {
        IconHitEffects.addDestroyEffects(this, manager)
    }
}
