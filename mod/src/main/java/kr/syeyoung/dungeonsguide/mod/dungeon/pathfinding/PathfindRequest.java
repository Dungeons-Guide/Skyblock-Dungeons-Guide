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

package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding;

import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.FeaturePathfindSettings;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.util.AxisAlignedBB;

import java.util.Comparator;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data @AllArgsConstructor
public class PathfindRequest {
    private FeaturePathfindSettings.AlgorithmSettings algorithmSettings;
    private DungeonRoomInfo dungeonRoomInfo;
    private Set<String> openMech; // excludes superboomable things.
    private BoundingBox target;

    public String getId() {
        String idStart = dungeonRoomInfo.getUuid().toString();
        idStart += ":";
        idStart += openMech.stream().sorted(String::compareTo).collect(Collectors.joining(","));
        idStart += ":";
        idStart += target.getBoundingBoxes().stream().sorted(
                Comparator
                        .<AxisAlignedBB>comparingDouble(a -> a.minX)
                        .thenComparingDouble(a -> a.minZ)
                        .thenComparingDouble(a -> a.minY)
                        .thenComparingDouble(a -> a.maxY)
                        .thenComparingDouble(a -> a.maxX)
                        .thenComparingDouble(a -> a.maxZ)).map(a -> a.minX+","+a.minY+","+a.minZ+"t"+a.maxX+","+a.maxY+","+a.maxZ).collect(Collectors.joining(";"));
        return idStart;
    }
}
