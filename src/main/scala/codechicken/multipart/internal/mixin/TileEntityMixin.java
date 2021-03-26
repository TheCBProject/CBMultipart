package codechicken.multipart.internal.mixin;

import codechicken.multipart.block.TileMultiPart;
import codechicken.multipart.init.CBMultipartModContent;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created by covers1624 on 21/3/21.
 */
@Mixin (TileEntity.class)
public class TileEntityMixin {

    @Inject (
            method = "loadStatic(Lnet/minecraft/block/BlockState;Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/tileentity/TileEntity;",
            at = @At ("HEAD"),
            cancellable = true
    )
    private static void onLoadStatic(BlockState state, CompoundNBT tag, CallbackInfoReturnable<TileEntity> cir) {
        String s = tag.getString("id");
        if (CBMultipartModContent.tileMultipartType.getRegistryName().toString().equals(s)) {
            cir.setReturnValue(TileMultiPart.fromNBT(tag));
        }
    }
}
