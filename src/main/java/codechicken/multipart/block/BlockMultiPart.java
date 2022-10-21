package codechicken.multipart.block;

import codechicken.lib.raytracer.RayTracer;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.util.MultiPartLoadHandler;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by covers1624 on 1/1/21.
 */
public class BlockMultiPart extends Block implements EntityBlock {

    public BlockMultiPart() {
        super(Block.Properties.of(Material.STONE)
                .dynamicShape()
                .noOcclusion()
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MultiPartLoadHandler.TileNBTContainer(pos, state);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        TileMultiPart tile = getTile(world, pos);
        return tile == null ? Shapes.empty() : tile.getShape(context);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        TileMultiPart tile = getTile(world, pos);
        return tile == null ? Shapes.empty() : tile.getCollisionShape(context);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter world, BlockPos pos) {
        TileMultiPart tile = getTile(world, pos);
        return tile == null ? Shapes.empty() : tile.getRenderOcclusionShape();
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos) {
        TileMultiPart tile = getTile(world, pos);
        return tile == null ? Shapes.empty() : tile.getInteractionShape();
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter world, BlockPos pos) {
        TileMultiPart tile = getTile(world, pos);
        return tile == null ? Shapes.empty() : tile.getBlockSupportShape();
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        TileMultiPart tile = getTile(world, pos);
        return tile == null ? Shapes.empty() : tile.getVisualShape(context);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null) {
            return tile.getExplosionResistance(explosion);
        }
        return 0;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null) {
            return tile.getLightEmission();
        }
        return 0;
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        TileMultiPart tile = getTile(world, pos);
        PartRayTraceResult hit = retracePart(world, pos, player);
        if (tile != null && hit != null) {
            return tile.getDestroyProgress(player, hit);
        }
        return 1 / 100F;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        TileMultiPart tile = getTile(level, pos);
        PartRayTraceResult hit = retracePart(level, pos, player);
        level.levelEvent(player, 2001, pos, Block.getId(state));

        if (hit == null || tile == null) {
            dropAndDestroy(level, pos);
            return true;
        }

        if (level.isClientSide && tile.isClientTile()) {
            hit.part.addDestroyEffects(hit, Minecraft.getInstance().particleEngine);
            return true;
        }

        tile.harvestPart(hit, player);
        return level.getBlockEntity(pos) == null;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileMultiPart tile = getTile(builder.getParameter(LootContextParams.BLOCK_ENTITY));//TODO
        if (tile != null) {
            return tile.getDrops();
        }

        return Collections.emptyList();
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        TileMultiPart tile = getTile(level, pos);
        PartRayTraceResult hit = retracePart(level, pos, player);
        if (tile != null && hit != null) {
            return tile.getCloneStack(hit);
        }
        return ItemStack.EMPTY;

    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit_) {
        TileMultiPart tile = getTile(world, pos);
        PartRayTraceResult hit = retracePart(world, pos, player);
        if (tile != null && hit != null) {
            return tile.use(player, hit, hand);
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void attack(BlockState state, Level world, BlockPos pos, Player player) {
        TileMultiPart tile = getTile(world, pos);
        PartRayTraceResult hit = retracePart(world, pos, player);
        if (tile != null && hit != null) {
            tile.attack(player, hit);
        }

    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null) {
            tile.entityInside(entity);
        }
    }

    @Override
    public void stepOn(Level world, BlockPos pos, BlockState state, Entity entity) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null) {
            tile.stepOn(entity);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null) {
            tile.onNeighborBlockChanged(fromPos);
        }
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null) {
            tile.onNeighborTileChange(neighbor);
        }
    }

    @Override
    public boolean getWeakChanges(BlockState state, LevelReader world, BlockPos pos) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null) {
            return tile.getWeakChanges();
        }
        return false;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction side) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null) {
            return side != null && tile.canConnectRedstone(side.ordinal() ^ 1);// 'side' is respect to connecting block, we want with respect to this block
        }
        return false;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null) {
            return tile.getDirectSignal(side.ordinal() ^ 1);// 'side' is respect to connecting block, we want with respect to this block
        }
        return 0;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null) {
            return tile.getSignal(side.ordinal() ^ 1);// 'side' is respect to connecting block, we want with respect to this block
        }
        return 0;
    }

    @Override
    @OnlyIn (Dist.CLIENT)
    public void animateTick(BlockState state, Level world, BlockPos pos, Random rand) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null) {
            tile.animateTick(rand);
        }
    }

//    @Override
//    @OnlyIn (Dist.CLIENT)
//    public boolean addHitEffects(BlockState state, World world, RayTraceResult blockHit, ParticleManager manager) {
//        TileMultiPart tile = getTile(world, ((BlockRayTraceResult) blockHit).getBlockPos());
//        if (tile != null && blockHit instanceof PartRayTraceResult) {
//            PartRayTraceResult hit = (PartRayTraceResult) blockHit;
//            hit.part.addHitEffects(hit, manager);
//        }
//        return true;
//    }

    // TODO client block properties stuffs
//    @Override
//    @OnlyIn (Dist.CLIENT)
//    public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager manager) {
//        return true;
//    }

    public static void dropAndDestroy(Level world, BlockPos pos) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null && !world.isClientSide) {
            tile.dropItems(tile.getDrops());
        }

        world.removeBlock(pos, false);
    }

    public static TileMultiPart getTile(BlockEntity tile) {
        if (tile instanceof TileMultiPart mp) {
            return mp;
        }
        return null;
    }

    public static TileMultiPart getTile(BlockGetter world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof TileMultiPart tile) {
            if (!tile.getPartList().isEmpty()) {
                return tile;
            }
        }
        return null;
    }

    public static TMultiPart getPart(BlockGetter world, BlockPos pos, int slot) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null) {
            return tile.getSlottedPart(slot);
        }
        return null;
    }

    public static PartRayTraceResult retracePart(BlockGetter world, BlockPos pos, Player player) {
        return RayTracer.retraceBlock(world, player, pos) instanceof PartRayTraceResult hit ? hit : null;
    }
}
