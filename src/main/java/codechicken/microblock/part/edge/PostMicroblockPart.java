package codechicken.microblock.part.edge;

import codechicken.lib.raytracer.VoxelShapeCache;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.factory.MicroblockPartFactory;
import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.part.MicroblockPart;
import codechicken.microblock.part.face.FaceMicroblockPart;
import codechicken.microblock.util.MaskedCuboid;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.api.part.TNormalOcclusionPart;
import codechicken.multipart.api.part.TPartialOcclusionPart;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Created by covers1624 on 21/10/22.
 */
public class PostMicroblockPart extends MicroblockPart implements TPartialOcclusionPart, TNormalOcclusionPart {

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
    public List<MaskedCuboid> getRenderCuboids(boolean isInventory) {
        return List.of(new MaskedCuboid(getBounds(), 0));
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
    public boolean occlusionTest(TMultiPart npart) {
        if (npart instanceof PostMicroblockPart post) {
            return post.getShapeSlot() != getShapeSlot();
        }

        if (npart instanceof FaceMicroblockPart facePart && (facePart.getSlot() >> 1) == getShapeSlot()) {
            return true;
        }

        return TNormalOcclusionPart.super.occlusionTest(npart);
    }
}
