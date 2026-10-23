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

package kr.syeyoung.dungeonsguide.mod.parallelUniverse.map;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MapDataManager {
    public static final MapDataManager INSTANCE = new MapDataManager();
    private Map<Integer, MapItemSavedData> mapDataMap = new HashMap<>();

    // 26.1 maps are created with the packet's scale / lock and the current dimension, like the client does
    public MapItemSavedData createMapData(int mapId, byte scale, boolean locked) {
        if (!mapDataMap.containsKey(mapId))
            mapDataMap.put(mapId, MapItemSavedData.createForClient(scale, locked, Minecraft.getInstance().level.dimension()));
        return mapDataMap.get(mapId);

    }

    public MapItemSavedData getMapData(int mapId) {
        return mapDataMap.get(mapId);
    }

    public void refresh() {
        mapDataMap.clear();
    }

    public Map<Integer, MapItemSavedData> getMapDataMap() {
        return Collections.unmodifiableMap(mapDataMap);
    }
}
