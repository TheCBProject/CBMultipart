package codechicken.multipart.minecraft;

import codechicken.lib.packet.PacketCustom;
import codechicken.lib.raytracer.RayTracer;
import codechicken.multipart.TileMultipart;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandler {

	private ThreadLocal<Object> placing = new ThreadLocal<Object>();

	@SubscribeEvent (priority = EventPriority.LOW)
	public void playerInteract(PlayerInteractEvent.RightClickBlock event) {
		if (event.getWorld().isRemote) {
			if (placing.get() != null) {
				return;//for mods that do dumb stuff and call this event like MFR
			}
			placing.set(event);
			if (place(event.getEntityPlayer(), event.getHand(), event.getEntityPlayer().world)) {
				event.setCanceled(true);
			}
			placing.set(null);
		}
	}

	public static boolean place(EntityPlayer player, EnumHand hand, World world) {
		RayTraceResult hit = RayTracer.retrace(player);
		if (hit == null) {
			return false;
		}

		BlockPos pos = new BlockPos(hit.getBlockPos()).offset(hit.sideHit);
		ItemStack held = player.getHeldItem(hand);
		McMetaPart part = null;
		if (held.isEmpty()) {
			return false;
		}

		Block heldBlock = Block.getBlockFromItem(held.getItem());
		if (heldBlock == null) {
			return false;
		}

		if (heldBlock == Blocks.TORCH) {
			part = new TorchPart();
		} else if (heldBlock == Blocks.LEVER) {
			part = new LeverPart();
		} else if (heldBlock == Blocks.STONE_BUTTON || heldBlock == Blocks.WOODEN_BUTTON) {
			part = new ButtonPart();
		} else if (heldBlock == Blocks.REDSTONE_TORCH) {
			part = new RedstoneTorchPart();
		}

		if (part == null) {
			return false;
		}

		part.setStateOnPlacement(world, pos, hit.sideHit, hit.hitVec, player, held);

		//TODO find purpose of keeping this
		//        if(world.isRemote && !player.isSneaking())//attempt to use block activated like normal and tell the server the right stuff
		//        {
		//            Vector3 f = new Vector3(hit.hitVec).add(-hit.blockX, -hit.blockY, -hit.blockZ);
		//            IBlockState state = world.getBlockState(hit.blockPos);
		//            Block block = state.getBlock();
		//            if(!ignoreActivate(block) && block.onBlockActivated(world, hit.blockPos, state, player, hit.sideHit, (float)f.x, (float)f.y, (float)f.z))
		//            {
		//                player.swingArm(EnumHand.MAIN_HAND);
		//                PacketCustom.sendToServer(new CPacketPlayerBlockPlacement(
		//                        hit.blockX, hit.blockY, hit.blockZ, hit.sideHit,
		//                        player.inventory.getCurrentItem(),
		//                        (float)f.x, (float)f.y, (float)f.z));
		//                return true;
		//            }
		//        }

		TileMultipart tile = TileMultipart.getOrConvertTile(world, pos);
		if (tile == null || !tile.canAddPart(part)) {
			return false;
		}

		if (!world.isRemote) {
			TileMultipart.addPart(world, pos, part);
			SoundType sound = part.getBlock().getSoundType();
			world.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);

			if (!player.capabilities.isCreativeMode) {
				held.shrink(1);
				if (held.getCount() == 0) {
					player.inventory.mainInventory.set(player.inventory.currentItem, ItemStack.EMPTY);
					MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, held, hand));
				}
			}
		} else {
			player.swingArm(hand);
			new PacketCustom(McMultipartSPH.channel, 1).writeBoolean(hand == EnumHand.MAIN_HAND).sendToServer();
		}
		return true;
	}

	//    /**
	//     * Because vanilla is weird.
	//     */
	//    private static boolean ignoreActivate(Block block)
	//    {
	//        if(block instanceof BlockFence)
	//            return true;
	//        return false;
	//    }
}
