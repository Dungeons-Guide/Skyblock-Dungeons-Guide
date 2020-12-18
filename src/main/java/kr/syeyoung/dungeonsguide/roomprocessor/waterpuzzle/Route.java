package kr.syeyoung.dungeonsguide.roomprocessor.waterpuzzle;

import lombok.Data;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class Route implements Cloneable {
    private Set<WaterNode> nodes = new LinkedHashSet<WaterNode>();
    private Set<WaterCondition> conditionList = new HashSet<WaterCondition>();
    private int x, y;


    @Override
    protected Route clone() {
        Route r = new Route();
        r.getNodes().addAll(nodes);
        r.getConditionList().addAll(conditionList);
        r.x = x;
        r.y = y;
        return r;
    }
}
