package kr.syeyoung.modapi.world;

import kr.syeyoung.modapi.data.VectorI3D;

public interface IBlockAccessible {
    public UBlockState getBlockStateAt(int x, int y, int z);
    public UBlockState getBlockStateAt(VectorI3D blockPos);
}
