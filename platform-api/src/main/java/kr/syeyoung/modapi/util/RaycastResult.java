package kr.syeyoung.modapi.util;

import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.UEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class RaycastResult {
    private VectorI3D blockHit;
    private HitType type;
    private Vector3D hitVec;
    private UEntity entityHit;

    public enum HitType {
        ENTITY, BLOCK, MISS;
    }
}
