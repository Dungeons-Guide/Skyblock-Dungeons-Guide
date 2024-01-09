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

import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindResult;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.FeaturePathfindSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.*;

public class AStarFineGridStonking implements IPathfinder {

    private int lastSx, lastSy, lastSz;
    private int dx, dy, dz;
    private DungeonRoom dungeonRoom;


    private Node startNode;
    private Node goalNode;

    @Getter
    private AxisAlignedBB destinationBB;
    private FeaturePathfindSettings.AlgorithmSettings algorithmSettings;

    public AStarFineGridStonking(FeaturePathfindSettings.AlgorithmSettings algorithmSettings) {
        this.algorithmSettings = algorithmSettings;
    }
    @Override
    public void init(DungeonRoom dungeonRoom, Vec3 destination) {
        this.dungeonRoom = dungeonRoom;

        this.dx = (int) (destination.xCoord * 2);
        this.dy = (int) (destination.yCoord * 2);
        this.dz = (int) (destination.zCoord * 2);
        destinationBB = AxisAlignedBB.fromBounds(dx-2, dy-2, dz-2, dx+2, dy+2, dz+2);
        startNode = openNode(dx, dy, dz, false);
    }
    private Map<Node.Coordinate, Node> nodeMap = new HashMap<>();
    @Getter
    private PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparing((Node a) -> a == null ? Float.MAX_VALUE : a.f)
            .thenComparing(a -> a == null ? Float.MAX_VALUE :  a.coordinate.x)
            .thenComparing(a -> a == null ? Float.MAX_VALUE :  a.coordinate.y)
            .thenComparing(a -> a == null ? Float.MAX_VALUE :  a.coordinate.z)
            .thenComparing(a -> a == null ? Float.MAX_VALUE : a.coordinate.stonk ? 1 : 0));

    private int pfindIdx = 0;
    private Node openNode(int x, int y, int z, boolean stonking)
    {
        Node.Coordinate coordinate = new Node.Coordinate(x,y,z, stonking);
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
        if (n == null) return true;
        if (n.lastVisited == pfindIdx) return false;
        n.lastVisited = pfindIdx;

        if (n == goalNode) {
            // route = reconstructPath(startNode)
            found = true;
            return true;
        }

        if (!n.coordinate.stonk || n.stonkLength <= algorithmSettings.getMaxStonk()) {
            for (EnumFacing value : EnumFacing.VALUES) {
                Node neighbor = openNode(n.coordinate.x + value.getFrontOffsetX(), n.coordinate.y + value.getFrontOffsetY(), n.coordinate.z + value.getFrontOffsetZ(), n.coordinate.stonk);

                DungeonRoom.NodeState nodeState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);

                // although this says up, it's actually going down for player. remember, we're pathfinding from the chest to the player.
                int up = 0;
                if (neighbor.coordinate.stonk) {
                    if (nodeState.isFall() && value.getFrontOffsetY() == 0) continue; // can not go into fall
                    boolean originalGrid = (n.coordinate.x % 2 == 0) == (n.coordinate.z % 2 == 0);
                    boolean newGrid = (neighbor.coordinate.x % 2 == 0) == (neighbor.coordinate.z % 2 == 0);
                    if (!originalGrid && !newGrid) {
                        continue; // push out to new position
                    }

                    while (nodeState.isFall()) {
                        up++;
                        neighbor = openNode(neighbor.coordinate.x, neighbor.coordinate.y + 1, neighbor.coordinate.z, neighbor.coordinate.stonk);
                        nodeState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);
                    }

                    if (!dungeonRoom.getLayer(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z).isInstabreak()) {
                        continue; // can't dig down non-instabreak
                    }
                }

                boolean isFlying = !dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y - 1, neighbor.coordinate.z).isBlockedNonStonk();
                boolean isFlying2 = !dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y - 2, neighbor.coordinate.z).isBlockedNonStonk();

                // check blocked.
                if (destinationBB.minX <= neighbor.coordinate.x && neighbor.coordinate.x <= destinationBB.maxX &&
                        destinationBB.minY <= neighbor.coordinate.y && neighbor.coordinate.y <= destinationBB.maxY &&
                        destinationBB.minZ <= neighbor.coordinate.z && neighbor.coordinate.z <= destinationBB.maxZ && !neighbor.coordinate.stonk) { // not blocked
                } else {
                    if (!n.coordinate.stonk && nodeState.isBlockedNonStonk()) {
                        continue;
                    }

                    if (n.coordinate.stonk && nodeState.isBlockedStonk()) {
                        continue;
                    } else if (n.coordinate.stonk && !nodeState.isBlockedNonStonk()) {
                        continue;
                    }
                }

                if (n.coordinate.stonk && neighbor.coordinate.x % 2 == 0 && neighbor.coordinate.z % 2 == 0) {
                    continue; // we do not visit corners.
                }

                // going up with stonk costs you 50 blocks.
                float gScore = n.g + (n.coordinate.stonk ? 7 : isFlying && isFlying2 ? 4 : isFlying ? 3 : 1) * (up + 1); // altho it's sq, it should be fine
                if (gScore < neighbor.g) {
                    neighbor.parent = n;
                    if (n.coordinate.stonk)
                        neighbor.stonkLength = (byte) (n.stonkLength + 1);
                    else
                        neighbor.stonkLength = 0;
                    neighbor.connectionType = n.coordinate.stonk ? PathfindResult.PathfindNode.NodeType.STONK_WALK : PathfindResult.PathfindNode.NodeType.WALK;
                    neighbor.g = gScore;
                    neighbor.f = gScore + manhatten(goalNode.coordinate.x - neighbor.coordinate.x, goalNode.coordinate.y - neighbor.coordinate.y, goalNode.coordinate.z - neighbor.coordinate.z);
                    open.add(neighbor);
                } else if (neighbor.lastVisited != pfindIdx) {
                    neighbor.f = gScore + manhatten(goalNode.coordinate.x - neighbor.coordinate.x, goalNode.coordinate.y - neighbor.coordinate.y, goalNode.coordinate.z - neighbor.coordinate.z);
                    open.add(neighbor);
                }
            }
        }

        DungeonRoom.NodeState originNodeState = dungeonRoom.getBlock(n.coordinate.x, n.coordinate.y, n.coordinate.z);

        // etherwarps.
        if (!n.coordinate.stonk && algorithmSettings.isRouteEtherwarp()) {
            if (n.coordinate.y % 2 == 0 && n.coordinate.x % 2 != 0 && n.coordinate.z % 2 != 0 && !originNodeState.isBlockedNonStonk()) {
                if (dungeonRoom.getBlock(n.coordinate.x, n.coordinate.y-2, n.coordinate.z).isBlockedNonStonk()
                || dungeonRoom.getBlock(n.coordinate.x, n.coordinate.y-1, n.coordinate.z).isBlockedNonStonk()) {
                    Node neighbor = openNode(n.coordinate.x, n.coordinate.y - 6, n.coordinate.z, false);
                    DungeonRoom.NodeState nodeState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);
                    while (!nodeState.isBlockedNonStonk()) {
                        neighbor = openNode(neighbor.coordinate.x, neighbor.coordinate.y - 1, neighbor.coordinate.z, false);
                        nodeState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);
                    }
                    neighbor = openNode(neighbor.coordinate.x, neighbor.coordinate.y + 1, neighbor.coordinate.z, false);
                    nodeState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);

                    if (!nodeState.isBlockedNonStonk()) {
                        float gScore = n.g + 100; // don't use etherwarp unelss it saves like 50 blocks

                        if (gScore < neighbor.g) {
                            neighbor.parent = n;
                            neighbor.stonkLength = 0;
                            neighbor.g = gScore;
                            neighbor.connectionType = PathfindResult.PathfindNode.NodeType.ETHERWARP;
                            neighbor.f = gScore + manhatten(goalNode.coordinate.x - neighbor.coordinate.x, goalNode.coordinate.y - neighbor.coordinate.y, goalNode.coordinate.z - neighbor.coordinate.z);
                            open.add(neighbor);
                        } else if (neighbor.lastVisited != pfindIdx) {
                            neighbor.f = gScore + manhatten(goalNode.coordinate.x - neighbor.coordinate.x, goalNode.coordinate.y - neighbor.coordinate.y, goalNode.coordinate.z - neighbor.coordinate.z);
                            open.add(neighbor);
                        }
                    }
                }
            }
        }


        if (originNodeState.isStonkEntrance() || originNodeState.isStonkExit()) {
           processStonks(n, originNodeState);
        }
        return false;
    }

    private void processStonks(Node n, DungeonRoom.NodeState originNodeState) {
        if (originNodeState == DungeonRoom.NodeState.ENTRANCE_STONK_UP && !algorithmSettings.isStonkUp()) return;
        if (originNodeState == DungeonRoom.NodeState.ENTRANCE_STONK_DOWN && !algorithmSettings.isStonkDown()) return;
        if (originNodeState == DungeonRoom.NodeState.ENTRANCE_STONK_DOWN_FALLING && !algorithmSettings.isStonkDown()) return;
        if (originNodeState == DungeonRoom.NodeState.ENTRANCE_TELEPORT_DOWN && !algorithmSettings.isStonkTeleport()) return;
        if (originNodeState == DungeonRoom.NodeState.ENTRANCE_ETHERWARP && !algorithmSettings.isStonkEtherwarp()) return;
        if (originNodeState == DungeonRoom.NodeState.ENTRANCE_ETHERWARP_FALL && !algorithmSettings.isStonkEtherwarp()) return;
        if (originNodeState == DungeonRoom.NodeState.ENTRANCE_STONK_DOWN_ECHEST && !algorithmSettings.isStonkEChest()) return;

            for (EnumFacing value : EnumFacing.VALUES) {
                int factor = originNodeState == DungeonRoom.NodeState.ENTRANCE_ETHERWARP ? 2 : 1;
                if (value.getFrontOffsetY() == -1 && !n.coordinate.stonk) factor = 2;

                Node neighbor = openNode(n.coordinate.x + value.getFrontOffsetX() * factor, n.coordinate.y + value.getFrontOffsetY() * factor, n.coordinate.z + value.getFrontOffsetZ() * factor, !n.coordinate.stonk);
                DungeonRoom.NodeState nodeState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);

                int up = 0;
                if (neighbor.coordinate.stonk) {
                    if (nodeState.isFall() && value.getFrontOffsetY() == 0) continue; // can not go into fall

                    while (nodeState.isFall()) {
                        up++;
                        // remember, we're going target to source.
                        neighbor = openNode(neighbor.coordinate.x, neighbor.coordinate.y + 1, neighbor.coordinate.z, neighbor.coordinate.stonk);
                        nodeState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);
                    }
                }

                if (!n.coordinate.stonk && (!nodeState.isBlockedNonStonk() || nodeState.isBlockedStonk())) {
                    continue;
                }
                if (n.coordinate.stonk && nodeState.isBlockedNonStonk()) {
                    continue;
                }
                if (originNodeState == DungeonRoom.NodeState.ENTRANCE_STONK_UP && value != EnumFacing.DOWN) { // stonk up is still considered stonking.
                    continue;
                }
                if ((originNodeState == DungeonRoom.NodeState.ENTRANCE_STONK_DOWN || originNodeState == DungeonRoom.NodeState.ENTRANCE_STONK_DOWN_FALLING) && value != EnumFacing.UP && up == 0) {
                    continue;
                }
                if ((originNodeState == DungeonRoom.NodeState.ENTRANCE_STONK_DOWN_ECHEST) && value != EnumFacing.UP && up == 0) {
                    continue;
                }
                if (originNodeState == DungeonRoom.NodeState.ENTRANCE_TELEPORT_DOWN && value != EnumFacing.UP && up == 0) {
                    continue;
                }


                float gScore = n.g + 15 + 4 * up; // altho it's sq, it should be fine
                if (gScore < neighbor.g) {
                    neighbor.parent = n;

                    neighbor.connectionType = !n.coordinate.stonk ? PathfindResult.PathfindNode.NodeType.STONK_EXIT :
                            originNodeState == DungeonRoom.NodeState.ENTRANCE_STONK_UP ? PathfindResult.PathfindNode.NodeType.DIG_UP :
                                    originNodeState == DungeonRoom.NodeState.ENTRANCE_STONK_DOWN || originNodeState == DungeonRoom.NodeState.ENTRANCE_STONK_DOWN_FALLING ? PathfindResult.PathfindNode.NodeType.DIG_DOWN :
                                            originNodeState == DungeonRoom.NodeState.ENTRANCE_STONK_DOWN_ECHEST ? PathfindResult.PathfindNode.NodeType.ECHEST :
                                                    originNodeState == DungeonRoom.NodeState.ENTRANCE_TELEPORT_DOWN ? PathfindResult.PathfindNode.NodeType.TELEPORT_INTO :
                                                            PathfindResult.PathfindNode.NodeType.ETHERWARP;
                    neighbor.g = gScore;
                    neighbor.f = gScore + manhatten(goalNode.coordinate.x - neighbor.coordinate.x, goalNode.coordinate.y - neighbor.coordinate.y, goalNode.coordinate.z - neighbor.coordinate.z);
                    open.add(neighbor);
                } else if (neighbor.lastVisited != pfindIdx) {
                    neighbor.f = gScore + manhatten(goalNode.coordinate.x - neighbor.coordinate.x, goalNode.coordinate.y - neighbor.coordinate.y, goalNode.coordinate.z - neighbor.coordinate.z);
                    open.add(neighbor);
                }
            }
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
        boolean blocked = dungeonRoom.getBlock(tobeX, tobeY, tobeZ).isBlockedNonStonk();

        this.lastSx = tobeX;
        this.lastSy = tobeY;
        this.lastSz = tobeZ;
        open.clear();
        pfindIdx ++;
        found = false;

        goalNode = openNode(lastSx, lastSy, lastSz, blocked);
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
    public PathfindResult getRoute(Vec3 from) {
        int lastSx = (int) Math.round(from.xCoord * 2);
        int lastSy = (int) Math.round(from.yCoord * 2);
        int lastSz = (int) Math.round(from.zCoord * 2);


        Node goalNode = openNode(lastSx, lastSy, lastSz, dungeonRoom.getBlock(lastSx, lastSy, lastSz).isBlockedNonStonk());
        LinkedList<PathfindResult.PathfindNode> route = new LinkedList<>();
        Node curr =goalNode;
        if (curr.parent == null) return null;
        Set<Node> visited = new HashSet<>();
        while(curr.parent != null && !visited.contains(curr)) {
            route.addLast(new PathfindResult.PathfindNode(curr.coordinate.x / 2.0f, curr.coordinate.y / 2.0f + 0.1f, curr.coordinate.z/ 2.0f, curr.connectionType));
            visited.add(curr);
            curr = curr.parent;
        }
        route.addLast(new PathfindResult.PathfindNode(curr.coordinate.x / 2.0f, curr.coordinate.y / 2.0f + 0.1f, curr.coordinate.z/ 2.0f, curr.connectionType));
        return new PathfindResult(route, goalNode.g);
    }


    private int manhatten(int x, int y, int z) {return Math.abs(x)+ Math.abs(y)+ Math.abs(z);}
    private float distSq(float x, float y, float z) {
        return MathHelper.sqrt_float(x * x + y * y + z * z);
    }


    public enum ConnectionType {
        ETHERWARP, DIG_DOWN, WARP_DOWN, DIG_UP, WALK
    }

    @RequiredArgsConstructor
    @Data
    public static final class Node {
        @Data
        @RequiredArgsConstructor
        public static final class Coordinate {
            private final int x, y, z;
            private final boolean stonk;
        }
        private final Coordinate coordinate;

        private float f = Float.MAX_VALUE, g = Float.MAX_VALUE;
        private byte stonkLength = 0;
        private int lastVisited;

        @EqualsAndHashCode.Exclude
        private Node parent;
        private PathfindResult.PathfindNode.NodeType connectionType = PathfindResult.PathfindNode.NodeType.WALK;

    }
}