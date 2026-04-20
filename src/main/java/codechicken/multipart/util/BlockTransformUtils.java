package codechicken.multipart.util;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;

public class BlockTransformUtils {

    /**
     * Rotates the given axis by the given rotation around the given rotationAxis.
     *
     * @param rotationAxis The axis to rotate around.
     * @param rotation The rotation to apply.
     * @param axis The axis to rotate.
     * @return The rotated axis.
     */
    public static Direction.Axis rotateAxis(Direction.Axis rotationAxis, Rotation rotation, Direction.Axis axis) {
        Direction facing = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        return rotateFacing(rotationAxis, rotation, facing).getAxis();
    }

    /**
     * Rotates the given facing by the given rotation around the given rotationAxis.
     *
     * @param rotationAxis The axis to rotate around.
     * @param rotation The rotation to apply.
     * @param facing The facing direction to rotate.
     * @return The rotated facing direction.
     */
    public static Direction rotateFacing(Direction.Axis rotationAxis, Rotation rotation, Direction facing) {
        for (int i = 0; i < rotation.ordinal(); i++)
            facing = facing.getClockWise(rotationAxis);
        return facing;
    }
}
