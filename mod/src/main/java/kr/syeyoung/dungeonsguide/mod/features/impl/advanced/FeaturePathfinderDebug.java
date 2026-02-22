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

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.commands.CommandDgDebug;
import kr.syeyoung.dungeonsguide.mod.commands.CommandParam;
import kr.syeyoung.dungeonsguide.mod.commands.DGCommand;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PrecalculatedStonk;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonOnewayLeverState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonLeftEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.pathfinding.PathfindResult;
import kr.syeyoung.dungeonsguide.mod.pathfinding.pathfinder.IPathfinder;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculation;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PrecalculatedPathfinder;
import kr.syeyoung.dungeonsguide.mod.pathfinding.world.PathfindRequest;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.command.UCommandContext;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.entity.UPlayerSelf;
import kr.syeyoung.modapi.event.events.RenderWorldEvent;
import kr.syeyoung.modapi.rendering.UWorldRenderContext;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FeaturePathfinderDebug extends SimpleFeature {

    public FeaturePathfinderDebug() {
        super("Debug", "Pathfind Result Debug", "View pfres file", "etc.pfresdebug", false);
    }

    public List<PathfindRequest> requests = new ArrayList<>();
    public List<PathfindPrecalculation> precalcs = new ArrayList<>();

    private List<IPathfinder> instance = new ArrayList<>();

    private List<Vector3D> pfDebugPts = new ArrayList<>();

    private int renderRequests(UWorldRenderContext context, int st, DungeonRoom drm, float partialTicks) {
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

                context.highlightBox(
                        new AABB(
                                offsetVec3.xCoord - 0.025f, offsetVec3.yCoord + 0.025f + 70, offsetVec3.zCoord - 0.025f,
                                offsetVec3.xCoord + 0.025f, offsetVec3.yCoord + 0.075f + 70, offsetVec3.zCoord + 0.025f
                        ),
                        actual.getRGB(),
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
            context.drawTextAtWorld("Request: "+request.getHash(), (float) cx, (float) cy, (float) cz, actual.getRGB(), 0.01f, false, true, partialTicks);
        }
        return i;
    }
    private int renderPrecalcs(UWorldRenderContext context, int st, DungeonRoom drm, float partialTicks) {
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

                context.highlightBox(
                        new AABB(
                                offsetVec3.xCoord - 0.025f, offsetVec3.yCoord + 0.075f + 70, offsetVec3.zCoord - 0.025f,
                                offsetVec3.xCoord + 0.025f, offsetVec3.yCoord + 0.125f + 70, offsetVec3.zCoord + 0.025f
                        ),
                        actual.getRGB(),
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
            context.drawTextAtWorld("Precalc: "+request.getId()+"/"+request.getTargetHash(), (float) cx, (float) cy, (float) cz, actual.getRGB(), 0.01f, false, true, partialTicks);
        }
        return i;
    }

    @DGEventHandler(triggerOutOfSkyblock = true)
    public void renderworldLast(RenderWorldEvent event) {

        DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (dungeonContext == null) return;
        if (dungeonContext.getScaffoldParser() == null) return;
        DungeonRoom drm = dungeonContext.getScaffoldParser().getRoomMap().get(
                dungeonContext.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(ModAPI.getAPI().getPlayer().getPositionVector())
        );
        if (drm == null) return;

        int i = renderRequests(event.getContext(), 0, drm, event.getPartialTicks());
        i = renderPrecalcs(event.getContext(), i, drm, event.getPartialTicks());


        int cnt = 0;
        for (Vector3D pfDebugPt : pfDebugPts) {
            for (IPathfinder precalculatedPathfinder : instance) {
                PathfindResult res = precalculatedPathfinder.getRoute(pfDebugPt);
                if (res == null) continue;
                cnt++;
                Color c = Color.getHSBColor(cnt / ((float)precalcs.size() * pfDebugPts.size()), 1.0f, 1.0f);

//                ClassicPathDisplayEngine.drawLinesPathfindNode(res.getNodeList(), $$ TODO:
//                        new AColor(c.getRGB(),true), 3.0f, event.getPartialTicks(), true);

                PathfindResult.PathfindNode n = res.getNodeList().get(0);

                event.getContext().drawTextAtWorld(
                        "Cost"+ res.getCost(),
                        (float) n.getX(),
                        (float) n.getY() + 0.3f*cnt,
                        (float) n.getZ(),
                        0xFF000000 | c.getRGB(),
                        0.03f,
                        false,
                        true,
                        event.getPartialTicks()
                );

                for (PathfindResult.PathfindNode pose : res.getNodeList()) {
                    if (pose.getType() != null && pose.getType() != PathfindResult.PathfindNode.NodeType.WALK && pose.getType() != PathfindResult.PathfindNode.NodeType.STONK_WALK &&
                            pose.distanceSq(ModAPI.getAPI().getPlayer().getPositionVector()) < 100) {
                        event.getContext().drawTextAtWorld(pose.getType().toString(), pose.getX(), pose.getY() + 0.5f, pose.getZ(),
                                0xFF000000 | c.getRGB(), 0.02f, false, true, event.getPartialTicks());
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

    @DGCommand("dgdebug pfresdebug clear")
    public void clear() {
        precalcs.clear();
        requests.clear();
        pfDebugPts.clear();
        for (IPathfinder precalculatedPathfinder : instance) {
            precalculatedPathfinder.close();
        }
        instance.clear();
    }
    @DGCommand("dgdebug pfresdebug load {file}")
    public void load(String file) {
        try {
            PathfindPrecalculation pfc = new PathfindPrecalculation(new File(file));
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
    }

    @DGCommand("dgdebug pfresdebug check")
    public void check() {
        pfDebugPts.add(ModAPI.getAPI().getPlayer().getPositionVector());
    }
    @DGCommand("dgdebug pfresdebug clearpt")
    public void clearpt() {
        pfDebugPts.clear();
    }



    public static class LeverNameSuggestor implements SuggestionProvider<UCommandContext> {
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<UCommandContext> commandContext, SuggestionsBuilder suggestionsBuilder) {
            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();
            if (thePlayer == null) {
                return suggestionsBuilder.buildFuture();
            }
            Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());

            DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
            for (Map.Entry<String, DungeonMechanicState> s : dungeonRoom.getMechanics().entrySet()) {
                if (s.getValue() instanceof DungeonOnewayLeverState)
                    suggestionsBuilder.suggest(s.getKey());
            }
            return suggestionsBuilder.buildFuture();
        }
    }

    @DGCommand("dgdebug pfresdebug stonkmech {mechanic}")
    public void stonkmech(@CommandParam(value = "mechanic", suggestionProvider = CommandDgDebug.MechanicNameSuggestor.class) String mechanic) {
        DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (dungeonContext == null) return;
        DungeonRoom drm = dungeonContext.getScaffoldParser().getRoomMap().get(
                dungeonContext.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(ModAPI.getAPI().getPlayer().getPositionVector())
        );
        if (drm == null) return;
        FeatureRegistry.DEBUG_ST.change(
                drm.getMechanics().get(mechanic)
        );
    }
    @DGCommand("dgdebug pfresdebug stonkmech2 {lever}")
    public void stonkmech2(@CommandParam(value = "lever", suggestionProvider = LeverNameSuggestor.class) String mechanic) {
        DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (dungeonContext == null) return;
        DungeonRoom drm = dungeonContext.getScaffoldParser().getRoomMap().get(
                dungeonContext.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(ModAPI.getAPI().getPlayer().getPositionVector())
        );
        if (drm == null) return;
        DungeonOnewayLeverState.DungeonOnewayLeverData mechanic1 = (DungeonOnewayLeverState.DungeonOnewayLeverData) drm.getDungeonRoomInfo().getMechanics().get(mechanic);

        mechanic1.setLeverCache(PrecalculatedStonk.createOne(
                drm.getDungeonRoomInfo(), mechanic1.getLeverPoint()
        ));
    }

}
