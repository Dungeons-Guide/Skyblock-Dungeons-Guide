package kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder;

import lombok.Getter;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

@Getter
public class RoomBounds {
    private final int minX;
    private final int minZ;
    private final int maxX;
    private final int maxZ;

    private BlockPos min;
    private BlockPos max;

    private final short shape;

    private final int unitLenX; // X
    private final int unitLenZ; // Z

    public RoomBounds(short shape, BlockPos min, BlockPos max) {
        this.shape = shape;
        this.minX = min.getX();
        this.minZ = min.getZ();
        this.maxX = max.getX();
        this.maxZ = max.getZ();
        this.min = min;
        this.max = max;

        unitLenX = (int) Math.ceil((max.getX() - min.getX()) / 32.0);
        unitLenZ = (int) Math.ceil((max.getZ() - min.getZ()) / 32.0);
    }

    public boolean canAccessAbsolute(int x, int y, int z) {
        return canAccessRelative(x - minX, z - minZ);
    }

    public boolean canAccessAbsolute(BlockPos pos) {
        return canAccessRelative(pos.getX() - minX, pos.getZ() - minZ);
    }

    public boolean canAccessRelative(int x, int z) {
        if (x/32 >= 4 || z / 32 >= 4) return false;
        boolean firstCond =  x> 0 && z > 0 && (shape >>((z/32) *4 +(x/32)) & 0x1) > 0;
        boolean zCond = (shape >> ((z / 32) * 4 + (x / 32) - 1) & 0x1) > 0;
        boolean xCond = (shape >> ((z / 32) * 4 + (x / 32) - 4) & 0x1) > 0;
        if (x % 32 == 0 && z % 32 == 0) {
            return firstCond && (shape >>((z/32) *4 +(x/32) - 5) & 0x1) > 0
                    && xCond
                    && zCond;
        } else if (x % 32 == 0) {
            return firstCond && zCond;
        } else if (z % 32 == 0) {
            return firstCond && xCond;
        }

        return firstCond;
    }


    public boolean isFullyWithin(Vec3 vec) {
        if (vec.xCoord * 2 <= minX * 2 + 3 || vec.zCoord * 2 <= minZ * 2 + 3) return false;
        if (vec.xCoord * 2 >= maxX * 2 + 1 || vec.zCoord * 2 >= maxZ * 2 + 1) return false;
        if (!canAccessRelative( (int) Math.floor((vec.xCoord * 2 - minX * 2 - 1) / 2), (int) Math.floor((vec.zCoord * 2 - minZ * 2 - 1) / 2))) return false;
        if (!canAccessRelative( (int) Math.floor((vec.xCoord * 2 - minX * 2 + 1) / 2), (int) Math.floor((vec.zCoord * 2 - minZ * 2 + 1) / 2))) return false;
        return true;
    }
}
