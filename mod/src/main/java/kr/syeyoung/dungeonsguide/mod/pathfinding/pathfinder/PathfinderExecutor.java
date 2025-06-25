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

package kr.syeyoung.dungeonsguide.mod.pathfinding.pathfinder;

import kr.syeyoung.dungeonsguide.mod.pathfinding.BoundingBox;
import kr.syeyoung.dungeonsguide.mod.pathfinding.PathfindResult;
import kr.syeyoung.dungeonsguide.mod.pathfinding.world.IPathfindWorld;
import kr.syeyoung.modapi.data.Vector3D;
import lombok.Getter;
import net.minecraft.util.Vec3;

import java.util.Collections;

public class PathfinderExecutor implements AutoCloseable {
    private boolean invalidate = false;
    @Getter
    private volatile Vector3D target;

    @Getter
    private IPathfindWorld dungeonRoom;

    @Getter
    private IPathfinder pathfinder;
    @Getter
    private boolean isComplete = false;
    private PathfindResult lastRoute = new PathfindResult(Collections.emptyList(), 0);

    public PathfinderExecutor(IPathfinder pathfinder, BoundingBox target, IPathfindWorld dungeonRoom) {
        this.pathfinder = pathfinder;
        Vec3 tv = target.center();
        this.target = new Vector3D(tv.xCoord, tv.yCoord, tv.zCoord);
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

    public void setTarget(Vector3D target) {
        this.target = target;
    }

    public PathfindResult getRoute(Vector3D target) {
        if (!isComplete) return lastRoute;
        PathfindResult route = pathfinder.getRoute(target);
        if (route == null) return lastRoute = pathfinder.getRoute(this.target);
        else return lastRoute = route;
    }

    public void close() {
        pathfinder.close();
    }
}
