package kr.syeyoung.modapi.entity;

import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;

public interface UEntity {
    public Vector3D getPositionVector();
    public Vector3D getPositionEyes(float partialTicks);
    VectorI3D getPosition();
    Vector3D getLook(float partialTicks);
    String getName();
}
