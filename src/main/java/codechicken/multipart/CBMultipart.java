package codechicken.multipart;

import codechicken.lib.world.TileChunkLoadHook;
import codechicken.multipart.api.RegisterMultipartTraitsEvent;
import codechicken.multipart.api.part.*;
import codechicken.multipart.api.part.redstone.RedstonePart;
import codechicken.multipart.handler.PlacementConversionHandler;
import codechicken.multipart.init.CBMultipartModContent;
import codechicken.multipart.init.ClientInit;
import codechicken.multipart.init.DataGenerators;
import codechicken.multipart.init.MultiPartRegistries;
import codechicken.multipart.network.MultiPartNetwork;
import codechicken.multipart.trait.*;
import codechicken.multipart.util.MultipartGenerator;
import codechicken.multipart.util.MultipartLoadHandler;
import codechicken.multipart.util.TickScheduler;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static codechicken.multipart.CBMultipart.MOD_ID;

/**
 * Created by covers1624 on 30/8/20.
 */
@Mod (MOD_ID)
public class CBMultipart {

    public static final String MOD_ID = "cb_multipart";

    public CBMultipart() {
        CBMultipartModContent.init();
        MultiPartRegistries.init();
        DataGenerators.init();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientInit::init);

        MultipartGenerator.INSTANCE.load();
        MultipartLoadHandler.init();
        MultiPartNetwork.init();
        PlacementConversionHandler.init();
        TickScheduler.init();
        TileChunkLoadHook.init();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterMultipartTraits);
    }

    private void onRegisterMultipartTraits(RegisterMultipartTraitsEvent event) {
        event.registerClientTrait(AnimateTickPart.class, TAnimateTickTile.class);
        event.registerTrait(CapabilityProviderPart.class, TCapabilityTile.class);
        event.registerTrait(Container.class, TInventoryTile.class);
        event.registerTrait(WorldlyContainer.class, TInventoryTile.class);
        event.registerTrait(PartialOcclusionPart.class, TPartialOcclusionTile.class);
        event.registerTrait(RedstonePart.class, TRedstoneTile.class);
        event.registerTrait(SlottedPart.class, TSlottedTile.class);
        event.registerTrait(TickablePart.class, TTickableTile.class);
        event.registerServerTrait(NeighborTileChangePart.class, TTileChangeTile.class);
    }
}
