package codechicken.multipart.block;

import codechicken.lib.raytracer.RayTracer;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.util.MultiPartLoadHandler;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by covers1624 on 1/1/21.
 */
public class BlockMultiPart extends Block {

    public BlockMultiPart() {
        super(Block.Properties.of(Material.STONE)
                .dynamicShape()
                .noOcclusion()
        );
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new MultiPartLoadHandler.TileNBTContainer();
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public boolean isAir(BlockState state, IBlockReader world, BlockPos pos) {
        TileMultiPart tile = getTile(world, pos);
        return tile == null || tile.getPartList().isEmpty();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        TileMultiPart tile = getTile(world, pos);
        return tile == null ? VoxelShapes.empty() : tile.getOutlineShape();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        TileMultiPart tile = getTile(world, pos);
        return tile == null ? VoxelShapes.empty() : tile.getCollisionShape();
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, IBlockReader world, BlockPos pos) {
        TileMultiPart tile = getTile(world, pos);
        return tile == null ? VoxelShapes.empty() : tile.getRenderOcclusionShape();
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, IBlockReader world, BlockPos pos) {
        TileMultiPart tile = getTile(world, pos);
        return tile == null ? VoxelShapes.empty() : tile.getInteractionShape();
    }
    
    //TODO getBlockSupportShape
    //TODO getVisualShape

    @Override
    public float getExplosionResistance(BlockState state, IBlockReader world, BlockPos pos, Explosion explosion) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null) {
            return tile.getExplosionResistance(explosion);
        }
        return 0;
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null) {
            return tile.getLightValue();
        }
        return 0;
    }

    @Override
    public float getDestroyProgress(BlockState state, PlayerEntity player, IBlockReader world, BlockPos pos) {
        TileMultiPart tile = getTile(world, pos);
        PartRayTraceResult hit = retracePart(world, pos, player);
        if (tile != null && hit != null) {
            return tile.getDestroyProgress(player, hit);
        }
        return 1 / 100F;
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        TileMultiPart tile = getTile(world, pos);
        PartRayTraceResult hit = retracePart(world, pos, player);
        world.levelEvent(player, 2001, pos, Block.getId(state));

        if (hit == null || tile == null) {
            dropAndDestroy(world, pos);
            return true;
        }

        if (world.isClientSide && tile.isClientTile()) {
            hit.part.addDestroyEffects(hit, Minecraft.getInstance().particleEngine);
            return true;
        }

        tile.harvestPart(hit, player);
        return world.getBlockEntity(pos) == null;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileMultiPart tile = getTile(builder.getParameter(LootParameters.BLOCK_ENTITY));//TODO
        if (tile != null) {
            return tile.getDrops();
        }

        return Collections.emptyList();
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileMultiPart tile = getTile(world, pos);
        PartRayTraceResult hit = retracePart(world, pos, player);
        if (tile != null && hit != null) {
            return tile.getPickBlock(hit);
        }
        return ItemStack.EMPTY;

    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit_) {
        TileMultiPart tile = getTile(world, pos);
        PartRayTraceResult hit = retracePart(world, pos, player);
        if (tile != null && hit != null) {
            return tile.use(player, hit, hand);
        }
        return ActionResultType.FAIL;
    }

    @Override
    public void attack(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        TileMultiPart tile = getTile(world, pos);
        PartRayTraceResult hit = retracePart(world, pos, player);
        if (tile != null && hit != null) {
            tile.attack(player, hit);
        }

    }

    @Override
    public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null) {
            tile.entityInside(entity);
        }
    }

    @Override
    public void stepOn(World world, BlockPos pos, Entity entity) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null) {
            tile.stepOn(entity);
        }
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null) {
            tile.onNeighborBlockChanged(fromPos);
        }
    }

    @Override
    public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null) {
            tile.onNeighborTileChange(neighbor);
        }
    }

    @Override
    public boolean getWeakChanges(BlockState state, IWorldReader world, BlockPos pos) {
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
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null) {
            return side != null && tile.canConnectRedstone(side.ordinal() ^ 1);// 'side' is respect to connecting block, we want with respect to this block
        }
        return false;
    }

    @Override
    public int getDirectSignal(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null) {
            return tile.getDirectSignal(side.ordinal() ^ 1);// 'side' is respect to connecting block, we want with respect to this block
        }
        return 0;
    }

    @Override
    public int getSignal(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null) {
            return tile.getSignal(side.ordinal() ^ 1);// 'side' is respect to connecting block, we want with respect to this block
        }
        return 0;
    }

    @Override
    @OnlyIn (Dist.CLIENT)
    public void animateTick(BlockState state, World world, BlockPos pos, Random rand) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null) {
            tile.animateTick(rand);
        }
    }

    @Override
    @OnlyIn (Dist.CLIENT)
    public boolean addHitEffects(BlockState state, World world, RayTraceResult blockHit, ParticleManager manager) {
        TileMultiPart tile = getTile(world, ((BlockRayTraceResult) blockHit).getBlockPos());
        if (tile != null && blockHit instanceof PartRayTraceResult) {
            PartRayTraceResult hit = (PartRayTraceResult) blockHit;
            hit.part.addHitEffects(hit, manager);
        }
        return true;
    }

    @Override
    @OnlyIn (Dist.CLIENT)
    public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager manager) {
        return true;
    }

    public static void dropAndDestroy(World world, BlockPos pos) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null && !world.isClientSide) {
            tile.dropItems(tile.getDrops());
        }

        world.removeBlock(pos, false);
    }

    public static TileMultiPart getTile(TileEntity tile) {
        if (tile instanceof TileMultiPart) {
            return (TileMultiPart) tile;
        }
        return null;
    }

    public static TileMultiPart getTile(IBlockReader world, BlockPos pos) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileMultiPart) {
            TileMultiPart t = (TileMultiPart) tile;
            if (!t.getPartList().isEmpty()) {
                return t;
            }
        }
        return null;
    }

    public static TMultiPart getPart(IBlockReader world, BlockPos pos, int slot) {
        TileMultiPart tile = getTile(world, pos);
        if (tile != null) {
            return tile.getSlottedPart(slot);
        }
        return null;
    }

    public static PartRayTraceResult retracePart(IBlockReader world, BlockPos pos, PlayerEntity player) {
        BlockRayTraceResult hit = RayTracer.retraceBlock(world, player, pos);
        if (hit instanceof PartRayTraceResult) {
            return (PartRayTraceResult) hit;
        }
        return null;
    }
}
