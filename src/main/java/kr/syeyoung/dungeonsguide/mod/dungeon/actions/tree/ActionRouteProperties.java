package kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree;

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
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
