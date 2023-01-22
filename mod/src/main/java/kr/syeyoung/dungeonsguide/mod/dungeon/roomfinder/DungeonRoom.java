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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonRoomDoor;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder.DungeonDoor;
import kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder.EDungeonDoorType;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.SerializableBlockPos;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.impl.DungeonRoomDiscoverEvent;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.impl.DungeonRoomMatchEvent;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.impl.DungeonStateChangeEvent;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonRoomScaffoldParser;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.ProcessorFactory;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.RoomProcessorGenerator;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.FeaturePathfindStrategy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.*;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;

@Getter
public class DungeonRoom {
    private final Set<Point> unitPoints;
    private final short shape;
    private final byte color;

    private final BlockPos min;
    private final BlockPos max;
    private final Point minRoomPt;

    private final DungeonContext context;

    private final List<DungeonDoor> doors = new ArrayList<>();

    private DungeonRoomInfo dungeonRoomInfo;

    private final int unitWidth; // X
    private final int unitHeight; // Z

    @Setter
    private int totalSecrets = -1;
    private RoomState currentState = RoomState.DISCOVERED;

    private Map<String, DungeonMechanic> cached = null;

    @Getter
    private final World cachedWorld;
    public Map<String, DungeonMechanic> getMechanics() {
        if (cached == null || EditingContext.getEditingContext() != null) {
            cached = new HashMap<>(dungeonRoomInfo.getMechanics());
            int index = 0;
            for (DungeonDoor door : doors) {
                if (door.getType().isExist()) cached.put((door.getType().getName())+"-"+(++index), new DungeonRoomDoor(this, door));
            }
        }
        return cached;
    }

    public void setCurrentState(RoomState currentState) {
        context.getRecorder().createEvent(new DungeonStateChangeEvent(unitPoints.iterator().next(),
                dungeonRoomInfo == null ? null : dungeonRoomInfo.getName(), this.currentState, currentState));
        this.currentState = currentState;
    }

    private final Map<BlockPos, AStarFineGrid> activeBetterAStar = new HashMap<>();
    private final Map<BlockPos, AStarCornerCut> activeBetterAStarCornerCut = new HashMap<>();
    private final Map<BlockPos, ThetaStar> activeThetaStar = new HashMap<>();

    public ScheduledFuture<List<Vec3>> createEntityPathTo(IBlockAccess blockaccess, Entity entityIn, BlockPos targetPos, float dist, int timeout) {
        FeaturePathfindStrategy.PathfindStrategy pathfindStrategy = FeatureRegistry.SECRET_PATHFIND_STRATEGY.getPathfindStrat();
        if (pathfindStrategy == FeaturePathfindStrategy.PathfindStrategy.JPS_LEGACY) {
            return asyncPathFinder.schedule(() -> {
                BlockPos min = new BlockPos(getMin().getX(), 0, getMin().getZ());
                BlockPos max=  new BlockPos(getMax().getX(), 255, getMax().getZ());
                JPSPathfinder pathFinder = new JPSPathfinder(this);
                pathFinder.pathfind(entityIn.getPositionVector(), new Vec3(targetPos).addVector(0.5, 0.5, 0.5), 1.5f,timeout);
                return pathFinder.getRoute();
            }, 0, TimeUnit.MILLISECONDS);
        } else if (pathfindStrategy == FeaturePathfindStrategy.PathfindStrategy.A_STAR_FINE_GRID) {
            return asyncPathFinder.schedule(() -> {
                AStarFineGrid pathFinder =
                        activeBetterAStar.computeIfAbsent(targetPos, (pos) -> new AStarFineGrid(this, new Vec3(pos.getX(), pos.getY(), pos.getZ()).addVector(0.5, 0.5, 0.5)));
                pathFinder.pathfind(entityIn.getPositionVector(),timeout);
                return pathFinder.getRoute();
            }, 0, TimeUnit.MILLISECONDS);
        }else if (pathfindStrategy == FeaturePathfindStrategy.PathfindStrategy.A_STAR_DIAGONAL) {
            return asyncPathFinder.schedule(() -> {
                AStarCornerCut pathFinder =
                        activeBetterAStarCornerCut.computeIfAbsent(targetPos, (pos) -> new AStarCornerCut(this, new Vec3(pos.getX(), pos.getY(), pos.getZ()).addVector(0.5, 0.5, 0.5)));
                pathFinder.pathfind(entityIn.getPositionVector(),timeout);
                return pathFinder.getRoute();
            }, 0, TimeUnit.MILLISECONDS);
        } else if (pathfindStrategy == FeaturePathfindStrategy.PathfindStrategy.THETA_STAR) {
            return asyncPathFinder.schedule(() -> {
                ThetaStar pathFinder =
                        activeThetaStar.computeIfAbsent(targetPos, (pos) -> new ThetaStar(this, new Vec3(pos.getX(), pos.getY(), pos.getZ()).addVector(0.5, 0.5, 0.5)));
                pathFinder.pathfind(entityIn.getPositionVector(),timeout);
                return pathFinder.getRoute();
            }, 0, TimeUnit.MILLISECONDS);
        } else {
            return asyncPathFinder.schedule(() -> {
                PathFinder pathFinder = new PathFinder(nodeProcessorDungeonRoom);
                PathEntity latest = pathFinder.createEntityPathTo(blockaccess, entityIn, targetPos, dist);
                if (latest != null) {
                    List<Vec3> poses = new ArrayList<>();
                    for (int i = 0; i < latest.getCurrentPathLength(); i++) {
                        PathPoint pathPoint = latest.getPathPointFromIndex(i);
                        poses.add(new Vec3(getMin().add(pathPoint.xCoord, pathPoint.yCoord, pathPoint.zCoord)).addVector(0.5,0.5,0.5));
                    }
                    return poses;
                }
                return new ArrayList<>();
            }, 0, TimeUnit.MILLISECONDS);
        }
    }
    private static final ExecutorService roomMatcherThread = Executors.newSingleThreadExecutor( DungeonsGuide.THREAD_FACTORY);

    private static final ScheduledExecutorService asyncPathFinder = Executors.newScheduledThreadPool(4, DungeonsGuide.THREAD_FACTORY);
    @Getter
    private final NodeProcessorDungeonRoom nodeProcessorDungeonRoom;

    @Getter
    private final Map<String, Object> roomContext = new HashMap<>();

    @AllArgsConstructor
    @Getter
    public enum RoomState {
        DISCOVERED(0), COMPLETE_WITHOUT_SECRETS(0), FINISHED(0), FAILED(-14);
        private final int scoreModifier;
    }

    private RoomProcessor roomProcessor;

    public DungeonRoom(Set<Point> points, short shape, byte color, BlockPos min, BlockPos max, DungeonContext context, Set<Tuple<Vector2d, EDungeonDoorType>> doorsAndStates) {
        this.unitPoints = points;
        this.shape = shape;
        this.color = color;
        this.min = min;
        this.max = max;
        this.context = context;

        minRoomPt = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (Point pt : unitPoints) {
            if (pt.x < minRoomPt.x) minRoomPt.x = pt.x;
            if (pt.y < minRoomPt.y) minRoomPt.y = pt.y;
        }
        unitWidth = (int) Math.ceil(max.getX() - min.getX() / 32.0);
        unitHeight = (int) Math.ceil(max.getZ() - min.getZ() / 32.0);


        ChunkCache chunkCache = new ChunkCache(getContext().getWorld(), min.add(-3, 0, -3), max.add(3,0,3), 0);
        this.cachedWorld =  new CachedWorld(chunkCache);



        minx = min.getX() * 2; miny = 0; minz = min.getZ() * 2;
        maxx = max.getX() * 2 + 2; maxy = 255 * 2 + 2; maxz = max.getZ() * 2 + 2;

        lenx = maxx - minx;
        leny = maxy - miny;
        lenz = maxz - minz;
        arr = new long[lenx *leny * lenz * 2 / 8];;

        buildDoors(doorsAndStates);
        nodeProcessorDungeonRoom = new NodeProcessorDungeonRoom(this);

        roomMatcherThread.submit(() -> {
            try {
                matchRoomAndSetupRoomProcessor();
                matched = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    private volatile boolean matched = false;
    private volatile boolean matching = false;

    public void tryRematch() {
        if (matched) return;
        if (matching )return;
        matching = true;
        roomMatcherThread.submit(() -> {
            try {
                matchRoomAndSetupRoomProcessor();
                matched = true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                matching = false;
            }
        });
    }
    private void matchRoomAndSetupRoomProcessor() {
        buildRoom();
        updateRoomProcessor();
    }

    private static final Set<Vector2d> directions = Sets.newHashSet(new Vector2d(0,16), new Vector2d(0, -16), new Vector2d(16, 0), new Vector2d(-16 , 0));

    private void buildDoors(Set<Tuple<Vector2d, EDungeonDoorType>> doorsAndStates) {
        Set<Tuple<BlockPos, EDungeonDoorType>> positions = new HashSet<>();
        BlockPos pos = context.getScaffoldParser().getDungeonMapLayout().roomPointToWorldPoint(minRoomPt).add(16,0,16);
        for (Tuple<Vector2d, EDungeonDoorType> doorsAndState : doorsAndStates) {
            Vector2d vector2d = doorsAndState.getFirst();
            BlockPos neu = pos.add(vector2d.x * 32, 0, vector2d.y * 32);
            positions.add(new Tuple<>(neu, doorsAndState.getSecond()));
        }

        for (Tuple<BlockPos, EDungeonDoorType> door : positions) {
            doors.add(new DungeonDoor(context.getWorld(), door.getFirst(), door.getSecond()));
        }
    }

    private RoomMatcher roomMatcher = null;
    private void buildRoom() {
        if (roomMatcher == null)
            roomMatcher = new RoomMatcher(this);
        DungeonRoomInfo dungeonRoomInfo = roomMatcher.match();
        if (dungeonRoomInfo == null) {
            dungeonRoomInfo = roomMatcher.createNew();
            if (color == 18) dungeonRoomInfo.setProcessorId("bossroom");
        } else {
            context.getRecorder().createEvent(new DungeonRoomMatchEvent(getUnitPoints().iterator().next(),
                    getRoomMatcher().getRotation(), new SerializableBlockPos(getMin()),
                    new SerializableBlockPos(getMax()), getShape(), getColor(),
                    dungeonRoomInfo.getUuid(),
                    dungeonRoomInfo.getName(),
                    dungeonRoomInfo.getProcessorId()));
        }
        ChatTransmitter.sendDebugChat(new ChatComponentText("New Map matched! shape: " + getShape() + " color: " +getColor() + " unitPos: " + unitPoints.iterator().next().x + "," + unitPoints.iterator().next().y));
        ChatTransmitter.sendDebugChat(new ChatComponentText("New Map matched! mapMin: " + getMin() + " mapMx: " + getMax()));
        ChatTransmitter.sendDebugChat(new ChatComponentText("New Map matched! id: " + dungeonRoomInfo.getUuid() + " name: " + dungeonRoomInfo.getName() +" proc: "+dungeonRoomInfo.getProcessorId()));


        this.dungeonRoomInfo = dungeonRoomInfo;
        totalSecrets = dungeonRoomInfo.getTotalSecrets();
    }

    public void updateRoomProcessor() {
        RoomProcessorGenerator roomProcessorGenerator = ProcessorFactory.getRoomProcessorGenerator(dungeonRoomInfo.getProcessorId());
        if (roomProcessorGenerator == null) this.roomProcessor = null;
        else this.roomProcessor = roomProcessorGenerator.createNew(this);
    }

    public Block getAbsoluteBlockAt(int x, int y, int z) {
        // validate x y z's
        BlockPos pos = new BlockPos(x,y,z);
        if (canAccessAbsolute(pos)) {
            return cachedWorld.getBlockState(pos).getBlock();
        }
        return null;
    }

    public Block getRelativeBlockAt(int x, int y, int z) {
        // validate x y z's
        if (canAccessRelative(x,z)) {
            BlockPos pos = new BlockPos(x,y,z).add(min.getX(),min.getY(),min.getZ());
            return cachedWorld.getBlockState(pos).getBlock();
        }
        return null;
    }

    public BlockPos getRelativeBlockPosAt(int x, int y, int z) {
            BlockPos pos = new BlockPos(x,y,z).add(min.getX(),min.getY(),min.getZ());
            return pos;
    }

    public int getRelativeBlockDataAt(int x, int y, int z) {
        // validate x y z's
        if (canAccessRelative(x,z)) {
            BlockPos pos = new BlockPos(x,y,z).add(min.getX(),min.getY(),min.getZ());
            IBlockState iBlockState = cachedWorld.getBlockState(pos);
            return iBlockState.getBlock().getMetaFromState(iBlockState);
        }
        return -1;
    }

    public int getAbsoluteBlockDataAt(int x, int y, int z) {
        // validate x y z's
        BlockPos pos = new BlockPos(x,y,z);
        if (canAccessAbsolute(pos)) {
            IBlockState iBlockState = cachedWorld.getBlockState(pos);
            return iBlockState.getBlock().getMetaFromState(iBlockState);
        }
        return -1;
    }

    public boolean canAccessAbsolute(BlockPos pos) {
        DungeonRoomScaffoldParser mapProcessor = this.context.getScaffoldParser();
        Point roomPt = mapProcessor.getDungeonMapLayout().worldPointToRoomPoint(pos);
        roomPt.translate(-minRoomPt.x, -minRoomPt.y);

        return (shape >>(roomPt.y *4 +roomPt.x) & 0x1) > 0;
    }
    public boolean canAccessRelative(int x, int z) {
        return  x>= 0 && z >= 0 && (shape >>((z/32) *4 +(x/32)) & 0x1) > 0;
    }



    long[] arr;
    // These values are doubled
    private final int minx;
    private final int miny;
    private final int minz;
    private final int maxx;
    private final int maxy;
    private final int maxz;
    private final int lenx, leny, lenz;
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
                    IBlockState iblockstate1 = cachedWorld.getBlockState(blockPos);
                    Block b = iblockstate1.getBlock();
                    if (!b.getMaterial().blocksMovement())continue;
                    if (b.isFullCube() && i2 == k-1) continue;
                    if (iblockstate1.equals( NodeProcessorDungeonRoom.preBuilt)) continue;
                    if (b.isFullCube()) {
                        theBit |= (3L << bitStart);
                        arr[location] = theBit;
                        return true;
                    }
                    try {
                        b.addCollisionBoxesToList(cachedWorld, blockPos, iblockstate1, bb, list, null);
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


    public void resetBlock(BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    resetBlock(pos.getX()*2 + x, pos.getY()*2 + y, pos.getZ()*2 + z);
                }
            }
        }
    }
    private void resetBlock(int x, int y, int z) {
        if (x < minx || z < minz || x >= maxx || z >= maxz || y < miny || y >= maxy) return;
        int dx = x - minx, dy = y - miny, dz = z - minz;
        int bitIdx = dx * leny * lenz + dy * lenz + dz;
        int location = bitIdx / 4;
        arr[location] = 0;
    }
}
