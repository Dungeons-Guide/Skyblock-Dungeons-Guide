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

package kr.syeyoung.dungeonsguide.mod.pathfinding.pathfinder;

import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.CollisionStateCalculatingCoordinateMap;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.PearlCalculatingCoordinateMap;
import kr.syeyoung.dungeonsguide.mod.pathfinding.BoundingBox;
import kr.syeyoung.dungeonsguide.mod.pathfinding.PathfindResult;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.pathfinding.world.IPathfindWorld;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.world.BlockType;
import kr.syeyoung.modapi.world.UBlockState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import java.util.*;

public class FineGridStonkingBFS implements IPathfinder {
    private int dx, dy, dz;
    private IPathfindWorld dungeonRoom;

    private Node startNode;

    @Getter
    private BoundingBox destinationBB;
    private AlgorithmSetting algorithmSetting;
    private long start;

    public FineGridStonkingBFS(AlgorithmSetting algorithmSetting) {
        this.algorithmSetting = algorithmSetting;
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


        for (AABB boundingBox : destinationBB.getBoundingBoxes()) {
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

    private boolean emptyFor(UBlockState blockState) {
        if (blockState.isOf(BlockType.AIR, BlockType.CARPET, BlockType.SKULL, BlockType.STANDING_SIGN, BlockType.WALL_SIGN, BlockType.QUARTZ_ORE)) return true;
        if (!blockState.canCollideCheck(false)) return true;
        if (blockState.getCollisionBoundingBox(ModAPI.getAPI().getWorld(), new VectorI3D(0,0,0)) == null) return true;
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


        CollisionStateCalculatingCoordinateMap.CollisionState originNodeState = dungeonRoom.getBlock(n.coordinate.x, n.coordinate.y, n.coordinate.z);
        PearlCalculatingCoordinateMap.PearlLandType originalPearlType = dungeonRoom.getPearl(n.coordinate.x, n.coordinate.y, n.coordinate.z);


        if (n.blocked && algorithmSetting.isStonkTeleport()
                && n.coordinate.x % 2 != 0 && n.coordinate.z % 2 != 0 && n.coordinate.y % 2 == 0) {
            UBlockState b = dungeonRoom.getActualBlock((n.coordinate.x-1) / 2, n.coordinate.y / 2 - 1, (n.coordinate.z-1) / 2);
            UBlockState b2 = dungeonRoom.getActualBlock((n.coordinate.x-1) / 2, n.coordinate.y / 2, (n.coordinate.z-1) / 2);
            if (b.isOf(BlockType.TAG_FENCE, BlockType.TAG_WALL)) {
                if (b2.isOf(BlockType.AIR)) {
                    Node neighbor = openNode(n.coordinate.x, n.coordinate.y + 1, n.coordinate.z);
                    CollisionStateCalculatingCoordinateMap.CollisionState neighborState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);
                    neighbor.blocked = neighborState.isBlocked();
                    if (!neighborState.isBlocked()) {
                        float gScore = n.g + 4;
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
        if (algorithmSetting.isRouteEtherwarp()
                && (n.coordinate.x % 2) != 0 && (n.coordinate.z % 2) != 0 && n.coordinate.y % 2 == 0) {
            UBlockState b = dungeonRoom.getActualBlock((n.coordinate.x-1) / 2, n.coordinate.y / 2 - 1, (n.coordinate.z-1) / 2);
            UBlockState b2 = dungeonRoom.getActualBlock((n.coordinate.x-1) / 2, n.coordinate.y / 2, (n.coordinate.z-1) / 2);
            UBlockState b3 = dungeonRoom.getActualBlock((n.coordinate.x-1) / 2, n.coordinate.y / 2 + 1, (n.coordinate.z-1) / 2);
            if (!emptyFor(b) && emptyFor(b2) && emptyFor(b3)) {
                // elligible for etherwarp.

                BlockPos start = new BlockPos((n.coordinate.x-1) / 2,
                        n.coordinate.y / 2 - 1,
                        (n.coordinate.z-1) / 2);

                for (BlockPos target : ShadowCast.realShadowcast((x,y,z) -> !dungeonRoom.getActualBlock(x,y,z).isOf(BlockType.AIR), start.getX(), start.getY(), start.getZ(),
                        algorithmSetting.getEtherwarpRadius(), algorithmSetting.getEtherwarpLeeway(), algorithmSetting.getEtherwarpOffset())) {
                    if (start.distanceSq(target.getX()/2.0, target.getY()/2.0 - 1.5 , target.getZ()/2.0) >57 * 57) continue;
                    if (target.getX()  < dungeonRoom.getMinX()) continue;
                    if (target.getY() - 3 < dungeonRoom.getMinY()) continue;
                    if (target.getZ() < dungeonRoom.getMinZ()) continue;
                    if (target.getX() >= dungeonRoom.getXwidth() + minX) continue;
                    if (target.getY() -3 >= dungeonRoom.getYwidth() + minY) continue;
                    if (target.getZ() >= dungeonRoom.getZwidth() + minZ) continue;

                    Node neighbor = openNode(target.getX(), target.getY()-3, target.getZ());
                    CollisionStateCalculatingCoordinateMap.CollisionState neighborState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);
                    if (!neighborState.isOnGround()) {
                        continue;
                    }
                    neighbor.blocked = neighborState.isBlocked();
//                        if (!neighborState.isBlocked()) {
                            float gScore = n.g + 20; // don't etherwarp unless it saves like 10 blocks
                            if (gScore < neighbor.g) {
                                neighbor.parent = n;
                                neighbor.stonkLength = 0;
                                neighbor.connectionType = PathfindResult.PathfindNode.NodeType.ETHERWARP;
                                neighbor.g = gScore;
                                neighbor.f = gScore;
                                open.add(neighbor);
                            }
//                        }
                }
            }
        }

        if ((originalPearlType == PearlCalculatingCoordinateMap.PearlLandType.FLOOR || originalPearlType == PearlCalculatingCoordinateMap.PearlLandType.CEILING) && algorithmSetting.isEnderpearl() && originNodeState.isBlocked()) {
            label: for (EnumFacing value : EnumFacing.VALUES) {
                if (value == EnumFacing.UP) continue;;
                for (int i = 1; i < 3; i++) {
                    PearlCalculatingCoordinateMap.PearlLandType landType = dungeonRoom.getPearl(n.coordinate.x + i* value.getFrontOffsetX(), n.coordinate.y + i*value.getFrontOffsetY(), n.coordinate.z + i*value.getFrontOffsetZ());
                    if (landType != PearlCalculatingCoordinateMap.PearlLandType.OPEN
                            && !(originalPearlType == PearlCalculatingCoordinateMap.PearlLandType.FLOOR && landType == PearlCalculatingCoordinateMap.PearlLandType.FLOOR)
                            && !(originalPearlType == PearlCalculatingCoordinateMap.PearlLandType.CEILING && landType == PearlCalculatingCoordinateMap.PearlLandType.CEILING)) continue label;
                }


                Node neighbor = openNode(n.coordinate.x + value.getFrontOffsetX() * 2, n.coordinate.y + value.getFrontOffsetY() * 2,
                        n.coordinate.z + value.getFrontOffsetZ() * 2);
                CollisionStateCalculatingCoordinateMap.CollisionState neighborState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);

                int down  =0;
                while (!neighborState.isOnGround() && neighbor.coordinate.y > 0) {
                    neighbor = openNode(neighbor.coordinate.x, neighbor.coordinate.y - 1, neighbor.coordinate.z);
                    neighborState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);
                    down ++;
                }

                if (neighbor.coordinate.y == 0) continue;

                neighbor.blocked = neighborState.isBlocked();
                if (!neighborState.isBlocked() && down < 10) {
                    float gScore = (float) (n.g + 20 + Math.sqrt(down*down + 16));
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

        if ((originalPearlType == PearlCalculatingCoordinateMap.PearlLandType.WALL) && algorithmSetting.isEnderpearl() && originNodeState.isBlocked() && originNodeState.isOnGround()) {
            label: for (EnumFacing value : EnumFacing.VALUES) {
                if (value == EnumFacing.UP) continue;;
                for (int i = 1; i < 2; i++) {
                    PearlCalculatingCoordinateMap.PearlLandType landType = dungeonRoom.getPearl(n.coordinate.x + i* value.getFrontOffsetX(), n.coordinate.y + i*value.getFrontOffsetY(), n.coordinate.z + i*value.getFrontOffsetZ());
                    CollisionStateCalculatingCoordinateMap.CollisionState collisionState = dungeonRoom.getBlock(n.coordinate.x + i* value.getFrontOffsetX(), n.coordinate.y + i*value.getFrontOffsetY(), n.coordinate.z + i*value.getFrontOffsetZ());
                    if (landType != PearlCalculatingCoordinateMap.PearlLandType.OPEN) continue label;
                    if (!collisionState.isBlocked() || !collisionState.isCanGo()) continue label;
                }


                Node neighbor = openNode(n.coordinate.x + value.getFrontOffsetX() * 2, n.coordinate.y + value.getFrontOffsetY() * 2,
                        n.coordinate.z + value.getFrontOffsetZ() * 2);
                CollisionStateCalculatingCoordinateMap.CollisionState neighborState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);

                int down  =0;
                while (!neighborState.isOnGround() && neighbor.coordinate.y > 0) {
                    neighbor = openNode(neighbor.coordinate.x, neighbor.coordinate.y - 1, neighbor.coordinate.z);
                    neighborState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);
                    down ++;
                }
                if (neighbor.coordinate.y == 0) continue;


                neighbor.blocked = neighborState.isBlocked();
                if (!neighborState.isBlocked() && 5 < down && down < 30) {
                    float gScore = (float) (n.g + 20 + Math.sqrt(down*down + 16));
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
                boolean ontop = dungeonRoom.getBlock(n.coordinate.x, n.coordinate.y + 1, n.coordinate.z) == CollisionStateCalculatingCoordinateMap.CollisionState.ONGROUND;
                label:
                for (EnumFacing value : EnumFacing.VALUES) {
                    Node neighbor = openNode(n.coordinate.x + value.getFrontOffsetX(), n.coordinate.y + (value == EnumFacing.DOWN ? 2 : 1) * value.getFrontOffsetY(), n.coordinate.z + value.getFrontOffsetZ());
                    CollisionStateCalculatingCoordinateMap.CollisionState neighborState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);
//                    DungeonRoom.PearlLandType pearlLandType = dungeonRoom.getPearl(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);
                    if (!neighborState.isCanGo() && (neighborState.isOnGround() || value != EnumFacing.UP)) {
                        continue; // obv, it's forbidden.
                    }
                    if (value.getFrontOffsetY() == 0 && !neighborState.isOnGround()) {
                        continue; // you need to keep falling
                    }
                    if (value.getFrontOffsetY() == -1 && !neighborState.isOnGround()) {
                        continue; // can not jump while floating in air.
                    }
//                    if (neighborState)

                    boolean elligibleForTntPearl = algorithmSetting.isTntpearl() && neighborState.isOnGround() && !neighborState.isClip()
                            && value.getFrontOffsetY() == 0 && neighbor.coordinate.y % 2 == 0 && originalPearlType == PearlCalculatingCoordinateMap.PearlLandType.FLOOR_WALL && dungeonRoom.getActualBlock((int) Math.floor(neighbor.coordinate.x / 2.0), neighbor.coordinate.y / 2, (int) Math.floor(neighbor.coordinate.z / 2.0)).isOf(BlockType.AIR);

                    if (!neighborState.isClip() && !elligibleForTntPearl) {
                        continue; // can not go from non-clip to blocked.
                    }
                    neighbor.blocked = neighborState.isBlocked();

                    if (neighbor.blocked && n.stonkLength + (value == EnumFacing.DOWN ? 2 : 1) > algorithmSetting.getMaxStonk())
                        continue;
                    if (neighborState == CollisionStateCalculatingCoordinateMap.CollisionState.ENDERCHEST && !algorithmSetting.isStonkEChest())
                        continue;
                    if (neighborState == CollisionStateCalculatingCoordinateMap.CollisionState.STAIR && !algorithmSetting.isStonkDown()) continue;


                    float gScore = n.g;
                    if (!neighborState.isClip() && elligibleForTntPearl)
                        gScore += 20; // tntpearl slow
                    if (!neighborState.isBlocked() && neighborState.isClip()) {
                        // stonk entrance!!!
                        gScore += neighborState == CollisionStateCalculatingCoordinateMap.CollisionState.ENDERCHEST ? 50 : 6; // don't enderchest unless it saves like 25 blocks
                    } else if (value.getFrontOffsetY() == -1) {
                        gScore += 100;
                    } else if (neighbor.coordinate.x % 2 == 0 || neighbor.coordinate.z % 2 == 0) {
                        gScore += 3;// pls don't jump.
                    } else {
                        gScore += 2;
                    }

                    if (gScore < neighbor.g) {
                        neighbor.parent = n;
                        if (neighbor.blocked)
                            neighbor.stonkLength = (byte) (n.stonkLength + (value == EnumFacing.DOWN ? 2 : 1));
                        else
                            neighbor.stonkLength = 0;
                        if (neighborState == CollisionStateCalculatingCoordinateMap.CollisionState.ENDERCHEST)
                            neighbor.connectionType = PathfindResult.PathfindNode.NodeType.ECHEST;
                        else if (neighborState == CollisionStateCalculatingCoordinateMap.CollisionState.STAIR)
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
                    CollisionStateCalculatingCoordinateMap.CollisionState neighborState = dungeonRoom.getBlock(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z);

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

                    boolean superboomthingy = (originNodeState == CollisionStateCalculatingCoordinateMap.CollisionState.SUPERBOOMABLE_AIR || originNodeState == CollisionStateCalculatingCoordinateMap.CollisionState.SUPERBOOMABLE_GROUND) &&
                            (neighborState != CollisionStateCalculatingCoordinateMap.CollisionState.SUPERBOOMABLE_AIR && neighborState != CollisionStateCalculatingCoordinateMap.CollisionState.SUPERBOOMABLE_GROUND);
                    float gScore = n.g + (superboomthingy ? 10 : neighborState.isOnGround() || value == EnumFacing.UP ? 1 : 2 * (updist + 1));
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



        return false;
    }


    @Override
    public void setTarget(Vector3D from) {
    }

    @Override
    public Vector3D getTarget() {
        return null;
//        return new OffsetVec3(lastSx / 2.0, lastSy / 2.0, lastSz / 2.0);
    }

    @Override
    public PathfindResult getRoute(Vector3D from) {
        int lastSx = (int) Math.round(from.x * 2);
        int lastSy = (int) Math.round(from.y * 2);
        int lastSz = (int) Math.round(from.z * 2);


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
    public double getCost(Vector3D from) {
        int lastSx = (int) Math.round(from.x * 2);
        int lastSy = (int) Math.round(from.y * 2);
        int lastSz = (int) Math.round(from.z * 2);


        Node goalNode = openNode(lastSx, lastSy, lastSz);
        if (goalNode.parent == null) return Double.NaN;
        return goalNode.g;
    }

    private int manhatten(int x, int y, int z) {return Math.abs(x)+ Math.abs(y)+ Math.abs(z);}
    private float distSq(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    @Override
    public void close()  {}


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
