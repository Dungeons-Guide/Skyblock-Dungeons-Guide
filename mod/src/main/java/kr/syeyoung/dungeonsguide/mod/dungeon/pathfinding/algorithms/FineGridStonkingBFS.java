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

import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.BoundingBox;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindResult;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.FeaturePathfindSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockWall;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.*;

public class FineGridStonkingBFS implements IPathfinder {
    private int dx, dy, dz;
    private IPathfindWorld dungeonRoom;

    private Node startNode;

    @Getter
    private BoundingBox destinationBB;
    private FeaturePathfindSettings.AlgorithmSettings algorithmSettings;
    private long start;

    public FineGridStonkingBFS(FeaturePathfindSettings.AlgorithmSettings algorithmSettings) {
        this.algorithmSettings = algorithmSettings;
    }
    @Override
    public void init(IPathfindWorld dungeonRoom, BoundingBox destination) {
        this.dungeonRoom = dungeonRoom;

        nodes = new Node[dungeonRoom.getXwidth()+10][dungeonRoom.getZwidth()+10][dungeonRoom.getYwidth()+10];
        this.minX = dungeonRoom.getMinX() - 5;
        this.minY = dungeonRoom.getMinY() - 5;
        this.minZ = dungeonRoom.getMinZ() - 5;

        destinationBB = destination.multiply(2);

        Vec3 centerOfGravity = destinationBB.center();
        this.dx = (int) (centerOfGravity.xCoord);
        this.dy = (int) (centerOfGravity.yCoord);
        this.dz = (int) (centerOfGravity.zCoord);


        for (AxisAlignedBB boundingBox : destinationBB.getBoundingBoxes()) {
            for (int x = (int) Math.ceil(boundingBox.minX); x < boundingBox.maxX; x ++) {
                for (int y = (int) Math.ceil(boundingBox.minY); y < boundingBox.maxY; y ++) {
                    for (int z = (int) Math.ceil(boundingBox.minZ); z < boundingBox.maxZ; z ++) {
                        Node startNode = openNode(x, y, z);
                        startNode.g = 0;
                        startNode.f = 0;
                        startNode.blocked = dungeonRoom.getBlock(x,y,z).isBlocked();
                        open.add(startNode);
                    }
                }
            }
        }

        start = System.currentTimeMillis();
    }
    private int minX, minY, minZ;
    @Getter
    private Node[][][] nodes;
    @Getter
    private PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparing((Node a) -> a == null ? Float.MAX_VALUE : a.f)
            .thenComparing(a -> a == null ? Float.MAX_VALUE :  a.coordinate.x)
            .thenComparing(a -> a == null ? Float.MAX_VALUE :  a.coordinate.y)
            .thenComparing(a -> a == null ? Float.MAX_VALUE :  a.coordinate.z));

    private Node openNode(int x, int y, int z)
    {
        Node node = nodes[x-minX][z-minZ][y-minY];

        if (node == null)
        {
            Node.Coordinate coordinate = new Node.Coordinate(x,y,z);
            node = new Node(coordinate);
            nodes[x-minX][z-minZ][y-minY] = node;
        }
        node.blocked = dungeonRoom.getBlock(x,y,z).isBlocked();

        return node;
    }
    private boolean finished = false;

    @Override
    public boolean doOneStep() {
        if (finished) return true;
        Node n = open.poll();
        if (n == null) {
            finished = true;
            long openNodes = Arrays.stream(nodes).flatMap(a -> Arrays.stream(a))
                            .flatMap(a -> Arrays.stream(a))
                                            .filter(a -> a != null).count();

            ChatTransmitter.sendDebugChat("Pathfinding took "+(System.currentTimeMillis() - start)+" ms with "+openNodes);
            return true;
        }

        if (n.blocked && algorithmSettings.isStonkTeleport()
                && n.coordinate.x % 2 == 1 && n.coordinate.z % 2 == 1 && n.coordinate.y % 2 == 0) {
            Block b = dungeonRoom.getActualBlock((n.coordinate.x-1) / 2, n.coordinate.y / 2, (n.coordinate.z-1) / 2).getBlock();
            Block b2 = dungeonRoom.getActualBlock((n.coordinate.x-1) / 2, n.coordinate.y / 2 + 1, (n.coordinate.z-1) / 2).getBlock();
            if (b instanceof BlockFence || b instanceof BlockWall) {
                if (b2 == Blocks.air) {
                    Node neighbor = openNode(n.coordinate.x, n.coordinate.y + 3, n.coordinate.z);
                    DungeonRoom.CollisionState neighborState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);
                    neighbor.blocked = neighborState.isBlocked();
                    if (!neighborState.isBlocked()) {
                        float gScore = n.g + 15;
                        if (gScore < neighbor.g) {
                            neighbor.parent = n;
                            neighbor.stonkLength = 0;
                            neighbor.connectionType = PathfindResult.PathfindNode.NodeType.TELEPORT_INTO;
                            neighbor.g = gScore;
                            neighbor.f = gScore;
                            open.add(neighbor);
                        }
                    }
                }
            }
        }
        if (n.blocked && algorithmSettings.isStonkEtherwarp()
                && n.coordinate.x % 2 == 1 && n.coordinate.z % 2 == 1 && n.coordinate.y % 2 == 0) {
            Block b = dungeonRoom.getActualBlock((n.coordinate.x-1) / 2, n.coordinate.y / 2, (n.coordinate.z-1) / 2).getBlock();
            if (b instanceof BlockFence || b instanceof BlockWall) {
                for (EnumFacing value : EnumFacing.VALUES) {
                    Block b2 = dungeonRoom.getActualBlock((n.coordinate.x-1) / 2 + value.getFrontOffsetX(),
                            n.coordinate.y / 2 + value.getFrontOffsetY(),
                            (n.coordinate.z-1) / 2 + value.getFrontOffsetZ()).getBlock();
                    if (b2 == Blocks.air) {
                        Node neighbor = openNode(n.coordinate.x + value.getFrontOffsetX() * 2, n.coordinate.y + value.getFrontOffsetY() * 2,
                                n.coordinate.z + value.getFrontOffsetZ() * 2);
                        DungeonRoom.CollisionState neighborState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);
                        neighbor.blocked = neighborState.isBlocked();
                        if (!neighborState.isBlocked()) {
                            float gScore = n.g + 15;
                            if (gScore < neighbor.g) {
                                neighbor.parent = n;
                                neighbor.stonkLength = 0;
                                neighbor.connectionType = PathfindResult.PathfindNode.NodeType.ETHERWARP;
                                neighbor.g = gScore;
                                neighbor.f = gScore;
                                open.add(neighbor);
                            }
                        }
                    }
                }
            }
        }


        if (n.blocked) {
            // in wall
            label: for (EnumFacing value : EnumFacing.VALUES) {
                Node neighbor = openNode(n.coordinate.x + value.getFrontOffsetX(), n.coordinate.y + (value == EnumFacing.DOWN ? 2 : 1)* value.getFrontOffsetY(), n.coordinate.z + value.getFrontOffsetZ());
                DungeonRoom.CollisionState neighborState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);

                if (!neighborState.isCanGo()) {
                    continue; // obv, it's forbidden.
                }
                if (value.getFrontOffsetY() == 0 && !neighborState.isOnGround()) {
                    continue; // you need to keep falling
                }
                if (value.getFrontOffsetY() == -1 && !neighborState.isOnGround()) {
                    continue; // can not jump while floating in air.
                }

                if (!neighborState.isClip()) {
                    continue; // can not go from non-clip to blocked.
                }
                neighbor.blocked = neighborState.isBlocked();

                if (neighbor.blocked && n.stonkLength + (value == EnumFacing.DOWN ? 2 : 1) > algorithmSettings.getMaxStonk()) continue;
                if (neighborState == DungeonRoom.CollisionState.ENDERCHEST && !algorithmSettings.isStonkEChest()) continue;
                if (neighborState == DungeonRoom.CollisionState.STAIR && !algorithmSettings.isStonkDown()) continue;


                float gScore = n.g;
                if (!neighborState.isBlocked() && neighborState.isClip()) {
                    // stonk entrance!!!
                    gScore += 15;
                } else {
                    gScore += value.getFrontOffsetY() == -1
                            || (neighbor.coordinate.x % 2 == 0 && neighbor.coordinate.z % 2 == 0) ? 20 : 7; // pls don't jump.
                }

                if (gScore < neighbor.g) {
                    neighbor.parent = n;
                    if (neighbor.blocked)
                        neighbor.stonkLength = (byte) (n.stonkLength + (value == EnumFacing.DOWN ? 2 : 1));
                    else
                        neighbor.stonkLength = 0;
                    if (neighborState == DungeonRoom.CollisionState.ENDERCHEST)
                        neighbor.connectionType = PathfindResult.PathfindNode.NodeType.ECHEST;
                    else if (neighborState == DungeonRoom.CollisionState.STAIR)
                        neighbor.connectionType = PathfindResult.PathfindNode.NodeType.DIG_DOWN;
                    else
                        neighbor.connectionType = PathfindResult.PathfindNode.NodeType.STONK_WALK;
                    neighbor.g = gScore;
                    neighbor.f = gScore;
                    open.add(neighbor);
                }
            }
        } else {
            label: for (EnumFacing value : EnumFacing.VALUES) {
                Node neighbor = openNode(n.coordinate.x + value.getFrontOffsetX(), n.coordinate.y + value.getFrontOffsetY(), n.coordinate.z + value.getFrontOffsetZ());
                DungeonRoom.CollisionState neighborState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);

                if (!neighborState.isCanGo()) {
                    continue;
                }
                int updist = 0;
                if (neighborState.isBlocked() && !neighborState.isOnGround() && value == EnumFacing.DOWN) {
                    updist++;
                    neighbor = openNode(n.coordinate.x + value.getFrontOffsetX(), n.coordinate.y + value.getFrontOffsetY(), n.coordinate.z + value.getFrontOffsetZ());
                    neighborState = dungeonRoom.getBlock(n.coordinate.x, n.coordinate.y, n.coordinate.z);


                    if (neighborState.isBlocked() && !neighborState.isOnGround())
                        continue;
                }

                if (neighborState.isBlocked() && !neighborState.isOnGround() && value.getFrontOffsetY() == 0)
                    continue;

                neighbor.blocked = neighborState.isBlocked();

                float gScore = n.g + (neighborState.isOnGround() ? 1 : 4 * (updist + 1));
                if (gScore < neighbor.g) {
                    neighbor.parent = n;
                    if (neighbor.blocked)
                        neighbor.stonkLength = (byte) (n.stonkLength + 1 + updist);
                    else
                        neighbor.stonkLength = 0;
                    if (neighborState.isBlocked())
                        neighbor.connectionType = PathfindResult.PathfindNode.NodeType.STONK_EXIT;
                    else
                        neighbor.connectionType = PathfindResult.PathfindNode.NodeType.WALK;
                    neighbor.g = gScore;
                    neighbor.f = gScore;
                    open.add(neighbor);
                }
            }
        }


        DungeonRoom.CollisionState originNodeState = dungeonRoom.getBlock(n.coordinate.x, n.coordinate.y, n.coordinate.z);

        // etherwarps.
        if (!n.blocked && algorithmSettings.isRouteEtherwarp()) {
            if (n.coordinate.y % 2 == 0 && n.coordinate.x % 2 != 0 && n.coordinate.z % 2 != 0 && !originNodeState.isBlocked()) {
                if (dungeonRoom.getBlock(n.coordinate.x, n.coordinate.y-2, n.coordinate.z).isBlocked()) {
                    int xp = (n.coordinate.x-1) / 2;
                    int yp = (n.coordinate.y) / 2 - 2;
                    int zp = (n.coordinate.z-1) / 2;
                    for (; yp >= 0; yp--) {
                        if (dungeonRoom.getActualBlock(xp,yp,zp).getBlock() != Blocks.air) break;
                    }
                    if (yp != -1) {
                        Node neighbor = openNode(n.coordinate.x, yp * 2 + 2, n.coordinate.z);
                        DungeonRoom.CollisionState nodeState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);
                        neighbor.blocked = nodeState.isBlocked();
                        if (!nodeState.isBlocked()) {
                            float gScore = n.g + 100; // don't use etherwarp unelss it saves like 50 blocks

                            if (gScore < neighbor.g) {
                                neighbor.parent = n;
                                neighbor.stonkLength = 0;
                                neighbor.g = gScore;
                                neighbor.connectionType = PathfindResult.PathfindNode.NodeType.ETHERWARP;
                                neighbor.f = gScore;
                                open.add(neighbor);
                            }
                        }
                    }
                }
            }
        }


//        if (originNodeState.isStonkEntrance() || originNodeState.isStonkExit()) {
//           processStonks(n, originNodeState);
//        }
        return false;
    }

//    private void processStonks(Node n, DungeonRoom.CollisionState originNodeState) {
//        if (originNodeState == DungeonRoom.CollisionState.ENTRANCE_STONK_UP && !algorithmSettings.isStonkUp()) return;
//        if (originNodeState == DungeonRoom.CollisionState.ENTRANCE_STONK_DOWN && !algorithmSettings.isStonkDown()) return;
//        if (originNodeState == DungeonRoom.CollisionState.ENTRANCE_STONK_DOWN_FALLING && !algorithmSettings.isStonkDown()) return;
//        if (originNodeState == DungeonRoom.CollisionState.ENTRANCE_TELEPORT_DOWN && !algorithmSettings.isStonkTeleport()) return;
//        if (originNodeState == DungeonRoom.CollisionState.ENTRANCE_ETHERWARP && !algorithmSettings.isStonkEtherwarp()) return;
//        if (originNodeState == DungeonRoom.CollisionState.ENTRANCE_ETHERWARP_FALL && !algorithmSettings.isStonkEtherwarp()) return;
//        if (originNodeState == DungeonRoom.CollisionState.ENTRANCE_STONK_DOWN_ECHEST && !algorithmSettings.isStonkEChest()) return;
//
//            for (EnumFacing value : EnumFacing.VALUES) {
//                int factor = originNodeState == DungeonRoom.CollisionState.ENTRANCE_ETHERWARP ? 2 : 1;
//                if (value.getFrontOffsetY() == -1 && !n.coordinate.stonk) factor = 2;
//
//                Node neighbor = openNode(n.coordinate.x + value.getFrontOffsetX() * factor, n.coordinate.y + value.getFrontOffsetY() * factor, n.coordinate.z + value.getFrontOffsetZ() * factor, !n.coordinate.stonk);
//                DungeonRoom.CollisionState nodeState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);
//
//                int up = 0;
//                if (neighbor.coordinate.stonk) {
//                    if (nodeState.isFall() && value.getFrontOffsetY() == 0) continue; // can not go into fall
//
//                    while (nodeState.isFall()) {
//                        up++;
//                        // remember, we're going target to source.
//                        neighbor = openNode(neighbor.coordinate.x, neighbor.coordinate.y + 1, neighbor.coordinate.z, neighbor.coordinate.stonk);
//                        nodeState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);
//                    }
//                }
//
//                if (!n.coordinate.stonk && (!nodeState.isBlockedNonStonk() || nodeState.isBlockedStonk())) {
//                    continue;
//                }
//                if (n.coordinate.stonk && nodeState.isBlockedNonStonk()) {
//                    continue;
//                }
//                if (originNodeState == DungeonRoom.CollisionState.ENTRANCE_STONK_UP && value != EnumFacing.DOWN) { // stonk up is still considered stonking.
//                    continue;
//                }
//                if ((originNodeState == DungeonRoom.CollisionState.ENTRANCE_STONK_DOWN || originNodeState == DungeonRoom.CollisionState.ENTRANCE_STONK_DOWN_FALLING) && value != EnumFacing.UP && up == 0) {
//                    continue;
//                }
//                if ((originNodeState == DungeonRoom.CollisionState.ENTRANCE_STONK_DOWN_ECHEST) && value != EnumFacing.UP && up == 0) {
//                    continue;
//                }
//                if (originNodeState == DungeonRoom.CollisionState.ENTRANCE_TELEPORT_DOWN && value != EnumFacing.UP && up == 0) {
//                    continue;
//                }
//
//
//                float gScore = n.g + 15 + 4 * up; // altho it's sq, it should be fine
//                if (gScore < neighbor.g) {
//                    neighbor.parent = n;
//
//                    neighbor.connectionType = !n.coordinate.stonk ? PathfindResult.PathfindNode.NodeType.STONK_EXIT :
//                            originNodeState == DungeonRoom.CollisionState.ENTRANCE_STONK_UP ? PathfindResult.PathfindNode.NodeType.DIG_UP :
//                                    originNodeState == DungeonRoom.CollisionState.ENTRANCE_STONK_DOWN || originNodeState == DungeonRoom.CollisionState.ENTRANCE_STONK_DOWN_FALLING ? PathfindResult.PathfindNode.NodeType.DIG_DOWN :
//                                            originNodeState == DungeonRoom.CollisionState.ENTRANCE_STONK_DOWN_ECHEST ? PathfindResult.PathfindNode.NodeType.ECHEST :
//                                                    originNodeState == DungeonRoom.CollisionState.ENTRANCE_TELEPORT_DOWN ? PathfindResult.PathfindNode.NodeType.TELEPORT_INTO :
//                                                            PathfindResult.PathfindNode.NodeType.ETHERWARP;
//                    neighbor.g = gScore;
//                    neighbor.f = gScore;
//                    open.add(neighbor);
//                }
//            }
//    }

    @Override
    public void setTarget(Vec3 from) {
    }

    @Override
    public Vec3 getTarget() {
        return null;
//        return new OffsetVec3(lastSx / 2.0, lastSy / 2.0, lastSz / 2.0);
    }

    @Override
    public PathfindResult getRoute(Vec3 from) {
        int lastSx = (int) Math.round(from.xCoord * 2);
        int lastSy = (int) Math.round(from.yCoord * 2);
        int lastSz = (int) Math.round(from.zCoord * 2);


        Node goalNode = openNode(lastSx, lastSy, lastSz);
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

    @Override
    public double getCost(Vec3 from) {
        int lastSx = (int) Math.round(from.xCoord * 2);
        int lastSy = (int) Math.round(from.yCoord * 2);
        int lastSz = (int) Math.round(from.zCoord * 2);


        Node goalNode = openNode(lastSx, lastSy, lastSz);
        if (goalNode.parent == null) return Double.NaN;
        return goalNode.g;
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
        }
        private final Coordinate coordinate;
        private boolean blocked;

        private float f = Float.MAX_VALUE, g = Float.MAX_VALUE;
        private byte stonkLength = 0;

        @EqualsAndHashCode.Exclude
        private Node parent;
        private PathfindResult.PathfindNode.NodeType connectionType = PathfindResult.PathfindNode.NodeType.WALK;

    }
}
