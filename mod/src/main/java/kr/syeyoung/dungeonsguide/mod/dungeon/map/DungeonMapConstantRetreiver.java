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
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.impl.DungeonNodataEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonContextInitializationEvent;
import kr.syeyoung.dungeonsguide.mod.utils.MapUtils;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.Set;


// This class is responsible for matching the world to hand held map.
public class DungeonMapConstantRetreiver {
    public static DungeonMapLayout beginParsingMap(byte[] mapData, BlockPos worldDoorLocation, Vector2d worldDoorDirection) {
        if (worldDoorLocation == null || worldDoorDirection == null) return null;

//        context.createEvent(new DungeonNodataEvent("MAP_PROCESSOR_INIT"));
//        MinecraftForge.EVENT_BUS.post(new DungeonContextInitializationEvent());

        Rectangle firstRoom = obtainStartingRoom(mapData);
        if (firstRoom == null) return null;
        Dimension unitRoomSize = firstRoom.getSize();
        Vector2d mapDoorDirection = obtainStartingRoomToFirstRoomDoorDirection(mapData, firstRoom);
        if (mapDoorDirection == null) return null;
        Dimension mapDoorDimension = obtainMapDoorDimensions(mapData, firstRoom, mapDoorDirection);
        if (mapDoorDimension == null) return null;
        int mapDoorWidth = mapDoorDimension.width;
        int mapRoomGap = mapDoorDimension.height;

        Point mapOriginPoint = obtainTopLeft(mapData, firstRoom, mapDoorDimension);
        BlockPos worldMin = obtainWorldMin(mapData, firstRoom, mapOriginPoint, mapDoorDimension,
                mapDoorDirection, worldDoorDirection, worldDoorLocation);

        ChatTransmitter.sendDebugChat(new ChatComponentText("door Pos:" + worldDoorDirection));


        ChatTransmitter.sendDebugChat(new ChatComponentText("Found Green room:" + firstRoom));
        ChatTransmitter.sendDebugChat(new ChatComponentText("World Min:" + worldMin));
        ChatTransmitter.sendDebugChat(new ChatComponentText("Dimension:" + unitRoomSize));
        ChatTransmitter.sendDebugChat(new ChatComponentText("top Left:" + mapOriginPoint));
        ChatTransmitter.sendDebugChat(new ChatComponentText("door dimension:" + mapDoorDimension));
        return new DungeonMapLayout(unitRoomSize, mapRoomGap, mapOriginPoint, worldMin);
    }

    private static Point mapPointToRoomPoint(Point mapPoint, Point topLeftMapPoint, Dimension unitRoomDimension, Dimension doorDimensions) {
        int x = (int) ((mapPoint.x - topLeftMapPoint.x) / ((double) unitRoomDimension.width + doorDimensions.height));
        int y = (int) ((mapPoint.y - topLeftMapPoint.y) / ((double) unitRoomDimension.height + doorDimensions.height));
        return new Point(x, y);
    }
    private static BlockPos obtainWorldMin(byte[] mapData, Rectangle firstRoom, Point topLeftMapPoint, Dimension doorDimension,
                                    Vector2d mapDoorOffset,
                                    Vector2d worldDoorOffset, BlockPos worldDoor) {
        Point unitPoint = mapPointToRoomPoint(firstRoom.getLocation(), topLeftMapPoint, firstRoom.getSize(), doorDimension);
        unitPoint.translate(unitPoint.x + 1, unitPoint.y + 1); // basically we make each room 2x2 large in this coordinate, and get the center coord
        unitPoint.translate((int) mapDoorOffset.x, (int) mapDoorOffset.y);

        if (!worldDoorOffset.equals(mapDoorOffset))
            throw new IllegalStateException("Map door offset does not match world door offset!! DG is not prepared for this kind of situation");

        int worldX = unitPoint.x * 16;
        int worldY = unitPoint.y * 16;
        BlockPos worldMin = worldDoor.add(-worldX, 0, -worldY);
        return worldMin;
    }
    private static  Point obtainTopLeft(byte[] mapData, Rectangle firstRoom, Dimension doorDimension) {
        int topLeftX = firstRoom.x;
        int topLeftY = firstRoom.y;
        while (topLeftX >= firstRoom.width + doorDimension.height)
            topLeftX -= firstRoom.width + doorDimension.height;
        while (topLeftY >= firstRoom.height + doorDimension.height)
            topLeftY -= firstRoom.height + doorDimension.height;
        return new Point(topLeftX, topLeftY);
    }
    private static  Rectangle obtainStartingRoom(byte[] mapData) {
        final Point firstRoom = MapUtils.findFirstColorWithIn(mapData, (byte) 30, new Rectangle(0, 0, 128, 128));
        if (firstRoom == null) return null;
        // Determine room dimension
        int width = MapUtils.getWidthOfColorAt(mapData, (byte) 30, firstRoom);
        int height = MapUtils.getHeightOfColorAt(mapData, (byte) 30, firstRoom);

        return new Rectangle(firstRoom, new Dimension(width, height));
    }

    private static final Set<Vector2d> directions = Sets.newHashSet(new Vector2d(0, 1), new Vector2d(0, -1), new Vector2d(1, 0), new Vector2d(-1, 0));
    private static  Dimension obtainMapDoorDimensions(byte[] mapData, Rectangle firstRoom, Vector2d doorDirection) {
        Point basePoint = new Point(firstRoom.x, firstRoom.y);
        if (doorDirection.x > 0) basePoint.x += firstRoom.width;
        if (doorDirection.x < 0) basePoint.x -= 1;
        if (doorDirection.y > 0) basePoint.y += firstRoom.height;
        if (doorDirection.y < 0) basePoint.y -= 1;
        // Base Point is the point I would want to start searching next room from.

        int gap = MapUtils.getLengthOfColorExtending(mapData, (byte) 0, basePoint, doorDirection);
        Point pt = MapUtils.findFirstColorWithInNegate(mapData, (byte) 0, new Rectangle(basePoint.x, basePoint.y, (int) Math.abs(doorDirection.y) * firstRoom.width + 1, (int) Math.abs(doorDirection.x) * firstRoom.height + 1));
        if (pt == null) {
            return null;
        }
        int doorWidth = MapUtils.getLengthOfColorExtending(mapData, MapUtils.getMapColorAt(mapData, pt.x, pt.y), pt, new Vector2d((int) Math.abs(doorDirection.y), (int) Math.abs(doorDirection.x)));
        return new Dimension(doorWidth, gap);
    }

    private static  Vector2d obtainStartingRoomToFirstRoomDoorDirection(byte[] mapData, Rectangle firstRoom) {
        Vector2d doorDir = null;
        Point midfirstRoom = new Point(firstRoom.x + firstRoom.width / 2, firstRoom.y + firstRoom.height / 2);
        final int halfWidth = firstRoom.width / 2 + 2;
        for (Vector2d v : directions) {
            byte color = MapUtils.getMapColorAt(mapData, (int) (v.x * halfWidth + midfirstRoom.x), (int) (v.y * halfWidth + midfirstRoom.y));
            if (color != 0) {
                doorDir = v;
                break;
            }
        }
        return doorDir;
    }

}
