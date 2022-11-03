package codechicken.microblock.part.edge;

import codechicken.lib.raytracer.VoxelShapeCache;
import codechicken.lib.vec.*;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.part.StandardMicroFactory;
import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.part.StandardMicroblockPart;
import codechicken.multipart.api.part.EdgePart;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Created by covers1624 on 21/10/22.
 */
public class EdgeMicroblockPart extends StandardMicroblockPart implements EdgePart {

    public static final Cuboid6[] aBounds = new Cuboid6[256];
    public static final VoxelShape[] aShapes = new VoxelShape[256];

    static {
        for (int s = 0; s < 12; s++) {
            int rx = (s & 2) != 0 ? -1 : 1;
            int rz = (s & 1) != 0 ? -1 : 1;
            Transformation tr = new TransformationList(new Scale(rx, 1, rz), AxisCycle.cycles[s >> 2]).at(Vector3.CENTER);

            for (int t = 1; t < 8; t++) {
                double d = t / 8D;
                int i = t << 4 | s;
                aBounds[i] = new Cuboid6(0, 0, 0, d, 1, d).apply(tr);
                aShapes[i] = VoxelShapeCache.getShape(aBounds[i]);
            }
        }
    }

    public EdgeMicroblockPart(MicroMaterial material) {
        super(material);
    }

    @Override
    public void setShape(int size, int slot) {
        shape = (byte) (size << 4 | (slot - 15));
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
    public StandardMicroFactory getMicroFactory() {
        return CBMicroblockModContent.EDGE_MICROBLOCK_PART.get();
    }

    @Override
    public int getSlot() {
        return getShapeSlot() + 15;
    }
}
