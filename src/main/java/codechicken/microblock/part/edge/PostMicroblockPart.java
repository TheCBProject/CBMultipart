package codechicken.microblock.part.edge;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.raytracer.VoxelShapeCache;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.part.MicroblockPartFactory;
import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.part.MicroblockPart;
import codechicken.microblock.part.face.FaceMicroblockPart;
import codechicken.microblock.util.MaskedCuboid;
import codechicken.microblock.util.MicroOcclusionHelper;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.api.part.NormalOcclusionPart;
import codechicken.multipart.api.part.PartialOcclusionPart;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by covers1624 on 21/10/22.
 */
public class PostMicroblockPart extends MicroblockPart implements PartialOcclusionPart, NormalOcclusionPart {

    public static final Cuboid6[] aBounds = new Cuboid6[256];
    public static final VoxelShape[] aShapes = new VoxelShape[256];

    static {
        for (int s = 0; s < 3; s++) {
            Transformation tr = Rotation.sideRotations[s << 1].at(Vector3.CENTER);
            for (int t = 2; t < 8; t += 2) {
                double d1 = 0.5 - t / 16D;
                double d2 = 0.5 + t / 16D;
                int i = t << 4 | s;
                aBounds[i] = new Cuboid6(d1, 0, d1, d2, 1, d2).apply(tr);
                aShapes[i] = VoxelShapeCache.getShape(aBounds[i]);
            }
        }
    }

    @Nullable
    public Cuboid6 renderBounds1 = null;
    @Nullable
    public Cuboid6 renderBounds2 = null;

    public PostMicroblockPart(MicroMaterial material) {
        super(material);
    }

    @Override
    public MicroblockPartFactory getMicroFactory() {
        return CBMicroblockModContent.POST_MICROBLOCK_PART.get();
    }

    @Override
    public Cuboid6 getBounds() {
        return aBounds[shape];
    }

    @Override
    public VoxelShape getShape(CollisionContext context) {
        return aShapes[shape];
    }

    @Override
    public int getItemFactoryId() {
        return CBMicroblockModContent.EDGE_MICROBLOCK_PART.get().factoryId;
    }

    @Override
    public Iterable<MaskedCuboid> getRenderCuboids(boolean isInventory) {
        if (isInventory) return List.of(new MaskedCuboid(getBounds(), 0));

        MaskedCuboid a = new MaskedCuboid(renderBounds1, 0);
        if (renderBounds2 == null) return List.of(a);

        return List.of(
                a,
                new MaskedCuboid(renderBounds2, 0)
        );
    }

    @Override
    public VoxelShape getOcclusionShape() {
        return getShape(CollisionContext.empty());
    }

    @Override
    public VoxelShape getPartialOcclusionShape() {
        return getOcclusionShape();
    }

    @Override
    public boolean occlusionTest(MultiPart nPart) {
        if (nPart instanceof PostMicroblockPart post) {
            return post.getShapeSlot() != getShapeSlot();
        }

        if (nPart instanceof FaceMicroblockPart facePart && (facePart.getSlot() >> 1) == getShapeSlot()) {
            return true;
        }

        return NormalOcclusionPart.super.occlusionTest(nPart);
    }

    @Override
    public void onPartChanged(@Nullable MultiPart part) {
        super.onPartChanged(part);
        if (level().isClientSide) {
            recalcBounds();
        }
    }

    @Override
    public void onAdded() {
        super.onAdded();
        if (level().isClientSide) {
            recalcBounds();
        }
    }

    @Override
    public void readUpdate(MCDataInput packet) {
        super.readUpdate(packet);
        if (level().isClientSide) {
            recalcBounds();
        }
    }

    public void recalcBounds() {
        renderBounds1 = getBounds().copy();
        renderBounds2 = null;

        shrinkFace(getShapeSlot() << 1);
        shrinkFace(getShapeSlot() << 1 | 1);

        for (MultiPart part : tile().getPartList()) {
            if (part instanceof PostMicroblockPart post) {
                shrinkPost(post);
            }
        }
    }

    private void shrinkFace(int fSide) {
        if (tile().getSlottedPart(fSide) instanceof FaceMicroblockPart fPart) {
            MicroOcclusionHelper.shrink(renderBounds1, fPart.getBounds(), fSide);
        }
    }

    private void shrinkPost(PostMicroblockPart post) {
        if (post == this) return;
        if (thisShrinks(post)) {
            if (renderBounds2 == null) {
                renderBounds2 = getBounds().copy();
            }
            MicroOcclusionHelper.shrink(renderBounds1, post.getBounds(), getShapeSlot() << 1 | 1);
            MicroOcclusionHelper.shrink(renderBounds2, post.getBounds(), getShapeSlot() << 1);
        }

    }

    private boolean thisShrinks(PostMicroblockPart other) {
        if (getSize() != other.getSize()) return getSize() < other.getSize();
        if (isTransparent() != other.isTransparent()) return isTransparent();

        return getShapeSlot() > other.getShapeSlot();
    }
}
