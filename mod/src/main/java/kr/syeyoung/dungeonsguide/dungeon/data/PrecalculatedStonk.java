/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.dungeon.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class PrecalculatedStonk implements Serializable {
    private final List<PossibleClickingSpot>[] spots;
    private final List<String> dependentRouteBlocker;

    public PrecalculatedStonk(List<String> dependentRouteBlocker, List<PossibleClickingSpot>[] spots) {
        this.spots = spots;
        this.dependentRouteBlocker = dependentRouteBlocker;
    }

    public List<PossibleClickingSpot> getPrecalculatedStonk(Set<String> routeBlockers) {
        int spotIdx = 0;
        for (String routeBlocker : routeBlockers) {
            int idx = dependentRouteBlocker.indexOf(routeBlocker);
            if (idx != -1) spotIdx += 2 << idx;
        }

        return spots[spotIdx];
    }

}
