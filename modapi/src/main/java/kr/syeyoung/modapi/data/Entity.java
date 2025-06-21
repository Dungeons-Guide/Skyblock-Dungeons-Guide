package kr.syeyoung.modapi.data;

import kr.syeyoung.modapi.RenderingContext;

public interface Entity {
    Vector3D getPosition();
    Vector3D getPositionPrev();
    Vector3D getVelocity();
    Vector3D getPosition(RenderingContext context);
}
