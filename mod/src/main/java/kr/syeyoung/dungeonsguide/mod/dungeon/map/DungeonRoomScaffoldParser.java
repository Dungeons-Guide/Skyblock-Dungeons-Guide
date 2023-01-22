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

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder.EDungeonDoorType;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.SerializableBlockPos;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.impl.DungeonRoomDiscoverEvent;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.utils.MapUtils;
import lombok.Getter;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Tuple;
import net.minecraft.world.storage.MapData;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DungeonRoomScaffoldParser {
    @Getter
    private DungeonMapLayout dungeonMapLayout;
    private DungeonContext context;

    @Getter
    private MapData latestMapData;



    ExecutorService es = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setThreadFactory(DungeonsGuide.THREAD_FACTORY)
            .setNameFormat("Dg-MapPlayerProcessor-%d").build());

    @Getter
    private final Map<Point, DungeonRoom> roomMap = new HashMap<>();
    @Getter
    private final List<DungeonRoom> dungeonRoomList = new ArrayList<>();

    public DungeonRoomScaffoldParser(DungeonMapLayout layout, DungeonContext context) {
        this.dungeonMapLayout = layout;
        this.context = context;
    }

    @Getter
    private int undiscoveredRoom = 0;

    public void processMap(MapData mapData2) {
        int roomHeight = (int) ((128.0 - dungeonMapLayout.getOriginPoint().y) / (dungeonMapLayout.getUnitRoomSize().height + dungeonMapLayout.getMapRoomGap()));
        int roomWidth = (int) ((128.0 - dungeonMapLayout.getOriginPoint().x) / (dungeonMapLayout.getUnitRoomSize().width + dungeonMapLayout.getMapRoomGap()));
        latestMapData = mapData2;
        byte[] mapData = mapData2.colors;
        if (MapUtils.getMapColorAt(mapData, 0, 0) != 0) return;
        undiscoveredRoom = 0;
        for (int y = 0; y <= roomHeight; y++) {
            for (int x = 0; x <= roomWidth; x++) {
                Point mapPoint = dungeonMapLayout.roomPointToMapPoint(new Point(x, y));
                byte color = MapUtils.getMapColorAt(mapData, mapPoint.x, mapPoint.y);
                MapUtils.record(mapData, mapPoint.x, mapPoint.y, new Color(255, 255, 0, 80));
                if (roomMap.containsKey(new Point(x, y))) {
                    DungeonRoom dungeonRoom = roomMap.get(new Point(x, y));
                    if (color == 18 && dungeonRoom.getCurrentState() != DungeonRoom.RoomState.FINISHED) {
                        dungeonRoom.setCurrentState(DungeonRoom.RoomState.COMPLETE_WITHOUT_SECRETS);
                        dungeonRoom.setTotalSecrets(0);
                    } else if (color == 30) {
                        dungeonRoom.setCurrentState(DungeonRoom.RoomState.FINISHED);
                        dungeonRoom.setTotalSecrets(0);
                    } else if (dungeonRoom.getCurrentState() != DungeonRoom.RoomState.FINISHED) {
                        byte centerColor = MapUtils.getMapColorAt(mapData, mapPoint.x + dungeonMapLayout.getUnitRoomSize().width / 2, mapPoint.y + dungeonMapLayout.getUnitRoomSize().height / 2);
                        MapUtils.record(mapData, mapPoint.x + dungeonMapLayout.getUnitRoomSize().width / 2, mapPoint.y + dungeonMapLayout.getUnitRoomSize().height / 2, new Color(0, 255, 0, 80));
                        if (centerColor == 34) {
                            dungeonRoom.setCurrentState(DungeonRoom.RoomState.COMPLETE_WITHOUT_SECRETS);
                        } else if (centerColor == 30) {
                            dungeonRoom.setCurrentState(DungeonRoom.RoomState.FINISHED);
                        } else if (centerColor == 18) {  // red
                            dungeonRoom.setCurrentState(DungeonRoom.RoomState.FAILED);
                        }
                    }
                    if (dungeonRoom.getTotalSecrets() == -1) {
                        if (dungeonRoom.getColor() == 82 || dungeonRoom.getColor() == 74) {
                            dungeonRoom.setTotalSecrets(0);
                        }
                        MapUtils.record(mapData, mapPoint.x, mapPoint.y + 1, new Color(0, 255, 0, 80));
                    }
                    continue;
                }

                if (color != 0 && color != 85) {
                    MapUtils.record(mapData, mapPoint.x, mapPoint.y, new Color(0, 255, 255, 80));
                    DungeonRoom room = buildRoom(mapData, new Point(x, y));


                    // USELESS DEBUG CODE
                    context.getRecorder().createEvent(new DungeonRoomDiscoverEvent(room.getUnitPoints().iterator().next(),
                            new SerializableBlockPos(room.getMin()), new SerializableBlockPos(room.getMax()),
                            room.getShape(), room.getColor()));
                    ChatTransmitter.sendDebugChat(new ChatComponentText("New Map discovered! shape: " + room.getShape() + " color: " + room.getColor() + " unitPos: " + x + "," + y));
                    ChatTransmitter.sendDebugChat(new ChatComponentText("New Map discovered! mapMin: " + room.getMin() + " mapMx: " + room.getMax()));
                    StringBuilder builder = new StringBuilder();
                    for (int dy = 0; dy < 4; dy++) {
                        builder.append("\n");
                        for (int dx = 0; dx < 4; dx++) {
                            boolean isSet = ((room.getShape() >> (dy * 4 + dx)) & 0x1) != 0;
                            builder.append(isSet ? "O" : "X");
                        }
                    }
                    ChatTransmitter.sendDebugChat(new ChatComponentText("Shape visual: " + builder));
                    // END


                    dungeonRoomList.add(room);
                    for (Point p : room.getUnitPoints()) {
                        roomMap.put(p, room);
                    }
                    if (room.getRoomProcessor() != null && room.getRoomProcessor().readGlobalChat()) {
                        context.getGlobalRoomProcessors().add(room.getRoomProcessor());
                    }
                } else if (color == 85) {
                    undiscoveredRoom++;
                }

            }
        }
    }
    private static final Set<Vector2d> directions = Sets.newHashSet(new Vector2d(0, 1), new Vector2d(0, -1), new Vector2d(1, 0), new Vector2d(-1, 0));
    private static final Set<Vector2d> door_dirs = Sets.newHashSet(new Vector2d(0, 0.5), new Vector2d(0, -0.5), new Vector2d(0.5, 0), new Vector2d(-0.5, 0));

    private DungeonRoom buildRoom(byte[] mapData, Point unitPoint) {
        java.util.Queue<Point[]> toCheck = new LinkedList<>();
        toCheck.add(new Point[]{unitPoint, unitPoint}); // requestor, target
        Set<Point> checked = new HashSet<>();
        Set<Point> ayConnected = new HashSet<>();

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = 0;
        int maxY = 0;
        while (toCheck.peek() != null) {
            Point[] check = toCheck.poll();
            if (checked.contains(check[1])) {
                continue;
            }
            checked.add(check[1]);

            if (checkIfConnected(mapData, check[0], check[1])) {
                ayConnected.add(check[1]);
                if (check[1].x < minX) minX = check[1].x;
                if (check[1].y < minY) minY = check[1].y;
                if (check[1].x > maxX) maxX = check[1].x;
                if (check[1].y > maxY) maxY = check[1].y;
                for (Vector2d dir : directions) {
                    Point newPt = new Point(check[1].x + (int) dir.x, check[1].y + (int) dir.y);
                    toCheck.add(new Point[]{check[1], newPt});
                }
            }
        }

        short shape = 0;
        for (Point p : ayConnected) {
            int localX = p.x - minX;
            int localY = p.y - minY;
            shape |= 1 << (localY * 4 + localX);
        }
        Set<Vector2d> doors = new HashSet<>();
        for (Point p : ayConnected) {
            for (Vector2d v : door_dirs) {
                Vector2d v2 = new Vector2d(p.x + v.x, p.y + v.y);
                if (doors.contains(v2)) doors.remove(v2);
                else doors.add(v2);
            }
        }
        Point pt2 = dungeonMapLayout.roomPointToMapPoint(ayConnected.iterator().next());
        byte unit1 = MapUtils.getMapColorAt(mapData, pt2.x, pt2.y);

        // 0: none 1: open door 2. unopen door 3: wither door 4. red door
        Set<Tuple<Vector2d, EDungeonDoorType>> doorsAndStates = new HashSet<>();
        final int halfWidth = dungeonMapLayout.getUnitRoomSize().width + 4;
        for (Vector2d door : doors) {
            int floorX = (int) Math.floor(door.x);
            int floorY = (int) Math.floor(door.y);
            Point mapPt = dungeonMapLayout.roomPointToMapPoint(new Point(floorX, floorY));
            Point target = new Point(mapPt.x + dungeonMapLayout.getUnitRoomSize().width / 2 + (int) (halfWidth * (door.x - floorX)), mapPt.y +
                    dungeonMapLayout.getUnitRoomSize().height / 2 + (int) (halfWidth * (door.y - floorY)));
            MapUtils.record(mapData, target.x, target.y, Color.green);

            byte color = MapUtils.getMapColorAt(mapData, target.x, target.y);

            Vector2d vector2d = new Vector2d(door.x - minX, door.y - minY);

            if (color == 0) {
                doorsAndStates.add(new Tuple<>(vector2d, EDungeonDoorType.NONE));
            } else if (color == 85) {
                doorsAndStates.add(new Tuple<>(vector2d, EDungeonDoorType.UNOPEN));
            } else if (color == 119) {
                doorsAndStates.add(new Tuple<>(vector2d, EDungeonDoorType.WITHER));
            } else if (color == 18 && unit1 != 18) {
                doorsAndStates.add(new Tuple<>(vector2d, EDungeonDoorType.BLOOD));
            } else {
                doorsAndStates.add(new Tuple<>(vector2d, EDungeonDoorType.ENTRANCE));
            }

        }


        return new DungeonRoom(ayConnected, shape, unit1, dungeonMapLayout.roomPointToWorldPoint(new Point(minX, minY)), dungeonMapLayout.roomPointToWorldPoint(new Point(maxX + 1, maxY + 1)).add(-1, 0, -1), context, doorsAndStates);

    }

    private boolean checkIfConnected(byte[] mapData, Point unitPoint1, Point unitPoint2) {
        if (unitPoint1 == unitPoint2) return true;
        if (unitPoint1.equals(unitPoint2)) return true;


        Point high;
        if (unitPoint2.y > unitPoint1.y) {
            high = unitPoint2;
        } else {
            if (unitPoint2.x > unitPoint1.x) {
                high = unitPoint2;
            } else {
                high = unitPoint1;
            }
        }

        Point low;
        if (high == unitPoint2) {
            low = unitPoint1;
        } else {
            low = unitPoint2;
        }

        int xOff = low.x - high.x;
        int yOff = low.y - high.y;
        Point pt = dungeonMapLayout.roomPointToMapPoint(high);
        Point pt2 = dungeonMapLayout.roomPointToMapPoint(low);
        byte unit1 = MapUtils.getMapColorAt(mapData, pt.x, pt.y);
        byte unit2 = MapUtils.getMapColorAt(mapData, pt2.x, pt2.y);
        pt.translate(xOff, yOff);
        byte unit3 = MapUtils.getMapColorAt(mapData, pt.x, pt.y);

        return unit1 == unit2 && unit2 == unit3 && unit1 != 0;
    }
}
