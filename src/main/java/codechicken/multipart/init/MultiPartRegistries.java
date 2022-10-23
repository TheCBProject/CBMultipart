package codechicken.multipart.init;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.multipart.CBMultipart;
import codechicken.multipart.api.MultiPartType;
import codechicken.multipart.api.PartConverter;
import codechicken.multipart.api.PartConverter.ConversionResult;
import codechicken.multipart.api.part.TMultiPart;
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

    public static IForgeRegistry<MultiPartType<?>> MULTIPART_TYPES;
    private static IForgeRegistry<PartConverter> PART_CONVERTERS;

    public static void init() {
        LOCK.lock();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(MultiPartRegistries::createRegistries);
    }

    private static void createRegistries(NewRegistryEvent event) {
        event.create(new RegistryBuilder<MultiPartType<?>>()
                .setName(new ResourceLocation(CBMultipart.MOD_ID, "multipart_types"))
                .setType(unsafeCast(MultiPartType.class))
                .disableSaving(), e -> MULTIPART_TYPES = (ForgeRegistry<MultiPartType<?>>) e);
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
     * Writes a {@link TMultiPart} to the provided {@link MCDataOutput} stream.
     * The part must have a valid {@link TMultiPart#getType()}.
     * <p>
     * First looks up the ID for the parts {@link MultiPartType} from
     * {@link TMultiPart#getType()}, written to the packet as {@link MCDataOutput#writeVarInt(int)}
     * followed by {@link TMultiPart#writeDesc(MCDataOutput)}.
     *
     * @param data The stream to write the data to.
     * @param part The {@link TMultiPart} to write to said stream.
     */
    public static void writePart(MCDataOutput data, TMultiPart part) {
        MultiPartType<?> type = Objects.requireNonNull(part.getType());
        ResourceLocation name = Objects.requireNonNull(type.getRegistryName());
        if (!MULTIPART_TYPES.containsKey(name)) {
            throw new RuntimeException("MultiPartType with name '" + name + "' is not registered.");
        }
        data.writeRegistryIdUnsafe(MULTIPART_TYPES, type);
        part.writeDesc(data);
    }

    /**
     * Reads a {@link TMultiPart} from a stream.
     * First reads a {@link MultiPartType} id using {@link MCDataInput#readVarInt()}
     * then calls {@link MultiPartType#createPartClient(MCDataInput)}, following that
     * calls {@link TMultiPart#readDesc(MCDataInput)}.
     * <p>
     * This method expects the part to be read without errors, errors
     * will cause the entire part space to break.
     *
     * @param data The stream to read from.
     * @return The TMultiPart.
     */
    public static TMultiPart readPart(MCDataInput data) {
        MultiPartType<?> type = data.readRegistryIdUnsafe(MULTIPART_TYPES);
        TMultiPart part = type.createPartClient(data);
        part.readDesc(data);
        return part;
    }

    /**
     * Saves a {@link TMultiPart} to an NBT tag.
     * The part must have a valid {@link TMultiPart#getType()}.
     * <p>
     * First writes {@link MultiPartType#getRegistryName()} to the 'id'
     * tag, then calls {@link TMultiPart#save(CompoundTag)}.
     *
     * @param nbt  The NBT tag to write to.
     * @param part The {@link TMultiPart} to write.
     * @return The same NBT tag provided.
     */
    public static CompoundTag savePart(CompoundTag nbt, TMultiPart part) {
        MultiPartType<?> type = Objects.requireNonNull(part.getType());
        ResourceLocation name = Objects.requireNonNull(type.getRegistryName());
        nbt.putString("id", name.toString());
        part.save(nbt);
        return nbt;
    }

    /**
     * Loads a {@link TMultiPart} from an NBT tag.
     * First looks up the {@link MultiPartType} from the 'id' tag,
     * Missing {@link MultiPartType}s are currently ignored and destroyed,
     * then calls {@link MultiPartType#createPartServer(CompoundTag)}
     * if the result is non null, then calls {@link TMultiPart#load(CompoundTag)}.
     *
     * @param nbt The NBT tag to read from.
     * @return The new {@link TMultiPart} instance, or null.
     */
    @Nullable
    public static TMultiPart loadPart(CompoundTag nbt) {
        ResourceLocation name = new ResourceLocation(nbt.getString("id"));
        MultiPartType<?> type = MULTIPART_TYPES.getValue(name);
        if (type == null) {
            //TODO 'dummy' parts to save these.
            logger.error("Missing mapping for MultiPartType with ID: {}", name);
            return null;
        }
        TMultiPart part = type.createPartServer(nbt);
        if (part != null) {
            part.load(nbt);
        }
        return part;
    }

    public static Collection<TMultiPart> convertBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        for (PartConverter conv : PART_CONVERTERS.getValues()) {
            ConversionResult<Collection<TMultiPart>> result = conv.convert(level, pos, state);
            if (result.success()) {
                assert result.result() != null;
                return result.result();
            }
        }
        return List.of();
    }

    @Nullable
    public static TMultiPart convertItem(UseOnContext context) {
        for (PartConverter conv : PART_CONVERTERS.getValues()) {
            ConversionResult<TMultiPart> result = conv.convert(context);
            if (result.success()) {
                assert result.result() != null;
                return result.result();
            }
        }
        return null;
    }

}
