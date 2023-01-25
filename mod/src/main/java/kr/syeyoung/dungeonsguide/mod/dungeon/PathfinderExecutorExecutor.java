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

import java.awt.*;

public class PathfinderExecutorExecutor extends Thread{
    public PathfinderExecutorExecutor(DungeonContext context) {
        super(DungeonsGuide.THREAD_GROUP, "DG Pathfinder");
        this.context =context;
    }
    private DungeonContext context;
    @Override
    public void run() {
        while(!isInterrupted()) {
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(Minecraft.getMinecraft().thePlayer.getPosition());

                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                try {
                    for (PathfinderExecutor executor : context.getExecutors()) {
                        if (executor.getDungeonRoom() == dungeonRoom)
                            executor.doStep();
                    }
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
