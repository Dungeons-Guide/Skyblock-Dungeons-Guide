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

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import javax.vecmath.Vector2d;
import java.awt.*;

@AllArgsConstructor @Getter
public class DungeonMapLayout {
    private final Dimension unitRoomSize;
    private final int mapRoomGap;
    // top left room pos
    private final Point originPoint;

    private final BlockPos worldMin;


    public BlockPos mapPointToWorldPoint(Point mapPoint) {
        int x = (int) ((mapPoint.x - originPoint.x) / ((double) unitRoomSize.width + mapRoomGap) * 32 + worldMin.getX());
        int y = (int) ((mapPoint.y - originPoint.y) / ((double) unitRoomSize.height + mapRoomGap) * 32 + worldMin.getZ());
        return new BlockPos(x, 70, y);
    }

    public Point roomPointToMapPoint(Point roomPoint) {
        return new Point(roomPoint.x * (unitRoomSize.width + mapRoomGap) + originPoint.x, roomPoint.y * (unitRoomSize.height + mapRoomGap) + originPoint.y);
    }

    public BlockPos roomPointToWorldPoint(Point roomPoint) {
        return new BlockPos(worldMin.getX() + (roomPoint.x * 32), worldMin.getY(), worldMin.getZ() + (roomPoint.y * 32));
    }

    public Point worldPointToRoomPoint(BlockPos worldPoint) {
        if (worldMin == null) return null;
        return new Point((worldPoint.getX() - worldMin.getX()) / 32, (worldPoint.getZ() - worldMin.getZ()) / 32);
    }

    public Point worldPointToMapPoint(Vec3 worldPoint) {
        if (worldMin == null) return null;
        return new Point(originPoint.x + (int) ((worldPoint.xCoord - worldMin.getX()) / 32.0f * (unitRoomSize.width + mapRoomGap)), originPoint.y + (int) ((worldPoint.zCoord - worldMin.getZ()) / 32.0f * (unitRoomSize.height + mapRoomGap)));
    }

    public Vector2d worldPointToMapPointFLOAT(Vec3 worldPoint) {
        if (worldMin == null) return null;
        double x = originPoint.x + ((worldPoint.xCoord - worldMin.getX()) / 32.0f * (unitRoomSize.width + mapRoomGap));
        double y = originPoint.y + ((worldPoint.zCoord - worldMin.getZ()) / 32.0f * (unitRoomSize.height + mapRoomGap));
        return new Vector2d(x, y);
    }

}
