package codechicken.multipart.api;

import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.block.TileMultiPart;
import codechicken.multipart.util.OffsetUseOnContext;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;

/**
 * Created by covers1624 on 1/1/21.
 */
public abstract class ItemMultiPart extends Item {

    public ItemMultiPart(Properties properties) {
        super(properties);
    }

    public abstract TMultiPart newPart(UseOnContext context);

    @Override
    public InteractionResult useOn(UseOnContext context) {

        Vector3 vHit = new Vector3(context.getClickLocation()).subtract(context.getClickedPos());
        double hitDepth = getHitDepth(vHit, context.getClickedFace().ordinal());

        if (hitDepth < 1 && place(context)) {
            return InteractionResult.SUCCESS;
        }

        return place(new OffsetUseOnContext(context)) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    private boolean place(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();

        TMultiPart part = newPart(context);
        if (part == null || !TileMultiPart.canPlacePart(context, part)) { return false; }

        if (!world.isClientSide) {
            TileMultiPart.addPart(world, pos, part);
            SoundType sound = part.getPlacementSound(context);
            if (sound != null) {
                world.playSound(null, pos, sound.getPlaceSound(),
                        SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            }
        }
        if (!context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return true;
    }

    private static double getHitDepth(Vector3 vHit, int side) {
        return vHit.scalarProject(Rotation.axes[side]) + (side % 2 ^ 1);
    }

}
