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

package kr.syeyoung.dungeonsguide.pathfinding;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import lombok.*;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.*;

public class ThetaStar {
    private final BlockPos min, max;
    private final World world;

    int lastSx, lastSy, lastSz;
    final int dx, dy, dz;
    private DungeonRoom dungeonRoom;

    @Getter
    private AxisAlignedBB destinationBB;

    public ThetaStar(DungeonRoom dungeonRoom, Vec3 destination) {
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
        if (dungeonRoom.isBlocked(lastSx, lastSy, lastSz)) return false;

        Node startNode = openNode(dx, dy, dz);
        Node goalNode = openNode(lastSx, lastSy, lastSz);
        startNode.g = 0;
        startNode.f = 0;
        goalNode.g = Integer.MAX_VALUE; goalNode.f = Integer.MAX_VALUE;
        if (goalNode.parent != null) {
            LinkedList<Vec3> route = new LinkedList<>();
            Node curr =goalNode;
            while(curr.parent != null) {
                route.addLast(new Vec3(curr.coordinate.x / 2.0, curr.coordinate.y / 2.0 + 0.1, curr.coordinate.z/ 2.0));
                curr = curr.parent;
            }
            route.addLast(new Vec3(curr.coordinate.x / 2.0, curr.coordinate.y / 2.0 + 0.1, curr.coordinate.z/ 2.0));
            this.route = route;
            System.out.println("Route len: "+route.size());
            return true;
        }
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

            for (EnumFacing value : EnumFacing.VALUES) {
                Node neighbor = openNode(n.coordinate.x + value.getFrontOffsetX(), n.coordinate.y + value.getFrontOffsetY(), n.coordinate.z + value.getFrontOffsetZ());

                // check blocked.
                if (!((destinationBB.minX <= neighbor.coordinate.x && neighbor.coordinate.x <= destinationBB.maxX &&
                        destinationBB.minY <= neighbor.coordinate.y && neighbor.coordinate.y <= destinationBB.maxY &&
                        destinationBB.minZ <= neighbor.coordinate.z && neighbor.coordinate.z <= destinationBB.maxZ) // near destination
                 || !dungeonRoom.isBlocked(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z))) { // not blocked
                    continue;
                }
                if (neighbor.lastVisited == pfindIdx) continue;

                boolean flag = false;
                if (n. parent != null) {
                    float tempGScore = n.parent.g + distSq(n.parent.coordinate.x - neighbor.coordinate.x, n.parent.coordinate.y - neighbor.coordinate.y, n.parent.coordinate.z - neighbor.coordinate.z);
                    if (tempGScore < neighbor.g && lineofsight(n.parent, neighbor)) {
                        neighbor.parent = n.parent;
                        neighbor.g = tempGScore;
                        neighbor.f = tempGScore + distSq(goalNode.coordinate.x - neighbor.coordinate.x, goalNode.coordinate.y - neighbor.coordinate.y, goalNode.coordinate.z - neighbor.coordinate.z);
                        open.add(neighbor);
                        flag = true;
                    }
                }
                if (!flag) {
                    float gScore = n.g + 1; // altho it's sq, it should be fine
                    if (gScore < neighbor.g) {
                        neighbor.parent = n;
                        neighbor.g = gScore;
                        neighbor.f = gScore +distSq(goalNode.coordinate.x - neighbor.coordinate.x, goalNode.coordinate.y - neighbor.coordinate.y, goalNode.coordinate.z - neighbor.coordinate.z);
                        open.add(neighbor);
                    } else if (neighbor.lastVisited != pfindIdx) {
                        neighbor.f = neighbor.g + distSq(goalNode.coordinate.x - neighbor.coordinate.x, goalNode.coordinate.y - neighbor.coordinate.y, goalNode.coordinate.z - neighbor.coordinate.z);
                        open.add(neighbor);
                    }
                }
            }
        }
        return true;
    }

    private boolean lineofsight(Node a, Node b) {
        if (a == null || b == null) return false;
        float sx = a.coordinate.x, sy = a.coordinate.y, sz = a.coordinate.z;
        int ex = b.coordinate.x, ey = b.coordinate.y, ez = b.coordinate.z;

        float dx = ex - sx, dy = ey - sy, dz = ez - sz;
        float len = distSq(dx, dy, dz);
        dx /= len; dy /= len; dz /= len;

        for (int d = 0; d <= len; d += 1) {
            if (dungeonRoom.isBlocked(Math.round(sx), (int) Math.ceil(sy), Math.round(sz))) return false;
            if (dungeonRoom.isBlocked(Math.round(sx)+1, (int) Math.ceil(sy), Math.round(sz)+1)) return false;
            if (dungeonRoom.isBlocked(Math.round(sx)-1, (int) Math.ceil(sy), Math.round(sz)-1)) return false;
            if (dungeonRoom.isBlocked(Math.round(sx)+1, (int) Math.ceil(sy), Math.round(sz)-1)) return false;
            if (dungeonRoom.isBlocked(Math.round(sx)-1, (int) Math.ceil(sy), Math.round(sz)+1)) return false;
            sx += dx; sy += dy; sz += dz;
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
        @ToString.Exclude
        private Node parent;
    }
}
