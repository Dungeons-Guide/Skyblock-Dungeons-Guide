/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret.triggers;


import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.types.TCKeybind;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionChangeState;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.PathfindImpossibleException;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRoute;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAG;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonRedstoneKeyState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.ISecret;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonRoomEnterEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.KeyBindPressedEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.routedisplay.RoomRouteHandler;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.entity.UPlayerSelf;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class FeatureSmartRoute extends SimpleFeature {
    public FeatureSmartRoute() {
        super("Pathfinding & Secrets.Smart Route", "Auto pathfind to new secret", "Generate smart route going through all secrets upon entering the room.", "secret.smartroute", false);
        addParameter("key", new FeatureParameter<Integer>("key", "Key", "Press to navigate to next best secret", Keyboard.KEY_NONE, TCKeybind.INSTANCE));
    }

    private final Set<DungeonRoom> triggered = Sets.newSetFromMap(new WeakHashMap<>());

    @DGEventHandler(ignoreDisabled = true)
    public void onKeybindPress(KeyBindPressedEvent keyBindPressedEvent) {
        if (keyBindPressedEvent.getKey() == this.<Integer>getParameter("key").getValue()) {

            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            if (!SkyblockStatus.isOnDungeon() || context == null) return;
            UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();
            if (thePlayer == null) return;
            if (context.getScaffoldParser() == null) return;
            Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());
            DungeonRoom currentRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
            if (currentRoom == null) return;
            if (!currentRoom.getRoomBounds().isFullyWithin(thePlayer.getPositionVector())) return;
            RoomRouteHandler handler = FeatureRegistry.SECRET_ROUTE_REGISTRY.getRoomHandler(currentRoom);
            if (handler == null) return;


            createSmartRoute(currentRoom, handler);
        }
    }

    @DGEventHandler
    public void onRoomEnter(DungeonRoomEnterEvent event) {
        if (triggered.contains(event.getDungeonRoom())) return;
        DungeonRoom dungeonRoom = event.getDungeonRoom();
        RoomRouteHandler handler = FeatureRegistry.SECRET_ROUTE_REGISTRY.getRoomHandler(dungeonRoom);
        if (handler == null) return;
        createSmartRoute(dungeonRoom, handler);
        triggered.add(dungeonRoom);
    }


    public void createSmartRoute(DungeonRoom dungeonRoom, RoomRouteHandler roomRouteHandler) {
        AlgorithmSetting algorithmSetting = roomRouteHandler.getAlgorithmSetting();
        ActionDAGBuilder actionDAGBuilder = new ActionDAGBuilder(dungeonRoom);
        for (Map.Entry<String, DungeonMechanicState> value : dungeonRoom.getMechanics().entrySet()) {
            if (value.getValue() instanceof ISecret && !((ISecret) value.getValue()).isFound(dungeonRoom)) {
                try {
                    actionDAGBuilder.requires(new ActionChangeState(value.getKey(), "found"), algorithmSetting);
                } catch (PathfindImpossibleException e) {
                    ChatTransmitter.addToQueue("Dungeons Guide :: Pathfind to "+value.getKey()+":found failed due to "+e.getMessage());
                    e.printStackTrace();
                    continue;
                }
            } else if (value.getValue() instanceof DungeonRedstoneKeyState && value.getValue().getCurrentState().equalsIgnoreCase("unobtained")) {
                try {
                    actionDAGBuilder.requires(new ActionChangeState(value.getKey(), "obtained-self"), algorithmSetting);
                } catch (PathfindImpossibleException e) {
                    ChatTransmitter.addToQueue("Dungeons Guide :: Pathfind to "+value.getKey()+":found failed due to "+e.getMessage());
                    e.printStackTrace();
                    continue;
                }
            }
        }

        try {
            ActionDAG dag = actionDAGBuilder.build();
            ActionRoute actionRoute = new ActionRoute("Smart Route", dungeonRoom, dag);
            roomRouteHandler.getPath().put("smart", FeatureRegistry.SECRET_LINE_PROPERTIES_SMART_ROUTE.createPathDisplayEngine(actionRoute));
//
        } catch (PathfindImpossibleException e) {
            ChatTransmitter.addToQueue("Dungeons Guide :: Pathfind to everything failed due to "+e.getMessage());
            e.printStackTrace();
        }
    }

}
