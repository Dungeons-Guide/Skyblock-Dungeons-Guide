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

package kr.syeyoung.dungeonsguide.mod.dungeon;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.PathfinderExecutor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import scala.reflect.internal.util.WeakHashSet;

import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PathfinderExecutorExecutor extends Thread{
    public PathfinderExecutorExecutor(DungeonContext context) {
        super(DungeonsGuide.THREAD_GROUP, "DG Pathfinder");
        this.context =context;
    }
    private DungeonContext context;
    private DungeonRoom target;

    public void setRoomIn(DungeonRoom target) {
        this.target = target;
    }

    @Override
    public void run() {
        List<WeakReference<PathfinderExecutor>> toRemove = new ArrayList<>();
        WeakReference<PathfinderExecutor>[] weakReferences = new WeakReference[200]; // shoulllld be enough
        while(!isInterrupted()) {
            if (context.getScaffoldParser() != null) {
                try {
                    boolean flag = false;
                    context.getExecutors().toArray(weakReferences);
                    for (int i = 0; i < weakReferences.length; i++) {
                        WeakReference<PathfinderExecutor> executor = weakReferences[i];
                        if (executor == null) break;

                        PathfinderExecutor executor1 = executor.get();
                        if (executor1 != null) {
                            if (executor1.getDungeonRoom() == target)
                                executor1.doStep();
                        } else {
                            flag = true;
                            toRemove.add(executor);
                        }
                    }
                    if (flag) {
                        context.getExecutors().removeAll(toRemove);
                        toRemove.clear();
                    }
//                    Thread.yield();
                } catch (Exception e) {
                    e.printStackTrace(); // wtf?
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
    }
}
