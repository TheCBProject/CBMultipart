package codechicken.multipart.internal.mixin;

import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.init.CBMultipartModContent;
import codechicken.multipart.network.MultiPartNetwork;
import codechicken.multipart.util.MultipartHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Created by covers1624 on 3/31/26.
 */
@Mixin (StructureTemplate.class)
abstract class StructureTemplateMixin {

    /**
     * Swaps in a Multipart tile's real mixin-generated class during structure template load. Required since Multipart
     * tiles load as container tiles by default.
     */
    @WrapOperation (
            method = "placeInWorld",
            at = @At (
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/world/level/storage/ValueInput;)V"
            )
    )
    private void onLoadWithComponents(BlockEntity instance, ValueInput input, Operation<Void> original) {
        var id = input.getString("id").orElse("");
        if (!CBMultipartModContent.MULTIPART_TILE_TYPE.getId().toString().equals(id)) {
            original.call(instance, input);
            return;
        }
        var tile = TileMultipart.fromNBT(input, instance.getBlockPos());
        if (tile != null) {
            MultipartHelper.silentAddTile(instance.getLevel(), instance.getBlockPos(), tile);
            MultiPartNetwork.sendDescUpdate(tile);
        }
    }
}
