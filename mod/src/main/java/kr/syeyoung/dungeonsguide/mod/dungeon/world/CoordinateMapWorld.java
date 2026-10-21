package kr.syeyoung.dungeonsguide.mod.dungeon.world;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;

public class CoordinateMapWorld implements DGWorld {
    private ICoordinateMap<BlockState> map;

    public CoordinateMapWorld(ICoordinateMap<BlockState> map) {
        this.map = map;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return map.getBlock(pos.getX(), pos.getY(), pos.getZ());
    }


    @Override
    public BlockState getBlock(int x, int y, int z) {
        return map.getBlock(x, y, z);
    }

    @Override
    public boolean isInScope(int x, int y, int z) {
        return map.isInScope(x, y, z);
    }

    @Override
    public int getMinX() {
        return map.getMinX();
    }

    @Override
    public int getMinY() {
        return map.getMinY();
    }

    @Override
    public int getMinZ() {
        return map.getMinZ();
    }

    @Override
    public int getMaxX() {
        return map.getMaxX();
    }

    @Override
    public int getMaxY() {
        return map.getMaxY();
    }

    @Override
    public int getMaxZ() {
        return map.getMaxZ();
    }

    // 1.8.9 also overrode isSideSolid (the default inside y 0..256), 26.1 decides face solidity from the block shape
}
