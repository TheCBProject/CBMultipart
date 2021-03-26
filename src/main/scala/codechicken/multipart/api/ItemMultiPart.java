package codechicken.multipart.api;

import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.block.TileMultiPart;
import codechicken.multipart.util.OffsetItemUseContext;
import net.minecraft.block.SoundType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by covers1624 on 1/1/21.
 */
public abstract class ItemMultiPart extends Item {

    public ItemMultiPart(Properties properties) {
        super(properties);
    }

    public abstract TMultiPart newPart(ItemUseContext context);

    @Override
    public ActionResultType useOn(ItemUseContext context) {

        Vector3 vHit = new Vector3(context.getClickLocation()).subtract(context.getClickedPos());
        double hitDepth = getHitDepth(vHit, context.getClickedFace().ordinal());

        if (hitDepth < 1 && place(context)) {
            return ActionResultType.SUCCESS;
        }

        return place(new OffsetItemUseContext(context)) ? ActionResultType.SUCCESS : ActionResultType.FAIL;
    }

    private boolean place(ItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();

        TMultiPart part = newPart(context);
        if (part == null || !TileMultiPart.canPlacePart(context, part)) { return false; }

        if (!world.isClientSide) {
            TileMultiPart.addPart(world, pos, part);
            SoundType sound = part.getPlacementSound(context);
            if (sound != null) {
                world.playSound(null, pos, sound.getPlaceSound(),
                        SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            }
        }
        if (!context.getPlayer().abilities.instabuild) {
            context.getItemInHand().shrink(1);
        }
        return true;
    }

    private static double getHitDepth(Vector3 vHit, int side) {
        return vHit.scalarProject(Rotation.axes[side]) + (side % 2 ^ 1);
    }

}
