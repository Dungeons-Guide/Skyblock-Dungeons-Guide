/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.dungeon;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.EDungeonDoorType;
import kr.syeyoung.dungeonsguide.dungeon.events.SerializableBlockPos;
import kr.syeyoung.dungeonsguide.dungeon.events.impl.DungeonMapUpdateEvent;
import kr.syeyoung.dungeonsguide.dungeon.events.impl.DungeonRoomDiscoverEvent;
import kr.syeyoung.dungeonsguide.dungeon.map.DungeonMapData;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.utils.MapUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.storage.MapData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapProcessor {

    private static final Set<Vector2d> directions = Sets.newHashSet(new Vector2d(0, 1), new Vector2d(0, -1), new Vector2d(1, 0), new Vector2d(-1, 0));
    private static final Set<Vector2d> door_dirs = Sets.newHashSet(new Vector2d(0, 0.5), new Vector2d(0, -0.5), new Vector2d(0.5, 0), new Vector2d(-0.5, 0));
    private static final Minecraft mc = Minecraft.getMinecraft();
    private final DungeonContext context;
    @Getter
    private final BiMap<String, String> mapIconToPlayerMap = HashBiMap.create();
    private final List<Point> roomsFound = new ArrayList<>();
    Logger logger = LogManager.getLogger("MapProcessor");
    /**
     * If the player on the map is closer than value this it won't save it
     * this should be done with render-distance but whateva
     */
    int clossnessDistance = 50;
    @Getter
    @Setter
    private Dimension unitRoomDimension;
    @Getter @Setter
    private Dimension doorDimensions; // width: width of door, height: gap between rooms
    @Getter
    @Setter
    private Point topLeftMapPoint;
    @Setter
    private boolean bugged = false;
    @Getter
    private boolean initialized = false;
    @Getter
    private int undiscoveredRoom = 0;
    private boolean processed = false;
    @Getter
    private MapData latestMapData;
    private int waitDelay = 0;
    private boolean processlock;

    public MapProcessor(DungeonContext context) {
        this.context = context;
    }

    private static void error(String prefix) {
        ChatTransmitter.addToQueue(new ChatComponentText(DungeonsGuide.PREFIX + prefix));
    }


    ExecutorService es = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Dg-MapProcessor-%d").build());


    int processMapThroddle;

    public void tick() {
        if (waitDelay < 5) {
            waitDelay++;
            return;
        }
        if (bugged) {
            return;
        }
        ItemStack stack = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(8);

        if (stack == null || !(stack.getItem() instanceof ItemMap)) {
            return;
        }

        MapData mapData = ((ItemMap) stack.getItem()).getMapData(stack, mc.theWorld);

        if (mapData != null) {

            if(processMapThroddle > 5 && !processlock){
                processMapData(mapData.colors);
                processMapThroddle = 0;
            }
            processMapThroddle++;

        }

        latestMapData = mapData;

        if (latestMapData != null && mapIconToPlayerMap.size() < context.getPlayers().size() && initialized) {
            getPlayersFromMap(latestMapData);
        }

    }

    private void processMapData(byte[] mapColorData) {

        // i just cant get this to work sad
        if (isThereDifference(latestMapData.colors, mapColorData)) {
            context.createEvent(new DungeonMapUpdateEvent(mapColorData));

            es.execute(() -> {
                processlock = true;
                if (doorDimensions == null || !initialized) {
                    assembleMap(mapColorData);
                } else {
                    processMap(mapColorData);
                }

                if (context.isEnded()) {
                    processFinishedMap(mapColorData);
                }
                processlock = false;
            });

        }

    }

    void assembleMap(final byte[] mapData){
        DungeonMapData data = new DungeonMapData(context, Minecraft.getMinecraft());

        data.eat(mapData);

        bugged = data.bugged;

        unitRoomDimension = data.unitRoomDimension;

        topLeftMapPoint = data.topLeftMapPoint;

        initialized = data.initialized;

        doorDimensions = data.doorDimensions;

    }


    public static Point mapPointToRoomPoint(Point mapPoint, Point topLeftMapPoint, Dimension unitRoomDimension, Dimension doorDimensions) {
        int x = (int) ((mapPoint.x - topLeftMapPoint.x) / ((double) unitRoomDimension.width + doorDimensions.height));
        int y = (int) ((mapPoint.y - topLeftMapPoint.y) / ((double) unitRoomDimension.height + doorDimensions.height));
        return new Point(x, y);
    }

    public BlockPos mapPointToWorldPoint(Point mapPoint) {
        int x = (int) ((mapPoint.x - topLeftMapPoint.x) / ((double) unitRoomDimension.width + doorDimensions.height) * 32 + context.getDungeonMin().getX());
        int y = (int) ((mapPoint.y - topLeftMapPoint.y) / ((double) unitRoomDimension.height + doorDimensions.height) * 32 + context.getDungeonMin().getZ());
        return new BlockPos(x, 70, y);
    }

    public Point roomPointToMapPoint(Point roomPoint) {
        return new Point(roomPoint.x * (unitRoomDimension.width + doorDimensions.height) + topLeftMapPoint.x, roomPoint.y * (unitRoomDimension.height + doorDimensions.height) + topLeftMapPoint.y);
    }

    public BlockPos roomPointToWorldPoint(Point roomPoint) {
        return new BlockPos(context.getDungeonMin().getX() + (roomPoint.x * 32), context.getDungeonMin().getY(), context.getDungeonMin().getZ() + (roomPoint.y * 32));
    }

    public Point worldPointToRoomPoint(BlockPos worldPoint) {
        if (context.getDungeonMin() == null) return null;
        return new Point((worldPoint.getX() - context.getDungeonMin().getX()) / 32, (worldPoint.getZ() - context.getDungeonMin().getZ()) / 32);
    }

    public Point worldPointToMapPoint(Vec3 worldPoint) {
        if (context.getDungeonMin() == null) return null;
        return new Point(topLeftMapPoint.x + (int) ((worldPoint.xCoord - context.getDungeonMin().getX()) / 32.0f * (unitRoomDimension.width + doorDimensions.height)), topLeftMapPoint.y + (int) ((worldPoint.zCoord - context.getDungeonMin().getZ()) / 32.0f * (unitRoomDimension.height + doorDimensions.height)));
    }

    public Vector2d worldPointToMapPointFLOAT(Vec3 worldPoint) {
        if (context.getDungeonMin() == null) return null;
        double x = topLeftMapPoint.x + ((worldPoint.xCoord - context.getDungeonMin().getX()) / 32.0f * (unitRoomDimension.width + doorDimensions.height));
        double y = topLeftMapPoint.y + ((worldPoint.zCoord - context.getDungeonMin().getZ()) / 32.0f * (unitRoomDimension.height + doorDimensions.height));
        return new Vector2d(x, y);
    }

    private void processMap(byte[] mapData) {
        int roomHeight = (int) ((128.0 - topLeftMapPoint.y) / (unitRoomDimension.height + doorDimensions.height));
        int roomWidth = (int) ((128.0 - topLeftMapPoint.x) / (unitRoomDimension.width + doorDimensions.height));
        if (MapUtils.getMapColorAt(mapData, 0, 0) != 0) return;
        undiscoveredRoom = 0;
        for (int y = 0; y <= roomHeight; y++) {
            for (int x = 0; x <= roomWidth; x++) {
                Point mapPoint = roomPointToMapPoint(new Point(x, y));
                byte color = MapUtils.getMapColorAt(mapData, mapPoint.x, mapPoint.y);
                MapUtils.record(mapData, mapPoint.x, mapPoint.y, new Color(255, 255, 0, 80));
                if (roomsFound.contains(new Point(x, y))) {
                    DungeonRoom dungeonRoom = context.getRoomMapper().get(new Point(x, y));
                    if (color == 18 && dungeonRoom.getCurrentState() != DungeonRoom.RoomState.FINISHED) {
                        dungeonRoom.setCurrentState(DungeonRoom.RoomState.COMPLETE_WITHOUT_SECRETS);
                        dungeonRoom.setTotalSecrets(0);
                    } else if (color == 30) {
                        dungeonRoom.setCurrentState(DungeonRoom.RoomState.FINISHED);
                        dungeonRoom.setTotalSecrets(0);
                    } else if (dungeonRoom.getCurrentState() != DungeonRoom.RoomState.FINISHED) {
                        byte centerColor = MapUtils.getMapColorAt(mapData, mapPoint.x + unitRoomDimension.width / 2, mapPoint.y + unitRoomDimension.height / 2);
                        MapUtils.record(mapData, mapPoint.x + unitRoomDimension.width / 2, mapPoint.y + unitRoomDimension.height / 2, new Color(0, 255, 0, 80));
                        switch (centerColor) {
                            case 34:
                                dungeonRoom.setCurrentState(DungeonRoom.RoomState.COMPLETE_WITHOUT_SECRETS);
                                break;
                            case 30:
                                dungeonRoom.setCurrentState(DungeonRoom.RoomState.FINISHED);
                                break;
                            case 18:  // red
                                dungeonRoom.setCurrentState(DungeonRoom.RoomState.FAILED);
                                break;
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
                    context.createEvent(new DungeonRoomDiscoverEvent(room.getUnitPoints().get(0), room.getRoomMatcher().getRotation(), new SerializableBlockPos(room.getMin()), new SerializableBlockPos(room.getMax()), room.getShape(), room.getColor(), room.getDungeonRoomInfo().getUuid(), room.getDungeonRoomInfo().getName(), room.getDungeonRoomInfo().getProcessorId()));
                    DungeonsGuide.sendDebugChat(new ChatComponentText("New Map discovered! shape: " + room.getShape() + " color: " + room.getColor() + " unitPos: " + x + "," + y));
                    DungeonsGuide.sendDebugChat(new ChatComponentText("New Map discovered! mapMin: " + room.getMin() + " mapMx: " + room.getMax()));
                    StringBuilder builder = new StringBuilder();
                    for (int dy = 0; dy < 4; dy++) {
                        builder.append("\n");
                        for (int dx = 0; dx < 4; dx++) {
                            boolean isSet = ((room.getShape() >> (dy * 4 + dx)) & 0x1) != 0;
                            builder.append(isSet ? "O" : "X");
                        }
                    }
                    DungeonsGuide.sendDebugChat(new ChatComponentText("Shape visual: " + builder));
                    // END


                    context.getDungeonRoomList().add(room);
                    for (Point p : room.getUnitPoints()) {
                        roomsFound.add(p);
                        context.getRoomMapper().put(p, room);
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

    private DungeonRoom buildRoom(byte[] mapData, Point unitPoint) {
        Queue<Point[]> toCheck = new LinkedList<>();
        toCheck.add(new Point[]{unitPoint, unitPoint}); // requestor, target
        Set<Point> checked = new HashSet<>();
        List<Point> ayConnected = new ArrayList<>();

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
        Point pt2 = roomPointToMapPoint(ayConnected.get(0));
        byte unit1 = MapUtils.getMapColorAt(mapData, pt2.x, pt2.y);

        // 0: none 1: open door 2. unopen door 3: wither door 4. red door
        Set<Tuple<Vector2d, EDungeonDoorType>> doorsAndStates = new HashSet<>();
        final int halfWidth = unitRoomDimension.width + 4;
        for (Vector2d door : doors) {
            int floorX = (int) Math.floor(door.x);
            int floorY = (int) Math.floor(door.y);
            Point mapPt = roomPointToMapPoint(new Point(floorX, floorY));
            Point target = new Point(mapPt.x + unitRoomDimension.width / 2 + (int) (halfWidth * (door.x - floorX)), mapPt.y + unitRoomDimension.height / 2 + (int) (halfWidth * (door.y - floorY)));
            MapUtils.record(mapData, target.x, target.y, Color.green);

            byte color = MapUtils.getMapColorAt(mapData, target.x, target.y);

            Vector2d vector2d = new Vector2d(door.x - minX, door.y - minY);

            switch (color) {
                case 0:
                    doorsAndStates.add(new Tuple<>(vector2d, EDungeonDoorType.NONE));
                    break;
                case 85:
                    doorsAndStates.add(new Tuple<>(vector2d, EDungeonDoorType.UNOPEN));
                    break;
                case 119:
                    doorsAndStates.add(new Tuple<>(vector2d, EDungeonDoorType.WITHER));
                    break;
                case 18:
                    if (unit1 != 18) {
                        doorsAndStates.add(new Tuple<>(vector2d, EDungeonDoorType.BLOOD));
                    }
                    break;
                default:
                    doorsAndStates.add(new Tuple<>(vector2d, EDungeonDoorType.ENTRANCE));
            }

        }


        return new DungeonRoom(ayConnected, shape, unit1, roomPointToWorldPoint(new Point(minX, minY)), roomPointToWorldPoint(new Point(maxX + 1, maxY + 1)).add(-1, 0, -1), context, doorsAndStates);

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
        Point pt = roomPointToMapPoint(high);
        Point pt2 = roomPointToMapPoint(low);
        byte unit1 = MapUtils.getMapColorAt(mapData, pt.x, pt.y);
        byte unit2 = MapUtils.getMapColorAt(mapData, pt2.x, pt2.y);
        pt.translate(xOff, yOff);
        byte unit3 = MapUtils.getMapColorAt(mapData, pt.x, pt.y);

        return unit1 == unit2 && unit2 == unit3 && unit1 != 0;
    }

    public boolean isThereDifference(byte[] colorData, byte[] colorData1) {

        return true;
//        boolean equals = Arrays.equals(colorData1, colorData);
//
//        boolean foundDIffrentThen0 = false;
//
//
//        for (byte colorDatum : colorData) {
//            if(colorDatum != 0){
//                foundDIffrentThen0 = true;
//                break;
//            }
//        }
//
//
//        return !(equals && foundDIffrentThen0);
    }

    private void processFinishedMap(byte[] mapData) {
        if (MapUtils.getMapColorAt(mapData, 0, 0) == 0) {
            return;
        }
        if (processed) {
            return;
        }
        processed = true;

        MapUtils.clearMap();
        MapUtils.record(mapData, 0, 0, Color.GREEN);


        FeatureRegistry.ETC_COLLECT_SCORE.collectDungeonRunData(mapData, context);

    }

    private void getPlayersFromMap(MapData mapdata) {

        if (DungeonsGuide.getDungeonsGuide().verbose) logger.info("Getting players from map");

        for (Map.Entry<String, Vec4b> stringVec4bEntry : mapdata.mapDecorations.entrySet()) {
            String mapDecString = stringVec4bEntry.getKey();
            Vec4b vec4 = stringVec4bEntry.getValue();

            if (!mapIconToPlayerMap.containsValue(mapDecString)) {
                if (DungeonsGuide.getDungeonsGuide().verbose) logger.info("mapIconToPlayerMap dosent have Player");

                int x = vec4.func_176112_b() / 2 + 64;
                int y = vec4.func_176113_c() / 2 + 64;
                BlockPos mapPos = mapPointToWorldPoint(new Point(x, y));
                String potentialPlayer = null;

                for (String player : context.getPlayers()) {
                    if (DungeonsGuide.getDungeonsGuide().verbose)
                        logger.info("Player: {} isNear: {} ", player, isPlayerNear(player, mapPos));
//                        if (!mapIconToPlayerMap.containsKey(player) && isPlayerNear(player, mapPos)) {
                    if (!mapIconToPlayerMap.containsKey(player)) {
                        if (DungeonsGuide.getDungeonsGuide().verbose) logger.info("Potential profile is: " + player);
                        potentialPlayer = player;
                        break;
                    }
                }


                if (potentialPlayer != null) {
                    if (DungeonsGuide.getDungeonsGuide().verbose) logger.info("potentialPlayer is not null");
                    boolean shouldSave = true;

                    for (Map.Entry<String, Vec4b> vec4bEntry : mapdata.mapDecorations.entrySet()) {
//                        String aaa = vec4bEntry.getKey();
                        Vec4b bbb = vec4bEntry.getValue();

//                            if (mapIconToPlayerMap.containsValue(aaa) || mapDecString.equals(aaa)) {
//                                shouldSave = false;
//                                break;
//                            }
//                            else {
                        int x2 = bbb.func_176112_b() / 2 + 64;
                        int y2 = bbb.func_176113_c() / 2 + 64;
                        int dx = x2 - x;
                        int dy = y2 - y;
                        if (dx * dx + dy * dy < clossnessDistance) {
                            shouldSave = false;
                            break;
                        }
//                            }
                    }

                    if (shouldSave) {
                        if (DungeonsGuide.getDungeonsGuide().verbose)
                            logger.info("added {} to mapIconPlayerMap with {}", potentialPlayer, stringVec4bEntry.getKey());
                        if (mapIconToPlayerMap.containsKey(potentialPlayer)) {
                            mapIconToPlayerMap.replace(potentialPlayer, stringVec4bEntry.getKey());
                        } else {
                            mapIconToPlayerMap.put(potentialPlayer, stringVec4bEntry.getKey());
                        }
                        if (DungeonsGuide.getDungeonsGuide().verbose) logger.info("mapIconToPlayerMap:");
                        if (DungeonsGuide.getDungeonsGuide().verbose)
                            mapIconToPlayerMap.forEach((key, value) -> logger.info("  {}: {}", key, value));
                    } else {
                        if (DungeonsGuide.getDungeonsGuide().verbose) logger.info("shouldSave is false");
                    }


                } else {
                    if (DungeonsGuide.getDungeonsGuide().verbose) logger.info("potentialPlayer is null");
                }

            } else {
                if (DungeonsGuide.getDungeonsGuide().verbose) logger.info("mapIconToPlayerMap has player ");
            }
        }


    }

    private boolean isPlayerNear(String player, BlockPos mapPos) {
        EntityPlayer entityPlayer = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(player);

        if (entityPlayer != null && !entityPlayer.isInvisible()) {
            BlockPos pos = entityPlayer.getPosition();
            int dx = mapPos.getX() - pos.getX();
            int dz = mapPos.getZ() - pos.getZ();
            return dx * dx + dz * dz < clossnessDistance;

        }

        return false;
    }
}
