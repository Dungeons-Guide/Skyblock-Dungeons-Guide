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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest;

import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonRedstoneKey;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonRoomDoor2;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.ISecret;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAG;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGNode;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonMapLayout;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonRoomScaffoldParser;
import kr.syeyoung.dungeonsguide.mod.dungeon.mocking.DRIWorld;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindRequest;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.CachedPathfinderRegistry;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.PathfindCache;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Column;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class WidgetCheckMissing extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "precalculationApi")
    public final BindableAttribute<Column> precalculationApi = new BindableAttribute<>(Column.class);
    @Bind(variableName = "precalculationList")
    public final BindableAttribute<List<Widget>> precalculationList = new BindableAttribute(WidgetList.class);

    public WidgetCheckMissing() {
        super(new ResourceLocation("dungeonsguide:gui/features/requestcalculation/checkmissing.gui"));
        List<Widget> widgets = new ArrayList<>();
        for (Map.Entry<UUID, List<PathfindCache>> uuidListEntry : CachedPathfinderRegistry.getByRooms().entrySet()) {
            if (DungeonRoomInfoRegistry.getByUUID(uuidListEntry.getKey()) != null)
                widgets.add(new WidgetPrecalculations(uuidListEntry.getKey(), uuidListEntry.getValue()));
        }
        this.precalculationList.setValue(widgets);
    }


    @On(functionName = "opendir")
    public void opendir() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        try {
            Desktop.getDesktop().open(new File(Main.getConfigDir(), "pfResult"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @On(functionName = "reload")
    public void reload() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        CachedPathfinderRegistry.loadAll(new File(Main.getConfigDir(), "pfResult"));
    }
    @On(functionName = "checkmissing")
    public void checkMissing() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        precalculationApi.getValue().removeAllWidget();
        for (DungeonRoomInfo dungeonRoomInfo : DungeonRoomInfoRegistry.getRegistered()) {
            List<PathfindRequest> requests = new LinkedList<>();
            DRIWorld driWorld = new DRIWorld(dungeonRoomInfo);
            DungeonContext fakeContext = new DungeonContext("TEST DG", driWorld);
            DungeonMapLayout dungeonMapLayout = new DungeonMapLayout(
                    new Dimension(16, 16),
                    5,
                    new Point(0, 0),
                    new BlockPos(0, 70, 0)
            );
            fakeContext.setScaffoldParser(new DungeonRoomScaffoldParser(dungeonMapLayout, fakeContext));
            DungeonRoom dungeonRoom = new DungeonRoom(fakeContext);

            ActionDAGBuilder builder = new ActionDAGBuilder(dungeonRoom);
            for (Map.Entry<String, DungeonMechanic> value : dungeonRoom.getMechanics().entrySet()) {

                if (value.getValue() instanceof ISecret) {
                    try {
                        builder.requires(new ActionChangeState(value.getKey(), "found"));
                    } catch (PathfindImpossibleException e) {
                        ChatTransmitter.addToQueue("Dungeons Guide :: Pathfind to " + value.getKey() + ":found failed due to " + e.getMessage());
                        e.printStackTrace();
                        continue;
                    }
                } else if (value.getValue() instanceof DungeonRedstoneKey) {
                    try {
                        builder.requires(new ActionChangeState(value.getKey(), "obtained-self"));
                    } catch (PathfindImpossibleException e) {
                        ChatTransmitter.addToQueue("Dungeons Guide :: Pathfind to " + value.getKey() + ":found failed due to " + e.getMessage());
                        e.printStackTrace();
                        continue;
                    }
                } else if (value.getValue() instanceof DungeonRoomDoor2) {
                    try {
                        builder.requires(new ActionChangeState(value.getKey(), "navigate"));
                    } catch (PathfindImpossibleException e) {
                        ChatTransmitter.addToQueue("Dungeons Guide :: Pathfind to door: " + value.getKey() + ":navigate failed due to " + e.getMessage());
                        e.printStackTrace();
                        continue;
                    }
                }
            }
            ActionDAG dag = builder.build();
            List<List<OffsetVec3>> toPfTo = new ArrayList<>();
            Set<String> openMech = new HashSet<>();
            for (ActionDAGNode allNode : dag.getAllNodes()) {
                if (allNode.getAction() instanceof AtomicAction) {
                    for (AbstractAction action : ((AtomicAction) allNode.getAction()).getActions()) {
                        if (action instanceof ActionMove) {
                            toPfTo.add(
                                    ((ActionMove) action).getTargets().stream().flatMap(a -> a.getOffsetPointSet().stream())
                                            .collect(Collectors.toList())
                            );
                        } else if (action instanceof ActionMoveSpot) {
                            toPfTo.add(
                                    ((ActionMoveSpot) action).getTargets().stream().flatMap(a -> a.getOffsetPointSet().stream())
                                            .collect(Collectors.toList())
                            );
                        } else if (action instanceof ActionMoveNearestAir) {
                            OffsetPoint offsetPoint = ((ActionMoveNearestAir) action).getTarget();
                            toPfTo.add(
                                    Collections.singletonList(new OffsetVec3(offsetPoint.getX(), offsetPoint.getY(), offsetPoint.getZ()))
                            );
                        } else if (action instanceof ActionChangeState) {
                            if (((ActionChangeState) action).getState().equalsIgnoreCase("open")) {
                                if (!((ActionChangeState) action).getMechanicName().startsWith("superboom") &&
                                        !((ActionChangeState) action).getMechanicName().startsWith("crypt") &&
                                        !((ActionChangeState) action).getMechanicName().startsWith("prince"))
                                    openMech.add(((ActionChangeState) action).getMechanicName());
                            }
                        }
                    }
                } else if (allNode.getAction() instanceof ActionMove) {
                    toPfTo.add(
                            ((ActionMove) allNode.getAction()).getTargets().stream().flatMap(a -> a.getOffsetPointSet().stream())
                                    .collect(Collectors.toList())
                    );
                } else if (allNode.getAction() instanceof ActionMoveSpot) {
                    toPfTo.add(
                            ((ActionMoveSpot) allNode.getAction()).getTargets().stream().flatMap(a -> a.getOffsetPointSet().stream())
                                    .collect(Collectors.toList())
                    );
                } else if (allNode.getAction() instanceof ActionMoveNearestAir) {
                    OffsetPoint offsetPoint = ((ActionMoveNearestAir) allNode.getAction()).getTarget();
                    toPfTo.add(
                            Collections.singletonList(new OffsetVec3(offsetPoint.getX(), offsetPoint.getY(), offsetPoint.getZ()))
                    );
                } else if (allNode.getAction() instanceof ActionChangeState) {
                    if (((ActionChangeState) allNode.getAction()).getState().equalsIgnoreCase("open")
                            && ((ActionChangeState) allNode.getAction()).getMechanicName().startsWith("door")) {
                        openMech.add(((ActionChangeState) allNode.getAction()).getMechanicName());
                    }
                }
            }

            List<String> openMechList = new ArrayList<>(openMech);

            for (List<OffsetVec3> offsetVec3s : toPfTo) {
                for (int i = 0; i < (1 << openMech.size()); i++) {
                    Set<String> open = new HashSet<>();
                    for (int i1 = 0; i1 < openMechList.size(); i1++) {
                        if (((i >> i1) & 0x1) > 0) {
                            open.add(openMechList.get(i1));
                        }
                    }
                    requests.add(new PathfindRequest(FeatureRegistry.SECRET_PATHFIND_SETTINGS.getAlgorithmSettings(), dungeonRoomInfo, open, offsetVec3s));
                }
            }

            fakeContext.cleanup();

            precalculationApi.getValue().addWidget(new WidgetMissingPrecalculations(
                    dungeonRoomInfo.getUuid(),
                    CachedPathfinderRegistry.getByRoom(dungeonRoomInfo.getUuid()) == null ? Collections.emptyList() : CachedPathfinderRegistry.getByRoom(dungeonRoomInfo.getUuid()),
                    requests
            ));
        }
    }
}
