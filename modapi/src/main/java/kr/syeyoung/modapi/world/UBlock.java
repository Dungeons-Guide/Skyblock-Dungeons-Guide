package kr.syeyoung.modapi.world;

import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.util.RaycastResult;

public interface UBlock {
    String getId();

    String getLocalizedName();

    RaycastResult collisionRaytrace(IBlockAccessible worldIn, VectorI3D pos, Vector3D start, Vector3D end);

    float getBlockHardness(IBlockAccessible worldIn, VectorI3D pos);

    boolean isHarvestPickaxe();
    boolean isHarvestAxe();
    boolean isHarvestShovel();
}
