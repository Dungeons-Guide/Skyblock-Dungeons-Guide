/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
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

package kr.syeyoung.dungeonsguide.dungeon.pathfinding;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.*;

public class AStarCornerCut {
    private final BlockPos min, max;
    private final World world;

    int lastSx, lastSy, lastSz;
    final int dx, dy, dz;
    private DungeonRoom dungeonRoom;

    @Getter
    private AxisAlignedBB destinationBB;

    public AStarCornerCut(DungeonRoom dungeonRoom, Vec3 destination) {
        this.min = new BlockPos(dungeonRoom.getMinx(), 0, dungeonRoom.getMinz());
        this.max = new BlockPos(dungeonRoom.getMaxx(), 255, dungeonRoom.getMaxz());

        this.world = dungeonRoom.getCachedWorld();
        this.dungeonRoom = dungeonRoom;

        this.dx = (int) (destination.xCoord * 2);
        this.dy = (int) (destination.yCoord * 2);
        this.dz = (int) (destination.zCoord * 2);
        destinationBB = AxisAlignedBB.fromBounds(dx-2, dy-2, dz-2, dx+2, dy+2, dz+2);
    }

    private Map<Node.Coordinate, Node> nodeMap = new HashMap<>();

    private Node openNode(int x, int y, int z)
    {
        Node.Coordinate coordinate = new Node.Coordinate(x,y,z);
        Node node = this.nodeMap.get(coordinate);

        if (node == null)
        {
            node = new Node(coordinate);
            this.nodeMap.put(coordinate, node);
        }

        return node;
    }

    @Getter
    private LinkedList<Vec3> route = new LinkedList<>();

    @Getter
    private PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparing((Node a) -> a == null ? Float.MAX_VALUE : a.f).thenComparing(a -> a == null ? Float.MAX_VALUE :  a.coordinate.x).thenComparing(a -> a == null ? Float.MAX_VALUE :  a.coordinate.y).thenComparing(a -> a == null ? Float.MAX_VALUE :  a.coordinate.z));

    private int pfindIdx = 0;

    public boolean pathfind(Vec3 from, long timeout) {
        pfindIdx ++;
        if (lastSx != (int)Math.round(from.xCoord * 2) || lastSy != (int)Math.round(from.yCoord*2) || lastSz != (int)Math.round(from.zCoord * 2))
            open.clear();

        this.lastSx = (int) Math.round(from.xCoord * 2);
        this.lastSy = (int) Math.round(from.yCoord * 2);
        this.lastSz = (int) Math.round(from.zCoord * 2);

        Node startNode = openNode(dx, dy, dz);
        Node goalNode = openNode(lastSx, lastSy, lastSz);

        if (goalNode.parent != null) {
            LinkedList<Vec3> route = new LinkedList<>();
            Node curr =goalNode;
            while(curr.parent != null) {
                route.addLast(new Vec3(curr.coordinate.x / 2.0, curr.coordinate.y / 2.0 + 0.1, curr.coordinate.z/ 2.0));
                curr = curr.parent;
            }
            route.addLast(new Vec3(curr.coordinate.x / 2.0, curr.coordinate.y / 2.0 + 0.1, curr.coordinate.z/ 2.0));
            this.route = route;
            return true;
        }

        startNode.g = 0;
        startNode.f = 0;
        goalNode.g = Integer.MAX_VALUE; goalNode.f = Integer.MAX_VALUE;


        open.add(startNode);

        long end = System.currentTimeMillis() + timeout;

        while (!open.isEmpty()) {
            if (System.currentTimeMillis() > end) {
                return false;
            }

            Node n = open.poll();
            if (n.lastVisited == pfindIdx) continue;
            n.lastVisited = pfindIdx;

            if (n == goalNode) {
                // route = reconstructPath(startNode)
                LinkedList<Vec3> route = new LinkedList<>();
                Node curr =goalNode;
                while(curr.parent != null) {
                    route.addLast(new Vec3(curr.coordinate.x / 2.0, curr.coordinate.y / 2.0 + 0.1, curr.coordinate.z/ 2.0));
                    curr = curr.parent;
                }
                route.addLast(new Vec3(curr.coordinate.x / 2.0, curr.coordinate.y / 2.0 + 0.1, curr.coordinate.z/ 2.0));
                this.route = route;
                return true;
            }

            for (int z = -1; z <= 1; z++) {for (int y = -1; y <= 1; y ++) { for(int x = -1; x <= 1; x++) {
                if (x == 0 && y == 0 && z == 0) continue;
                Node neighbor = openNode(n.coordinate.x +x, n.coordinate.y +y, n.coordinate.z + z);

                // check blocked.
                if (!((destinationBB.minX <= neighbor.coordinate.x && neighbor.coordinate.x <= destinationBB.maxX &&
                        destinationBB.minY <= neighbor.coordinate.y && neighbor.coordinate.y <= destinationBB.maxY &&
                        destinationBB.minZ <= neighbor.coordinate.z && neighbor.coordinate.z <= destinationBB.maxZ) // near destination
                 || !dungeonRoom.isBlocked(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z))) { // not blocked
                    continue;
                }
                if (neighbor.lastVisited == pfindIdx) continue;


                float gScore = n.g + MathHelper.sqrt_float(x*x + y*y + z*z); // altho it's sq, it should be fine
                if (gScore < neighbor.g ) {
                    neighbor.parent = n;
                    neighbor.g = gScore;
                    neighbor.f = gScore + distSq(goalNode.coordinate.x - neighbor.coordinate.x, goalNode.coordinate.y - neighbor.coordinate.y, goalNode.coordinate.z - neighbor.coordinate.z);
                    open.add(neighbor);
                } else if (neighbor.lastVisited != pfindIdx) {
                    neighbor.f = gScore + distSq(goalNode.coordinate.x - neighbor.coordinate.x, goalNode.coordinate.y - neighbor.coordinate.y, goalNode.coordinate.z - neighbor.coordinate.z);
                    open.add(neighbor);
                }
            }}}
        }
        return true;
    }

    private int manhatten(int x, int y, int z) {return Math.abs(x)+ Math.abs(y)+ Math.abs(z);}
    private float distSq(float x, float y, float z) {
        return MathHelper.sqrt_float(x * x + y * y + z * z);
    }

    @RequiredArgsConstructor
    @Data
    public static final class Node {
        @Data
        @RequiredArgsConstructor
        public static final class Coordinate {
            private final int x, y, z;
        }
        private final Coordinate coordinate;

        private float f = Float.MAX_VALUE, g = Float.MAX_VALUE;
        private int lastVisited;

        @EqualsAndHashCode.Exclude
        private Node parent;

        public static long makeHash(int x, int y, int z)
        {
            return y & 32767L | ((short)x & 32767L) << 16 | ((short)z & 32767L) << 32;
        }
    }
}
