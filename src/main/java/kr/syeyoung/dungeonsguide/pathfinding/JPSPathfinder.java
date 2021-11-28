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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

import java.lang.reflect.Proxy;
import java.util.*;

public class JPSPathfinder {
    private final BlockPos min, max;
    private final World world;

    private Vec3 start;
    private Vec3 destination;
    private DungeonRoom dungeonRoom;

    @Getter
    private AxisAlignedBB destinationBB;

    public JPSPathfinder(DungeonRoom dungeonRoom){
        this.min = new BlockPos(dungeonRoom.getMinx(), 0, dungeonRoom.getMinz());
        this.max = new BlockPos(dungeonRoom.getMaxx(), 255, dungeonRoom.getMaxz());

        this.world = dungeonRoom.getCachedWorld();
        this.dungeonRoom = dungeonRoom;
    }

    private IntHashMap<Node> nodeMap = new IntHashMap();

    private Node openNode(int x, int y, int z)
    {
        int i = Node.makeHash(x, y, z);
        Node node = this.nodeMap.lookup(i);

        if (node == null)
        {
            node = new Node(x, y, z);
            this.nodeMap.addKey(i, node);
        }

        return node;
    }

    @Getter
    private LinkedList<Vec3> route = new LinkedList<>();

    @Getter
    private PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparing((Node a) -> a.f).thenComparing(a -> a.x).thenComparing(a -> a.y).thenComparing(a -> a.z));

    private int tx, ty, tz;

    private Node addNode(Node parent, Node jumpPt, boolean addToOpen) {
        float ng = parent.g + distSq(jumpPt.x - parent.x, jumpPt.y - parent.y, jumpPt.z - parent.z);

        if (ng < jumpPt.g) {
            if (addToOpen)
            open.remove(jumpPt);
            jumpPt.g = ng;
            jumpPt.h = jumpPt.h == -1 ? distSq(tx - jumpPt.x, ty - jumpPt.y, tz - jumpPt.z) : jumpPt.h;
            jumpPt.f = jumpPt.h + jumpPt.g;
            jumpPt.parent = parent;
            if (addToOpen)
            open.add(jumpPt);
        }
        return jumpPt;
    }


    long arr[];

    public boolean pathfind(Vec3 from, Vec3 to, float within, long timeout) {
        route.clear(); nodeMap.clearMap();

        {
            from = new Vec3(((int)(from.xCoord * 2)) / 2.0, ((int)(from.yCoord * 2)) / 2.0, ((int)(from.zCoord* 2)) / 2.0);
            to = new Vec3(((int)(to.xCoord * 2)) / 2.0, ((int)(to.yCoord * 2)) / 2.0, ((int)(to.zCoord* 2)) / 2.0);
        }

        this.start = from; this.destination = to;
        tx = (int)(to.xCoord * 2);
        ty = (int)(to.yCoord * 2);
        tz = (int)(to.zCoord * 2);

        destinationBB = AxisAlignedBB.fromBounds((to.xCoord - within)* 2, (to.yCoord - within) * 2, (to.zCoord - within) * 2, (to.xCoord + within) * 2, (to.yCoord + within)* 2, (to.zCoord + within) *2);
        open.clear();
        Node start;
        open.add(start = openNode((int)from.xCoord* 2 + 1, (int)from.yCoord* 2 + 1, (int)from.zCoord * 2 + 1));
        start.g = 0; start.f = 0; start.h = (float) from.squareDistanceTo(to);

        Node end = null; float minDist = Float.MAX_VALUE;
        long forceEnd = System.currentTimeMillis() + timeout;
        while(!open.isEmpty()) {
            if (forceEnd < System.currentTimeMillis() && timeout != -1) break;
            Node n = open.poll();
            n.closed= true;
            if (minDist > n.h) {
                minDist = n.h;
                end = n;
            }
            if (n.x > destinationBB.minX && n.x < destinationBB.maxX && n.y > destinationBB.minY && n.y < destinationBB.maxY && n.z > destinationBB.minZ && n.z < destinationBB.maxZ) {
                break;
            }

            for (Node neighbor : getNeighbors(n.parent == null ? n : n.parent, n)) {
                Node jumpPT = expand(n.x, n.y, n.z, neighbor.x - n.x, neighbor.y - n.y, neighbor.z - n.z);
                if (jumpPT == null || jumpPT.closed) continue;

                addNode(n, jumpPT, true);
            }
        }

        if (end == null) return false;
        Node p = end;
        while (p != null) {
            route.addLast(new Vec3(p.x / 2.0f, p.y / 2.0f + 0.1, p.z / 2.0f));
            p = p.parent;
        }


        return true;
    }

    private float distSq(float x, float y, float z) {
        return MathHelper.sqrt_float(x * x + y * y + z * z);
    }

    public Set<Node> getNeighbors(Node prevN, Node n) {
//        if (true) throw new RuntimeException("ah");
        int dx = MathHelper.clamp_int(n.x - prevN.x, -1, 1);
        int dy = MathHelper.clamp_int(n.y - prevN.y, -1, 1);
        int dz = MathHelper.clamp_int(n.z - prevN.z, -1, 1);
        int x = n.x, y = n.y, z = n.z;
        int nx = n.x + dx, ny = n.y + dy, nz = n.z + dz;

        Set<Node> nexts = new HashSet<>();
        int determinant = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
        if (determinant == 0) {
            for (int i = -1; i <= 1; i++)
                for (int j = -1; j <= 1; j++)
                    for (int k = -1; k <= 1; k++) {
                        if (i == 0 && j == 0 && k == 0) continue;
                        nexts.add(openNode(x+i, y+j, z+k));
                    }
        } else if (determinant == 1) {
            nexts.add(openNode(nx,ny,nz));
            for (int i = -1; i<=1; i++) {
                for (int j = - 1; j<=1; j++) {
                    if (i == 0 && j == 0) continue;
                    if (dx != 0 && dungeonRoom.isBlocked(x, y + i, z + j)) nexts.add(openNode(nx,y+i,z+j));
                    if (dy != 0 && dungeonRoom.isBlocked(x + i, y, z + j)) nexts.add(openNode(x+i,ny,z+j));
                    if (dz != 0 && dungeonRoom.isBlocked(x + i, y + j, z)) nexts.add(openNode(x+i,y+j,nz));
                }
            }
        } else if (determinant == 2) {
            if (dz != 0) nexts.add(openNode(x,y,nz));
            if (dy != 0) nexts.add(openNode(x,ny,z));
            if (dx != 0) nexts.add(openNode(nx,y,z));
            nexts.add(openNode(nx,ny,nz));
            if (dx == 0) {
                if (dungeonRoom.isBlocked(x, y, z-dz)) {
                    nexts.add(openNode(x, ny, z-dz));
                    if (dungeonRoom.isBlocked(x+1, y, z-dz)) nexts.add(openNode(x+1, ny, z-dz));
                    if (dungeonRoom.isBlocked(x-1, y, z-dz)) nexts.add(openNode(x-1, ny, z-dz));
                }
                if (dungeonRoom.isBlocked(x, y-dy, z)) {
                    nexts.add(openNode(x, y-dy, nz));
                    if (dungeonRoom.isBlocked(x+1, y-dy, z))nexts.add(openNode(x+1, y-dy, nz));
                    if (dungeonRoom.isBlocked(x-1, y-dy, z))nexts.add(openNode(x+1, y-dy, nz));
                }
            } else if (dy == 0) {
                if (dungeonRoom.isBlocked(x, y, z-dz)) {
                    nexts.add(openNode(x, ny, z-dz));
                    if (dungeonRoom.isBlocked(x, y+1, z-dz)) nexts.add(openNode(nx, y+1, z-dz));
                    if (dungeonRoom.isBlocked(x, y-1, z-dz)) nexts.add(openNode(nx, y-1, z-dz));
                }
                if (dungeonRoom.isBlocked(x-dx, y, z)) {
                    nexts.add(openNode(x-dx, y, nz));
                    if (dungeonRoom.isBlocked(x-dx, y+1, z))nexts.add(openNode(x-dx, y+1, nz));
                    if (dungeonRoom.isBlocked(x-dx, y-1, z))nexts.add(openNode(x-dx, y-1, nz));
                }
            } else if (dz == 0) {
                if (dungeonRoom.isBlocked(x, y-dy, z)) {
                    nexts.add(openNode(nx, y-dy, z));
                    if (dungeonRoom.isBlocked(x, y-dy, z+1))nexts.add(openNode(nx, y-dy, z+1));
                    if (dungeonRoom.isBlocked(x, y-dy, z-1))nexts.add(openNode(nx, y-dy, z-1));
                }
                if (dungeonRoom.isBlocked(x-dx, y, z)) {
                    nexts.add(openNode(x-dx, ny, z));
                    if (dungeonRoom.isBlocked(x-dx, y, z+1))nexts.add(openNode(x-dx, ny, z+1));
                    if (dungeonRoom.isBlocked(x-dx, y, z-1))nexts.add(openNode(x-dx, ny, z-1));
                }
            }
        } else if (determinant == 3) {
            nexts.add(openNode(x,y,nz));
            nexts.add(openNode(x,ny,z));
            nexts.add(openNode(nx,y,z));
            nexts.add(openNode(nx,y,nz));
            nexts.add(openNode(x,ny,nz));
            nexts.add(openNode(nx,ny,z));
            nexts.add(openNode(nx,ny,nz));

            if (dungeonRoom.isBlocked(x,y,z-dz)) {
                nexts.add(openNode(x,ny,z-dz));
                nexts.add(openNode(nx,ny,z-dz));
                nexts.add(openNode(nx,y,z-dz));
            }
            if (dungeonRoom.isBlocked(x-dx,y,z)) {
                nexts.add(openNode(x-dx,ny,nz));
                nexts.add(openNode(x-dx,ny,z));
                nexts.add(openNode(x-dx,y,nz));
            }
            if (dungeonRoom.isBlocked(x,y-dy,z)) {
                nexts.add(openNode(x,y-dy,nz));
                nexts.add(openNode(nx,y-dy,nz));
                nexts.add(openNode(nx,y-dy,nz));
            }
        }
        return nexts;
    }

    public Node expand(int x, int y, int z, int dx, int dy, int dz) {
        while(true) {
            int nx =  x + dx, ny = y + dy, nz = z + dz;
            if (dungeonRoom.isBlocked(nx, ny, nz)) return null;

            if (nx > destinationBB.minX && nx < destinationBB.maxX && ny > destinationBB.minY && ny < destinationBB.maxY && nz > destinationBB.minZ && nz < destinationBB.maxZ) return openNode(nx,ny,nz);

            int determinant = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
            if (determinant == 1) {
                for (int i = -1; i<=1; i++) {
                    for (int j = - 1; j<=1; j++) {
                        if (i == 0 && j == 0) continue;
                        if (dx != 0 && dungeonRoom.isBlocked(nx, ny + i, nz + j) && !dungeonRoom.isBlocked(nx+dx, ny + i, nz + j)) return  openNode(nx,ny,nz);
                        if (dy != 0 && dungeonRoom.isBlocked(nx + i, ny, nz + j) && !dungeonRoom.isBlocked(nx + i, ny+dy, nz + j)) return openNode(nx,ny,nz);
                        if (dz != 0 && dungeonRoom.isBlocked(nx + i, ny + j , nz) && !dungeonRoom.isBlocked(nx + i, ny + j , nz+dz)) return openNode(nx,ny,nz);
                    }
                }
            } else if (determinant == 2) {
                if ((dx != 0 && dungeonRoom.isBlocked(nx , y , z ) && !dungeonRoom.isBlocked(nx + dx, y , z))
                        || (dy != 0 && dungeonRoom.isBlocked(x , ny  , z) && !dungeonRoom.isBlocked(x , ny+dy  , z))
                        || (dz != 0 && dungeonRoom.isBlocked(x  , y , nz  ) && !dungeonRoom.isBlocked(x  , y , nz+dz))) return openNode(nx,ny,nz);
                if (dx != 0 &&  expand(nx, ny, nz, dx, 0,0) != null) return openNode(nx,ny,nz);
                if (dy != 0 && expand(nx, ny, nz, 0, dy,0) != null) return openNode(nx,ny,nz);
                if (dz != 0 && expand(nx, ny, nz, 0, 0,dz) != null) return openNode(nx,ny,nz);
            } else if (determinant == 3) {
                if (dungeonRoom.isBlocked(x, ny, nz ) || dungeonRoom.isBlocked(nx, y , nz) || dungeonRoom.isBlocked(nx, ny, z)) return openNode(nx,ny,nz);
                if (expand(nx, ny, nz, dx, 0, 0) != null ||
                        expand(nx, ny, nz, dx, dy, 0) != null ||
                        expand(nx, ny, nz, dx, 0, dz) != null ||
                        expand(nx, ny, nz, 0, dy, 0) != null ||
                        expand(nx, ny, nz, 0, dy, dz) != null ||
                        expand(nx, ny, nz, 0, 0, dz) != null) return openNode(nx,ny,nz);
            }
            x = nx; y = ny; z = nz;
        }
    }

    @RequiredArgsConstructor
    @Data
    public static final class Node {
        private final int x, y, z;

        private float f, g = Float.MAX_VALUE, h = -1;
        private boolean closed;

        @EqualsAndHashCode.Exclude
        private Node parent;

        public static int makeHash(int x, int y, int z)
        {
            return y & 255 | (x & 32767) << 8 | (z & 32767) << 24 | (x < 0 ? Integer.MIN_VALUE : 0) | (z < 0 ? 32768 : 0);
        }

        public Node close() {
            this.closed = true;
            return this;
        }

    }
}
