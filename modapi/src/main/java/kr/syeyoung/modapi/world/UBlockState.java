package kr.syeyoung.modapi.world;

import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.EnumFacing;
import kr.syeyoung.modapi.data.EnumHalf;
import kr.syeyoung.modapi.data.VectorI3D;

import java.util.List;

public interface UBlockState {
    String getId();
    UBlock getBlock();

    String serialize();


    boolean isOf(BlockType... blocks);

    EnumFacing getLeverFacing();

    EnumFacing getAnyBlockFacingIfItExists();

    boolean hasTileEntity();

    int getWaterLevel();

    int getColor();

    Object getIBlockState();

    AABB getSelectedBoundingBox(IBlockAccessible blockAccessible, VectorI3D pos);

    boolean canCollideCheck(boolean hitIfLiquid);

    int getHarvestLevel();
    String getHarvestTool();

    void addCollisionBoxesToList(IBlockAccessible blockAccessible, VectorI3D pos, AABB mask, List<AABB> list);

    EnumHalf getStairHalf();

    int getLegacyId();
    int getLegacyMeta();

    UBlockState withFacing(EnumFacing facing);

    int getLegacyStateId();

    AABB getCollisionBoundingBox(IBlockAccessible theWorld, VectorI3D blockPos);
}
