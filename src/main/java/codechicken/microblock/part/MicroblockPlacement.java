package codechicken.microblock.part;

import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.api.MicroMaterial;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.util.ControlKeyModifier;
import codechicken.multipart.util.MultipartHelper;
import codechicken.multipart.util.MultipartPlaceContext;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 20/10/22.
 */
public class MicroblockPlacement {

    public final Player player;
    public final InteractionHand hand;
    public final BlockHitResult hit;
    public final int size;
    public final MicroMaterial material;
    public final boolean checkMaterial;
    public final PlacementProperties pp;

    public final Level level;
    public final MicroblockPartFactory microFactory;
    public final BlockPos pos;
    public final Vector3 vHit;
    public final Pair<TileMultipart, Boolean> gTile;
    public final TileMultipart hTile;
    public final int side;
    public final int slot;
    public final int oSlot;

    public final double hitDepth;
    public final boolean useOppMod;
    public final boolean oppMod;
    public final boolean internal;
    public final boolean doExpand;

    public MicroblockPlacement(Player player, InteractionHand hand, BlockHitResult hit, int size, MicroMaterial material, boolean checkMaterial, PlacementProperties pp) {
        this.player = player;
        this.hand = hand;
        this.hit = hit;
        this.size = size;
        this.material = material;
        this.checkMaterial = checkMaterial;
        this.pp = pp;

        level = player.level();
        microFactory = pp.microFactory();
        pos = hit.getBlockPos();
        vHit = new Vector3(hit.getLocation()).subtract(pos);
        gTile = MultipartHelper.getOrConvertTile2(level, pos);
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

    @Nullable
    public ExecutablePlacement calculate() {
        ExecutablePlacement customPlacement = pp.customPlacement(this);
        if (customPlacement != null) return customPlacement;

        if (slot < 0) return null;

        if (doExpand) {
            MultiPart hPart = ((PartRayTraceResult) hit).part;
            if (hPart.getType() == microFactory) {
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
        BlockPos pos = mPart.pos();
        if (TileMultipart.isUnobstructed(level, pos, nPart) && mPart.tile().canReplacePart(mPart, nPart)) {
            return new ExecutablePlacement.ExpandingPlacement(pos, nPart, mPart);
        }
        return null;
    }

    @Nullable
    public ExecutablePlacement internalPlacement(TileMultipart tile, int slot) {
        return internalPlacement(tile, create(size, slot, material));
    }

    @Nullable
    public ExecutablePlacement internalPlacement(TileMultipart tile, MicroblockPart part) {
        BlockPos pos = tile.getBlockPos();
        if (TileMultipart.isUnobstructed(level, pos, part) && tile.canAddPart(part)) {
            return new ExecutablePlacement.AdditionPlacement(pos, part);
        }
        return null;
    }

    @Nullable
    public ExecutablePlacement externalPlacement(int slot) {
        return externalPlacement(create(size, slot, material));
    }

    @Nullable
    public ExecutablePlacement externalPlacement(MicroblockPart part) {
        BlockPos pos = this.pos.relative(Direction.from3DDataValue(side));
        if (TileMultipart.canPlacePart(new MultipartPlaceContext(player, hand, hit).applyOffset(), part)) {
            return new ExecutablePlacement.AdditionPlacement(pos, part);
        }
        return null;
    }

    public static double getHitDepth(Vector3 vHit, int side) {
        return vHit.copy().scalarProject(Rotation.axes[side]) + (side % 2 ^ 1);
    }

    public MicroblockPart create(int size, int slot, MicroMaterial material) {
        MicroblockPart part = microFactory.create(level.isClientSide, material);
        part.setShape(size, slot);
        return part;
    }
}
