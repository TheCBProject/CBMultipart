package codechicken.multipart.internal.mixin;

import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.init.CBMultipartModContent;
import codechicken.multipart.network.MultiPartSPH;
import codechicken.multipart.util.MultipartHelper;
import codechicken.multipart.util.MultipartLoadHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Created by covers1624 on 3/31/26.
 */
@Mixin (StructureTemplate.class)
abstract class StructureTemplateMixin {

    @WrapOperation (
            method = "placeInWorld",
            at = @At (
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V"
            )
    )
    private void onLoadWithComponents(BlockEntity instance, CompoundTag tag, HolderLookup.Provider registries, Operation<Void> original) {
        var id = tag.getString("id");
        if (!CBMultipartModContent.MULTIPART_TILE_TYPE.getId().toString().equals(id)) {
            original.call(instance, tag, registries);
            return;
        }
        var tile = TileMultipart.fromNBT(tag, instance.getBlockPos(), registries);
        if (tile != null) {
            MultipartHelper.silentAddTile(instance.getLevel(), instance.getBlockPos(), tile);
            MultiPartSPH.sendDescUpdate(tile);
        }
    }
}
