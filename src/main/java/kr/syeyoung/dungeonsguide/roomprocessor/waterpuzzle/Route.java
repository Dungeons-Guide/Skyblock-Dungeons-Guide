package kr.syeyoung.dungeonsguide.roomprocessor.waterpuzzle;

import kr.syeyoung.dungeonsguide.roomprocessor.waterpuzzle.nodes.WaterNodeEnd;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Data
public class Route implements Cloneable, Comparable {
    private Set<WaterNode> nodes = new LinkedHashSet<WaterNode>();
    private List<LeverState> conditionList = new ArrayList<LeverState>();
    private Set<WaterNodeEnd> endingNodes = new HashSet<WaterNodeEnd>();


    private int matches = 0;
    private int stateFlops = 0;
    private int notMatches = 0;

    public double calculateCost() {
        return (1.0/matches) * 50 + stateFlops * 10 + notMatches * 100;
    }

    @Override
    protected Route clone() {
        Route r = new Route();
        r.getNodes().addAll(nodes);
        r.getConditionList().addAll(conditionList);
        r.getEndingNodes().addAll(endingNodes);
        return r;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof Route) {
            double var0 = calculateCost();
            double var1 = ((Route)o).calculateCost();
            return Double.compare(var0, var1);
        }
        return 0;
    }
}
