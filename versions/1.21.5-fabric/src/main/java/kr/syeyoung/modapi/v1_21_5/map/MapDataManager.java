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

package kr.syeyoung.modapi.v1_21_5.map;

import kr.syeyoung.modapi.world.IMapUtils;
import net.minecraft.block.material.MapColor;
import net.minecraft.world.storage.MapData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MapDataManager implements IMapUtils {
    public static final MapDataManager INSTANCE = new MapDataManager();
    private Map<Integer, MapData> mapDataMap = new HashMap<>();

    public MapData createMapData(int mapId) {
        if (!mapDataMap.containsKey(mapId))
            mapDataMap.put(mapId, new MapData("map_"+mapId));
        return mapDataMap.get(mapId);

    }

    public MapData getMapData(int mapId) {
        return mapDataMap.get(mapId);
    }

    public void refresh() {
        mapDataMap.clear();
    }

    public Map<Integer, MapData> getMapDataMap() {
        return Collections.unmodifiableMap(mapDataMap);
    }

    @Override
    public int getRGBColor(int mapColor) {
        return MapColor.mapColorArray[mapColor / 4].getMapColor(mapColor & 3);
    }
}
