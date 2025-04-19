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
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionChangeState;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.PathfindImpossibleException;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRoute;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAG;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.ISecret;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonRoomEnterEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.KeyBindPressedEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.lineproperties.styles.ClassicPathDisplayEngine;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.routedisplay.RoomRouteHandler;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

import java.awt.*;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class FeaturePathfindToAll extends SimpleFeature {
    public FeaturePathfindToAll(){
        super("Pathfinding & Secrets.Display All", "Start pathfind to all secrets upon entering a room", "Auto browse to all secrets in the room", "secret.secretpathfind.allbrowse", false);
        addParameter("bat", new FeatureParameter<Boolean>("bat", "Trigger pathfind to Bat", "This feature will trigger pathfind to all bats in this room when entering a room", true, TCBoolean.INSTANCE));
        addParameter("chest", new FeatureParameter<Boolean>("chest", "Trigger pathfind to Chest", "This feature will trigger pathfind to all chests in this room when entering a room", true, TCBoolean.INSTANCE));
        addParameter("essence", new FeatureParameter<Boolean>("essence", "Trigger pathfind to Essence", "This feature will trigger pathfind to all essences in this room when entering a room", true, TCBoolean.INSTANCE));
        addParameter("itemdrop", new FeatureParameter<Boolean>("itemdrop", "Trigger pathfind to Itemdrop", "This feature will trigger pathfind to all itemdrops in this room when entering a room", true, TCBoolean.INSTANCE));
    }

    public boolean isBat() {
        return this.<Boolean>getParameter("bat").getValue();
    }
    public boolean isChest() {
        return this.<Boolean>getParameter("chest").getValue();
    }
    public boolean isEssence() {
        return this.<Boolean>getParameter("essence").getValue();
    }
    public boolean isItemdrop() {
        return this.<Boolean>getParameter("itemdrop").getValue();
    }



    private final Set<DungeonRoom> triggered = Sets.newSetFromMap(new WeakHashMap<>());


    @DGEventHandler
    public void onRoomEnter(DungeonRoomEnterEvent event) {
        if (triggered.contains(event.getDungeonRoom())) return;
        DungeonRoom dungeonRoom = event.getDungeonRoom();
        RoomRouteHandler handler = FeatureRegistry.SECRET_ROUTE_REGISTRY.getRoomHandler(dungeonRoom);
        if (handler == null) return;
        createRoute(dungeonRoom, handler);
        triggered.add(dungeonRoom);
    }


    public void createRoute(DungeonRoom dungeonRoom, RoomRouteHandler roomRouteHandler) {
        for (Map.Entry<String, DungeonMechanicState> value : dungeonRoom.getMechanics().entrySet()) {
            if (value.getValue() instanceof ISecret && !((ISecret) value.getValue()).isFound(dungeonRoom)) {
                ISecret secret = (ISecret) value.getValue();
                try {
                    if (FeatureRegistry.SECRET_PATHFIND_ALL.isBat() && secret instanceof DungeonSecretBatState)
                        roomRouteHandler.pathfind(value.getKey(), "found", FeatureRegistry.SECRET_LINE_PROPERTIES_PATHFINDALL_BAT.getRouteProperties());
                    if (FeatureRegistry.SECRET_PATHFIND_ALL.isChest() && secret instanceof DungeonSecretChestState)
                        roomRouteHandler.pathfind(value.getKey(), "found", FeatureRegistry.SECRET_LINE_PROPERTIES_PATHFINDALL_CHEST.getRouteProperties());
                    if (FeatureRegistry.SECRET_PATHFIND_ALL.isEssence() && secret instanceof DungeonSecretEssenceState)
                        roomRouteHandler.pathfind(value.getKey(), "found", FeatureRegistry.SECRET_LINE_PROPERTIES_PATHFINDALL_ESSENCE.getRouteProperties());
                    if (FeatureRegistry.SECRET_PATHFIND_ALL.isItemdrop() && secret instanceof DungeonSecretItemDropState)
                        roomRouteHandler.pathfind(value.getKey(), "found", FeatureRegistry.SECRET_LINE_PROPERTIES_PATHFINDALL_ITEM_DROP.getRouteProperties());
                } catch (Exception e) {
                    ChatTransmitter.addToQueue("Dungeons Guide :: Pathfind to "+value.getKey()+":found failed due to "+e.getMessage());
                }
            }
        }
    }

}
