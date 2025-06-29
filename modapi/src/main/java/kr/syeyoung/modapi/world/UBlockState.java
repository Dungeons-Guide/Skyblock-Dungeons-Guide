package kr.syeyoung.modapi.world;

import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.EnumFacing;
import kr.syeyoung.modapi.data.VectorI3D;

public interface UBlockState {
    String getId();
    UBlock getBlock();

    String serialize();


    boolean isOf(BlockType... blocks);

    EnumFacing getLeverFacing();

    boolean hasTileEntity();

    int getWaterLevel();

    int getColor();

    Object getIBlockState();

    AABB getSelectedBoundingBox(IBlockAccessible blockAccessible, VectorI3D pos);

    boolean canCollideCheck(boolean hitIfLiquid);

    int getHarvestLevel();
    String getHarvestTool();
}
