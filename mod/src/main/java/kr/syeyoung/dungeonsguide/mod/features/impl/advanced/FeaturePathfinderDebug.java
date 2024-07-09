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

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.AbstractActionMove;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionMove;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRoute;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindResult;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.ShadowCast;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.CachedPathfinder;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.PathfindCache;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FeaturePathfinderDebug extends SimpleFeature {

    public FeaturePathfinderDebug() {
        super("Debug", "Pathfind Result Debug", "View pfres file", "etc.pfresdebug", false);
    }

    private List<CachedPathfinder> instance = new ArrayList<>();


    private List<Vec3> pfDebugPts = new ArrayList<>();

    @DGEventHandler(triggerOutOfSkyblock = true)
    public void renderworldLast(RenderWorldLastEvent event) {
        if (instance == null) return;

        DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (dungeonContext == null) return;
        DungeonRoom drm = dungeonContext.getScaffoldParser().getRoomMap().get(
                dungeonContext.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(Minecraft.getMinecraft().thePlayer.getPosition())
        );
        if (drm == null) return;


        int cnt = 0;
        for (Vec3 pfDebugPt : pfDebugPts) {
            for (CachedPathfinder cachedPathfinder : instance) {
                PathfindResult res = cachedPathfinder.getRoute(pfDebugPt);
                if (res == null) continue;
                cnt++; EnderPea
                Color c = Color.getHSBColor(cnt / ((float)instance.size() * pfDebugPts.size()), 1.0f, 1.0f);

                GlStateManager.disableDepth();
                AbstractActionMove.drawLinesPathfindNode(res.getNodeList(),
                        new AColor(c.getRGB(),true), 3.0f, event.partialTicks);
                GlStateManager.enableDepth();

                PathfindResult.PathfindNode n = res.getNodeList().get(0);

                RenderUtils.drawTextAtWorld(
                        "Cost"+ res.getCost(),
                        (float) n.getX(),
                        (float) n.getY() + 0.3f*cnt,
                        (float) n.getZ(),
                        0xFF000000 | c.getRGB(),
                        0.03f,
                        false,
                        true,
                        event.partialTicks
                );

                for (PathfindResult.PathfindNode pose : res.getNodeList()) {
                    if (pose.getType() != null && pose.getType() != PathfindResult.PathfindNode.NodeType.WALK && pose.getType() != PathfindResult.PathfindNode.NodeType.STONK_WALK && pose.distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()) < 100) {
                        RenderUtils.drawTextAtWorld(pose.getType().toString(), pose.getX(), pose.getY() + 0.5f, pose.getZ(),
                                0xFF000000 | c.getRGB(), 0.02f, false, true, event.partialTicks);
                    }

                }
            }
        }
    }

    public void onCommand(String[] args) {
        if (args[1].equals("reset")) {
            pfDebugPts.clear();
            instance.clear();
        } else if (args[1].equals("load")) {
            try {
                PathfindCache pfc = new PathfindCache(new File(args[2]));
                DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
                DungeonRoom drm = dungeonContext.getScaffoldParser().getRoomMap().get(
                        dungeonContext.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(Minecraft.getMinecraft().thePlayer.getPosition())
                );

                CachedPathfinder cachedPathfinder = (CachedPathfinder) pfc.createPathfinder(drm.getRoomMatcher().getRotation());
                cachedPathfinder.init(drm, null);
                instance.add(cachedPathfinder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (args[1].equals("check")) {
            pfDebugPts.add(Minecraft.getMinecraft().thePlayer.getPositionVector());
        } else if (args[1].equals("clearpt")) {
            pfDebugPts.clear();
        }
    }
}
