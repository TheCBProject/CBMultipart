package codechicken.microblock.part;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.vec.Cuboid6;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.util.MaskedCuboid;
import codechicken.microblock.util.MicroOcclusionHelper;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.api.part.TPartialOcclusionPart;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static codechicken.microblock.util.MicroOcclusionHelper.shapePriority;
import static codechicken.multipart.util.PartMap.edgeAxisMask;
import static codechicken.multipart.util.PartMap.unpackEdgeBits;

/**
 * Represents a 'standard' MicroblockPart.
 * <p>
 * Created by covers1624 on 27/6/22.
 */
public abstract class StandardMicroblockPart extends MicroblockPart implements IMicroOcclusion, TPartialOcclusionPart, IMicroShrinkRender {

    @Nullable
    public Cuboid6 renderBounds = null;
    public int renderMask;

    public StandardMicroblockPart(MicroMaterial material) {
        super(material);
    }

    @Override
    public abstract StandardMicroFactory getMicroFactory();

    @Override
    public int getItemFactoryId() {
        return getMicroFactory().factoryId;
    }

    @Override
    public int getSlot() {
        return getShapeSlot();
    }

    @Override
    public int getSlotMask() {
        return 1 << getSlot();
    }

    @Override
    public VoxelShape getPartialOcclusionShape() {
        return getShape(CollisionContext.empty());
    }

    @Override
    public boolean occlusionTest(TMultiPart npart) {
        if (!super.occlusionTest(npart)) {
            return false;
        }

        if (!(npart instanceof IMicroOcclusion mpart)) {
            return true;
        }

        int shape1 = shapePriority(getSlot());
        int shape2 = shapePriority(mpart.getSlot());

        if (mpart.getSize() + getSize() > 8) { // intersecting if opposite
            if (shape1 == 2 && shape2 == 2) {
                if (mpart.getSlot() == (getSlot() ^ 1)) {
                    return false;
                }
            }

            if (mpart.getMaterial() != getMaterial()) {
                if (shape1 == 1 && shape2 == 1) {
                    int axisMask = (getSlot() - 7) ^ (mpart.getSlot() - 7);
                    if (axisMask == 3 || axisMask == 5 || axisMask == 6) {
                        return false;
                    }
                }

                if (shape1 == 0 && shape2 == 1) {
                    if (!edgeCornerOcclusionTest(this, mpart)) {
                        return false;
                    }
                }

                if (shape1 == 1 && shape2 == 0) {
                    if (!edgeCornerOcclusionTest(mpart, this)) {
                        return false;
                    }
                }

                if (shape1 == 0 && shape2 == 0) {
                    int e1 = getSlot() - 15;
                    int e2 = mpart.getSlot() - 15;
                    if ((e1 & 0xC) == (e2 & 0xC) && ((e1 & 3) ^ (e2 & 3)) == 3) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private static boolean edgeCornerOcclusionTest(IMicroOcclusion edge, IMicroOcclusion corner) {
        return ((corner.getSlot() - 7) & edgeAxisMask(edge.getSlot() - 15)) == unpackEdgeBits(edge.getSlot() - 15);
    }

    @Override
    public List<MaskedCuboid> getRenderCuboids(boolean isInventory) {
        if (isInventory) return List.of(new MaskedCuboid(getBounds(), 0));

        return List.of(new MaskedCuboid(renderBounds, renderMask));
    }

    @Override
    public void onPartChanged(@Nullable TMultiPart part) {
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
        renderBounds = getBounds().copy();
        renderMask = MicroOcclusionHelper.recalcBounds(this, renderBounds);
    }

    @Override
    public int getPriorityClass() {
        return 0;
    }
}
