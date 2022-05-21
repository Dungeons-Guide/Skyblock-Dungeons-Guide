/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
        return (1.0/matches) * 50 + stateFlops * 20 + notMatches * 10000;
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
