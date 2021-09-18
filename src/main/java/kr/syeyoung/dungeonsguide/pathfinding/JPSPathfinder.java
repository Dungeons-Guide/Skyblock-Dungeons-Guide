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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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

    @Getter
    private AxisAlignedBB destinationBB;

    public JPSPathfinder(World world, BlockPos min, BlockPos max ){
        this.min = min;
        this.max = max;

        ChunkCache chunkCache = new ChunkCache(world, min, max, 0);
        this.world =  new CachedWorld(chunkCache);

        minx = min.getX() * 2; miny = min.getY() * 2; minz = min.getZ() * 2;
        maxx = max.getX() * 2 + 2; maxy = max.getY() * 2 + 2; maxz = max.getZ() * 2 + 2;

        lenx = maxx - minx;
        leny = maxy - miny;
        lenz = maxz - minz;

    }

    private IntHashMap<Node> nodeMap = new IntHashMap();

    private Node openNode(int x, int y, int z)
    {
        int i = Node.makeHash(x, y, z);
        Node node = (Node)this.nodeMap.lookup(i);

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

    private final int minx, miny, minz, maxx, maxy, maxz;
    private final int lenx, leny, lenz;

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

    public boolean pathfind(Vec3 from, Vec3 to) {
        route.clear(); nodeMap.clearMap();

        this.start = from; this.destination = to;
        tx = (int)(to.xCoord * 2);
        ty = (int)(to.yCoord * 2);
        tz = (int)(to.zCoord * 2);

        arr = new long[lenx *leny * lenz * 2 / 8];

        destinationBB = AxisAlignedBB.fromBounds((to.xCoord - 0.6)* 2, (to.yCoord - 0.6) * 2, (to.zCoord - 0.6) * 2, (to.xCoord + 0.6) * 2, (to.yCoord + 0.6)* 2, (to.zCoord + 0.6) *2);
        open.clear();
        Node start;
        open.add(start = openNode((int)from.xCoord* 2 + 1, (int)from.yCoord* 2 + 1, (int)from.zCoord * 2 + 1));
        start.g = 0; start.f = 0; start.h = (float) from.squareDistanceTo(to);

        Node end = null; float minDist = Float.MAX_VALUE;
        long forceEnd = System.currentTimeMillis() + 5000;
        while(!open.isEmpty()) {
            if (forceEnd < System.currentTimeMillis()) break;
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
            route.addLast(new Vec3(p.x / 2.0f, p.y / 2.0f, p.z / 2.0f));
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
                    if (dx != 0 && isBlocked(x, y + i, z + j)) nexts.add(openNode(nx,y+i,z+j));
                    if (dy != 0 && isBlocked(x + i, y, z + j)) nexts.add(openNode(x+i,ny,z+j));
                    if (dz != 0 && isBlocked(x + i, y + j, z)) nexts.add(openNode(x+i,y+j,nz));
                }
            }
        } else if (determinant == 2) {
            if (dz != 0) nexts.add(openNode(x,y,nz));
            if (dy != 0) nexts.add(openNode(x,ny,z));
            if (dx != 0) nexts.add(openNode(nx,y,z));
            nexts.add(openNode(nx,ny,nz));
            if (dx == 0) {
                if (isBlocked(x, y, z-dz)) {
                    nexts.add(openNode(x, ny, z-dz));
                    if (isBlocked(x+1, y, z-dz)) nexts.add(openNode(x+1, ny, z-dz));
                    if (isBlocked(x-1, y, z-dz)) nexts.add(openNode(x-1, ny, z-dz));
                }
                if (isBlocked(x, y-dy, z)) {
                    nexts.add(openNode(x, y-dy, nz));
                    if (isBlocked(x+1, y-dy, z))nexts.add(openNode(x+1, y-dy, nz));
                    if (isBlocked(x-1, y-dy, z))nexts.add(openNode(x+1, y-dy, nz));
                }
            } else if (dy == 0) {
                if (isBlocked(x, y, z-dz)) {
                    nexts.add(openNode(x, ny, z-dz));
                    if (isBlocked(x, y+1, z-dz)) nexts.add(openNode(nx, y+1, z-dz));
                    if (isBlocked(x, y-1, z-dz)) nexts.add(openNode(nx, y-1, z-dz));
                }
                if (isBlocked(x-dx, y, z)) {
                    nexts.add(openNode(x-dx, y, nz));
                    if (isBlocked(x-dx, y+1, z))nexts.add(openNode(x-dx, y+1, nz));
                    if (isBlocked(x-dx, y-1, z))nexts.add(openNode(x-dx, y-1, nz));
                }
            } else if (dz == 0) {
                if (isBlocked(x, y-dy, z)) {
                    nexts.add(openNode(nx, y-dy, z));
                    if (isBlocked(x, y-dy, z+1))nexts.add(openNode(nx, y-dy, z+1));
                    if (isBlocked(x, y-dy, z-1))nexts.add(openNode(nx, y-dy, z-1));
                }
                if (isBlocked(x-dx, y, z)) {
                    nexts.add(openNode(x-dx, ny, z));
                    if (isBlocked(x-dx, y, z+1))nexts.add(openNode(x-dx, ny, z+1));
                    if (isBlocked(x-dx, y, z-1))nexts.add(openNode(x-dx, ny, z-1));
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

            if (isBlocked(x,y,z-dz)) {
                nexts.add(openNode(x,ny,z-dz));
                nexts.add(openNode(nx,ny,z-dz));
                nexts.add(openNode(nx,y,z-dz));
            }
            if (isBlocked(x-dx,y,z)) {
                nexts.add(openNode(x-dx,ny,nz));
                nexts.add(openNode(x-dx,ny,z));
                nexts.add(openNode(x-dx,y,nz));
            }
            if (isBlocked(x,y-dy,z)) {
                nexts.add(openNode(x,y-dy,nz));
                nexts.add(openNode(nx,y-dy,x));
                nexts.add(openNode(nx,y-dy,nz));
            }
        }
        return nexts;
    }

    public Node expand(int x, int y, int z, int dx, int dy, int dz) {
        while(true) {
            int nx =  x + dx, ny = y + dy, nz = z + dz;
            if (isBlocked(nx, ny, nz)) return null;

            if (nx > destinationBB.minX && nx < destinationBB.maxX && ny > destinationBB.minY && ny < destinationBB.maxY && nz > destinationBB.minZ && nz < destinationBB.maxZ) return openNode(nx,ny,nz);

            int determinant = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
            if (determinant == 1) {
                for (int i = -1; i<=1; i++) {
                    for (int j = - 1; j<=1; j++) {
                        if (i == 0 && j == 0) continue;
                        if (dx != 0 && isBlocked(nx, ny + i, nz + j) && !isBlocked(nx+dx, ny + i, nz + j)) return  openNode(nx,ny,nz);
                        if (dy != 0 && isBlocked(nx + i, ny, nz + j) && !isBlocked(nx + i, ny+dy, nz + j)) return openNode(nx,ny,nz);
                        if (dz != 0 && isBlocked(nx + i, ny + j , nz) && !isBlocked(nx + i, ny + j , nz+dz)) return openNode(nx,ny,nz);
                    }
                }
            } else if (determinant == 2) {
                if ((dx != 0 && isBlocked(nx , y , z ) && !isBlocked(nx + dx, y , z))
                        || (dy != 0 && isBlocked(x , ny  , z) && !isBlocked(x , ny+dy  , z))
                        || (dz != 0 && isBlocked(x  , y , nz  ) && !isBlocked(x  , y , nz+dz))) return openNode(nx,ny,nz);
                if (dx != 0 &&  expand(nx, ny, nz, dx, 0,0) != null) return openNode(nx,ny,nz);
                if (dy != 0 && expand(nx, ny, nz, 0, dy,0) != null) return openNode(nx,ny,nz);
                if (dz != 0 && expand(nx, ny, nz, 0, 0,dz) != null) return openNode(nx,ny,nz);
            } else if (determinant == 3) {
                if (isBlocked(x, ny, nz ) || isBlocked(nx, y , nz) || isBlocked(nx, ny, z)) return openNode(nx,ny,nz);
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

    private static final float playerWidth = 0.3f;
    public boolean isBlocked(int x,int y, int z) {
        if (x < minx || z < minz || x >= maxx || z >= maxz || y < miny || y >= maxy) return true;
        int dx = x - minx, dy = y - miny, dz = z - minz;
        int bitIdx = dx * leny * lenz + dy * lenz + dz;
        int location = bitIdx / 4;
        int bitStart = (2 * (bitIdx % 4));
        long theBit = arr[location];
        if (((theBit >> bitStart) & 0x2) > 0) return ((theBit >> bitStart) & 1) > 0;
            float wX = x / 2.0f, wY = y / 2.0f, wZ = z / 2.0f;


            AxisAlignedBB bb = AxisAlignedBB.fromBounds(wX - playerWidth, wY, wZ - playerWidth, wX + playerWidth, wY + 1.9f, wZ + playerWidth);

            int i = MathHelper.floor_double(bb.minX);
            int j = MathHelper.floor_double(bb.maxX + 1.0D);
            int k = MathHelper.floor_double(bb.minY);
            int l = MathHelper.floor_double(bb.maxY + 1.0D);
            int i1 = MathHelper.floor_double(bb.minZ);
            int j1 = MathHelper.floor_double(bb.maxZ + 1.0D);
            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

            List<AxisAlignedBB> list = new ArrayList<>();
            for (int k1 = i; k1 < j; ++k1) {
                for (int l1 = i1; l1 < j1; ++l1) {
                    for (int i2 = k - 1; i2 < l; ++i2) {
                        blockPos.set(k1, i2, l1);
                        IBlockState iblockstate1 = world.getBlockState(blockPos);
                        Block b = iblockstate1.getBlock();
                        if (!b.getMaterial().blocksMovement())continue;
                        if (b.isFullCube() && i2 == k-1) continue;
                        if (b.isFullCube()) {
                            theBit |= (3L << bitStart);
                            arr[location] = theBit;
                            return true;
                        }
                        try {
                            b.addCollisionBoxesToList(world, blockPos, iblockstate1, bb, list, null);
                        } catch (Exception e) {
                            return true;
                        }
                        if (list.size() > 0) {
                            theBit |= (3L << bitStart);
                            arr[location] = theBit;
                            return true;
                        }
                    }
                }
            }
        theBit |= 2L << bitStart;
        arr[location] = theBit;
        return false;
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
