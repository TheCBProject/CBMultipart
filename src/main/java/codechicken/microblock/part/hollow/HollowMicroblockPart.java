package codechicken.microblock.part.hollow;

import codechicken.lib.math.MathHelper;
import codechicken.lib.raytracer.VoxelShapeCache;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.api.ISlottedHollowConnect;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.factory.StandardMicroFactory;
import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.part.StandardMicroblockPart;
import codechicken.microblock.part.face.FaceMicroblockPart;
import codechicken.microblock.util.MaskedCuboid;
import codechicken.multipart.api.part.TFacePart;
import codechicken.multipart.api.part.TNormalOcclusionPart;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.covers1624.quack.collection.StreamableIterable;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Created by covers1624 on 20/10/22.
 */
public class HollowMicroblockPart extends StandardMicroblockPart implements TFacePart, TNormalOcclusionPart {

    private static final Int2ObjectMap<VoxelShape> OCCLUSION_SHAPE_CACHE = Int2ObjectMaps.synchronize(new Int2ObjectArrayMap<>());
    private static final Int2ObjectMap<VoxelShape> COLLISION_SHAPE_CACHE = Int2ObjectMaps.synchronize(new Int2ObjectArrayMap<>());

    // XXXXXXXX XXXXXXXX XXXXXXXX TTTTSSSS
    public static final Cuboid6[][] pBoxes = new Cuboid6[256][4];
    public static final VoxelShape[] pShapes = new VoxelShape[256];
    public static final Cuboid6[] occBounds = new Cuboid6[256];

    static {
        for (int s = 0; s < 6; s++) {
            Transformation tr = Rotation.sideRotations[s].at(Vector3.CENTER);
            for (int t = 1; t < 8; t++) {
                int i = t << 4 | s;
                double d = t / 8D;
                double w1 = 1 / 8D;
                pBoxes[i][0] = new Cuboid6(0, 0, 0, w1, d, 1).apply(tr);
                pBoxes[i][1] = new Cuboid6(1 - w1, 0, 0, 1, d, 1).apply(tr);
                pBoxes[i][2] = new Cuboid6(w1, 0, 0, 1 - w1, d, w1).apply(tr);
                pBoxes[i][3] = new Cuboid6(w1, 0, 1 - w1, 1 - w1, d, 1).apply(tr);

                occBounds[i] = new Cuboid6(1 / 8D, 0, 1 / 8D, 7 / 8D, d, 7 / 8D).apply(tr);
                pShapes[i] = StreamableIterable.of(pBoxes[i]).map(VoxelShapeCache::getShape).fold(Shapes.empty(), Shapes::or);
            }
        }
    }

    public HollowMicroblockPart(MicroMaterial material) {
        super(material);
    }

    public int getHoleSize() {
        if (tile() != null && tile().getSlottedPart(6) instanceof ISlottedHollowConnect part) {
            return MathHelper.clip(part.getHoleSize(getSlot()), 1, 11);
        }
        return 8;
    }

    @Override
    public List<MaskedCuboid> getRenderCuboids(boolean isInventory) {
        int holeSize = getHoleSize();
        double d1 = 0.5 - holeSize / 32D;
        double d2 = 0.5 + holeSize / 32D;
        double t = (shape >> 4) / 8D;

        Transformation tr = Rotation.sideRotations[shape & 0xF].at(Vector3.CENTER);

        return List.of(
                new MaskedCuboid(new Cuboid6(0, 0, 0, 1, t, d1).apply(tr), 0),
                new MaskedCuboid(new Cuboid6(0, 0, d2, 1, t, 1).apply(tr), 0),
                new MaskedCuboid(new Cuboid6(0, 0, d1, d1, t, d2).apply(tr), 0),
                new MaskedCuboid(new Cuboid6(d2, 0, d1, 1, t, d2).apply(tr), 0)
        );
    }

    @Override
    public StandardMicroFactory getMicroFactory() {
        return CBMicroblockModContent.HOLLOW_MICROBLOCK_PART.get();
    }

    @Override
    public Cuboid6 getBounds() {
        return FaceMicroblockPart.aBounds[shape];
    }

    @Override
    public VoxelShape getShape(CollisionContext context) {
        return getCollisionShape(context);
    }

    @Override
    public VoxelShape getPartialOcclusionShape() {
        return pShapes[shape];
    }

    @Override
    public VoxelShape getCollisionShape(CollisionContext context) {
        int holeSize = getHoleSize();
        return COLLISION_SHAPE_CACHE.computeIfAbsent((holeSize << 8) | shape, i -> computeCollisionShape(holeSize, shape));
    }

    @Override
    public VoxelShape getInteractionShape() {
        return getCollisionShape(CollisionContext.empty());
    }

    @Override
    public VoxelShape getOcclusionShape() {
        int holeSize = getHoleSize();
        return OCCLUSION_SHAPE_CACHE.computeIfAbsent((holeSize << 8) | shape, i -> computeOcclusionShape(holeSize, shape));
    }

    @Override
    public boolean allowCompleteOcclusion() {
        return true;
    }

    @Override
    public int redstoneConductionMap() {
        return 0x10;
    }

    private static VoxelShape computeOcclusionShape(int holeSize, int shape) {
        int slot = shape & 0xF;

        Cuboid6 c = occBounds[shape];
        double d1 = 0.5 - holeSize / 32D;
        double d2 = 0.5 + holeSize / 32D;
        double x1 = c.min.x;
        double x2 = c.max.x;
        double y1 = c.min.y;
        double y2 = c.max.y;
        double z1 = c.min.z;
        double z2 = c.max.z;
        return switch (slot) {
            case 0, 1 -> StreamableIterable.of(
                            new Cuboid6(d2, y1, d1, x2, y2, d2),
                            new Cuboid6(x1, y1, d1, d1, y2, d2),
                            new Cuboid6(x1, y1, d2, x2, y2, z2),
                            new Cuboid6(x1, y1, z1, x2, y2, d1))
                    .map(VoxelShapeCache::getShape)
                    .fold(Shapes.empty(), Shapes::or);
            case 2, 3 -> StreamableIterable.of(
                            new Cuboid6(d1, d2, z1, d2, y2, z2),
                            new Cuboid6(d1, y1, z1, d2, d1, z2),
                            new Cuboid6(d2, y1, z1, x2, y2, z2),
                            new Cuboid6(x1, y1, z1, d1, y2, z2))
                    .map(VoxelShapeCache::getShape)
                    .fold(Shapes.empty(), Shapes::or);
            case 4, 5 -> StreamableIterable.of(
                            new Cuboid6(x1, d1, d2, x2, d2, z2),
                            new Cuboid6(x1, d1, z1, x2, d2, d1),
                            new Cuboid6(x1, d2, z1, x2, y2, z2),
                            new Cuboid6(x1, y1, z1, x2, d1, z2))
                    .map(VoxelShapeCache::getShape)
                    .fold(Shapes.empty(), Shapes::or);
            default -> throw new IllegalStateException("Unexpected value: " + slot);
        };
    }

    private static VoxelShape computeCollisionShape(int holeSize, int shape) {
        double d1 = 0.5 - holeSize / 32D;
        double d2 = 0.5 + holeSize / 32D;
        double t = (shape >> 4) / 8D;

        Transformation tr = Rotation.sideRotations[shape & 0xF].at(Vector3.CENTER);
        return StreamableIterable.of(
                        new Cuboid6(0, 0, 0, 1, t, d1),
                        new Cuboid6(0, 0, d2, 1, t, 1),
                        new Cuboid6(0, 0, d1, d1, t, d2),
                        new Cuboid6(d2, 0, d1, 1, t, d2))
                .map(e -> e.apply(tr))
                .map(VoxelShapeCache::getShape)
                .fold(Shapes.empty(), Shapes::or);
    }
}
