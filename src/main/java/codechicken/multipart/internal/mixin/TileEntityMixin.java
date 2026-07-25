package codechicken.multipart.internal.mixin;

import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.init.CBMultipartModContent;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created by covers1624 on 21/3/21.
 */
@Mixin (BlockEntity.class)
public class TileEntityMixin {

    @Final
    @Shadow
    private static Codec<BlockEntityType<?>> TYPE_CODEC;

    @Final
    @Shadow
    private static Logger LOGGER;

    @Inject (
            method = "loadStatic",
            at = @At ("HEAD"),
            cancellable = true
    )
    private static void onLoadStatic(BlockPos pos, BlockState state, CompoundTag tag, HolderLookup.Provider registries, CallbackInfoReturnable<BlockEntity> cir) {
        BlockEntityType<?> type = tag.read("id", TYPE_CODEC).orElse(null);
        if (CBMultipartModContent.MULTIPART_TILE_TYPE.get() == type) {
            try (var reporter = new ProblemReporter.ScopedCollector(() -> BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type) + "@" + pos, LOGGER)) {
                cir.setReturnValue(TileMultipart.fromNBT(
                        TagValueInput.create(reporter, registries, tag),
                        pos
                ));
            } catch (Throwable ex) {
                LOGGER.error("Failed to load data for block entity {} for block {} at position {}", type, pos, state, ex);
                cir.setReturnValue(null);
            }
        }
    }
}
