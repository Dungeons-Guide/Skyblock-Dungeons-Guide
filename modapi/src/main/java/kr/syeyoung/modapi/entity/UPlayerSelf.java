package kr.syeyoung.modapi.entity;

import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;

public interface UPlayerSelf extends UEntity {
    String getClientBrand();

    String getName();


    float getPrevRotationYawHead();

    float getRotationYawHead();

}
