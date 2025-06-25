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
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PrecalculatedStonk;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonOnewayLeverState;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonLeftEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.classic.ClassicPathDisplayEngine;
import kr.syeyoung.dungeonsguide.mod.pathfinding.PathfindResult;
import kr.syeyoung.dungeonsguide.mod.pathfinding.pathfinder.IPathfinder;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculation;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PrecalculatedPathfinder;
import kr.syeyoung.dungeonsguide.mod.pathfinding.world.PathfindRequest;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.Vector3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FeaturePathfinderDebug extends SimpleFeature {

    public FeaturePathfinderDebug() {
        super("Debug", "Pathfind Result Debug", "View pfres file", "etc.pfresdebug", false);
    }

    public List<PathfindRequest> requests = new ArrayList<>();
    public List<PathfindPrecalculation> precalcs = new ArrayList<>();

    private List<IPathfinder> instance = new ArrayList<>();

    private List<Vector3D> pfDebugPts = new ArrayList<>();

    private int renderRequests(int st, DungeonRoom drm, float partialTicks) {
        int cnt = requests.size() + precalcs.size();
        int i = st;
        for (PathfindRequest request : requests) {
            i++;
            Color c = Color.getHSBColor(
                    1.0f * i / cnt , 0.5f, 1.0f
            );
            Color actual = new Color(c.getRGB(), true);

            double cx = 0, cy =0 , cz = 0;
            for (OffsetVec3 offsetVec3 : request.getTarget()) {
                Vector3D pos = offsetVec3.getPos(drm);

                RenderUtils.highlightBox(
                        new AxisAlignedBB(
                                offsetVec3.xCoord - 0.025f, offsetVec3.yCoord + 0.025f + 70, offsetVec3.zCoord - 0.025f,
                                offsetVec3.xCoord + 0.025f, offsetVec3.yCoord + 0.075f + 70, offsetVec3.zCoord + 0.025f
                        ),
                        actual,
                        partialTicks,
                        false
                );
                cx += offsetVec3.xCoord;
                cy += offsetVec3.yCoord + 70;
                cz += offsetVec3.zCoord;
            }

            cx /= request.getTarget().size();
            cy /= request.getTarget().size();
            cz /= request.getTarget().size();
            cy += 0.2f;
            RenderUtils.drawTextAtWorld("Request: "+request.getHash(), (float) cx, (float) cy, (float) cz, actual.getRGB(), 0.01f, false, true, partialTicks);
        }
        return i;
    }
    private int renderPrecalcs(int st, DungeonRoom drm, float partialTicks) {
        int cnt = requests.size() + precalcs.size();
        int i = st;
        for (PathfindPrecalculation request : precalcs) {
            i++;
            Color c = Color.getHSBColor(
                    1.0f * i / cnt , 0.5f, 1.0f
            );
            Color actual = new Color(c.getRGB(), true);

            double cx = 0, cy =0 , cz = 0;
            for (OffsetVec3 offsetVec3 : request.getTargetLocations()) {
//                Vector3D pos = offsetVec3.getPos(drm);

                RenderUtils.highlightBox(
                        new AxisAlignedBB(
                                offsetVec3.xCoord - 0.025f, offsetVec3.yCoord + 0.075f + 70, offsetVec3.zCoord - 0.025f,
                                offsetVec3.xCoord + 0.025f, offsetVec3.yCoord + 0.125f + 70, offsetVec3.zCoord + 0.025f
                        ),
                        actual,
                        partialTicks,
                        false
                );
                cx += offsetVec3.xCoord;
                cy += offsetVec3.yCoord + 70;
                cz += offsetVec3.zCoord;
            }

            cx /= request.getTargetLocations().size();
            cy /= request.getTargetLocations().size();
            cz /= request.getTargetLocations().size();
            cy += 0.4f;
            RenderUtils.drawTextAtWorld("Precalc: "+request.getId()+"/"+request.getTargetHash(), (float) cx, (float) cy, (float) cz, actual.getRGB(), 0.01f, false, true, partialTicks);
        }
        return i;
    }

    @DGEventHandler(triggerOutOfSkyblock = true)
    public void renderworldLast(RenderWorldLastEvent event) {

        DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (dungeonContext == null) return;
        if (dungeonContext.getScaffoldParser() == null) return;
        DungeonRoom drm = dungeonContext.getScaffoldParser().getRoomMap().get(
                dungeonContext.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(ModAPI.getAPI().getPlayer().getPositionVector())
        );
        if (drm == null) return;

        int i = renderRequests(0, drm, event.partialTicks);
        i = renderPrecalcs(i, drm, event.partialTicks);


        int cnt = 0;
        for (Vector3D pfDebugPt : pfDebugPts) {
            for (IPathfinder precalculatedPathfinder : instance) {
                PathfindResult res = precalculatedPathfinder.getRoute(pfDebugPt);
                if (res == null) continue;
                cnt++;
                Color c = Color.getHSBColor(cnt / ((float)precalcs.size() * pfDebugPts.size()), 1.0f, 1.0f);

                GlStateManager.disableDepth();
                ClassicPathDisplayEngine.drawLinesPathfindNode(res.getNodeList(),
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

    @DGEventHandler(triggerOutOfSkyblock = true, ignoreDisabled = false)
    public void onDungeonUnload(DungeonLeftEvent event) {
        precalcs.clear();
        requests.clear();
        pfDebugPts.clear();
        for (IPathfinder precalculatedPathfinder : instance) {
            precalculatedPathfinder.close();
        }
        instance.clear();
    }

    public void onCommand(String[] args) {
        if (args[1].equals("reset")) {
            precalcs.clear();
            requests.clear();
            pfDebugPts.clear();
            for (IPathfinder precalculatedPathfinder : instance) {
                precalculatedPathfinder.close();
            }
            instance.clear();
        } else if (args[1].equals("load")) {
            try {
                PathfindPrecalculation pfc = new PathfindPrecalculation(new File(args[2]));
                DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
                DungeonRoom drm = dungeonContext.getScaffoldParser().getRoomMap().get(
                        dungeonContext.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(ModAPI.getAPI().getPlayer().getPositionVector())
                );

                PrecalculatedPathfinder precalculatedPathfinder = (PrecalculatedPathfinder) pfc.createPathfinder(drm.getRoomMatcher().getRotation());
                precalculatedPathfinder.init(((GeneralRoomProcessor)drm.getRoomProcessor()).getPathfinderWorld(), null);
                instance.add(precalculatedPathfinder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (args[1].equals("check")) {
            pfDebugPts.add(ModAPI.getAPI().getPlayer().getPositionVector());
        } else if (args[1].equals("clearpt")) {
            pfDebugPts.clear();
        } else if (args[1].equals("stonkmech")) {

            DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            if (dungeonContext == null) return;
            DungeonRoom drm = dungeonContext.getScaffoldParser().getRoomMap().get(
                    dungeonContext.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(ModAPI.getAPI().getPlayer().getPositionVector())
            );
            if (drm == null) return;
            FeatureRegistry.DEBUG_ST.change(
                    drm.getMechanics().get(args[2])
            );
        } else if (args[1].equals("stonkmech2")) {
            DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            if (dungeonContext == null) return;
            DungeonRoom drm = dungeonContext.getScaffoldParser().getRoomMap().get(
                    dungeonContext.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(ModAPI.getAPI().getPlayer().getPositionVector())
            );
            if (drm == null) return;
            DungeonOnewayLeverState.DungeonOnewayLeverData mechanic1 = (DungeonOnewayLeverState.DungeonOnewayLeverData) drm.getDungeonRoomInfo().getMechanics().get(args[2]);

                mechanic1.setLeverCache(PrecalculatedStonk.createOne(
                        drm.getDungeonRoomInfo(), mechanic1.getLeverPoint()
                ));
        }
    }
}
