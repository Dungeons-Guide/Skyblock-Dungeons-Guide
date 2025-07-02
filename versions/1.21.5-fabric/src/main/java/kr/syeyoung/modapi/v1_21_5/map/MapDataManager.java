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
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.map.MapState;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MapDataManager implements IMapUtils {
    public static final MapDataManager INSTANCE = new MapDataManager();
    private Map<MapIdComponent, MapState> mapDataMap = new HashMap<>();

    public MapState createMapData(MapIdComponent mapId, byte scale, boolean locked) {
        if (!mapDataMap.containsKey(mapId))
            mapDataMap.put(mapId, MapState.of(scale, locked, MinecraftClient.getInstance().world.getRegistryKey()));
        return mapDataMap.get(mapId);

    }

    public MapState getMapData(MapIdComponent mapId) {
        return mapDataMap.get(mapId);
    }

    public void refresh() {
        mapDataMap.clear();
    }

    public Map<MapIdComponent, MapState> getMapDataMap() {
        return Collections.unmodifiableMap(mapDataMap);
    }

    @Override
    public int getRGBColor(int mapColor) {
        return MapColor.getRenderColor(mapColor);
    }
}
