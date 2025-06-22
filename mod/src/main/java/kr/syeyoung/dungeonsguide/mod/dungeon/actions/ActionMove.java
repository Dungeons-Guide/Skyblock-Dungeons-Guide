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

package kr.syeyoung.dungeonsguide.mod.dungeon.actions;


import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PossibleClickingSpot;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.minecraft.client.Minecraft;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper=false)
public class ActionMove extends AbstractActionMove {
    @Getter
    private List<PossibleClickingSpot> targets;

    public static OffsetVec3 getCenterOf(List<OffsetVec3> set) {
        double cx = 0, cy = 0, cz = 0;
        for (OffsetVec3 offsetVec3 : set) {
            cx += offsetVec3.xCoord;
            cy += offsetVec3.yCoord;
            cz += offsetVec3.zCoord;
        }
        cx /= set.size(); cy /= set.size(); cz /= set.size();

        double cost = Double.POSITIVE_INFINITY;
        double finalCx = cx;
        double finalCy = cy;
        double finalCz = cz;
        return set.stream()
                .min(Comparator.<OffsetVec3>comparingDouble(offsetVec3 -> Math.abs(offsetVec3.xCoord - finalCx) + Math.abs(offsetVec3.yCoord - finalCy) + Math.abs(offsetVec3.zCoord - finalCz))
                        .thenComparingDouble(a -> a.xCoord)
                        .thenComparingDouble(a -> a.yCoord)
                        .thenComparingDouble(a -> a.zCoord)).orElse(null);
    }

    public ActionMove(List<PossibleClickingSpot> target, DungeonRoom dungeonRoom) {
        super(
                getCenterOf(RaytraceHelper.chooseMinimalY(target).stream()
                        .min(Comparator.comparingInt(b -> !b.isStonkingReq() ? 1 : 0)).get()
                        .getOffsetPointSet()),
                target.stream().flatMap(a -> a.getOffsetPointSet().stream()).collect(Collectors.toList())
        );
        this.targets = target;
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return targets.stream().flatMap(a -> a.getOffsetPointSet().stream()).anyMatch(
                a-> a.getPos(dungeonRoom).squareDistanceTo(Minecraft.getMinecraft().thePlayer.getPositionVector()) < 0.625
        );
    }

    @Override
    public String toString() {
        return "Move\n- target: "+targets.get(0).toString();
    }

}
