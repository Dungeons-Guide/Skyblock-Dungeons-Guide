package kr.syeyoung.dungeonsguide.mod.dungeon.world;

import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.world.IBlockAccessible;
import kr.syeyoung.modapi.world.UBlockState;
import kr.syeyoung.modapi.world.UTileEntity;

public class CoordinateMapBlockAccessible implements IBlockAccessible {
    private ICoordinateMap<UBlockState> map;

    public CoordinateMapBlockAccessible(ICoordinateMap<UBlockState> map) {
        this.map = map;
    }

    @Override
    public UBlockState getBlockStateAt(int x, int y, int z) {
        return map.getBlock(x,y,z);
    }

    @Override
    public UBlockState getBlockStateAt(VectorI3D blockPos) {
        return map.getBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @Override
    public UTileEntity getTileEntityAt(int x, int y, int z) {
        return null;
    }

    @Override
    public UTileEntity getTileEntityAt(VectorI3D blockPos) {
        return null;
    }
}
