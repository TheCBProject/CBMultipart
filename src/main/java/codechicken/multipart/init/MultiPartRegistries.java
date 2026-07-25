package codechicken.multipart.init;

import codechicken.multipart.api.MultipartType;
import codechicken.multipart.api.PartConverter;
import codechicken.multipart.api.PartConverter.ConversionResult;
import codechicken.multipart.api.RegisterPartConvertersEvent;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.util.MultipartPlaceContext;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Created by covers1624 on 3/16/20.
 */
public class MultiPartRegistries {

    private static final Logger logger = LogManager.getLogger();
    private static final CrashLock LOCK = new CrashLock("Already initialized.");

    private static final List<PartConverter> PART_CONVERTERS = new ArrayList<>();

    public static void init(IEventBus modBus) {
        LOCK.lock();
        modBus.addListener(MultiPartRegistries::createRegistries);

        modBus.addListener(EventPriority.LOWEST, MultiPartRegistries::onLateCommonSetup);
    }

    private static void createRegistries(NewRegistryEvent event) {
        event.register(MultipartType.REGISTRY);
    }

    private static void onLateCommonSetup(FMLCommonSetupEvent event) {
        ModLoader.postEvent(new RegisterPartConvertersEvent(Collections.synchronizedList(PART_CONVERTERS)));
    }

    /**
     * Writes a {@link MultiPart} to the provided {@link RegistryFriendlyByteBuf} stream.
     * The part must have a valid {@link MultiPart#getType()}.
     * <p>
     * First looks up the ID for the parts {@link MultipartType} from
     * {@link MultiPart#getType()}, written to the packet as {@link RegistryFriendlyByteBuf#writeVarInt(int)}
     * followed by {@link MultiPart#writeDesc(RegistryFriendlyByteBuf)}.
     *
     * @param data The stream to write the data to.
     * @param part The {@link MultiPart} to write to said stream.
     */
    public static void writePart(RegistryFriendlyByteBuf data, MultiPart part) {
        data.cc$writeWithRegistryCodec(MultipartType.STREAM_CODEC, requireNonNull(part.getType()));
        part.writeDesc(data);
    }

    /**
     * Reads a {@link MultiPart} from a stream.
     * First reads a {@link MultipartType} id using {@link RegistryFriendlyByteBuf#readVarInt()}
     * then calls {@link MultipartType#createPartClient(RegistryFriendlyByteBuf)}, following that
     * calls {@link MultiPart#readDesc(RegistryFriendlyByteBuf)}.
     * <p>
     * This method expects the part to be read without errors, errors
     * will cause the entire part space to break.
     *
     * @param data The stream to read from.
     * @return The TMultiPart.
     */
    public static MultiPart readPart(RegistryFriendlyByteBuf data) {
        var type = data.cc$readWithRegistryCodec(MultipartType.STREAM_CODEC);
        var part = type.createPartClient(data);
        part.readDesc(data);
        return part;
    }

    /**
     * Saves a {@link MultiPart} to an NBT tag.
     * The part must have a valid {@link MultiPart#getType()}.
     * <p>
     * First writes {@link MultipartType#getRegistryName()} to the 'id'
     * tag, then calls {@link MultiPart#save(ValueOutput)}.
     *
     * @param output The NBT tag to write to.
     * @param part   The {@link MultiPart} to write.
     */
    public static void savePart(ValueOutput output, MultiPart part) {
        output.store("id", MultipartType.TYPE_CODEC, part.getType());
        part.save(output);
    }

    /**
     * Loads a {@link MultiPart} from an NBT tag.
     * First looks up the {@link MultipartType} from the 'id' tag,
     * Missing {@link MultipartType}s are currently ignored and destroyed,
     * then calls {@link MultipartType#createPartServer(ValueInput)}
     * if the result is non null, then calls {@link MultiPart#load(ValueInput)}.
     *
     * @param input The NBT tag to read from.
     * @return The new {@link MultiPart} instance, or null.
     */
    @Nullable
    public static MultiPart loadPart(ValueInput input) {
        var type = input.read("id", MultipartType.TYPE_CODEC);
        if (type.isEmpty()) {
            //TODO 'dummy' parts to save these.
            logger.error("Missing mapping for MultiPartType with ID: {}", input.getStringOr("id", "null"));
            return null;
        }
        MultiPart part = type.get().createPartServer(input);
        if (part != null) {
            part.load(input);
        }
        return part;
    }

    public static Collection<MultiPart> convertBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        for (PartConverter conv : PART_CONVERTERS) {
            ConversionResult<Collection<MultiPart>> result = conv.convert(level, pos, state);
            if (result.success()) {
                assert result.result() != null;
                return result.result();
            }
        }
        return List.of();
    }

    @Nullable
    public static MultiPart convertItem(MultipartPlaceContext context) {
        for (PartConverter conv : PART_CONVERTERS) {
            ConversionResult<MultiPart> result = conv.convert(context);
            if (result.success()) {
                assert result.result() != null;
                return result.result();
            }
        }
        return null;
    }
}
