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
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockWall;
import net.minecraft.block.state.IBlockState;
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

    private boolean emptyFor(IBlockState blockState) {
        if (blockState.getBlock() == Blocks.carpet) return true;
        if (blockState.getBlock() == Blocks.skull) return true;
        if (blockState.getBlock() == Blocks.standing_sign) return false;
        if (blockState.getBlock() == Blocks.wall_sign) return false;
        if (blockState.getBlock() == Blocks.air) return true;
        if (!blockState.getBlock().canCollideCheck(blockState, false)) return true;
        return false;
    }

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


        DungeonRoom.CollisionState originNodeState = dungeonRoom.getBlock(n.coordinate.x, n.coordinate.y, n.coordinate.z);



        if (n.blocked && algorithmSettings.isStonkTeleport()
                && n.coordinate.x % 2 == 1 && n.coordinate.z % 2 == 1 && n.coordinate.y % 2 == 0) {
            IBlockState b = dungeonRoom.getActualBlock((n.coordinate.x-1) / 2, n.coordinate.y / 2 - 1, (n.coordinate.z-1) / 2);
            IBlockState b2 = dungeonRoom.getActualBlock((n.coordinate.x-1) / 2, n.coordinate.y / 2, (n.coordinate.z-1) / 2);
            if (b.getBlock() instanceof BlockFence || b.getBlock() instanceof BlockWall) {
                if (b2.getBlock() == Blocks.air) {
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
            IBlockState b = dungeonRoom.getActualBlock((n.coordinate.x-1) / 2, n.coordinate.y / 2 - 1, (n.coordinate.z-1) / 2);
            IBlockState b2 = dungeonRoom.getActualBlock((n.coordinate.x-1) / 2, n.coordinate.y / 2, (n.coordinate.z-1) / 2);
            IBlockState b3 = dungeonRoom.getActualBlock((n.coordinate.x-1) / 2, n.coordinate.y / 2 + 1, (n.coordinate.z-1) / 2);
            if (!emptyFor(b) && emptyFor(b2) && emptyFor(b3)) {
                for (EnumFacing value : EnumFacing.VALUES) {
                    Block near = dungeonRoom.getActualBlock((n.coordinate.x-1) / 2 + value.getFrontOffsetX(),
                            n.coordinate.y / 2 + value.getFrontOffsetY() - 1,
                            (n.coordinate.z-1) / 2 + value.getFrontOffsetZ()).getBlock();
                    if (near == Blocks.air) {
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

        if (originNodeState.isPearltarget() && algorithmSettings.isEnderpearl()) {
            for (EnumFacing value : EnumFacing.VALUES) {
                if (value == EnumFacing.UP) continue;

                IBlockState b1 = dungeonRoom.getActualBlock((n.coordinate.x-1) / 2 + value.getFrontOffsetX(),
                        n.coordinate.y / 2 + value.getFrontOffsetY(),
                        (n.coordinate.z-1) / 2 + value.getFrontOffsetZ());
                IBlockState b2 = dungeonRoom.getActualBlock((n.coordinate.x-1) / 2 + 2*value.getFrontOffsetX(),
                        n.coordinate.y / 2 + 2*value.getFrontOffsetY(),
                        (n.coordinate.z-1) / 2 + 2*value.getFrontOffsetZ());
                if (b1.getBlock() != Blocks.air) continue;
                if (b2.getBlock() != Blocks.air) continue;

                Node neighbor = openNode(n.coordinate.x + value.getFrontOffsetX() * 4, n.coordinate.y + value.getFrontOffsetY() * 4,
                        n.coordinate.z + value.getFrontOffsetZ() * 4);
                DungeonRoom.CollisionState neighborState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);

                int down  =0;
                while (!neighborState.isOnGround() && neighbor.coordinate.y > 0) {
                    neighbor = openNode(neighbor.coordinate.x, neighbor.coordinate.y - 1, neighbor.coordinate.z);
                    neighborState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);
                    down ++;
                }


                neighbor.blocked = neighborState.isBlocked();
                if (!neighborState.isBlocked()) {
                    float gScore = n.g + 150 + MathHelper.sqrt_float(down*down + 16);
                    if (gScore < neighbor.g) {
                        neighbor.parent = n;
                        neighbor.stonkLength = 0;
                        neighbor.connectionType = PathfindResult.PathfindNode.NodeType.ENDERPEARL;
                        neighbor.g = gScore;
                        neighbor.f = gScore;
                        open.add(neighbor);
                    }
                }
            }
        }


//        if (originNodeState.isCanGo()) {
            if (n.blocked) {
                // in wall
                label:
                for (EnumFacing value : EnumFacing.VALUES) {
                    Node neighbor = openNode(n.coordinate.x + value.getFrontOffsetX(), n.coordinate.y + (value == EnumFacing.DOWN ? 2 : 1) * value.getFrontOffsetY(), n.coordinate.z + value.getFrontOffsetZ());
                    DungeonRoom.CollisionState neighborState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);

                    if (!neighborState.isCanGo() && (neighborState.isOnGround() || value != EnumFacing.UP)) {
                        continue; // obv, it's forbidden.
                    }
                    if (value.getFrontOffsetY() == 0 && !neighborState.isOnGround()) {
                        continue; // you need to keep falling
                    }
                    if (value.getFrontOffsetY() == -1 && !neighborState.isOnGround()) {
                        continue; // can not jump while floating in air.
                    }

                    boolean elligibleForTntPearl = algorithmSettings.isTntpearl() && neighborState.isOnGround() && !neighborState.isClip()
                            && ((n.coordinate.x % 2 == 0) != (n.coordinate.z % 2 == 0))
                            && value.getFrontOffsetY() == 0 && neighbor.coordinate.y % 2 == 0;


                    if (!neighborState.isClip() && !elligibleForTntPearl) {
                        continue; // can not go from non-clip to blocked.
                    }
                    neighbor.blocked = neighborState.isBlocked();

                    if (neighbor.blocked && n.stonkLength + (value == EnumFacing.DOWN ? 2 : 1) > algorithmSettings.getMaxStonk())
                        continue;
                    if (neighborState == DungeonRoom.CollisionState.ENDERCHEST && !algorithmSettings.isStonkEChest())
                        continue;
                    if (neighborState == DungeonRoom.CollisionState.STAIR && !algorithmSettings.isStonkDown()) continue;


                    float gScore = n.g;
                    if (!neighborState.isClip() && elligibleForTntPearl)
                        gScore += 300; // tntpearl slow
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
                        else if (elligibleForTntPearl)
                            neighbor.connectionType = PathfindResult.PathfindNode.NodeType.TNTPEARL;
                        else
                            neighbor.connectionType = PathfindResult.PathfindNode.NodeType.STONK_WALK;
                        neighbor.g = gScore;
                        neighbor.f = gScore;
                        open.add(neighbor);
                    }
                }
            } else {
                label:
                for (EnumFacing value : EnumFacing.VALUES) {
                    Node neighbor = openNode(n.coordinate.x + value.getFrontOffsetX(), n.coordinate.y + value.getFrontOffsetY(), n.coordinate.z + value.getFrontOffsetZ());
                    DungeonRoom.CollisionState neighborState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);

                    if (!neighborState.isCanGo()) {
                        continue;
                    }
                    int updist = 0;
                    if (neighborState.isBlocked() && !neighborState.isOnGround() && value == EnumFacing.DOWN) {
                        updist++;
                        neighbor = openNode(n.coordinate.x + value.getFrontOffsetX(), n.coordinate.y + value.getFrontOffsetY(), n.coordinate.z + value.getFrontOffsetZ());
                        neighborState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);


                        if (neighborState.isBlocked() && !neighborState.isOnGround())
                            continue;
                    }

                    if (neighborState.isBlocked() && !neighborState.isOnGround() && value.getFrontOffsetY() == 0)
                        continue;

                    neighbor.blocked = neighborState.isBlocked();

                    boolean superboomthingy = (originNodeState == DungeonRoom.CollisionState.SUPERBOOMABLE_AIR || originNodeState == DungeonRoom.CollisionState.SUPERBOOMABLE_GROUND) &&
                            (neighborState != DungeonRoom.CollisionState.SUPERBOOMABLE_AIR && neighborState != DungeonRoom.CollisionState.SUPERBOOMABLE_GROUND);
                    float gScore = n.g + (superboomthingy ? 15 : neighborState.isOnGround() ? 1 : 4 * (updist + 1));
                    if (gScore < neighbor.g) {
                        neighbor.parent = n;
                        if (neighbor.blocked)
                            neighbor.stonkLength = (byte) (n.stonkLength + 1 + updist);
                        else
                            neighbor.stonkLength = 0;

                        if (superboomthingy)
                            neighbor.connectionType = PathfindResult.PathfindNode.NodeType.SUPERBOOM;
                        else if (neighborState.isBlocked())
                            neighbor.connectionType = PathfindResult.PathfindNode.NodeType.STONK_EXIT;
                        else
                            neighbor.connectionType = PathfindResult.PathfindNode.NodeType.WALK;
                        neighbor.g = gScore;
                        neighbor.f = gScore;
                        open.add(neighbor);
                    }
                }
            }
//        }


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

        return false;
    }


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
