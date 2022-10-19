package kr.syeyoung.dungeonsguide.dungeon.actions.tree;

import kr.syeyoung.dungeonsguide.config.types.AColor;
import lombok.Data;

@Data
public class ActionRouteProperties {
    private boolean pathfind;
    private int lineRefreshRate;
    private AColor lineColor;
    private float lineWidth;

    private boolean beacon;
    private AColor beaconColor;
    private AColor beaconBeamColor;
}
