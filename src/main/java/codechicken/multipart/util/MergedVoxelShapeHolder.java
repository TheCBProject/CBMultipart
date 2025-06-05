package codechicken.multipart.util;

import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Created by covers1624 on 3/10/20.
 *
 * @deprecated Now unused, Functionality inlined into TileMultipart
 */
@Deprecated (forRemoval = true)
public class MergedVoxelShapeHolder<T> {

    private final Set<VoxelShape> shapeParts = new HashSet<>();
    private final Set<VoxelShape> partCache = new HashSet<>();

    private final Function<VoxelShape, VoxelShape> postProcess;

    @Nullable
    private VoxelShape mergedShape;

    public MergedVoxelShapeHolder(Function<VoxelShape, VoxelShape> postProcess) {
        this.postProcess = postProcess;
    }

    public void clear() {
        shapeParts.clear();
        partCache.clear();
        mergedShape = null;
    }

    public VoxelShape update(Collection<T> things, Function<T, VoxelShape> extractor) {
        synchronized (partCache) {
            partCache.clear();
            for (T thing : things) {
                partCache.add(extractor.apply(thing));
            }

            if (!partCache.equals(shapeParts) || mergedShape == null) {
                shapeParts.clear();
                shapeParts.addAll(partCache);

                //Same as VoxelShapes.or(VoxelShapes.empty(), shapeParts.toArray()); Except we skip useless array creation.
                VoxelShape merged = shapeParts.stream().reduce(Shapes.empty(), Shapes::or);
                mergedShape = postProcess.apply(merged);
            }
        }

        return mergedShape;
    }

}
