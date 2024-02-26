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

import io.netty.buffer.ByteBuf;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.BoundingBox;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindResult;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.FineGridStonkingBFS;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.IPathfindWorld;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.IPathfinder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.Vec3;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

@AllArgsConstructor
public class CachedPathfinder implements IPathfinder {
    private PathfindCache cache;
    private int rotation;
    private ByteBuffer array;

    private int xStart, yStart, zStart;
    private int xLen, yLen, zLen;


    private int roomXMin, roomYMin, roomZMin;
    private int roomXLen, roomZLen;
    public CachedPathfinder(PathfindCache cache, int rotation, int xStart, int yStart, int zStart, int xLen, int yLen, int zLen, ByteBuffer data) {
        this.cache = cache;
        this.rotation = rotation;
        this.xStart = xStart;
        this.yStart = yStart;
        this.zStart = zStart;
        this.xLen = xLen;
        this.yLen = yLen;
        this.zLen = zLen;
        this.array = data;
    }


    @Override
    public void init(IPathfindWorld dungeonRoom, BoundingBox destination) {
        this.roomXMin = dungeonRoom.getMinX() - 2;
        this.roomYMin = dungeonRoom.getMinY();
        this.roomZMin = dungeonRoom.getMinZ() - 2;
        this.roomXLen = dungeonRoom.getXwidth() / 2;
        this.roomZLen = dungeonRoom.getZwidth() / 2;
    }

    @Override
    public boolean doOneStep() {
        return true;
    }

    private Vec3 target;
    @Override
    public void setTarget(Vec3 from) {
        this.target = from;
    }

    @Override
    public Vec3 getTarget() {
        return target;
    }

    @Override
    public PathfindResult getRoute(Vec3 from) {
        OffsetVec3 offsetVec3 = new OffsetVec3(0,0,0);
        offsetVec3.setPosInWorld(xLen/2, zLen/2,roomXMin/2, roomYMin/2, roomZMin/2, from.xCoord, from.yCoord, from.zCoord, rotation);
        int nodeX = (int) Math.round(offsetVec3.xCoord * 2);
        int nodeY = (int) Math.round(offsetVec3.yCoord * 2);
        int nodeZ = (int) Math.round(offsetVec3.zCoord * 2);

        LinkedList<PathfindResult.PathfindNode> route = new LinkedList<>();
        CachedPathfindNode curr = getNode(nodeX, nodeY, nodeZ);
        float gScore = curr.gScore;
        if (curr.nodeType == null) return null;
        Vec3 nextPos = new Vec3(((int)Math.round(from.xCoord * 2)) / 2.0,((int)Math.round(from.yCoord * 2)) / 2.0 + 0.05,((int)Math.round(from.zCoord * 2)) / 2.0);
        int cnt = 0;
        while(curr.nodeType != null && curr.nodeType != PathfindResult.PathfindNode.NodeType.DESTINATION && curr.gScore <= gScore) {
            route.addLast(new PathfindResult.PathfindNode(nextPos.xCoord, nextPos.yCoord, nextPos.zCoord, curr.nodeType));

            OffsetVec3 offsetVec31 = new OffsetVec3(curr.x / 2.0, curr.y / 2.0, curr.z / 2.0);
            nextPos = offsetVec31.toRotatedRelBlockPos(rotation, zLen, xLen).addVector(roomXMin / 2.0, roomYMin / 2.0 + 0.05, roomZMin / 2.0);

            curr = getNode(curr.x, curr.y, curr.z);
            cnt ++;
            if (cnt > 1000) break;
        }
        route.addLast(new PathfindResult.PathfindNode(nextPos.xCoord, nextPos.yCoord, nextPos.zCoord, curr.nodeType));
        return new PathfindResult(route, gScore);
    }

    @AllArgsConstructor @Getter
    public static class CachedPathfindNode {
        private int x, y, z;
        private float gScore;
        private PathfindResult.PathfindNode.NodeType nodeType;
    }

    private CachedPathfindNode getNode(int x, int y, int z) {
        if (x < xStart || y < yStart || z < zStart || x >= xStart + xLen || y >= yStart + yLen || z >= zStart + zLen) {
            return new CachedPathfindNode(0,0,0, Float.POSITIVE_INFINITY, null);
        }
        int relX = x - xStart;
        int relY = y - yStart;
        int relZ = z - zStart;
        int idx = (relY * xLen * zLen + relZ * xLen + relX) * 8;
        array.position(idx);
        byte[] data = new byte[8];
        array.get(data);
        if (data[3] == 12) {
            return new CachedPathfindNode(0,0,0, Float.POSITIVE_INFINITY,null);
        }

        int parentX = Byte.toUnsignedInt(data[0]);
        int parentY = Byte.toUnsignedInt(data[1]);
        int parentZ = Byte.toUnsignedInt(data[2]);
        PathfindResult.PathfindNode.NodeType type = PathfindResult.PathfindNode.NodeType.values()[data[3]];
        float gScore = Float.intBitsToFloat((data[7] << 24 & 0xFF000000) | (data[6] << 16 & 0xFF0000) | (data[ 5] << 8 & 0xFF00) | (data[4] & 0xFF));
        return new CachedPathfindNode(parentX, parentY, parentZ, gScore, type);
    }

    @Override
    public double getCost(Vec3 from) {
        OffsetVec3 offsetVec3 = new OffsetVec3(0,0,0);
        offsetVec3.setPosInWorld(xLen/2, zLen/2,roomXMin/2, roomYMin/2, roomZMin/2, from.xCoord, from.yCoord, from.zCoord, rotation);
        int nodeX = (int) Math.round(offsetVec3.xCoord * 2);
        int nodeY = (int) Math.round(offsetVec3.yCoord * 2);
        int nodeZ = (int) Math.round(offsetVec3.zCoord * 2);
        return getNode(nodeX, nodeY, nodeZ).gScore;
    }
}
