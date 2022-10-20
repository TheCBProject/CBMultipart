package codechicken.microblock.init;

import codechicken.lib.model.ModelRegistryHelper;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.client.MicroBlockPartRenderer;
import codechicken.microblock.client.MicroblockItemRenderer;
import codechicken.microblock.factory.StandardMicroFactory;
import codechicken.microblock.item.ItemMicroBlock;
import codechicken.multipart.api.MultipartClientRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Created by covers1624 on 20/10/22.
 */
public class ClientInit {

    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

    private static final ModelRegistryHelper MODEL_HELPER = new ModelRegistryHelper();

    public static void init() {
        LOCK.lock();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientInit::clientSetup);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, ClientInit::onDrawHighlight);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        MultipartClientRegistry.register(CBMicroblockModContent.FACE_MICROBLOCK_PART.get(), MicroBlockPartRenderer.INSTANCE);
        MultipartClientRegistry.register(CBMicroblockModContent.HOLLOW_MICROBLOCK_PART.get(), MicroBlockPartRenderer.INSTANCE);
        MODEL_HELPER.register(new ModelResourceLocation(CBMicroblockModContent.MICRO_BLOCK_ITEM.getId(), "inventory"), new MicroblockItemRenderer());
    }

    // TODO move elsewhere
    private static void onDrawHighlight(DrawSelectionEvent.HighlightBlock event) {
        Camera camera = event.getCamera();
        if (camera.getEntity() instanceof Player player) {
            ItemStack stack = player.getMainHandItem();
            // TODO offhand?

            // Nothing to do.
            if (stack.isEmpty() || !stack.is(CBMicroblockModContent.MICRO_BLOCK_ITEM.get())) return;

            PoseStack pStack = event.getPoseStack();
            pStack.pushPose();
            pStack.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
            if (renderHighlight(player, InteractionHand.MAIN_HAND, stack, event.getTarget(), pStack, event.getMultiBufferSource(), event.getPartialTicks())) {
                event.setCanceled(true);
            }

            pStack.popPose();
        }
    }

    private static boolean renderHighlight(Player player, InteractionHand mainHand, ItemStack stack, BlockHitResult hit, PoseStack pStack, MultiBufferSource buffers, float partialTicks) {
        MicroMaterial material = ItemMicroBlock.getMaterialFromStack(stack);
        StandardMicroFactory factory = ItemMicroBlock.getFactory(stack);
        int size = ItemMicroBlock.getSize(stack);

        if (material == null || factory == null) return false;

        factory.placementProperties().placementGrid().render(pStack, new Vector3(hit.getLocation()), hit.getDirection().ordinal(), buffers);
        return true;
    }
}
