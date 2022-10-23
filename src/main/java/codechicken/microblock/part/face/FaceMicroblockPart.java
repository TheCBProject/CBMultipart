package codechicken.microblock.part.face;

import codechicken.lib.raytracer.VoxelShapeCache;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.part.StandardMicroFactory;
import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.part.StandardMicroblockPart;
import codechicken.microblock.util.MaskedCuboid;
import codechicken.multipart.api.part.TFacePart;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Created by covers1624 on 20/10/22.
 */
public class FaceMicroblockPart extends StandardMicroblockPart implements TFacePart {

    public static final Cuboid6[] aBounds = new Cuboid6[256];
    public static final VoxelShape[] aShapes = new VoxelShape[256];

    static {
        for (int s = 0; s < 6; s++) {
            Transformation transform = Rotation.sideRotations[s].at(Vector3.CENTER);
            for (int t = 1; t < 8; t++) {
                double d = t / 8D;
                int i = t << 4 | s;
                aBounds[i] = new Cuboid6(0, 0, 0, 1, d, 1).apply(transform);
                aShapes[i] = VoxelShapeCache.getShape(aBounds[i]);
            }
        }
    }

    public FaceMicroblockPart(MicroMaterial material) {
        super(material);
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
    public Iterable<MaskedCuboid> getRenderCuboids(boolean isInventory) {
        if (isInventory) return List.of(new MaskedCuboid(getBounds(), 0));

        if (isTransparent()) return List.of(new MaskedCuboid(renderBounds, renderMask));

        return List.of(
                new MaskedCuboid(renderBounds, renderMask | 1 << getSlot()), // All faces except our slot use these bounds.
                new MaskedCuboid(getBounds(), ~(1 << getSlot()))             // Our slot renders at our full bounds.
        );
    }

    @Override
    public StandardMicroFactory getMicroFactory() {
        return CBMicroblockModContent.FACE_MICROBLOCK_PART.get();
    }
}
