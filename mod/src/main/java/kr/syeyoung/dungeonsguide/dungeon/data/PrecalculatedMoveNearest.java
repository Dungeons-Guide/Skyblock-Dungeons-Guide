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

import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.RouteBlocker;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.RaytraceHelper;
import kr.syeyoung.dungeonsguide.mod.dungeon.mocking.DRIWorld;
import lombok.Getter;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PrecalculatedMoveNearest implements Serializable {
    private final List<PossibleMoveSpot>[] spots;
    @Getter
    private final List<String> dependentRouteBlocker;
    @Getter
    private final OffsetPoint target;

    public PrecalculatedMoveNearest(List<String> dependentRouteBlocker, List<PossibleMoveSpot>[] spots, OffsetPoint target) {
        this.spots = spots;
        this.dependentRouteBlocker = dependentRouteBlocker;
        this.target = target;
    }

    public List<PossibleMoveSpot> getPrecalculatedStonk(Collection<String> openBlockers) {
        int spotIdx = 0;
        for (String routeBlocker : openBlockers) {
            int idx = dependentRouteBlocker.indexOf(routeBlocker);
            if (idx != -1) spotIdx += 1 << idx;
        }

        return spots[spotIdx];
    }

    public static PrecalculatedMoveNearest createOneBat(OffsetPoint offsetPoint, DungeonRoomInfo dri) {
        List<String> calculateFor = new ArrayList<>();
        for (Map.Entry<String, DungeonMechanic> value : dri.getMechanics().entrySet()) {
            if (!(value.getValue() instanceof RouteBlocker)) continue;
            for (OffsetPoint blockedPoint : ((RouteBlocker) value.getValue()).blockedPoints()) {
                int xDiff = Math.abs(blockedPoint.getX() - offsetPoint.getX());
                int yDiff = Math.abs(blockedPoint.getY() - offsetPoint.getY());
                int zDiff = Math.abs(blockedPoint.getZ() - offsetPoint.getZ());
                if (Math.max(xDiff, Math.max(yDiff, zDiff)) <= 5) {
                    calculateFor.add(value.getKey());
                    break;
                }
            }
        }
        Vec3 vec = new Vec3(offsetPoint.getX() + 0.5, offsetPoint.getY() + 70.5, offsetPoint.getZ() + 0.5);
        List<PossibleMoveSpot>[] spots = new List[1 << calculateFor.size()];
        for (int i = 0; i < (1 << calculateFor.size()); i++) {
            List<String> included = new ArrayList<>();
            for (int i1 = 0; i1 < calculateFor.size(); i1++) {
                if (((i >> i1) & 0x1) > 0) included.add(calculateFor.get(i1));
            }

            spots[i] = RaytraceHelper.findMovespots(new DRIWorld(dri, included), new BlockPos(offsetPoint.getX(), offsetPoint.getY()+70, offsetPoint.getZ()),
                    a -> a.squareDistanceTo(vec) <= 5, 6);
        }
        return new PrecalculatedMoveNearest(calculateFor, spots, offsetPoint);
    }
    public static PrecalculatedMoveNearest createOneItem(OffsetPoint offsetPoint, DungeonRoomInfo dri) {
        List<String> calculateFor = new ArrayList<>();
        for (Map.Entry<String, DungeonMechanic> value : dri.getMechanics().entrySet()) {
            if (!(value.getValue() instanceof RouteBlocker)) continue;
            for (OffsetPoint blockedPoint : ((RouteBlocker) value.getValue()).blockedPoints()) {
                int xDiff = Math.abs(blockedPoint.getX() - offsetPoint.getX());
                int yDiff = Math.abs(blockedPoint.getY() - offsetPoint.getY());
                int zDiff = Math.abs(blockedPoint.getZ() - offsetPoint.getZ());
                if (Math.max(xDiff, Math.max(yDiff, zDiff)) <= 5) {
                    calculateFor.add(value.getKey());
                    break;
                }
            }
        }
        Vec3 vec = new Vec3(offsetPoint.getX() + 0.5, offsetPoint.getY() + 70.5, offsetPoint.getZ() + 0.5);
        List<PossibleMoveSpot>[] spots = new List[1 << calculateFor.size()];
        AxisAlignedBB check = AxisAlignedBB.fromBounds(
                vec.xCoord - 3, vec.yCoord - 1.5, vec.zCoord -3,
                vec.xCoord + 3, vec.yCoord + 3, vec.zCoord + 3
        );
        for (int i = 0; i < (1 << calculateFor.size()); i++) {
            List<String> included = new ArrayList<>();
            for (int i1 = 0; i1 < calculateFor.size(); i1++) {
                if (((i >> i1) & 0x1) > 0) included.add(calculateFor.get(i1));
            }

            spots[i] = RaytraceHelper.findMovespots(new DRIWorld(dri, included), new BlockPos(offsetPoint.getX(), offsetPoint.getY()+70, offsetPoint.getZ()),
                    a -> check.isVecInside(a), 8);
        }
        return new PrecalculatedMoveNearest(calculateFor, spots, offsetPoint);
    }

}
