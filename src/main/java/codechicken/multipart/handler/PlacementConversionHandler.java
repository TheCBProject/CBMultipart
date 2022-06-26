package codechicken.multipart.handler;

import codechicken.lib.packet.PacketCustom;
import codechicken.lib.raytracer.RayTracer;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.block.TileMultiPart;
import codechicken.multipart.init.MultiPartRegistries;
import codechicken.multipart.util.MultiPartHelper;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;

import static codechicken.multipart.network.MultiPartNetwork.NET_CHANNEL;
import static codechicken.multipart.network.MultiPartNetwork.S_MULTIPART_PLACEMENT;

/**
 * Created by covers1624 on 1/9/20.
 */
public class PlacementConversionHandler {

    private static final CrashLock LOCK = new CrashLock("Already initialized.");
    //Interaction lock
    private static final ThreadLocal<Object> placing = new ThreadLocal<>();

    public static void init() {
        LOCK.lock();
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, PlacementConversionHandler::onRightClickBlock);
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level world = event.getWorld();
        if (world.isClientSide) {
            if (placing.get() != null) {
                return;
            }
            placing.set(event);
            if (place(event.getPlayer(), event.getHand(), world)) {
                event.setCanceled(true);
            }
            placing.set(null);
        }
    }

    //TODO, unify with ItemMultiPart
    public static boolean place(Player player, InteractionHand hand, Level world) {
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            return false;
        }
        BlockHitResult hit = RayTracer.retrace(player);
        if (hit.getType() == HitResult.Type.MISS) {
            return false;
        }

        BlockPos pos = hit.getBlockPos().relative(hit.getDirection());
        UseOnContext ctx = new UseOnContext(player, hand, hit);
        TMultiPart part = MultiPartRegistries.convertItem(ctx);
        TileMultiPart tile = MultiPartHelper.getOrConvertTile(world, pos);

        if (part == null || tile == null || !tile.canAddPart(part)) {
            return false;
        }

        if (!world.isClientSide) {
            TileMultiPart.addPart(world, pos, part);
            SoundType sound = part.getPlacementSound(ctx);
            if (sound != null) {
                world.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            }
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
                if (held.isEmpty()) {
                    ForgeEventFactory.onPlayerDestroyItem(player, held, hand);
                }
            }
        } else {
            player.swing(hand);
            PacketCustom packet = new PacketCustom(NET_CHANNEL, S_MULTIPART_PLACEMENT);
            packet.writeBoolean(hand == InteractionHand.MAIN_HAND);
            packet.sendToServer();
        }

        return true;
    }
}
