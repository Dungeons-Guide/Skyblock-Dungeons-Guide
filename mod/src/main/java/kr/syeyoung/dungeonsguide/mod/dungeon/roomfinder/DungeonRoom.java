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
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonBreakableWall;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonRoomDoor;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonTomb;
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
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.ProcessorFactory;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.RoomProcessorGenerator;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.FeaturePathfindSettings;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.FeaturePathfindStrategy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;

@Getter
public class DungeonRoom implements IPathfindWorld {
    private final Set<Point> unitPoints;
    private final short shape;
    private final byte color;

    @Getter
    private int blockUpdateId = 0;

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

    @Setter
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
//                if (!canAccessAbsolute(new BlockPos(x * 16+8,0, z*16+8))) {
//                    continue;
//                } just don't check, it causes more problems
                Chunk c = getContext().getWorld().getChunkFromChunkCoords(x,z);
                if (c.isEmpty()) {
                    ChatTransmitter.sendDebugChat("Chunk not loaded: "+x+"/"+z);
                    throw new IllegalStateException("Chunk not loaded: "+x+"/"+z);
                }
                boolean nonNull = false;
                for (ExtendedBlockStorage extendedBlockStorage : c.getBlockStorageArray()) {
                    if (extendedBlockStorage != null) {
                        nonNull = true;
                        break;
                    }
                }
                if (!nonNull) {
                    ChatTransmitter.sendDebugChat("Chunk not loaded: "+x+"/"+z);
                    throw new IllegalStateException("Chunk not loaded: "+x+"/"+z);
                }
            }
        }

        this.chunkCache = new EditableChunkCache(getContext().getWorld(), min.add(-3, 0, -3), max.add(3,0,3), 0);
        CachedWorld cachedWorld =  new CachedWorld(chunkCache);

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

    private final Map<Vec3, WeakReference<PathfinderExecutor>> activePathfind = new HashMap<>();

    public PathfinderExecutor createEntityPathTo(BoundingBox pos) {
        FeaturePathfindStrategy.PathfindStrategy pathfindStrategy = FeatureRegistry.SECRET_PATHFIND_STRATEGY.getPathfindStrat();
//        if (activePathfind.containsKey(pos.center())) {
//            WeakReference<PathfinderExecutor> executorWeakReference = activePathfind.get(pos.center());
//            PathfinderExecutor executor = executorWeakReference.get();
//            if (executor != null) {
//                return executor;
//            }
//        }
        PathfinderExecutor executor;
        if (pathfindStrategy == FeaturePathfindStrategy.PathfindStrategy.A_STAR_FINE_GRID_SMART) {
            executor = new PathfinderExecutor(new FineGridStonkingBFS(algorithmSettings), pos, this);
        } else {
            return  null;
        }
        activePathfind.put(pos.center(), new WeakReference<>(executor));
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




        minx = min.getX() * 2 + 2; miny = 0; minz = min.getZ() * 2 + 2;
        maxx = max.getX() * 2 + 2; maxy = 255 * 2 + 2; maxz = max.getZ() * 2 + 2;

        lenx = maxx - minx;
        leny = maxy - miny;
        lenz = maxz - minz;

        whole = new BitStorage(lenx, leny, lenz, CollisionState.BITS); // plus 1 , because I don't wanna do floating point op for dividing and ceiling

        this.doorsAndStates = doorsAndStates;
        tryRematch();

        algorithmSettings = context.getAlgorithmSettings();
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


                    for (DungeonMechanic value : getMechanics().values()) {
                        if (value instanceof DungeonTomb) {
                            for (OffsetPoint offsetPoint : ((DungeonTomb) value).blockedPoints()) {
                                poses.add(offsetPoint.getBlockPos(this));
                            }
                        } else if (value instanceof DungeonBreakableWall) {
                            for (OffsetPoint offsetPoint : ((DungeonBreakableWall) value).blockedPoints()) {
                                poses.add(offsetPoint.getBlockPos(this));
                            }
                        }
                    }
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

    public Vec3 getRelativeVec3At(double x, double y, double z) {
        Vec3 pos = new Vec3(x,y,z).addVector(min.getX(),min.getY(),min.getZ());
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
        DungeonRoomScaffoldParser mapProcessor = this.context.getScaffoldParser();
        Point roomPt = mapProcessor.getDungeonMapLayout().worldPointToRoomPoint(pos);
        roomPt.translate(-minRoomPt.x, -minRoomPt.y);
        if (roomPt.x < 0 || roomPt.y < 0 || roomPt.x >= 4 || roomPt.y >= 4) return false;

        return (shape >>(roomPt.y *4 +roomPt.x) & 0x1) > 0;
    }
    public boolean canAccessRelative(int x, int z) {
        return  x>= 0 && z >= 0 && (shape >>((z/32) *4 +(x/32)) & 0x1) > 0;
    }




    BitStorage oneLayer, whole;
    // These values are doubled
    private final int minx;
    private final int miny;
    private final int minz;
    private final int maxx;
    private final int maxy;
    private final int maxz;
    private final int lenx, leny, lenz;
    private static final float playerWidth = 0.25f;

    private FeaturePathfindSettings.AlgorithmSettings algorithmSettings;


    private boolean isNoInstaBreak(IBlockState iBlockState, BlockPos pos) {
        Block b = iBlockState.getBlock();
        if (b == Blocks.air) return false;
        if (b.getBlockHardness(getCachedWorld(), pos) < 0) {
            return true;
        } else if (algorithmSettings.getPickaxeSpeed() > 0 &&
                ((algorithmSettings.getPickaxe().canHarvestBlock(b) &&
                b.getBlockHardness(getCachedWorld(), pos) <= algorithmSettings.getPickaxeSpeed() / 30.0) ||
                (b.getBlockHardness(getCachedWorld(), pos) <= algorithmSettings.getPickaxeSpeed() / 100.0))
        ) {
        } else if (algorithmSettings.getShovelSpeed() > 0
                && b.isToolEffective("shovel", iBlockState)
                && b.getBlockHardness(getCachedWorld(), pos) <= algorithmSettings.getShovelSpeed()) {
        } else if (algorithmSettings.getAxeSpeed() > 0 && b.isToolEffective("axe", iBlockState) && b.getBlockHardness(getCachedWorld(), pos) <= algorithmSettings.getAxeSpeed()) {
        } else {
            return true;
        }
        return false;
    }
    public boolean isInstabreak(int x, int y, int z) {
        if (x < minx || z < minz || x >= maxx || z >= maxz || y < miny || y+4 >= maxy) return false;
        if (x%2 != 0 && z%2 != 0) return false;

        BlockPos pos = new BlockPos(x/2,y/2,z/2);
        IBlockState blockState = getCachedWorld().getBlockState(pos);
        return !isNoInstaBreak(blockState, pos);
    }

    private HashSet<BlockPos> poses = new HashSet<>();

    private CollisionState calculateIsBlocked(int x, int y, int z) {
        if (x < minx || z < minz || x >= maxx || z >= maxz || y < miny || y+4 >= maxy) return CollisionState.BLOCKED;

        float wX = x / 2.0f, wY = y / 2.0f, wZ = z / 2.0f;

        AxisAlignedBB bb = AxisAlignedBB
                .fromBounds(wX - playerWidth, wY+0.06251, wZ - playerWidth,
                        wX + playerWidth, wY +0.06251 + 1.8, wZ + playerWidth);
        AxisAlignedBB pearlTest = AxisAlignedBB.fromBounds(
                wX - 0.5, wY - 0.5, wZ - 0.5, wX + 0.5, wY + 0.5, wZ+0.5
        );

        int minX = MathHelper.floor_double(bb.minX);
        int maxX = MathHelper.floor_double(bb.maxX + 1.0D);
        int minY = MathHelper.floor_double(bb.minY);
        int maxY = MathHelper.floor_double(bb.maxY + 1.0D);
        int minZ = MathHelper.floor_double(bb.minZ);
        int maxZ = MathHelper.floor_double(bb.maxZ + 1.0D);

        AxisAlignedBB testBox = bb.offset(0, -0.5, 0);
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        List<AxisAlignedBB> list = new ArrayList<>();
        List<AxisAlignedBB> list2 = new ArrayList<>();
        List<AxisAlignedBB> pearlList = new ArrayList<>();
        int size = 0;

//        boolean
        boolean stairs = false;
        boolean superboom = false;
        int notstonkable = 0;
        for (int k1 = minX; k1 < maxX; ++k1) {
            for (int l1 = minZ; l1 < maxZ; ++l1) {
                label: for (int i2 = minY-1; i2 < maxY; ++i2) {
                    blockPos.set(k1, i2, l1);


                    IBlockState state = getCachedWorld().getBlockState(blockPos);
                    Block block = state.getBlock();
                    block.addCollisionBoxesToList(
                            getCachedWorld(), blockPos, state, testBox, list, null
                    );
                    block.addCollisionBoxesToList(
                            getCachedWorld(), blockPos, state, bb, list2, null
                    );
                    block.addCollisionBoxesToList(
                            getCachedWorld(), blockPos, state, pearlTest, pearlList, null
                    );


                    if (list2.size() != size) {
                        // collision!!

                        if (poses.contains(blockPos)) {
                            for (int i = 0; i < Math.max(0, list2.size() - size); i++)
                                list2.remove(size);
                            superboom = true;
                            continue label;
                        }

                        if (isNoInstaBreak(state, blockPos)) {
                            if (i2 == maxY - 1 && (state.getBlock() != Blocks.iron_bars && !(state.getBlock() instanceof BlockFence))) {
                                // head level no break
                                notstonkable = 99;
                            } else {
                                notstonkable++;
                            }
                            if (state.getBlock() == Blocks.bedrock) {
                                notstonkable = 99;
                            }
                        }

                    }
                    size = list2.size();
                    if (block instanceof BlockStairs && i2 != minY - 1) {
                        stairs = true;
                    }
                }
            }
        }
        boolean isOnGround = list.stream().anyMatch(a -> a.maxY <= bb.minY);
        boolean blocked = !list2.isEmpty();

        // weirdest thing ever check.
        list2.clear();
        size = 0;

        if (!blocked && (x%2 == 0) != (z%2 == 0) && y %2 == 0 && isOnGround) {
            boolean stairFloor = false;
            boolean elligible = false;
            for (int k1 = minX; k1 < maxX; ++k1) {
                for (int l1 = minZ; l1 < maxZ; ++l1) {
                    blockPos.set(k1, minY - 1, l1);

                    IBlockState state = getCachedWorld().getBlockState(blockPos);
                    Block block = state.getBlock();

                    block.addCollisionBoxesToList(
                            getCachedWorld(), blockPos, state, testBox, list2, null
                    );
                    if (size != list2.size()) {
                        elligible = true;
                    } else if (block instanceof BlockStairs) {
                        stairFloor = true;
                    }
                    size = list2.size();
                }
            }
            if (elligible && stairFloor) {
                return CollisionState.ENDERCHEST;
            }
        }

        if (!blocked) { // I'm on ground
            if (superboom) {
                if (isOnGround) {
                    return CollisionState.SUPERBOOMABLE_GROUND;
                } else {
                    return CollisionState.SUPERBOOMABLE_AIR;
                }
            }
            if (stairs && isOnGround) {
                return CollisionState.STAIR;
            }

            if (isOnGround) {
                return CollisionState.ONGROUND;
            } else {
                return CollisionState.ONAIR;
            }
        } else {
            double intersectArea = pearlList.stream().map(a -> {
                double miX = Math.max(a.minX, pearlTest.minX);
                double miY = Math.max(a.minY, pearlTest.minY);
                double miZ = Math.max(a.minZ, pearlTest.minZ);
                double maX = Math.min(a.maxX, pearlTest.maxX);
                double maY = Math.min(a.maxY, pearlTest.maxY);
                double maZ = Math.min(a.maxZ, pearlTest.maxZ);
                return (maX - miX) * (maY - miY) * (maZ - miZ);
            }).reduce(0.0, Double::sum);
            boolean pearlable =  0 < intersectArea && intersectArea < 0.9 && (x%2 != 0 && z%2 != 0);

            // from here, blocked = true.
            if (notstonkable > 2) {
                if (!isOnGround) {
                    return pearlable ? CollisionState.BLOCKED_PEARLTARGET : CollisionState.BLOCKED;
                } else {
                    return pearlable ? CollisionState.BLOCKED_PEARLTARGET_GROUND : CollisionState.BLOCKED_GROUND;
                }
            }

            if (!isOnGround) {
                return pearlable ? CollisionState.STONKING_PEARLTARGET_AIR : CollisionState.STONKING_AIR;
            } else {
                return pearlable ? CollisionState.STONKING_PEARLTARGET : CollisionState.STONKING;
            }
        }
    }

    @AllArgsConstructor @Getter
    public enum CollisionState {
        ONAIR(true, false, false, false, false, new Color(0x3300FF00, true)),
        ONGROUND(true, false, false, true , false, new Color(0x33007700, true)),
        SUPERBOOMABLE_GROUND(true, false, false, true, false, new Color(0x33007777, true)),
        SUPERBOOMABLE_AIR(true, false, false, false, false, new Color(0x3300FFFF, true)),
        STAIR(true, true, false, true, false, new Color(0x33FFFF00, true)), // can't enter stonking while flying, I tried, it's so hard.
        ENDERCHEST(true, true, false, true, false, new Color(0x33FFFF00, true)),
        STONKING(true, true, true, true, false, new Color(0x33000077, true)),
        STONKING_AIR(true, true, true, false, false, new Color(0x330000FF, true)),
        STONKING_PEARLTARGET(true, true, true, true, true, new Color(0x33770077, true)),
        STONKING_PEARLTARGET_AIR(true, true, true, false, true, new Color(0x33FF00FF, true)),
        BLOCKED(false, true, true, false, false, new Color(0x33FF0000, true)),
        BLOCKED_GROUND(false, true, true, true, false, new Color(0x33FF0000, true)),
        BLOCKED_PEARLTARGET(false, true, true, false, true, new Color(0x33FFFFFF, true)),
        BLOCKED_PEARLTARGET_GROUND(false, true, true, true, true, new Color(0x33777777, true)),; // 3 bytes per.... ehmmm...


        private boolean canGo;
        private boolean isClip;
        private boolean blocked;
        private boolean onGround;
        private boolean pearltarget;
        private Color color;

        public static final int BITS = (int) Math.ceil(Math.log(CollisionState.values().length ) / Math.log(2));
        public static final CollisionState[] VALUES = CollisionState.values();
    }


    @Override
    public IBlockState getActualBlock(int x, int y, int z) {
        return getCachedWorld().getBlockState(new BlockPos(x,y,z));
    }

    public CollisionState getBlock(int x, int y, int z) {
        if (x < minx || z < minz || x >= maxx || z >= maxz || y < miny || y >= maxy) return CollisionState.BLOCKED;
        int dx = x - minx, dy = y - miny, dz = z - minz;
        int data = whole.read(dx, dy, dz);
        if (data != 0) return CollisionState.VALUES[data];
        CollisionState val = calculateIsBlocked(x, y, z);
        whole.store(dx,dy,dz, val.ordinal());
        return val;
    }


    @Override
    public int getXwidth() {
        return lenx;
    }

    @Override
    public int getYwidth() {
        return leny;
    }

    @Override
    public int getZwidth() {
        return lenz;
    }

    @Override
    public int getMinX() {
        return minx;
    }

    @Override
    public int getMinY() {
        return miny;
    }

    @Override
    public int getMinZ() {
        return minz;
    }


    public void resetBlock(BlockPos pos) { // I think it can be optimize due to how it is saved in arr
        for (int x = -2; x <= 2; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -2; z <= 2; z++) {
                    resetBlock2(pos.getX()*2 + x, pos.getY()*2 + y, pos.getZ()*2 + z);
                }
            }
        }

    }
    private void resetBlock2(int x, int y, int z) {
        if (x < minx || z < minz || x >= maxx || z >= maxz || y < miny || y >= maxy) return;
        int dx = x - minx, dy = y - miny, dz = z - minz;
        if (whole.store(dx, dy, dz, calculateIsBlocked(x,y,z).ordinal())) {
            blockUpdateId++;
        }
    }

    public void chunkUpdate(int cx, int cz) {
        if (!canAccessAbsolute(new BlockPos(cx * 16+8,0, cz*16+8))) {
            return;
        }
//        ChatTransmitter.sendDebugChat("UPDATING!!! "+cx+"/"+cz +" from "+dungeonRoomInfo.getName());
        chunkCache.updateChunk(new BlockPos(cx*16+8, 0, cz*16+8));

        for (int x = 0; x < 16; x ++) { // fix pf not going through big block updates
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 255; y++) {
                    resetBlock2(cx * 16 + x, y, cz * 16 + z);
                }
            }
        }
    }
}
