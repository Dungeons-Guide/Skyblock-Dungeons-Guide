package kr.syeyoung.dungeonsguide.mod.dungeon.world;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.RoomBounds;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.world.IBlockAccessible;
import kr.syeyoung.modapi.world.UBlockState;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class PearlCalculatingCoordinateMap implements ICoordinateMap<PearlCalculatingCoordinateMap.PearlLandType> {
    protected ICoordinateMap<UBlockState> map;
    @Getter
    private int minX, minY, minZ, maxX, maxY, maxZ, lenX, lenY, lenZ;

    private IBlockAccessible world;
    private RoomBounds roomBounds;

    public PearlCalculatingCoordinateMap(ICoordinateMap<UBlockState> map, RoomBounds roomBounds) {
        this.map = map;
        this.world = map instanceof IBlockAccessible ? (IBlockAccessible) map : new CoordinateMapBlockAccessible(map);

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

        AABB pearlTest = new AABB(
                wX-0.3, wY-0.3, wZ-0.3, wX+ 0.3, wY+ 0.3, wZ + 0.3
        );

        int minX = (int) Math.floor(pearlTest.minX);
        int maxX = (int) Math.floor(pearlTest.maxX + 1.0D);
        int minY = (int) Math.floor(pearlTest.minY);
        int maxY = (int) Math.floor(pearlTest.maxY + 1.0D);
        int minZ = (int) Math.floor(pearlTest.minZ);
        int maxZ = (int) Math.floor(pearlTest.maxZ + 1.0D);

        VectorI3D blockPos = new VectorI3D(0,0,0);
        List<AABB> pearlList = new ArrayList<>();
        for (int k1 = minX; k1 < maxX; ++k1) {
            for (int l1 = minZ; l1 < maxZ; ++l1) {
                label: for (int i2 = minY-1; i2 < maxY; ++i2) {
                    blockPos.x = k1; blockPos.y = i2; blockPos.z = l1;


                    UBlockState state = map.getBlock(k1, i2, l1);
                    state.addCollisionBoxesToList(world, blockPos, pearlTest, pearlList);
                }
            }
        }
        if (pearlList.isEmpty()) return PearlLandType.OPEN;
        double wholeVolume = 0;
        double topVolume = 0;
        for (AABB a : pearlList) {
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
