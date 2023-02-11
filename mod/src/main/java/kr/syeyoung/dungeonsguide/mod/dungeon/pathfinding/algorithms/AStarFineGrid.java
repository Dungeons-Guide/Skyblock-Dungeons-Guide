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

package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.*;

import java.util.*;

public class AStarFineGrid implements IPathfinder {

    private int lastSx, lastSy, lastSz;
    private int dx, dy, dz;
    private DungeonRoom dungeonRoom;


    private Node startNode;
    private Node goalNode;

    @Getter
    private AxisAlignedBB destinationBB;
    @Override
    public void init(DungeonRoom dungeonRoom, Vec3 destination) {
        this.dungeonRoom = dungeonRoom;

        this.dx = (int) (destination.xCoord * 2);
        this.dy = (int) (destination.yCoord * 2);
        this.dz = (int) (destination.zCoord * 2);
        destinationBB = AxisAlignedBB.fromBounds(dx-2, dy-2, dz-2, dx+2, dy+2, dz+2);
        startNode = openNode(dx, dy, dz);
    }
    private Map<Node.Coordinate, Node> nodeMap = new HashMap<>();
    @Getter
    private PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparing((Node a) -> a == null ? Float.MAX_VALUE : a.f).thenComparing(a -> a == null ? Float.MAX_VALUE :  a.coordinate.x).thenComparing(a -> a == null ? Float.MAX_VALUE :  a.coordinate.y).thenComparing(a -> a == null ? Float.MAX_VALUE :  a.coordinate.z));

    private int pfindIdx = 0;
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
    private boolean found = false;

    @Override
    public boolean doOneStep() {
        if (found) return true;
        Node n = open.poll();
        if (n == null) return false;
        if (n.lastVisited == pfindIdx) return false;
        n.lastVisited = pfindIdx;

        if (n == goalNode) {
            // route = reconstructPath(startNode)
            found = true;
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

            float gScore = n.g + 1; // altho it's sq, it should be fine
            if (gScore < neighbor.g) {
                neighbor.parent = n;
                neighbor.g = gScore;
                neighbor.f = gScore + distSq(goalNode.coordinate.x - neighbor.coordinate.x, goalNode.coordinate.y - neighbor.coordinate.y, goalNode.coordinate.z - neighbor.coordinate.z);
                open.add(neighbor);
            } else if (neighbor.lastVisited != pfindIdx) {
                neighbor.f = gScore + distSq(goalNode.coordinate.x - neighbor.coordinate.x, goalNode.coordinate.y - neighbor.coordinate.y, goalNode.coordinate.z - neighbor.coordinate.z);
                open.add(neighbor);
            }
        }
        return false;
    }

    @Override
    public void setTarget(Vec3 from) {
        int tobeX = (int) Math.round(from.xCoord * 2);
        int tobeY = (int) Math.round(from.yCoord * 2);
        int tobeZ = (int) Math.round(from.zCoord * 2);
        if (lastSx != tobeX || lastSy != tobeY || lastSz != tobeZ) {
        } else {
            return;
        }
        if (dungeonRoom.isBlocked(tobeX, tobeY, tobeZ)) return;

        this.lastSx = tobeX;
        this.lastSy = tobeY;
        this.lastSz = tobeZ;
        open.clear();
        pfindIdx++;
        found = false;

        goalNode = openNode(lastSx, lastSy, lastSz);

        startNode.g = 0;
        startNode.f = 0;
        goalNode.g = Integer.MAX_VALUE; goalNode.f = Integer.MAX_VALUE;

        open.add(startNode);


        if (goalNode.parent != null) {
            found = true;
        }
    }

    @Override
    public Vec3 getTarget() {
        return new Vec3(lastSx / 2.0, lastSy / 2.0, lastSz / 2.0);
    }

    @Override
    public List<Vec3> getRoute(Vec3 from) {
        int lastSx = (int) Math.round(from.xCoord * 2);
        int lastSy = (int) Math.round(from.yCoord * 2);
        int lastSz = (int) Math.round(from.zCoord * 2);

        Node goalNode = openNode(lastSx, lastSy, lastSz);

        LinkedList<Vec3> route = new LinkedList<>();
        Node curr =goalNode;
        if (curr.parent == null) return null;
        while(curr.parent != null) {
            route.addLast(new Vec3(curr.coordinate.x / 2.0, curr.coordinate.y / 2.0 + 0.1, curr.coordinate.z/ 2.0));
            curr = curr.parent;
        }
        route.addLast(new Vec3(curr.coordinate.x / 2.0, curr.coordinate.y / 2.0 + 0.1, curr.coordinate.z/ 2.0));
        return route;
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

    }
}
