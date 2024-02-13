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
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionChangeState;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRoute;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGNode;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.features.RawRenderingGuiFeature;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;

public class FeatureDAGs extends RawRenderingGuiFeature {
    public FeatureDAGs() {
        super("Debug", "DAG Renderer", "View DAG of actions that needs to be taken", "secret.dagview", false, 500, 500);
    }

    public boolean isHUDViewable() {
        if (!SkyblockStatus.isOnDungeon()) return false;
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() == null || DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getScaffoldParser() == null) return false;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
        if (dungeonRoom == null) return false;
        return dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor;
    }

    Map<ActionDAGNode, Point> locations = new HashMap<>();
    Map<Integer, Integer> lvCount = new HashMap<>();

    @DGEventHandler
    public void onTick(DGTickEvent tickEvent) {
        locations.clear();
        lvCount.clear();
        if (!isHUDViewable()) {
            return;
        }

        if (!SkyblockStatus.isOnDungeon()) return;
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() == null || DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getScaffoldParser() == null) return;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
        if (dungeonRoom == null) return;
        GeneralRoomProcessor roomProcessor = (GeneralRoomProcessor) dungeonRoom.getRoomProcessor();
        int defaultLvCount = 0;
        for (Map.Entry<String, ActionRoute> stringActionRouteEntry : roomProcessor.getPath().entrySet()) {
            if (stringActionRouteEntry.getValue().isCalculating()) continue;

            ActionDAGNode rootNode = stringActionRouteEntry.getValue().getDag().getActionDAGNode();
            // let's dfs!!!

            boolean[] visited = new boolean[stringActionRouteEntry.getValue().getDag().getAllNodes().size()];

            Stack<ActionDAGNode> path = new Stack<>();
            path.push(rootNode);

            int maxLvCount = 0;
            while (!path.isEmpty()) {
                ActionDAGNode actionDAGNode = path.peek();
                boolean found = false;
                for (int i = 0; i < actionDAGNode.getPotentialRequires().size(); i++) {
                    ActionDAGNode actionDAGNode1 = actionDAGNode.getPotentialRequires().get(i);
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
                        lvCount.get(pop.getMaximumDepth())*50, pop.getMaximumDepth() * 50 + 20
                ));
                lvCount.put(pop.getMaximumDepth(), lvCount.get(pop.getMaximumDepth())+1);
                if (maxLvCount < lvCount.get(pop.getMaximumDepth())) maxLvCount = lvCount.get(pop.getMaximumDepth());
            }
            defaultLvCount = maxLvCount;
        }
    }

    @Override
    public void drawHUD(float partialTicks) {
        if (!isHUDViewable()) return;

        if (!SkyblockStatus.isOnDungeon()) return;
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() == null || DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getScaffoldParser() == null) return;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
        if (dungeonRoom == null) return;
        GeneralRoomProcessor roomProcessor = (GeneralRoomProcessor) dungeonRoom.getRoomProcessor();

        // we got all positions in above tick.

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString("Black=Disabled / Pink=Current / Dark Green=Parent Completed / Green=Completed", 0 ,0, 0xFFFFFF00);
        GL11.glLineWidth(5.0f);

        for (ActionRoute value : roomProcessor.getPath().values()) {
            if (value.isCalculating()) continue;


            WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();
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
                RenderUtils.drawRect(p.x, p.y, p.x+25, p.y+25, new AColor(color, true));

                String name = allNode.getAction().toString().split("\n")[0];
                if (allNode.getAction() instanceof ActionChangeState) {
                    ActionChangeState state = (ActionChangeState) allNode.getAction();
                    if (state.getState().equalsIgnoreCase("found")) {
                        name = state.getMechanicName();
                    } else {
                        name = state.getMechanicName()+":"+state.getState();
                    }
                }
                fr.drawString(name, p.x, p.y, 0xFFFFFFFF);


                GlStateManager.color(1,1,1,1);
                GlStateManager.disableTexture2D();
                worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
                for (ActionDAGNode actionDAGNode : allNode.getPotentialRequires()) {
                    Point p2 = locations.get(actionDAGNode);
                    if (p2 == null) continue;
                    worldRenderer.pos(p.x + 12.5, p.y + 12.5, 0).color(
                            0.0f,
                            0.0f,
                            1.0f,
                            1.0f
                    ).endVertex();
                    worldRenderer.pos(p2.x + 12.5, p2.y + 12.5, 0).color(
                            0.0f,
                            0.0f,
                            1.0f,
                            1.0f
                    ).endVertex();
                }
                Tessellator.getInstance().draw();
                GlStateManager.enableTexture2D();
            }

            GlStateManager.color(1,1,1,1);
            worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            GlStateManager.enableTexture2D();
            int cnt = 0;
            for (ActionDAGNode actionDAGNode : value.getOrder()) {
                Point p = locations.get(actionDAGNode);
                if (p != null) {
                    worldRenderer.pos(p.x + 15, p.y + 15, 0).color(
                            0.0f,
                            1.0f,
                            0.0f,
                            1.0f
                    ).endVertex();
                    cnt++;
                    fr.drawString(cnt+"", p.x, p.y+10, 0xFFFFFFFF);
                }
            }
            GlStateManager.disableTexture2D();
            Tessellator.getInstance().draw();
            GlStateManager.enableTexture2D();
        }
        int y=  300;
        for (String s : dungeonRoom.getRoomContext().entrySet().stream().map(a -> a.getKey() + ":" + a.getValue()).collect(Collectors.toList())) {

            fr.drawString(s, 0 ,y, 0xFFFFFFFF);
            y += 10;
        }

    }
}
