package kr.syeyoung.dungeonsguide.dungeon.data;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class OffsetPointSet implements Cloneable, Serializable {
    private List<OffsetPoint> offsetPointList = new ArrayList<OffsetPoint>();

    @Override
    public Object clone() throws CloneNotSupportedException {
        OffsetPointSet ops = new OffsetPointSet();
        for (OffsetPoint offsetPoint : offsetPointList) {
            ops.offsetPointList.add((OffsetPoint) offsetPoint.clone());
        }
        return ops;
    }
}
