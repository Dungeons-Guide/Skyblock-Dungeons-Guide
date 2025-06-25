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
import kr.syeyoung.dungeonsguide.mod.config.types.TCBoolean;
import kr.syeyoung.dungeonsguide.mod.config.types.TCKeybind;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonSecretBatState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.ISecret;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonRoomEnterEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.KeyBindPressedEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.routedisplay.RoomRouteHandler;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.UPlayerSelf;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class FeatureSingleTargetPathfind extends SimpleFeature {
    public FeatureSingleTargetPathfind() {
        super("Pathfinding & Secrets.Display One", "Auto pathfind to new secret", "Auto browse best secret upon entering the room. if enabled. (click configure to see more options)", "secret.autouponenter", false);
        addParameter("key", new FeatureParameter<Integer>("key", "Key", "Press to navigate to next best secret", Keyboard.KEY_NONE, TCKeybind.INSTANCE));
        addParameter("nextAuto", new FeatureParameter<Boolean>("nextAuto", "Auto Pathfind to next secret", "Auto browse best next secret after current one completes.\nthe first pathfinding of first secret needs to be triggered first in order for this option to work", false, TCBoolean.INSTANCE));
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


            createRoute(currentRoom, handler);
        }
    }

    @DGEventHandler
    public void onRoomEnter(DungeonRoomEnterEvent event) {
        if (triggered.contains(event.getDungeonRoom())) return;
        DungeonRoom dungeonRoom = event.getDungeonRoom();
        RoomRouteHandler handler = FeatureRegistry.SECRET_ROUTE_REGISTRY.getRoomHandler(dungeonRoom);
        if (handler == null) return;
        createRoute(dungeonRoom, handler);
        triggered.add(dungeonRoom);
    }

    @DGEventHandler
    public void onTick(DGTickEvent tickEvent) {
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
        if (!triggered.contains(currentRoom)) return;

        if (handler.getPath("AUTO-BROWSE") == null && this.<Boolean>getParameter("nextAuto").getValue()) {
            createRoute(currentRoom, handler);
        }
    }

    private WeakHashMap<DungeonRoom, Set<String>> visitedMap = new WeakHashMap<>();

    public void createRoute(DungeonRoom dungeonRoom, RoomRouteHandler roomRouteHandler) {
        Set<String> visited = visitedMap.computeIfAbsent(dungeonRoom, (a) -> new HashSet<>());

        if (dungeonRoom.getCurrentState() == DungeonRoom.RoomState.FINISHED) {
            roomRouteHandler.cancelAll();
            return;
        }

        VectorI3D pos = ModAPI.getAPI().getPlayer().getPosition();

        double lowestCost = 99999999999999.0;
        Map.Entry<String, DungeonMechanicState> lowestWeightMechanic = null;
        for (Map.Entry<String, DungeonMechanicState> mech: dungeonRoom.getMechanics().entrySet()) {
            if (!(mech.getValue() instanceof ISecret)) continue;
            if (visited.contains(mech.getKey())) continue;
            if (!((ISecret) mech.getValue()).isFound(dungeonRoom)) {
                double cost = 0;
                if (mech.getValue() instanceof DungeonSecretBatState &&
                        ((ISecret)mech.getValue()).getPreRequisite().size() == 0) {
                    cost += -100000000;
                }
                if (mech.getValue().getRepresentingPoint() == null) continue;
                VectorI3D blockpos = mech.getValue().getRepresentingPoint().getBlockPos(dungeonRoom);

                cost += blockpos.distanceSq(pos);
                cost += ((ISecret) mech.getValue()).getPreRequisite().size() * 100;

                if (cost < lowestCost) {
                    lowestCost = cost;
                    lowestWeightMechanic = mech;
                }
            }
        }
        if (lowestWeightMechanic != null) {
            visited.add(lowestWeightMechanic.getKey());
            try {
                roomRouteHandler.pathfind("AUTO-BROWSE", lowestWeightMechanic.getKey(), "found", FeatureRegistry.SECRET_LINE_PROPERTIES_AUTOPATHFIND::createPathDisplayEngine);
            } catch (Exception e) {
                ChatTransmitter.addToQueue("Dungeons Guide :: Pathfind to "+lowestWeightMechanic.getKey()+":found failed due to "+e.getMessage());
                e.printStackTrace();
            }
        } else {
            visited.clear();
        }
    }

}
