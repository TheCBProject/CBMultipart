package codechicken.microblock.part.corner;

import codechicken.lib.raytracer.VoxelShapeCache;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.factory.StandardMicroFactory;
import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.part.StandardMicroblockPart;
import codechicken.microblock.util.MaskedCuboid;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Created by covers1624 on 21/10/22.
 */
public class CornerMicroblockPart extends StandardMicroblockPart {

    public static final Cuboid6[] aBounds = new Cuboid6[256];
    public static final VoxelShape[] aShapes = new VoxelShape[256];

    static {
        for (int s = 0; s < 8; s++) {
            int rx = (s & 4) != 0 ? -1 : 1;
            int ry = (s & 1) != 0 ? -1 : 1;
            int rz = (s & 2) != 0 ? -1 : 1;
            Transformation tr = new Scale(rx, ry, rz).at(Vector3.CENTER);

            for (int t = 0; t < 8; t++) {
                double d = t / 8D;
                int i = t << 4 | s;
                aBounds[i] = new Cuboid6(0, 0, 0, d, d, d).apply(tr);
                aShapes[i] = VoxelShapeCache.getShape(aBounds[i]);
            }
        }
    }

    public CornerMicroblockPart(MicroMaterial material) {
        super(material);
    }

    @Override
    public void setShape(int size, int slot) {
        shape = (byte) (size << 4 | (slot - 7));
    }

    @Override
    public VoxelShape getShape(CollisionContext context) {
        return aShapes[shape];
    }

    @Override
    public Cuboid6 getBounds() {
        return aBounds[shape];
    }

    @Override
    public StandardMicroFactory getMicroFactory() {
        return CBMicroblockModContent.CORNER_MICROBLOCK_PART.get();
    }

    @Override
    public int getSlot() {
        return getShapeSlot() + 7;
    }
}
