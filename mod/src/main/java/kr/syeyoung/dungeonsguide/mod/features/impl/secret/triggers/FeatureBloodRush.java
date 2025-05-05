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
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.types.TCKeybind;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonRoomDoor2State;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonRoomDoorState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonRoomEnterEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.KeyBindPressedEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.routedisplay.RoomRouteHandler;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Keyboard;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class FeatureBloodRush extends SimpleFeature {
    public FeatureBloodRush() {
        super("Pathfinding & Secrets.Blood Rush", "Blood Rush Mode", "Auto pathfind to witherdoors. \nCan be toggled with key set in settings", "secret.bloodrush", false);
        addParameter("key", new FeatureParameter<Integer>("key", "Key", "Press to toggle Blood Rush", Keyboard.KEY_NONE, TCKeybind.INSTANCE));
    }

    private final Set<DungeonRoom> triggered = Sets.newSetFromMap(new WeakHashMap<>());

    @DGEventHandler(ignoreDisabled = true)
    public void onKeybindPress(KeyBindPressedEvent keyBindPressedEvent) {
        if (keyBindPressedEvent.getKey() == this.<Integer>getParameter("key").getValue()) {
            setEnabled(!isEnabled());
            try {
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §fToggled Blood Rush to §e"+(FeatureRegistry.SECRET_BLOOD_RUSH.isEnabled() ? "on":"off")));
            } catch (Exception ignored) {}
        }
    }

    @DGEventHandler
    public void onRoomEnter(DungeonRoomEnterEvent event) {
        if (triggered.contains(event.getDungeonRoom())) return;
        DungeonRoom dungeonRoom = event.getDungeonRoom();
        RoomRouteHandler handler = FeatureRegistry.SECRET_ROUTE_REGISTRY.getRoomHandler(dungeonRoom);
        if (handler == null) return;

        for (Map.Entry<String, DungeonMechanicState> value : dungeonRoom.getMechanics().entrySet()) {
            if (value.getValue() instanceof DungeonRoomDoorState) {
                DungeonRoomDoorState dungeonDoor = (DungeonRoomDoorState) value.getValue();
                if (dungeonDoor.getDoorfinder().getType().isHeadToBlood()) {
                    try {
                        handler.pathfind("blood-rush", value.getKey(), "navigate", FeatureRegistry.SECRET_BLOOD_RUSH_LINE_PROPERTIES::createPathDisplayEngine);
                    } catch (Exception e) {
                        ChatTransmitter.addToQueue("Dungeons Guide :: Pathfind to "+value.getKey()+":found failed due to "+e.getMessage());
                    }
                }
            } else if (value.getValue() instanceof DungeonRoomDoor2State) {
                DungeonRoomDoor2State dungeonDoor = (DungeonRoomDoor2State) value.getValue();
                if (dungeonDoor.isHeadtoBlood(dungeonRoom)) {
                    try {
                        handler.pathfind("blood-rush", value.getKey(), "navigate", FeatureRegistry.SECRET_BLOOD_RUSH_LINE_PROPERTIES::createPathDisplayEngine);
                    } catch (Exception e) {
                        ChatTransmitter.addToQueue("Dungeons Guide :: Pathfind to "+value.getKey()+":found failed due to "+e.getMessage());
                    }
                }
            }
        }
        triggered.add(dungeonRoom);
    }
}
