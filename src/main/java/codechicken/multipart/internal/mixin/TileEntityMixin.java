package codechicken.multipart.internal.mixin;

import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.init.CBMultipartModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created by covers1624 on 21/3/21.
 */
@Mixin (BlockEntity.class)
public class TileEntityMixin {

    @Inject (
            method = "loadStatic",
            at = @At ("HEAD"),
            cancellable = true
    )
    private static void onLoadStatic(BlockPos pos, BlockState state, CompoundTag tag, CallbackInfoReturnable<BlockEntity> cir) {
        String s = tag.getString("id");
        if (CBMultipartModContent.MULTIPART_TILE_TYPE.get().getRegistryName().toString().equals(s)) {
            cir.setReturnValue(TileMultipart.fromNBT(tag, pos));
        }
    }
}
