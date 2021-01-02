package codechicken.multipart.init;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.util.CrashLock;
import codechicken.multipart.CBMultipart;
import codechicken.multipart.api.MultiPartType;
import codechicken.multipart.api.PartConverter;
import codechicken.multipart.api.part.TMultiPart;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import static codechicken.lib.util.SneakyUtils.unsafeCast;

/**
 * Created by covers1624 on 3/16/20.
 */
public class MultiPartRegistries {

    private static final Logger logger = LogManager.getLogger();
    private static final CrashLock LOCK = new CrashLock("Already initialized.");

    private static ForgeRegistry<MultiPartType<?>> MULTIPART_TYPES;
    private static IForgeRegistry<PartConverter> PART_CONVERTERS;

    public static void init(IEventBus eventBus) {
        LOCK.lock();
        eventBus.addListener(MultiPartRegistries::createRegistries);
    }

    private static void createRegistries(RegistryEvent.NewRegistry event) {
        MULTIPART_TYPES = unsafeCast(new RegistryBuilder<MultiPartType<?>>()//
                .setName(new ResourceLocation(CBMultipart.MOD_ID, "multipart_types"))//
                .setType(unsafeCast(MultiPartType.class))//
                .disableSaving()//
                .create()//
        );
        PART_CONVERTERS = unsafeCast(new RegistryBuilder<PartConverter>()//
                .setName(new ResourceLocation(CBMultipart.MOD_ID, "part_converters"))//
                .setType(PartConverter.class)//
                .disableOverrides()//
                .disableSaving()//
                .disableSync()//
                .create()//
        );
    }

    /**
     * Writes a {@link TMultiPart} to the provided {@link MCDataOutput} stream.
     * The part must have a valid {@link TMultiPart#getType()}.
     *
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
        data.writeVarInt(MULTIPART_TYPES.getID(name));
        part.writeDesc(data);
    }

    /**
     * Reads a {@link TMultiPart} from a stream.
     * First reads a {@link MultiPartType} id using {@link MCDataInput#readVarInt()}
     * then calls {@link MultiPartType#createPartClient(MCDataInput)}, following that
     * calls {@link TMultiPart#readDesc(MCDataInput)}.
     *
     * This method expects the part to be read without errors, errors
     * will cause the entire part space to break.
     *
     * @param data The stream to read from.
     * @return The TMultiPart.
     */
    public static TMultiPart readPart(MCDataInput data) {
        int id = data.readVarInt();
        MultiPartType<?> type = MULTIPART_TYPES.getValue(id);
        TMultiPart part = type.createPartClient(data);
        part.readDesc(data);
        return part;
    }

    /**
     * Saves a {@link TMultiPart} to an NBT tag.
     * The part must have a valid {@link TMultiPart#getType()}.
     *
     * First writes {@link MultiPartType#getRegistryName()} to the 'id'
     * tag, then calls {@link TMultiPart#save(CompoundNBT)}.
     *
     * @param nbt  The NBT tag to write to.
     * @param part The {@link TMultiPart} to write.
     * @return The same NBT tag provided.
     */
    public static CompoundNBT savePart(CompoundNBT nbt, TMultiPart part) {
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
     * then calls {@link MultiPartType#createPartServer(CompoundNBT)}
     * if the result is non null, then calls {@link TMultiPart#load(CompoundNBT)}.
     *
     * @param nbt The NBT tag to read from.
     * @return The new {@link TMultiPart} instance, or null.
     */
    @Nullable
    public static TMultiPart loadPart(CompoundNBT nbt) {
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

    public static Collection<TMultiPart> convertBlock(IWorld world, BlockPos pos, BlockState state) {
        return PART_CONVERTERS.getValues().stream()//
                .map(c -> c.convert(world, pos, state))//
                .filter(e -> e.getType() == ActionResultType.SUCCESS)//
                .findFirst()//
                .map(ActionResult::getResult)//
                .orElse(Collections.emptyList());
    }

    public static TMultiPart convertItem(ItemUseContext context) {
        return PART_CONVERTERS.getValues().stream()//
                .map(c -> c.convert(context))//
                .filter(e -> e.getType() == ActionResultType.SUCCESS)//
                .findFirst()//
                .map(ActionResult::getResult)//
                .orElse(null);
    }

}
