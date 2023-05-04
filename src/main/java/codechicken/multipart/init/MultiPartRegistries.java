package codechicken.multipart.init;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.multipart.CBMultipart;
import codechicken.multipart.api.MultipartType;
import codechicken.multipart.api.PartConverter;
import codechicken.multipart.api.PartConverter.ConversionResult;
import codechicken.multipart.api.part.MultiPart;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

/**
 * Created by covers1624 on 3/16/20.
 */
public class MultiPartRegistries {

    private static final Logger logger = LogManager.getLogger();
    private static final CrashLock LOCK = new CrashLock("Already initialized.");

    public static IForgeRegistry<MultipartType<?>> MULTIPART_TYPES;
    private static IForgeRegistry<PartConverter> PART_CONVERTERS;

    public static void init() {
        LOCK.lock();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(MultiPartRegistries::createRegistries);
    }

    private static void createRegistries(NewRegistryEvent event) {
        event.create(new RegistryBuilder<MultipartType<?>>()
                .setName(MultipartType.MULTIPART_TYPES)
                .setType(unsafeCast(MultipartType.class))
                .disableSaving(), e -> MULTIPART_TYPES = (ForgeRegistry<MultipartType<?>>) e);
        event.create(new RegistryBuilder<PartConverter>()
                        .setName(new ResourceLocation(CBMultipart.MOD_ID, "part_converters"))
                        .setType(PartConverter.class)
                        .disableOverrides()
                        .disableSaving()
                        .disableSync(),
                e -> PART_CONVERTERS = e
        );
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
        MultipartType<?> type = Objects.requireNonNull(part.getType());
        ResourceLocation name = Objects.requireNonNull(type.getRegistryName());
        if (!MULTIPART_TYPES.containsKey(name)) {
            throw new RuntimeException("MultiPartType with name '" + name + "' is not registered.");
        }
        data.writeRegistryIdUnsafe(MULTIPART_TYPES, type);
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
        MultipartType<?> type = data.readRegistryIdUnsafe(MULTIPART_TYPES);
        MultiPart part = type.createPartClient(data);
        part.readDesc(data);
        return part;
    }

    /**
     * Saves a {@link MultiPart} to an NBT tag.
     * The part must have a valid {@link MultiPart#getType()}.
     * <p>
     * First writes {@link MultipartType#getRegistryName()} to the 'id'
     * tag, then calls {@link MultiPart#save(CompoundTag)}.
     *
     * @param nbt  The NBT tag to write to.
     * @param part The {@link MultiPart} to write.
     * @return The same NBT tag provided.
     */
    public static CompoundTag savePart(CompoundTag nbt, MultiPart part) {
        MultipartType<?> type = Objects.requireNonNull(part.getType());
        ResourceLocation name = Objects.requireNonNull(type.getRegistryName());
        nbt.putString("id", name.toString());
        part.save(nbt);
        return nbt;
    }

    /**
     * Loads a {@link MultiPart} from an NBT tag.
     * First looks up the {@link MultipartType} from the 'id' tag,
     * Missing {@link MultipartType}s are currently ignored and destroyed,
     * then calls {@link MultipartType#createPartServer(CompoundTag)}
     * if the result is non null, then calls {@link MultiPart#load(CompoundTag)}.
     *
     * @param nbt The NBT tag to read from.
     * @return The new {@link MultiPart} instance, or null.
     */
    @Nullable
    public static MultiPart loadPart(CompoundTag nbt) {
        ResourceLocation name = new ResourceLocation(nbt.getString("id"));
        MultipartType<?> type = MULTIPART_TYPES.getValue(name);
        if (type == null) {
            //TODO 'dummy' parts to save these.
            logger.error("Missing mapping for MultiPartType with ID: {}", name);
            return null;
        }
        MultiPart part = type.createPartServer(nbt);
        if (part != null) {
            part.load(nbt);
        }
        return part;
    }

    public static Collection<MultiPart> convertBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        for (PartConverter conv : PART_CONVERTERS.getValues()) {
            ConversionResult<Collection<MultiPart>> result = conv.convert(level, pos, state);
            if (result.success()) {
                assert result.result() != null;
                return result.result();
            }
        }
        return List.of();
    }

    @Nullable
    public static MultiPart convertItem(UseOnContext context) {
        for (PartConverter conv : PART_CONVERTERS.getValues()) {
            ConversionResult<MultiPart> result = conv.convert(context);
            if (result.success()) {
                assert result.result() != null;
                return result.result();
            }
        }
        return null;
    }

}
