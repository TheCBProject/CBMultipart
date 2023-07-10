package codechicken.multipart.handler;

import codechicken.lib.raytracer.RayTracer;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.init.MultiPartRegistries;
import codechicken.multipart.util.MultipartHelper;
import codechicken.multipart.util.MultipartPlaceContext;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;

/**
 * Created by covers1624 on 1/9/20.
 */
public class PlacementConversionHandler {

    private static final CrashLock LOCK = new CrashLock("Already initialized.");

    public static void init() {
        LOCK.lock();
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, PlacementConversionHandler::onRightClickBlock);
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level world = event.getLevel();

        if (place(event.getEntity(), event.getHand())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(world.isClientSide));
        }
    }

    private static boolean place(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            return false;
        }
        BlockHitResult hit = RayTracer.retrace(player);
        if (hit.getType() == HitResult.Type.MISS) {
            return false;
        }

        MultipartPlaceContext ctx = new MultipartPlaceContext(player, hand, hit);

        if (ctx.getHitDepth() < 1 && place(player, hand, ctx)) {
            return true;
        }
        return place(player, hand, ctx.applyOffset());
    }

    private static boolean place(Player player, InteractionHand hand, MultipartPlaceContext ctx) {
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();

        MultiPart part = MultiPartRegistries.convertItem(ctx);
        if (part == null) return false;

        TileMultipart tile = MultipartHelper.getOrConvertTile(world, pos);
        if (tile == null) return false;

        if (!tile.canAddPart(part)) return false;

        if (!world.isClientSide) {
            TileMultipart.addPart(world, pos, part);
            SoundType sound = part.getPlacementSound(ctx);
            if (sound != null) {
                world.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            }
            if (!player.getAbilities().instabuild) {
                ItemStack held = ctx.getItemInHand();
                held.shrink(1);
                if (held.isEmpty()) {
                    ForgeEventFactory.onPlayerDestroyItem(player, held, hand);
                }
            }
        } else {
            player.swing(hand);
        }

        return true;
    }
}