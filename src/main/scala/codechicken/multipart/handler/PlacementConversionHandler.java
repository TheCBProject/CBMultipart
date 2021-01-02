package codechicken.multipart.handler;

import codechicken.lib.packet.PacketCustom;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.util.CrashLock;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.block.TileMultiPart;
import codechicken.multipart.init.MultiPartRegistries;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
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
        World world = event.getWorld();
        if (world.isRemote) {
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
    public static boolean place(PlayerEntity player, Hand hand, World world) {
        ItemStack held = player.getHeldItem(hand);
        if (held.isEmpty()) {
            return false;
        }
        BlockRayTraceResult hit = RayTracer.retrace(player);
        if (hit == null || hit.getType() == RayTraceResult.Type.MISS) {
            return false;
        }

        BlockPos pos = hit.getPos().offset(hit.getFace());
        ItemUseContext ctx = new ItemUseContext(player, hand, hit);
        TMultiPart part = MultiPartRegistries.convertItem(ctx);

        if (part == null) {
            return false;
        }

        if (!world.isRemote) {
            TileMultiPart.addPart(world, pos, part);
            SoundType sound = part.getPlacementSound(ctx);
            if (sound != null) {
                world.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            }
            if (!player.abilities.isCreativeMode) {
                held.shrink(1);
                if (held.isEmpty()) {
                    ForgeEventFactory.onPlayerDestroyItem(player, held, hand);
                }
            }
        } else {
            player.swingArm(hand);
            PacketCustom packet = new PacketCustom(NET_CHANNEL, S_MULTIPART_PLACEMENT);
            packet.writeBoolean(hand == Hand.MAIN_HAND);
            packet.sendToServer();
        }

        return true;
    }
}
