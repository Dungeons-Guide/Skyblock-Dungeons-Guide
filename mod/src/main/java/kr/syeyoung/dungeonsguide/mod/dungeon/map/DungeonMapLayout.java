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

import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.vecmath.Vector2d;
import java.awt.*;

@AllArgsConstructor @Getter
public class DungeonMapLayout {
    private final Dimension unitRoomSize;
    private final int mapRoomGap;
    // top left room pos
    private final Point originPoint;

    private final VectorI3D worldMin;


    public VectorI3D mapPointToWorldPoint(Point mapPoint) {
        int x = (int) ((mapPoint.x - originPoint.x + mapRoomGap / 2.0) / ((double) unitRoomSize.width + mapRoomGap) * 32 + worldMin.getX());
        int y = (int) ((mapPoint.y - originPoint.y + mapRoomGap / 2.0) / ((double) unitRoomSize.height + mapRoomGap) * 32 + worldMin.getZ());
        return new VectorI3D(x, 70, y);
    }

    public Point roomPointToMapPoint(Point roomPoint) {
        return new Point(roomPoint.x * (unitRoomSize.width + mapRoomGap) + originPoint.x, roomPoint.y * (unitRoomSize.height + mapRoomGap) + originPoint.y);
    }

    public VectorI3D roomPointToWorldPoint(Point roomPoint) {
        return new VectorI3D(worldMin.getX() + (roomPoint.x * 32), worldMin.getY(), worldMin.getZ() + (roomPoint.y * 32));
    }

    public Point worldPointToRoomPoint(VectorI3D worldPoint) {
        if (worldMin == null) return null;
        return new Point((int) Math.floor((worldPoint.getX() - worldMin.getX()) / 32.0), (int) Math.floor((worldPoint.getZ() - worldMin.getZ()) / 32.0));
    }
    public Point worldPointToRoomPoint(Vector3D worldPoint) {
        if (worldMin == null) return null;
        return new Point((int) Math.floor((worldPoint.x - worldMin.getX()) / 32.0), (int) Math.floor((worldPoint.z - worldMin.getZ()) / 32.0));
    }


    public Point worldPointToMapPoint(Vector3D worldPoint) {
        if (worldMin == null) return null;
        return new Point(originPoint.x + (int) ((worldPoint.x - worldMin.getX()) / 32.0f * (unitRoomSize.width + mapRoomGap)) - mapRoomGap / 2, originPoint.y + (int) ((worldPoint.z - worldMin.getZ()) / 32.0f * (unitRoomSize.height + mapRoomGap)) - mapRoomGap / 2);
    }

    public Vector2d worldPointToMapPointFLOAT(Vector3D worldPoint) {
        if (worldMin == null) return null;
        double x = originPoint.x + ((worldPoint.x - worldMin.getX()) / 32.0f * (unitRoomSize.width + mapRoomGap)) - mapRoomGap / 2.0;
        double y = originPoint.y + ((worldPoint.z - worldMin.getZ()) / 32.0f * (unitRoomSize.height + mapRoomGap)) - mapRoomGap / 2.0;
        return new Vector2d(x, y);
    }

}
