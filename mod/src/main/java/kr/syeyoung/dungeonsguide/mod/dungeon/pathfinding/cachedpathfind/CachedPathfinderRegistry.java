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

package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind;

import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import lombok.Getter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.*;

public class CachedPathfinderRegistry {

    @Getter
    private static final List<PathfindCache> registered = new ArrayList<>();
    private static final Map<String, PathfindCache> idMap = new HashMap<String, PathfindCache>();
    private static final Map<UUID, List<PathfindCache>> byRoom = new HashMap<UUID, List<PathfindCache>>();

    public static List<PathfindCache> getByRoom(UUID uid) {
        return byRoom.get(uid);
    }

    public static Map<UUID, List<PathfindCache>> getByRooms() {
        return byRoom;
    }
    public static PathfindCache getById(String id) {
        return idMap.get(id);
    }

    public static void register(PathfindCache cachedPathfinder) {
        if (idMap.containsKey(cachedPathfinder.getId())) {
            PathfindCache cache = idMap.remove(cachedPathfinder.getId());
            registered.remove(cache);
            byRoom.get(cache.getRoomId()).remove(cache);
            System.out.println("Dupe? "+cachedPathfinder.getId());
        }

        registered.add(cachedPathfinder);
        idMap.put(cachedPathfinder.getId(), cachedPathfinder);
//        System.out.println("Loading "+cachedPathfinder.getId());
        byRoom.computeIfAbsent(cachedPathfinder.getRoomId(),(a) -> new ArrayList<>());
        byRoom.get(cachedPathfinder.getRoomId()).add(cachedPathfinder);
    }

    public static void loadAll(File dir) {
        registered.clear();
        idMap.clear();
        byRoom.clear();
        for (File f : dir.listFiles()) {
            if (!f.getName().endsWith(".pfres")) continue;
            try {
                PathfindCache pathfindCache = new PathfindCache(f);
                register(pathfindCache);
            } catch (Exception e) {
                System.out.println(f.getName());e.printStackTrace();}
        }
    }
}
