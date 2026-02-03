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

package kr.syeyoung.dungeonsguide.mod.features.impl.advanced;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionChangeState;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRoute;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGNode;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.RawRenderingGuiFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.IPathDisplayEngine;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.routedisplay.RoomRouteHandler;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.entity.UPlayerSelf;
import kr.syeyoung.modapi.event.events.ClientTickEvent;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class FeatureDAGs extends RawRenderingGuiFeature {
    public FeatureDAGs() {
        super("Debug", "DAG Renderer", "View DAG of actions that needs to be taken", "secret.dagview", false, 500, 500);
        setEnabled(false);
    }

    public boolean isHUDViewable() {
        if (!SkyblockStatus.isOnDungeon()) return false;
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() == null || DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getScaffoldParser() == null) return false;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();
        Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());
        DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
        if (dungeonRoom == null) return false;
        return dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor;
    }

    Map<ActionDAGNode, Point> locations = new HashMap<>();
    Map<Integer, Integer> lvCount = new HashMap<>();

    @DGEventHandler
    public void onTick(ClientTickEvent tickEvent) {
        locations.clear();
        lvCount.clear();
        if (!isHUDViewable()) {
            return;
        }

        if (!SkyblockStatus.isOnDungeon()) return;
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() == null || DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getScaffoldParser() == null) return;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();
        Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());
        DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
        if (dungeonRoom == null) return;
        RoomRouteHandler roomRouteHandler = FeatureRegistry.SECRET_ROUTE_REGISTRY.getRoomHandler(dungeonRoom);
        if (roomRouteHandler == null) return;


        int defaultLvCount = 0;
        for (Map.Entry<String, IPathDisplayEngine<?>> stringActionRouteEntry : roomRouteHandler.getPath().entrySet()) {
//            if (stringActionRouteEntry.getValue().isCalculating()) continue;

            ActionDAGNode rootNode = stringActionRouteEntry.getValue().getActionRoute().getDag().getActionDAGNode();
            // let's dfs!!!

            boolean[] visited = new boolean[stringActionRouteEntry.getValue().getActionRoute().getDag().getAllNodes().size()];

            Deque<ActionDAGNode> path = new ArrayDeque<>();
            path.push(rootNode);

            int maxLvCount = 0;
            while (!path.isEmpty()) {
                ActionDAGNode actionDAGNode = path.peek();
                boolean found = false;
                for (int i = 0; i < actionDAGNode.getAllChildren().size(); i++) {
                    ActionDAGNode actionDAGNode1 = actionDAGNode.getAllChildren().get(i);
                    if (visited[actionDAGNode1.getId()]) continue;
                    path.push(actionDAGNode1);
                    found = true;
                    break;
                }
                if (found) {
                    continue;
                }

                ActionDAGNode pop = path.pop();
                visited[pop.getId()] = true;

                if (lvCount.get(pop.getMaximumDepth()) == null || lvCount.get(pop.getMaximumDepth()) < defaultLvCount) {
                    lvCount.put(pop.getMaximumDepth(), defaultLvCount);
                }
                locations.put(pop, new Point(
                        lvCount.get(pop.getMaximumDepth())*70, pop.getMaximumDepth() * 50 + 20
                ));
                lvCount.put(pop.getMaximumDepth(), lvCount.get(pop.getMaximumDepth())+1);
                if (maxLvCount < lvCount.get(pop.getMaximumDepth())) maxLvCount = lvCount.get(pop.getMaximumDepth());
            }
            defaultLvCount = maxLvCount;
        }
    }

    @Override
    public void drawHUD(RenderingContext ctx, float partialTicks) {
        if (!isHUDViewable()) return;

        if (!SkyblockStatus.isOnDungeon()) return;
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() == null || DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getScaffoldParser() == null) return;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();
        Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());
        DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
        if (dungeonRoom == null) return;
        RoomRouteHandler roomRouteHandler = FeatureRegistry.SECRET_ROUTE_REGISTRY.getRoomHandler(dungeonRoom);
        if (roomRouteHandler == null) return;

        // we got all positions in above tick.

        ctx.drawString("Black=Disabled / Pink=Current / Dark Green=Parent Completed / Green=Completed", 0 ,0, 0xFFFFFF00);

        for (IPathDisplayEngine<?> value2 : roomRouteHandler.getPath().values()) {
//            if (value.isCalculating()) continue;
            ActionRoute value = value2.getActionRoute();

//            WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();
            int nodestatus[] = value.getDag().getNodeStatus(value.getDagId());
            for (ActionDAGNode allNode : value.getDag().getAllNodes()) {
                Point p = locations.get(allNode);
                if (p == null) continue;

                int status = nodestatus[allNode.getId()];
                // 0: disabled
                // 1: ocmplted
                // 2: completed due to parent
                // 3: online

                int color =
                        value.getCurrentAction() == allNode.getAction() ? 0xFFFF00FF :
                        status == 0 ? 0xFF000000 : status == 1 ? 0xFF00DD00 : status == 2 ? 0xFF007700 : status == 3 ? 0xFF777777 : -1;
                ctx.drawRect(p.x, p.y, p.x+25, p.y+25, color);

                String name = allNode.getAction().toString().split("\n")[0];
                if (allNode.getAction() instanceof ActionChangeState) {
                    ActionChangeState state = (ActionChangeState) allNode.getAction();
                    if (state.getState().equalsIgnoreCase("found")) {
                        name = state.getMechanicName();
                    } else {
                        name = state.getMechanicName()+":"+state.getState();
                    }
                }
                ctx.drawString(name, p.x, p.y, 0xFFFFFFFF);

                for (ActionDAGNode actionDAGNode : allNode.getRequire()) {
                    Point p2 = locations.get(actionDAGNode);
                    if (p2 == null) continue;
                    ctx.drawLine(p.x+12.5, p.y+12.5, p2.x + 12.5, p2.y+12.5, 0xFF7F00FF, 5.0f);
                }
                for (ActionDAGNode actionDAGNode : allNode.getOr()) {
                    Point p2 = locations.get(actionDAGNode);
                    if (p2 == null) continue;
                    ctx.drawLine(p.x+12.5, p.y+12.5, p2.x + 12.5, p2.y+12.5, 0xFF007FFF, 5.0f);
                }
                for (ActionDAGNode actionDAGNode : allNode.getOptional()) {
                    Point p2 = locations.get(actionDAGNode);
                    if (p2 == null) continue;
                    ctx.drawLine(p.x+12.5, p.y+12.5, p2.x + 12.5, p2.y+12.5, 0xFF0000FF, 5.0f);
                }
            }

            if (!value.isCalculating()) {
                int cnt = 0;
                Point last = null;
                for (ActionDAGNode actionDAGNode : value.getOrder()) {
                    Point p = locations.get(actionDAGNode);
                    if (p != null && last != null) {
                        ctx.drawLine(last.x + 15, last.y + 15, p.x + 15, p.y + 15, 0xFF00FF00, 5.0f);
                        cnt++;
                        ctx.drawString(cnt + "", p.x, p.y + 10, 0xFFFFFFFF);
                    }
                    if (p != null) last = p;
                }
            }
        }
    }
}
