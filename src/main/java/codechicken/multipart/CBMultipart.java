package codechicken.multipart;

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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;

import static codechicken.multipart.CBMultipart.MOD_ID;
import static java.util.Objects.requireNonNull;

/**
 * Created by covers1624 on 30/8/20.
 */
@Mod (MOD_ID)
public class CBMultipart {

    public static final String MOD_ID = "cb_multipart";

    private static @Nullable ModContainer container;

    public CBMultipart(ModContainer container, IEventBus modBus) {
        CBMultipart.container = container;
        CBMultipartModContent.init(modBus);
        MultiPartRegistries.init(modBus);
        DataGenerators.init(modBus);

        if (FMLEnvironment.dist.isClient()) {
            ClientInit.init(modBus);
        }

        MultipartGenerator.INSTANCE.load(modBus);
        MultipartLoadHandler.init();
        MultiPartNetwork.init(modBus);
        PlacementConversionHandler.init();
        TickScheduler.init();

        modBus.addListener(this::onRegisterMultipartTraits);
    }

    public static ModContainer container() {
        return requireNonNull(container);
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
