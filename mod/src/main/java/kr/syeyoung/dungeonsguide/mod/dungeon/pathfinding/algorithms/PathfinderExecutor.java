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

import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.BoundingBox;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindResult;
import lombok.Getter;
import net.minecraft.util.Vec3;

import java.util.Collections;

public class PathfinderExecutor {
    private boolean invalidate = false;
    @Getter
    private volatile Vec3 target;

    @Getter
    private IPathfindWorld dungeonRoom;

    private IPathfinder pathfinder;
    @Getter
    private boolean isComplete = false;
    private PathfindResult lastRoute = new PathfindResult(Collections.emptyList(), 0);

    public PathfinderExecutor(IPathfinder pathfinder, BoundingBox target, IPathfindWorld dungeonRoom) {
        this.pathfinder = pathfinder;
        this.target = target.center();
        this.dungeonRoom = dungeonRoom;

        pathfinder.init(dungeonRoom, target);
    }

    public boolean doStep() {
        pathfinder.setTarget(target);
        isComplete = pathfinder.doOneStep();
        return isComplete;
    }

    public double findCost() {
        pathfinder.setTarget(target);
        while(!pathfinder.doOneStep());
        return pathfinder.getCost(target);
    }

    public void setTarget(Vec3 target) {
        this.target = target;
    }

    public PathfindResult getRoute(Vec3 target) {
        if (!isComplete) return lastRoute;
        PathfindResult route = pathfinder.getRoute(target);
        if (route == null) return lastRoute = pathfinder.getRoute(this.target);
        else return lastRoute = route;
    }
}
