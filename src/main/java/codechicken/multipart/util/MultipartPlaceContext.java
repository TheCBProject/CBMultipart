package codechicken.multipart.util;

import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.block.TileMultipart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;

import java.util.Objects;

public class MultipartPlaceContext extends BlockPlaceContext {

    private final double hitDepth;
    private final BlockPos offsetPos;

    private boolean isOffset = false;

    public MultipartPlaceContext(UseOnContext context) {
        this(Objects.requireNonNull(context.getPlayer()), context.getHand(), context.getHitResult());
    }

    public MultipartPlaceContext(Player player, InteractionHand hand, BlockHitResult hit) {
        super(player, hand, player.getItemInHand(hand), hit);

        this.hitDepth = calcHitDepth(hit.getLocation(), hit.getBlockPos(), hit.getDirection());
        this.offsetPos = hit.getBlockPos().relative(hit.getDirection());
    }

    /**
     * Puts this placement into offset mode
     *
     * @return this
     */
    @Contract("-> this")
    public MultipartPlaceContext applyOffset() {
        isOffset = true;
        return this;
    }

    /**
     * Distance from the clicked face to that same face of the enclosing block space.
     */
    public double getHitDepth() {
        return hitDepth;
    }

    /**
     * False when placement is being run inside the clicked block, true when it is offset by one block.
     */
    public boolean isOffset() {
        return isOffset;
    }

    /**
     * Checks if part can be added to the world. Useful for cases where your part can optionally be altered
     * if its initially calculated placement state cannot be placed.
     * <p>
     * For example, Lever parts are rectangular with a long and short side. Occlusion may allow placement in
     * one orientation but not another. The initial placement state can be run through this method, and then
     * rotated if placement is not possible.
     * <p>
     * Note that this method does not need to be used if conditional placement states are not required. This is
     * explicitly re-checked once you return your candidate part.
     *
     * @param part The part to test
     * @return True if placement is possible (either the space is empty, or occlusion allows placement)
     */
    public boolean canPlacePart(MultiPart part) {
        return TileMultipart.canPlacePart(this, part);
    }

    @Override
    public BlockPos getClickedPos() {
        return isOffset ? offsetPos : getHitResult().getBlockPos();
    }

    private double calcHitDepth(Vec3 clickLocation, BlockPos clickPos, Direction face) {
        Vector3 vHit = new Vector3(clickLocation).subtract(clickPos);
        int side = face.ordinal();
        return vHit.scalarProject(Rotation.axes[side]) + (side % 2 ^ 1);
    }
}
