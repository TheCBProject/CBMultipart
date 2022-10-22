package codechicken.microblock.util;

import codechicken.lib.vec.Cuboid6;
import codechicken.microblock.part.IMicroShrinkRender;
import codechicken.multipart.block.TileMultiPart;

import static codechicken.multipart.util.PartMap.edgeAxisMask;
import static codechicken.multipart.util.PartMap.unpackEdgeBits;

/**
 * Created by covers1624 on 10/7/22.
 */
public class MicroOcclusionHelper {

    public static void shrink(Cuboid6 renderBounds, Cuboid6 b, int side) {
        switch (side) {
            case 0 -> { if (renderBounds.min.y < b.max.y) renderBounds.min.y = b.max.y; }
            case 1 -> { if (renderBounds.max.y > b.min.y) renderBounds.max.y = b.min.y; }
            case 2 -> { if (renderBounds.min.z < b.max.z) renderBounds.min.z = b.max.z; }
            case 3 -> { if (renderBounds.max.z > b.min.z) renderBounds.max.z = b.min.z; }
            case 4 -> { if (renderBounds.min.x < b.max.x) renderBounds.min.x = b.max.x; }
            case 5 -> { if (renderBounds.max.x > b.min.x) renderBounds.max.x = b.min.x; }
        }
    }

    public static int shrinkFrom(IMicroShrinkRender p, IMicroShrinkRender other, Cuboid6 renderBounds) {
        if (shrinkTest(p, other)) {
            shrink(renderBounds, other.getBounds(), shrinkSide(p.getSlot(), other.getSlot()));
        } else if (other.getSlot() < 6 && !other.isTransparent()) { // other gets full face, we didn't shrink, flag rendermask
            boolean flag = switch (other.getSlot()) {
                case 0 -> renderBounds.min.y <= 0;
                case 1 -> renderBounds.max.y >= 1;
                case 2 -> renderBounds.min.z <= 0;
                case 3 -> renderBounds.max.z >= 1;
                case 4 -> renderBounds.min.x <= 0;
                case 5 -> renderBounds.max.x >= 1;
                default -> throw new IllegalArgumentException("Switch Falloff");
            };
            if (flag) {
                return 1 << other.getSlot();
            }
        }
        return 0;
    }

    public static int shrink(IMicroShrinkRender p, Cuboid6 renderBounds, int m) {
        int renderMask = 0;
        TileMultiPart tile = p.tile();
        for (int i = 0; i < m; i++) {
            if (i != p.getSlot()) {
                if (tile.getSlottedPart(i) instanceof IMicroShrinkRender other) {
                    renderMask |= shrinkFrom(p, other, renderBounds);
                }
            }
        }
        return renderMask;
    }

    public static int shrinkSide(int s1, int s2) {
        if (s2 < 6) { // other is a cover
            return s2;
        }
        if (s1 < 15) { // both corners
            int c1 = s1 - 7;
            int c2 = s2 - 7;
            return switch (c1 ^ c2) {
                case 1 -> c2 & 1;
                case 2 -> 2 | (c2 & 2) >> 1;
                case 4 -> 4 | (c2 & 4) >> 2;
                default -> -1;
            };
        }
        if (s2 < 15) { // edge, other corner
            int e1 = s1 - 15;
            int c2 = s2 - 7;
            int ebits = unpackEdgeBits(e1);
            if ((c2 & edgeAxisMask(e1)) != ebits) {
                return -1;
            }

            return (e1 & 0xC) >> 1 | (c2 & (~ebits)) >> (e1 >> 2);
        }

        // both edges
        int e1 = s1 - 15;
        int e2 = s2 - 15;
        int e1bits = unpackEdgeBits(e1);
        int e2bits = unpackEdgeBits(e2);
        if ((e1 & 0xC) == (e2 & 0xC)) { // same axis
            return switch (e1bits ^ e2bits) {
                case 1 -> ((e2bits & 1) == 0) ? 0 : 1;
                case 2 -> ((e2bits & 2) == 0) ? 2 : 3;
                case 4 -> ((e2bits & 4) == 0) ? 4 : 5;
                default -> -1;
            };
        }

        int mask = edgeAxisMask(e1) & edgeAxisMask(e2);
        if ((e1bits & mask) != (e2bits & mask)) {
            return -1;
        }

        return switch (e1 >> 2) {
            case 0 -> ((e2bits & 1) == 0) ? 0 : 1;
            case 1 -> ((e2bits & 2) == 0) ? 2 : 3;
            case 2 -> ((e2bits & 4) == 0) ? 4 : 5;
            default -> throw new IllegalArgumentException("Switch Falloff");
        };
    }

    public static int recalcBounds(IMicroShrinkRender p, Cuboid6 renderBounds) {
        if (p.getSlot() < 6) return shrink(p, renderBounds, 6);
        if (p.getSlot() < 15) return shrink(p, renderBounds, 15);
        return shrink(p, renderBounds, 27);
    }

    public static int shapePriority(int slot) {
        if (slot < 6) return 2;
        if (slot < 15) return 1;
        return 0;
    }

    public static boolean shrinkTest(IMicroShrinkRender a, IMicroShrinkRender b) {
        if (a.getPriorityClass() != b.getPriorityClass()) return a.getPriorityClass() < b.getPriorityClass();

        int shape1 = shapePriority(a.getSlot());
        int shape2 = shapePriority(b.getSlot());

        if (shape1 != shape2) return shape1 < shape2;

        if (a.getSlot() < 6) { //transparency takes precedence for covers
            if (a.isTransparent() != b.isTransparent()) return a.isTransparent();
            if (a.getSize() != b.getSize()) return a.getSize() < b.getSize();
        } else {
            if (a.getSize() != b.getSize()) return a.getSize() < b.getSize();
            if (a.isTransparent() != b.isTransparent()) return a.isTransparent();
        }
        return a.getSlot() < b.getSlot();
    }
}
