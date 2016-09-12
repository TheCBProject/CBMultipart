package codechicken.multipart.minecraft;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.BlockRenderer;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.*;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.Collections;

public abstract class McBlockPart extends TMultiPart implements TCuboidPart, TNormalOcclusionPart, TIconHitEffectsPart
{
    public abstract Block getBlock();

    @Override
    public Iterable<IndexedCuboid6> getSubParts()
    {
        return Arrays.asList(new IndexedCuboid6(0, getBounds()));
    }

    public Iterable<Cuboid6> getCollisionBoxes()
    {
        return Collections.emptyList();
    }

    @Override
    public void renderBreaking(Vector3 pos, TextureAtlasSprite texture)
    {
        CCRenderState.setPipeline(pos.translation(), new IconTransformation(texture));
        BlockRenderer.renderCuboid(getBounds(), 0);
    }

    @Override
    public Iterable<Cuboid6> getOcclusionBoxes()
    {
        return Arrays.asList(getBounds());
    }

    @Override
    public boolean occlusionTest(TMultiPart npart)
    {
        return NormalOcclusionTest.apply(this, npart);
    }

    @Override
    public Iterable<ItemStack> getDrops()
    {
        return Arrays.asList(getDropStack());
    }

    @Override
    public ItemStack pickItem(CuboidRayTraceResult hit)
    {
        return getDropStack();
    }

    public ItemStack getDropStack()
    {
        return new ItemStack(getBlock());
    }
}
