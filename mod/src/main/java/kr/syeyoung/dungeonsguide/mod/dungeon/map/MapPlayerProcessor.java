/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.dungeon.map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.UEntityPlayer;
import kr.syeyoung.modapi.item.Item;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.world.UMapData;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.Map;

public class MapPlayerProcessor {

   private static final Minecraft mc = Minecraft.getMinecraft();
    private final DungeonContext context;
    @Getter
    private final BiMap<String, String> mapIconToPlayerMap = HashBiMap.create();
    Logger logger = LogManager.getLogger("DG-MapPlayerProcessor");
    private int waitDelay = 0;

    public MapPlayerProcessor(DungeonContext context) {
        this.context = context;
    }

    private static void error(String prefix) {
        ChatTransmitter.addToQueue(ChatTransmitter.prefix + prefix);
    }



    public void tick() {
        if (waitDelay < 5) {
            waitDelay++;
            return;
        }
        UItemStack stack = ModAPI.getAPI().getPlayer().getInventory().getMainInventory()[8];
        if (stack == null || !(stack.getItem() == Item.FILLED_MAP || stack.getItem() == Item.MAP)) {
            return;
        }

        UMapData mapData = ModAPI.getAPI().getWorld().getMapData(stack);

        if (mapData != null && mapIconToPlayerMap.size() < context.getPlayers().size()) {
            getPlayersFromMap(mapData);
        }

    }


    private void getPlayersFromMap(UMapData mapdata) {
        int lim = Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16;
        lim = lim * lim;

        if (context.getScaffoldParser() == null) return;
        for (Map.Entry<String, UMapData.MapMarker> stringMapMarkerEntry : mapdata.getMarkers().entrySet()) {
            String mapDecString = stringMapMarkerEntry.getKey();
            UMapData.MapMarker marker = stringMapMarkerEntry.getValue();
            if (marker.getMarkerId() == 1) continue;

            if (!mapIconToPlayerMap.containsValue(mapDecString)) {
                int x = marker.getX() / 2 + 64;
                int y = marker.getY() / 2 + 64;
                VectorI3D worldPos = context.getScaffoldParser().getDungeonMapLayout().mapPointToWorldPoint(new Point(x, y));
                if (ModAPI.getAPI().getPlayer().getPositionVector().distanceSq(worldPos) > lim) continue; // too far away
                String potentialPlayer = null;

                int players = 0;

                for (String player : context.getPlayers()) {
                    if (player.equals(ModAPI.getAPI().getPlayer().getName())) continue;
                    if (!mapIconToPlayerMap.containsKey(player) && isPlayerNear(player, worldPos)) {
                        potentialPlayer = player;
                        players++;
                    }
                }

                if (players == 1) {
                    mapIconToPlayerMap.put(potentialPlayer, mapDecString);
                }
            }
        }
    }

    private boolean isPlayerNear(String player, VectorI3D mapPos) {
        UEntityPlayer entityPlayer = context.getUworld().getUPlayerEntityByName(player);

        if (entityPlayer != null && !entityPlayer.isInvisible()) {
            VectorI3D pos = entityPlayer.getPosition();
            int dx = mapPos.getX() - pos.getX();
            int dz = mapPos.getZ() - pos.getZ();
            return dx * dx + dz * dz < 256; // deviation is within 16 blocks
        }

        return false;
    }
}
