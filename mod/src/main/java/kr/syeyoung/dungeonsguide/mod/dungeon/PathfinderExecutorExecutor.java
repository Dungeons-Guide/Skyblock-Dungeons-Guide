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
import kr.syeyoung.dungeonsguide.mod.pathfinding.pathfinder.PathfinderExecutor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import lombok.Getter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.LockSupport;

public class PathfinderExecutorExecutor extends Thread{
    public PathfinderExecutorExecutor(DungeonContext context) {
        super(DungeonsGuide.THREAD_GROUP, "DG Pathfinder");
        this.context =context;
    }
    private DungeonContext context;
    private DungeonRoom target;

    public void setRoomIn(DungeonRoom target) {
        this.target = target;
        LockSupport.unpark(this);
    }

    private final List<WeakReference<PathfinderExecutor>> executors = new CopyOnWriteArrayList<>();

    public void registerExecutor(PathfinderExecutor executor) {
        executors.add(new WeakReference<>(executor));
        LockSupport.unpark(this);
    }

    @Override
    public void run() {
        try {
            List<WeakReference<PathfinderExecutor>> toRemove = new ArrayList<>();
            WeakReference<PathfinderExecutor>[] weakReferences = new WeakReference[200]; // shoulllld be enough
            while (!isInterrupted()) {
                if (context.getScaffoldParser() != null) {
                    try {
                        boolean flag = false;
                        executors.toArray(weakReferences);
                        boolean foundAny = false;
                        for (int i = 0; i < weakReferences.length; i++) {
                            WeakReference<PathfinderExecutor> executor = weakReferences[i];
                            if (executor == null) break;

                            PathfinderExecutor executor1 = executor.get();
                            if (executor1 != null) {
                                if (executor1.getDungeonRoom() == target) {
                                    executor1.doStep();
                                    foundAny = true;
                                }
                            } else {
                                flag = true;
                                toRemove.add(executor);
                            }
                        }
                        if (flag) {
                            executors.removeAll(toRemove);
                            toRemove.clear();
                        }
                        if (!foundAny)
                            LockSupport.park();
//                    Thread.yield();
                    } catch (Exception e) {
                        FeatureCollectDiagnostics.queueSendLogAsync(e);
                        e.printStackTrace(); // wtf?
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }
}
