package codechicken.multipart

import codechicken.lib.raytracer.CuboidRayTraceResult
import codechicken.lib.render.DigIconParticle
import codechicken.lib.vec.{Cuboid6, Vector3}
import net.minecraft.client.particle.ParticleManager
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

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
object IconHitEffects
{
    def addHitEffects(part:TIconHitEffectsPart, hit:CuboidRayTraceResult, manager:ParticleManager)
    {
        DigIconParticle.addBlockHitEffects(part.tile.getWorld,
            part.getBounds.copy.add(Vector3.fromTile(part.tile)),
            hit.sideHit.ordinal, part.getBreakingIcon(hit), manager)
    }

    def addDestroyEffects(part:TIconHitEffectsPart, manager:ParticleManager)
    {
        addDestroyEffects(part, manager, true)
    }

    def addDestroyEffects(part:TIconHitEffectsPart, manager:ParticleManager, scaleDensity:Boolean)
    {
        val icons = new Array[TextureAtlasSprite](6)
        for(i <- 0 until 6)
            icons(i) = part.getBrokenIcon(i)
        val bounds =
            if(scaleDensity) part.getBounds.copy
            else Cuboid6.full.copy

        DigIconParticle.addBlockDestroyEffects(part.tile.getWorld,
            bounds.add(Vector3.fromTile(part.tile)), icons, manager)
    }
}

/**
  * Part interface that contain callbacks for particle rendering. Be sure to
  * manually override addHitEffects and addDestoyEffects as done below if you
  * are using this in Java.
  */
trait TIconHitEffectsPart extends TMultiPart
{
    def getBounds:Cuboid6

    @SideOnly(Side.CLIENT)
    def getBreakingIcon(hit:CuboidRayTraceResult):TextureAtlasSprite

    @SideOnly(Side.CLIENT)
    def getBrokenIcon(side:Int):TextureAtlasSprite

    @SideOnly(Side.CLIENT)
    override def addHitEffects(hit:CuboidRayTraceResult, manager:ParticleManager)
    {
        IconHitEffects.addHitEffects(this, hit, manager)
    }

    @SideOnly(Side.CLIENT)
    override def addDestroyEffects(hit:CuboidRayTraceResult, manager:ParticleManager)
    {
        IconHitEffects.addDestroyEffects(this, manager)
    }
}