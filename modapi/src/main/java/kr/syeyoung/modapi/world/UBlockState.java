package kr.syeyoung.modapi.world;

import kr.syeyoung.modapi.data.EnumFacing;

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
}
