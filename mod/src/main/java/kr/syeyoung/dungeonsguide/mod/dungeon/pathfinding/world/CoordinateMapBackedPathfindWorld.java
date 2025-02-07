package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.world;

import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.IPathfindWorld;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.RoomBounds;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;

import java.util.Set;

public class CoordinateMapBackedPathfindWorld implements IPathfindWorld {
    private ICoordinateMap<IBlockState> backingWorld;

    private AlgorithmSetting algorithmSetting;

    private BitCachingCoordinateMap<PearlCalculatingCoordinateMap.PearlLandType> enderpearl;
    private BitCachingCoordinateMap<CollisionStateCalculatingCoordinateMap.CollisionState> whole;
    private InstaBreakFactorCalculatingCoordinateMap instaBreak;

    public CoordinateMapBackedPathfindWorld(ICoordinateMap<IBlockState> backingWorld, AlgorithmSetting algorithmSetting, RoomBounds roomBounds, Set<BlockPos> superboom) { // plan to remove roombounds.
        this.backingWorld = backingWorld;

        minx = roomBounds.getMinX() * 2 + 2; miny = 0; minz = roomBounds.getMinZ() * 2 + 2;
        maxx = roomBounds.getMaxX() * 2 + 2; maxy = 255 * 2 + 2; maxz = roomBounds.getMaxZ() * 2 + 2;
        lenx = maxx - minx; leny = maxy - miny; lenz = maxz - minz;

        instaBreak = new InstaBreakFactorCalculatingCoordinateMap(backingWorld, algorithmSetting);
        enderpearl = new BitCachingCoordinateMap<>(new PearlCalculatingCoordinateMap(backingWorld, roomBounds), PearlCalculatingCoordinateMap.PearlLandType.VALUES, PearlCalculatingCoordinateMap.PearlLandType.BLOCKED);
        whole = new BitCachingCoordinateMap<>(new CollisionStateCalculatingCoordinateMap(backingWorld, superboom, instaBreak, roomBounds), CollisionStateCalculatingCoordinateMap.CollisionState.VALUES, CollisionStateCalculatingCoordinateMap.CollisionState.BLOCKED);
    }

    @Override
    public IBlockState getActualBlock(int x, int y, int z) {
        return backingWorld.getBlock(x, y, z);
    }

    @Override
    public CollisionStateCalculatingCoordinateMap.CollisionState getBlock(int x, int y, int z) {
        return whole.getBlock(x, y, z);
    }

    @Override
    public PearlCalculatingCoordinateMap.PearlLandType getPearl(int x, int y, int z) {
        return enderpearl.getBlock(x, y, z);
    }


    private final int minx;
    private final int miny;
    private final int minz;
    private final int maxx;
    private final int maxy;
    private final int maxz;
    private final int lenx, leny, lenz;

    @Override
    public boolean isInstabreak(int x, int y, int z) {
        if (x < minx || z < minz || x >= maxx || z >= maxz || y < miny || y+4 >= maxy) return false;
        if (x%2 != 0 && z%2 != 0) return false;

        return instaBreak.getBlock(x/2, y/2, z/2).getFactor()  == 0;
    }

    @Override
    public int getXwidth() {
        return lenx;
    }

    @Override
    public int getYwidth() {
        return leny;
    }

    @Override
    public int getZwidth() {
        return lenz;
    }

    @Override
    public int getMinX() {
        return minx;
    }

    @Override
    public int getMinY() {
        return miny;
    }

    @Override
    public int getMinZ() {
        return minz;
    }



    public void resetBlock(BlockPos pos) { // I think it can be optimize due to how it is saved in arr
        for (int x = -2; x <= 2; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -2; z <= 2; z++) {
                    whole.update(pos.getX() * 2 + x, pos.getY() * 2 + y, pos.getZ() *2 + z);
                    enderpearl.update(pos.getX() * 2 + x, pos.getY() * 2 + y, pos.getZ() *2 + z);
                }
            }
        }
    }

    public void resetChunk(int cx, int cz) {
        for (int x = 0; x < 16; x ++) { // fix pf not going through big block updates
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 255; y++) {
                    whole.invalidateCache(cx * 16 + x, y, cz * 16 + z);
                    enderpearl.invalidateCache(cx * 16 + x, y, cz * 16 + z);
                }
            }
        }
    }
}
