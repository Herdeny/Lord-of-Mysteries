package top.aurora.lordofmysteries.core;

import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class WorkstationShapes {
    public static final VoxelShape CRUCIBLE = Shapes.or(
            box(2d, 0d, 2d, 5d, 3d, 5d),
            box(11d, 0d, 2d, 14d, 3d, 5d),
            box(2d, 0d, 11d, 5d, 3d, 14d),
            box(11d, 0d, 11d, 14d, 3d, 14d),
            box(1d, 3d, 1d, 15d, 5d, 15d),
            box(1d, 5d, 1d, 3d, 14d, 15d),
            box(13d, 5d, 1d, 15d, 14d, 15d),
            box(3d, 5d, 1d, 13d, 14d, 3d),
            box(3d, 5d, 13d, 13d, 14d, 15d),
            box(0d, 9d, 6d, 1d, 12d, 10d),
            box(15d, 9d, 6d, 16d, 12d, 10d));

    public static final VoxelShape RITUAL_ALTAR = Shapes.or(
            box(0d, 0d, 0d, 16d, 3d, 16d),
            box(3d, 3d, 3d, 13d, 12d, 13d),
            box(0d, 12d, 0d, 16d, 15d, 16d),
            box(1d, 15d, 1d, 15d, 16d, 15d));

    public static final VoxelShape CHARACTERISTIC_SEPARATOR = Shapes.or(
            box(1d, 0d, 1d, 15d, 4d, 15d),
            box(2d, 4d, 6d, 4d, 15d, 10d),
            box(12d, 4d, 6d, 14d, 15d, 10d),
            box(2d, 14d, 6d, 14d, 16d, 10d),
            box(6d, 6d, 6d, 10d, 12d, 10d),
            box(4d, 4d, 4d, 12d, 6d, 12d));

    public static final VoxelShape IMPRINT_WASHING_ALTAR = Shapes.or(
            box(1d, 0d, 1d, 15d, 3d, 15d),
            box(3d, 3d, 3d, 13d, 9d, 13d),
            box(0d, 9d, 0d, 16d, 11d, 16d),
            box(0d, 11d, 0d, 2d, 16d, 16d),
            box(14d, 11d, 0d, 16d, 16d, 16d),
            box(2d, 11d, 0d, 14d, 16d, 2d),
            box(2d, 11d, 14d, 14d, 16d, 16d),
            box(2d, 11d, 2d, 14d, 12d, 14d));

    public static final VoxelShape COMMISSION_BOARD = Shapes.or(
            box(2d, 0d, 3d, 14d, 2d, 13d),
            box(3d, 2d, 6d, 5d, 8d, 9d),
            box(11d, 2d, 6d, 13d, 8d, 9d),
            box(0d, 7d, 6d, 16d, 16d, 10d),
            box(2d, 8d, 5.8d, 7d, 14d, 6d),
            box(8d, 9d, 5.8d, 14d, 15d, 6d),
            box(2d, 8d, 10d, 14d, 15d, 10.2d));

    private static VoxelShape box(double minX, double minY, double minZ,
                                  double maxX, double maxY, double maxZ) {
        return Shapes.box(minX / 16, minY / 16, minZ / 16,
                maxX / 16, maxY / 16, maxZ / 16);
    }

    private WorkstationShapes() {}
}
