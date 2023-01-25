/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import lombok.Getter;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;

public class PathfinderExecutor {
    private boolean invalidate = false;
    @Getter
    private Vec3 target;

    @Getter
    private DungeonRoom dungeonRoom;

    private IPathfinder pathfinder;
    private boolean isComplete = false;
    private List<Vec3> lastRoute = new ArrayList<>();

    public PathfinderExecutor(IPathfinder pathfinder, Vec3 target, DungeonRoom dungeonRoom) {
        this.pathfinder = pathfinder;
        this.target = target;
        this.dungeonRoom = dungeonRoom;
        pathfinder.init(dungeonRoom, target);
    }

    public boolean doStep() {
        pathfinder.setTarget(target);
        isComplete = pathfinder.doOneStep();
        return isComplete;
    }

    public void setTarget(Vec3 target) {
        this.target = target;
    }

    public List<Vec3> getRoute(Vec3 target) {
        if (!isComplete) return lastRoute;
        List<Vec3> route = pathfinder.getRoute(target);
        if (route == null) return lastRoute = pathfinder.getRoute(getTarget());
        else return lastRoute = route;
    }
}
