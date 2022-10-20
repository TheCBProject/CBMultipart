package codechicken.microblock.part;

import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.factory.MicroblockPartFactory;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.block.TileMultiPart;
import codechicken.multipart.util.ControlKeyModifier;
import codechicken.multipart.util.MultiPartHelper;
import codechicken.multipart.util.OffsetUseOnContext;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 20/10/22.
 */
// TODO make this all static?
public class MicroblockPlacement {

    private final Player player;
    private final InteractionHand hand;
    private final BlockHitResult hit;
    private final int size;
    private final MicroMaterial material;
    private final boolean checkMaterial;
    private final PlacementProperties pp;

    private final Level level;
    private final MicroblockPartFactory microFactory;
    private final BlockPos pos;
    private final Vector3 vHit;
    private final Pair<TileMultiPart, Boolean> gTile;
    private final TileMultiPart hTile;
    private final int side;
    private final int slot;
    private final int oSlot;

    private final double hitDepth;
    private final boolean useOppMod;
    private final boolean oppMod;
    private final boolean internal;
    private final boolean doExpand;

    public MicroblockPlacement(Player player, InteractionHand hand, BlockHitResult hit, int size, MicroMaterial material, boolean checkMaterial, PlacementProperties pp) {
        this.player = player;
        this.hand = hand;
        this.hit = hit;
        this.size = size;
        this.material = material;
        this.checkMaterial = checkMaterial;
        this.pp = pp;

        level = player.level;
        microFactory = pp.microFactory();
        pos = hit.getBlockPos();
        vHit = new Vector3(hit.getLocation()).subtract(pos);
        gTile = MultiPartHelper.getOrConvertTile2(level, pos);
        hTile = gTile.getLeft();
        side = hit.getDirection().ordinal();
        slot = pp.placementGrid().getHitSlot(vHit, side);
        oSlot = pp.opposite(slot, side);

        hitDepth = getHitDepth(vHit, side);
        useOppMod = pp.sneakOpposite(slot, side);
        oppMod = ControlKeyModifier.isControlDown(player);
        internal = hitDepth < 1 && hTile != null;
        doExpand = internal && !gTile.getRight() && !player.isCrouching() && !(oppMod && useOppMod) && pp.expand(slot, side);
    }

    public ExecutablePlacement apply() {
        ExecutablePlacement customPlacement = pp.customPlacement(this);
        if (customPlacement != null) return customPlacement;

        // TODO impossible now as we use Direction ordinal?
        if (slot < 0) return null;

        if (doExpand) {
            TMultiPart hPart = ((PartRayTraceResult) hit).part;
            if (hPart.getType() == microFactory.getType()) {
                StandardMicroblockPart mPart = (StandardMicroblockPart) hPart;
                if (mPart.material == material && mPart.getSize() + size < 8) {
                    return expand(mPart);
                }
            }
        }

        if (internal) {
            if (hitDepth < 0.5 || !useOppMod) {
                ExecutablePlacement ret = internalPlacement(hTile, slot);
                if (ret != null) {
                    if (useOppMod && oppMod) {
                        return internalPlacement(hTile, oSlot);
                    }

                    return ret;
                }
            }
            if (useOppMod && !oppMod) {
                return internalPlacement(hTile, oSlot);
            }
            return externalPlacement(slot);
        }

        if (useOppMod && oppMod) {
            return externalPlacement(oSlot);
        }

        return externalPlacement(slot);

    }

    @Nullable
    public ExecutablePlacement expand(StandardMicroblockPart part) {
        return expand(part, create(part.getSize() + size, part.getSlot(), part.material));
    }

    @Nullable
    public ExecutablePlacement expand(MicroblockPart mPart, MicroblockPart nPart) {
        BlockPos pos = mPart.tile().getBlockPos();
        if (TileMultiPart.isUnobstructed(level, pos, nPart) && mPart.tile().canReplacePart(mPart, nPart)) {
            return new ExecutablePlacement.ExpandingPlacement(pos, nPart, mPart);
        }
        return null;
    }

    @Nullable
    private ExecutablePlacement internalPlacement(TileMultiPart tile, int slot) {
        return internalPlacement(tile, create(size, slot, material));
    }

    @Nullable
    private ExecutablePlacement internalPlacement(TileMultiPart tile, MicroblockPart part) {
        BlockPos pos = tile.getBlockPos();
        if (TileMultiPart.isUnobstructed(level, pos, part) && tile.canAddPart(part)) {
            return new ExecutablePlacement.AdditionPlacement(pos, part);
        }
        return null;
    }

    @Nullable
    private ExecutablePlacement externalPlacement(int slot) {
        return externalPlacement(create(size, slot, material));
    }

    @Nullable
    private ExecutablePlacement externalPlacement(MicroblockPart part) {
        BlockPos pos = this.pos.relative(Direction.from3DDataValue(side));
        if (TileMultiPart.canPlacePart(new OffsetUseOnContext(new UseOnContext(player, hand, hit)), part)) {
            return new ExecutablePlacement.AdditionPlacement(pos, part);
        }
        return null;
    }

    private static double getHitDepth(Vector3 vHit, int side) {
        return vHit.copy().scalarProject(Rotation.axes[side]) + (side % 2 ^ 1);
    }

    private MicroblockPart create(int size, int slot, MicroMaterial material) {
        MicroblockPart part = microFactory.create(level.isClientSide, material);
        part.setShape(size, slot);
        return part;
    }
}
