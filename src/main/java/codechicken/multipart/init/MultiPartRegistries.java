package codechicken.multipart.init;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.multipart.api.MultipartType;
import codechicken.multipart.api.PartConverter;
import codechicken.multipart.api.PartConverter.ConversionResult;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.util.MultipartPlaceContext;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Created by covers1624 on 3/16/20.
 */
public class MultiPartRegistries {

    private static final Logger logger = LogManager.getLogger();
    private static final CrashLock LOCK = new CrashLock("Already initialized.");

    private static @Nullable Registry<MultipartType<?>> MULTIPART_TYPES;
    private static @Nullable Registry<PartConverter> PART_CONVERTERS;

    public static void init(IEventBus modBus) {
        LOCK.lock();
        modBus.addListener(MultiPartRegistries::createRegistries);
    }

    private static void createRegistries(NewRegistryEvent event) {
        MULTIPART_TYPES = event.create(new RegistryBuilder<>(MultipartType.MULTIPART_TYPES)
                .sync(true)
        );
        PART_CONVERTERS = event.create(new RegistryBuilder<>(PartConverter.PART_CONVERTERS)
                .sync(false)
        );

    }

    public static Registry<MultipartType<?>> multipartTypes() {
        return requireNonNull(MULTIPART_TYPES, "MultipartType registry not created yet.");
    }

    public static Registry<PartConverter> partConverters() {
        return requireNonNull(PART_CONVERTERS, "PartConverter registry not created yet.");
    }

    /**
     * Writes a {@link MultiPart} to the provided {@link MCDataOutput} stream.
     * The part must have a valid {@link MultiPart#getType()}.
     * <p>
     * First looks up the ID for the parts {@link MultipartType} from
     * {@link MultiPart#getType()}, written to the packet as {@link MCDataOutput#writeVarInt(int)}
     * followed by {@link MultiPart#writeDesc(MCDataOutput)}.
     *
     * @param data The stream to write the data to.
     * @param part The {@link MultiPart} to write to said stream.
     */
    public static void writePart(MCDataOutput data, MultiPart part) {
        MultipartType<?> type = requireNonNull(part.getType());
        ResourceLocation name = requireNonNull(type.getRegistryName());
        if (!MULTIPART_TYPES.containsKey(name)) {
            throw new RuntimeException("MultiPartType with name '" + name + "' is not registered.");
        }
        data.writeRegistryIdDirect(MULTIPART_TYPES, type);
        part.writeDesc(data);
    }

    /**
     * Reads a {@link MultiPart} from a stream.
     * First reads a {@link MultipartType} id using {@link MCDataInput#readVarInt()}
     * then calls {@link MultipartType#createPartClient(MCDataInput)}, following that
     * calls {@link MultiPart#readDesc(MCDataInput)}.
     * <p>
     * This method expects the part to be read without errors, errors
     * will cause the entire part space to break.
     *
     * @param data The stream to read from.
     * @return The TMultiPart.
     */
    public static MultiPart readPart(MCDataInput data) {
        MultipartType<?> type = data.readRegistryIdDirect(MULTIPART_TYPES);
        MultiPart part = type.createPartClient(data);
        part.readDesc(data);
        return part;
    }

    /**
     * Saves a {@link MultiPart} to an NBT tag.
     * The part must have a valid {@link MultiPart#getType()}.
     * <p>
     * First writes {@link MultipartType#getRegistryName()} to the 'id'
     * tag, then calls {@link MultiPart#save(CompoundTag, HolderLookup.Provider)}.
     *
     * @param nbt  The NBT tag to write to.
     * @param part The {@link MultiPart} to write.
     * @return The same NBT tag provided.
     */
    public static CompoundTag savePart(CompoundTag nbt, MultiPart part, HolderLookup.Provider registries) {
        MultipartType<?> type = requireNonNull(part.getType());
        ResourceLocation name = requireNonNull(type.getRegistryName());
        nbt.putString("id", name.toString());
        part.save(nbt, registries);
        return nbt;
    }

    /**
     * Loads a {@link MultiPart} from an NBT tag.
     * First looks up the {@link MultipartType} from the 'id' tag,
     * Missing {@link MultipartType}s are currently ignored and destroyed,
     * then calls {@link MultipartType#createPartServer(CompoundTag)}
     * if the result is non null, then calls {@link MultiPart#load(CompoundTag, HolderLookup.Provider)}.
     *
     * @param nbt The NBT tag to read from.
     * @return The new {@link MultiPart} instance, or null.
     */
    @Nullable
    public static MultiPart loadPart(CompoundTag nbt, HolderLookup.Provider registries) {
        ResourceLocation name = ResourceLocation.parse(nbt.getString("id"));
        MultipartType<?> type = MULTIPART_TYPES.get(name);
        if (type == null) {
            //TODO 'dummy' parts to save these.
            logger.error("Missing mapping for MultiPartType with ID: {}", name);
            return null;
        }
        MultiPart part = type.createPartServer(nbt);
        if (part != null) {
            part.load(nbt, registries);
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
