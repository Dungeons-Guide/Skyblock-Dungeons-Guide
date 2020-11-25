package kr.syeyoung.dungeonsguide.dungeon.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OffsetPointSet {
    private List<OffsetPoint> offsetPointList = new ArrayList<OffsetPoint>();
}
