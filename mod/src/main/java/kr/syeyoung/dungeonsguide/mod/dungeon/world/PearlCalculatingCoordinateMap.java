package kr.syeyoung.dungeonsguide.mod.dungeon.world;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.RoomBounds;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class PearlCalculatingCoordinateMap implements ICoordinateMap<PearlCalculatingCoordinateMap.PearlLandType> {
    protected ICoordinateMap<IBlockState> map;
    @Getter
    private int minX, minY, minZ, maxX, maxY, maxZ, lenX, lenY, lenZ;

    private CoordinateMapWorld world;
    private RoomBounds roomBounds;

    public PearlCalculatingCoordinateMap(ICoordinateMap<IBlockState> map, RoomBounds roomBounds) {
        this.map = map;
        this.world = new CoordinateMapWorld(map);

        this.minX = roomBounds.getMinX() * 2 + 2;
        this.minY = 0;
        this.minZ = roomBounds.getMinZ() * 2 + 2;
        this.maxX = roomBounds.getMaxX() * 2 + 2;
        this.maxY = 256 * 2;
        this.maxZ = roomBounds.getMaxZ() * 2 + 2;

        this.lenX = maxX - minX;
        this.lenY = maxY - minY;
        this.lenZ = maxZ - minZ;
        this.roomBounds = roomBounds;
    }

    public PearlLandType getBlock(int x, int y, int z) {
        if (!roomBounds.canAccessRelative( (x - minX + 2) / 2, (z - minZ + 2) / 2)) return PearlLandType.BLOCKED;

        float wX = x / 2.0f, wY = y / 2.0f, wZ = z / 2.0f;

        AxisAlignedBB pearlTest = AxisAlignedBB.fromBounds(
                wX-0.3, wY-0.3, wZ-0.3, wX+ 0.3, wY+ 0.3, wZ + 0.3
        );

        int minX = MathHelper.floor_double(pearlTest.minX);
        int maxX = MathHelper.floor_double(pearlTest.maxX + 1.0D);
        int minY = MathHelper.floor_double(pearlTest.minY);
        int maxY = MathHelper.floor_double(pearlTest.maxY + 1.0D);
        int minZ = MathHelper.floor_double(pearlTest.minZ);
        int maxZ = MathHelper.floor_double(pearlTest.maxZ + 1.0D);

        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        List<AxisAlignedBB> pearlList = new ArrayList<>();
        for (int k1 = minX; k1 < maxX; ++k1) {
            for (int l1 = minZ; l1 < maxZ; ++l1) {
                label: for (int i2 = minY-1; i2 < maxY; ++i2) {
                    blockPos.set(k1, i2, l1);


                    IBlockState state = map.getBlock(k1, i2, l1);
                    Block block = state.getBlock();
                    block.addCollisionBoxesToList(
                            world, blockPos, state, pearlTest, pearlList, null
                    );
                }
            }
        }
        if (pearlList.isEmpty()) return PearlLandType.OPEN;
        double wholeVolume = 0;
        double topVolume = 0;
        for (AxisAlignedBB a : pearlList) {
            double miX = Math.max(a.minX, pearlTest.minX);
            double miY = Math.max(a.minY, pearlTest.minY);
            double miZ = Math.max(a.minZ, pearlTest.minZ);
            double maX = Math.min(a.maxX, pearlTest.maxX);
            double maY = Math.min(a.maxY, pearlTest.maxY);
            double maZ = Math.min(a.maxZ, pearlTest.maxZ);
            wholeVolume += (maX - miX) * (maY - miY) * (maZ - miZ);
            miY = Math.max(a.minY, pearlTest.minY+0.3);
            if (miY > maY) continue;
            topVolume += (maX - miX) * (maY - miY) * (maZ - miZ);
        }
        // total is 0.216
        if (wholeVolume > 0.215) return PearlLandType.BLOCKED;
        if (wholeVolume > 0.027 && 0 == topVolume) return PearlLandType.FLOOR;
        if (wholeVolume  == topVolume && wholeVolume > 0.027) return PearlLandType.CEILING;
        // floor wall and ceiling wall.
        if (wholeVolume - topVolume > 0.027 && topVolume > 0 && wholeVolume != topVolume * 2) return PearlLandType.FLOOR_WALL;
        if (wholeVolume > 0) return PearlLandType.WALL;
        return PearlLandType.OPEN;
    }

    @Override
    public boolean isInScope(int x, int y, int z) {
        return !(x < minX || z < minZ || x >= maxX || z >= maxZ || y < minY || y+4 >= maxY);
    }

    public enum PearlLandType {
        UNCACHED, FLOOR, CEILING, FLOOR_WALL, WALL, BLOCKED, OPEN;

        public static final int BITS = (int) Math.ceil(Math.log(PearlLandType.values().length ) / Math.log(2));
        public static final PearlLandType[] VALUES = PearlLandType.values();
    }
}
