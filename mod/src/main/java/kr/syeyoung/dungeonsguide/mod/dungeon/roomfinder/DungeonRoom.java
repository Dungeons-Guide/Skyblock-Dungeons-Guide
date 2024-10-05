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
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonRoomDoor;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder.DungeonDoor;
import kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder.EDungeonDoorType;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.SerializableBlockPos;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.impl.DungeonRoomMatchEvent;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.impl.DungeonStateChangeEvent;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonRoomScaffoldParser;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.AStarCornerCut;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.AStarFineGrid;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.PathfinderExecutor;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.ThetaStar;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.ProcessorFactory;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.RoomProcessorGenerator;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.FeaturePathfindStrategy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.*;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.MinecraftForge;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.lang.ref.WeakReference;
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

    private World cachedWorld;
    private EditableChunkCache chunkCache;

    public World getCachedWorld() {
        if (this.cachedWorld != null) return cachedWorld;


        int minZChunk = getMin().getZ() >> 4;
        int minXChunk = getMin().getX() >> 4;
        int maxZChunk = getMax().getZ() >> 4;
        int maxXChunk = getMax().getX() >> 4;

        for (int z = minZChunk; z <= maxZChunk; z++) {
            for (int x = minXChunk; x <= maxXChunk; x++) {
                if (!canAccessAbsolute(new BlockPos(x * 16,0, z*16)) && !canAccessAbsolute(new BlockPos(x * 16+15,0, z*16+15))
                && !canAccessAbsolute(new BlockPos(x * 16+15,0, z*16)) && !canAccessAbsolute(new BlockPos(x * 16,0, z*16+15))) {
                    continue;
                }
                Chunk c = getContext().getWorld().getChunkFromChunkCoords(x,z);
                if (c.isEmpty()) {
                    throw new IllegalStateException("Chunk not loaded: "+x+"/"+z);
                }
                for (ExtendedBlockStorage extendedBlockStorage : c.getBlockStorageArray()) {
                    if (extendedBlockStorage == null) {
                        throw new IllegalStateException("Chunk not loaded: "+x+"/"+z);
                    }
                }
            }
        }

        this.chunkCache = new EditableChunkCache(getContext().getWorld(), min.add(-3, 0, -3), max.add(3,0,3), 0);
        CachedWorld cachedWorld =  new CachedWorld(chunkCache, context.getWorld().provider);

        return this.cachedWorld = cachedWorld;
    }
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

    private final Map<BlockPos, WeakReference<PathfinderExecutor>> activePathfind = new HashMap<>();

    public PathfinderExecutor createEntityPathTo(BlockPos pos) {
        FeaturePathfindStrategy.PathfindStrategy pathfindStrategy = FeatureRegistry.SECRET_PATHFIND_STRATEGY.getPathfindStrat();
        if (activePathfind.containsKey(pos)) {
            WeakReference<PathfinderExecutor> executorWeakReference = activePathfind.get(pos);
            PathfinderExecutor executor = executorWeakReference.get();
            if (executor != null) {
                return executor;
            }
        }
        PathfinderExecutor executor;
        if (pathfindStrategy == FeaturePathfindStrategy.PathfindStrategy.A_STAR_FINE_GRID) {
            executor = new PathfinderExecutor(new AStarFineGrid(), new Vec3(pos.getX(), pos.getY(), pos.getZ()).addVector(0.5, 0.5, 0.5), this);
        } else if (pathfindStrategy == FeaturePathfindStrategy.PathfindStrategy.A_STAR_DIAGONAL) {
            executor = new PathfinderExecutor(new AStarCornerCut(), new Vec3(pos.getX(), pos.getY(), pos.getZ()).addVector(0.5, 0.5, 0.5), this);
        } else if (pathfindStrategy == FeaturePathfindStrategy.PathfindStrategy.THETA_STAR) {
            executor = new PathfinderExecutor(new ThetaStar(), new Vec3(pos.getX(), pos.getY(), pos.getZ()).addVector(0.5, 0.5, 0.5), this);
        } else {
            return  null;
        }
        activePathfind.put(pos, new WeakReference<>(executor));
        context.getExecutors().add(new WeakReference<>(executor));
        return executor;
    }
    private static final ExecutorService roomMatcherThread = DungeonsGuide.getDungeonsGuide().registerExecutorService(Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setThreadFactory(DungeonsGuide.THREAD_FACTORY)
                    .setNameFormat("DG-RoomMatcher-%d").build()));

    @Getter
    private final Map<String, Object> roomContext = new HashMap<>();

    @AllArgsConstructor
    @Getter
    public enum RoomState {
        DISCOVERED(0), COMPLETE_WITHOUT_SECRETS(0), FINISHED(0), FAILED(-14);
        private final int scoreModifier;
    }

    private RoomProcessor roomProcessor;

    private Set<Tuple<Vector2d, EDungeonDoorType>> doorsAndStates;
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
        unitWidth = (int) Math.ceil((max.getX() - min.getX()) / 32.0);
        unitHeight = (int) Math.ceil((max.getZ() - min.getZ()) / 32.0);




        minx = min.getX() * 2; miny = 0; minz = min.getZ() * 2;
        maxx = max.getX() * 2 + 2; maxy = 255 * 2 + 2; maxz = max.getZ() * 2 + 2;

        lenx = maxx - minx;
        leny = maxy - miny;
        lenz = maxz - minz;
        arr = new long[lenx *leny * lenz * 2 / 8];;

        this.doorsAndStates = doorsAndStates;
        tryRematch();
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
                if (e.getMessage() == null || !e.getMessage().contains("Chunk not loaded")) {
                    FeatureCollectDiagnostics.queueSendLogAsync(e);
                    e.printStackTrace();
                }
            } finally {
                matching = false;
            }
        });
    }
    private void matchRoomAndSetupRoomProcessor() {
        getCachedWorld();
        buildRoom();
        buildDoors(doorsAndStates);

        Minecraft.getMinecraft().addScheduledTask(() -> {
            try {
                this.updateRoomProcessor();
            } catch (Exception e) {
                if (e.getMessage() == null || !e.getMessage().contains("Chunk not loaded")) {
                    FeatureCollectDiagnostics.queueSendLogAsync(e);
                    e.printStackTrace();
                }
            }
        });
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

        if (this.roomProcessor != null && this.roomProcessor.readGlobalChat()) {
            context.getGlobalRoomProcessors().add(this.roomProcessor);
        }
    }

    public Block getAbsoluteBlockAt(int x, int y, int z) {
        // validate x y z's
        BlockPos pos = new BlockPos(x,y,z);
        if (canAccessAbsolute(pos)) {
            return getCachedWorld().getBlockState(pos).getBlock();
        }
        return null;
    }

    public Block getRelativeBlockAt(int x, int y, int z) {
        // validate x y z's
        if (canAccessRelative(x,z)) {
            BlockPos pos = new BlockPos(x,y,z).add(min.getX(),min.getY(),min.getZ());
            return getCachedWorld().getBlockState(pos).getBlock();
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
            IBlockState iBlockState = getCachedWorld().getBlockState(pos);
            return iBlockState.getBlock().getMetaFromState(iBlockState);
        }
        return -1;
    }

    public int getAbsoluteBlockDataAt(int x, int y, int z) {
        // validate x y z's
        BlockPos pos = new BlockPos(x,y,z);
        if (canAccessAbsolute(pos)) {
            IBlockState iBlockState = getCachedWorld().getBlockState(pos);
            return iBlockState.getBlock().getMetaFromState(iBlockState);
        }
        return -1;
    }

    public boolean canAccessAbsolute(BlockPos pos) {
        return canAccessRelative(pos.getX() - this.min.getX(), pos.getZ() - this.min.getZ());
    }
    public boolean canAccessRelative(int x, int z) {
        if (x/32 >= 4 || z / 32 >= 4) return false;
        boolean firstCond =  x> 0 && z > 0 && (shape >>((z/32) *4 +(x/32)) & 0x1) > 0;
        boolean zCond = (shape >> ((z / 32) * 4 + (x / 32) - 1) & 0x1) > 0;
        boolean xCond = (shape >> ((z / 32) * 4 + (x / 32) - 4) & 0x1) > 0;
        if (x % 32 == 0 && z % 32 == 0) {
            return firstCond && (shape >>((z/32) *4 +(x/32) - 5) & 0x1) > 0
                    && xCond
                    && zCond;
        } else if (x % 32 == 0) {
            return firstCond && zCond;
        } else if (z % 32 == 0) {
            return firstCond && xCond;
        }

        return firstCond;
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

    private int calculateIsBlocked(int x, int y, int z) {
        if (x < minx || z < minz || x >= maxx || z >= maxz || y < miny || y >= maxy) return 3;
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
                    IBlockState iBlockState1 = getCachedWorld().getBlockState(blockPos);
                    Block b = iBlockState1.getBlock();
                    if (!b.getMaterial().blocksMovement())continue;
                    if (b.isFullCube() && i2 == k-1) continue;
                    if (iBlockState1.equals( NodeProcessorDungeonRoom.preBuilt)) continue;
                    if (b.isFullCube()) {
                        return 3;
                    }
                    try {
                        b.addCollisionBoxesToList(getCachedWorld(), blockPos, iBlockState1, bb, list, null);
                    } catch (Exception e) {
                        return 3;
                    }
                    if (list.size() > 0) {
                        return 3;
                    }
                }
            }
        }
        return 2;
    }
    public boolean isBlocked(int x,int y, int z) {
        if (x < minx || z < minz || x >= maxx || z >= maxz || y < miny || y >= maxy) return true;
        int dx = x - minx, dy = y - miny, dz = z - minz;
        int bitIdx = dx * leny * lenz + dy * lenz + dz;
        int location = bitIdx / 4;
        int bitStart = (2 * (bitIdx % 4));
        long theBit = arr[location];
        if (((theBit >> bitStart) & 0x2) > 0) return ((theBit >> bitStart) & 1) > 0;

        long val = calculateIsBlocked(x, y, z);
        theBit |= val << bitStart;
        arr[location] = theBit;
        return val == 3;
    }


    public void resetBlock(BlockPos pos) { // I think it can be optimize due to how it is saved in arr
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
        arr[location] = (arr[location] & ~(3L << (2 * (bitIdx % 4)))) | (long) calculateIsBlocked(x, y, z) << (2 * (bitIdx % 4));
    }

    public void chunkUpdate(int cx, int cz) {
        if (!chunkCache.isManaged(cx, cz)) {
            return;
        }
//        ChatTransmitter.sendDebugChat("UPDATING!!! "+cx+"/"+cz +" from "+dungeonRoomInfo.getName());
        chunkCache.updateChunk(new BlockPos(cx*16+8, 0, cz*16+8));

        for (int x = 0; x < 16; x ++) { // fix pf not going through big block updates
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 255; y++) {
                    resetBlock(cx * 16 + x, y, cz * 16 + z);
                }
            }
        }
    }
}
