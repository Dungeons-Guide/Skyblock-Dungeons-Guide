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
import net.minecraft.util.*;
import net.minecraft.world.ChunkCache;
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
public class DungeonRoom {
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

    private World cachedWorld;

    public World getCachedWorld() {
        if (this.cachedWorld != null) return cachedWorld;


        int minZChunk = getMin().getZ() >> 4;
        int minXChunk = getMin().getX() >> 4;
        int maxZChunk = getMax().getZ() >> 4;
        int maxXChunk = getMax().getX() >> 4;

        for (int z = minZChunk; z <= maxZChunk; z++) {
            for (int x = minXChunk; x <= maxXChunk; x++) {
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

        ChunkCache chunkCache = new ChunkCache(getContext().getWorld(), min.add(-3, 0, -3), max.add(3,0,3), 0);
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
        } else if (pathfindStrategy == FeaturePathfindStrategy.PathfindStrategy.A_STAR_FINE_GRID_SMART) {
            executor = new PathfinderExecutor(new AStarFineGridStonking(algorithmSettings), new Vec3(pos.getX(), pos.getY(), pos.getZ()).addVector(0.5, 0.5, 0.5), this);
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

        oneLayer = new BitStorage(lenx, leny, lenz, LayerNodeState.BITS);
        whole = new BitStorage(lenx, leny, lenz, NodeState.BITS); // plus 1 , because I don't wanna do floating point op for dividing and ceiling

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
        DungeonRoomScaffoldParser mapProcessor = this.context.getScaffoldParser();
        Point roomPt = mapProcessor.getDungeonMapLayout().worldPointToRoomPoint(pos);
        roomPt.translate(-minRoomPt.x, -minRoomPt.y);

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
    private static final float playerWidth = 0.3f;

    private FeaturePathfindSettings.AlgorithmSettings algorithmSettings;


    private boolean isNoInstaBreak(IBlockState iBlockState, BlockPos.MutableBlockPos pos) {
        Block b = iBlockState.getBlock();
        if (b.getBlockHardness(getCachedWorld(), pos) < 0) {
            return true;
        } else if (algorithmSettings.getPickaxeSpeed() > 0 && algorithmSettings.getPickaxe().canHarvestBlock(b) && b.getBlockHardness(getCachedWorld(), pos) <= algorithmSettings.getPickaxeSpeed()) {
        } else if (algorithmSettings.getShovelSpeed() > 0 && b.isToolEffective("shovel", iBlockState) && b.getBlockHardness(getCachedWorld(), pos) <= algorithmSettings.getShovelSpeed()) {
        } else if (algorithmSettings.getAxeSpeed() > 0 && b.isToolEffective("axe", iBlockState) && b.getBlockHardness(getCachedWorld(), pos) <= algorithmSettings.getAxeSpeed()) {
        } else {
            return true;
        }
        return false;
    }

    private LayerNodeState calculateOneLayerIsBlocked(int x, int y, int z) {
        if (x < minx || z < minz || x >= maxx || z >= maxz || y < miny || y >= maxy) return LayerNodeState.OUT_OF_DUNGEON;
        float wX = x / 2.0f, wY = y / 2.0f, wZ = z / 2.0f;


        AxisAlignedBB bb = AxisAlignedBB.fromBounds(wX - playerWidth, wY+0.06251, wZ - playerWidth, wX + playerWidth, wY + .49f, wZ + playerWidth);

        int minX = MathHelper.floor_double(bb.minX);
        int maxX = MathHelper.floor_double(bb.maxX + 1.0D);
        int minY = MathHelper.floor_double(bb.minY);
        int maxY = MathHelper.floor_double(bb.maxY + 1.0D);
        int minZ = MathHelper.floor_double(bb.minZ);
        int maxZ = MathHelper.floor_double(bb.maxZ + 1.0D);
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        List<AxisAlignedBB> list = new ArrayList<>();
        int blocked = 0;
        int nonInstamineCount = 0;

        int stairValid = 0;
        int fence = 0;
        boolean missedStair = false;
        for (int k1 = minX; k1 < maxX; ++k1) {
            for (int l1 = minZ; l1 < maxZ; ++l1) {
                for (int i2 = minY-1; i2 < maxY; ++i2) {
                    boolean blocked2 = false;
                    blockPos.set(k1, i2, l1);
                    IBlockState iBlockState1 = getCachedWorld().getBlockState(blockPos);
                    Block b = iBlockState1.getBlock();
                    if (!b.getMaterial().blocksMovement())continue;
                    if (!(b instanceof BlockWall || b instanceof BlockFence || b instanceof BlockFenceGate) && i2 == minY-1) continue;
                    if (iBlockState1.equals( NodeProcessorDungeonRoom.preBuilt)) continue;


                    if (b.isFullCube()) {
                        blocked2 = true;

                        if (b.getBlockHardness(getCachedWorld(), blockPos) < 0) {
                            return LayerNodeState.FORBIDDEN;
                        }
                    }

                    if (b instanceof BlockStairs) {
                        if (iBlockState1.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.BOTTOM  && y % 2 == 0 && (stairValid >= 0)) {
                            stairValid = 1;
                        } else if (iBlockState1.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.TOP && y % 2 == 1 && (stairValid >= 0)) {
                            stairValid = 1;
                        } else {
                            stairValid = -1;
                        }
                    }

                    try {
                        int prev = list.size();
                        b.addCollisionBoxesToList(getCachedWorld(), blockPos, iBlockState1, bb, list, null);
                        if (list.size() - prev > 0) {
                            blocked2 = true;
                        }

                        if (b instanceof BlockStairs && !blocked2) {
                            missedStair = true;
                        } else if (b instanceof BlockStairs) {
                            missedStair = false;
                        }
                    } catch (Exception e) {
                        blocked2 = true;
                    }


                    if (blocked2 && isNoInstaBreak(iBlockState1, blockPos) && i2 != minY -1) {
                        nonInstamineCount++;
                    }
                    if (blocked2) {
                        blocked++;
                    }
                    if (blocked2 && i2 == minY - 1) {
                        fence++;
                    }
                }
            }
        }

        if (blocked > 0) {
            if (nonInstamineCount >= 2) {
                if (x%2 == 0 && z%2 == 0) {
                    return LayerNodeState.FORBIDDEN;
                } else {
                    return LayerNodeState.BLOCKED_ONE_STONK;
                }
            }

            if (stairValid == 1 && fence == 0) {
                return nonInstamineCount == 0 ? LayerNodeState.ENTRANCE_STAIR_STONK : LayerNodeState.ENTRANCE_STAIR_BLOCK;
            }

            if (nonInstamineCount == 1){
                if (missedStair)
                    return LayerNodeState.BLOCKED_ONE_STONK_STAIR_MID;
                else
                    return LayerNodeState.BLOCKED_ONE_STONK;
            } else if (nonInstamineCount == 0) {
                if (missedStair)
                    return LayerNodeState.STONKABLE_STAIR_MID;
                else
                    return LayerNodeState.STONKABLE;
            }
        }


        return LayerNodeState.OPEN;
    }

    private NodeState calculateIsBlocked(int x, int y, int z) {
        if (x < minx || z < minz || x >= maxx || z >= maxz || y < miny || y+4 >= maxy) return NodeState.OUT_OF_DUNGEON;

        LayerNodeState bottom = getLayer(x,y,z);
        LayerNodeState bottomMid = getLayer(x,y+1,z);
        LayerNodeState top= getLayer(x,y+2,z);
        LayerNodeState topMid = getLayer(x,y+3,z);


        int barelyStonkCount = 0;
        int stonkCount = 0;
        int openCount = 0;


        if (y%2 == 0) {
            if (!bottom.isInstabreak() || !bottomMid.isInstabreak()) barelyStonkCount++;
            if (!top.isInstabreak()) barelyStonkCount++;
        } else {
            if (!bottom.isInstabreak()) barelyStonkCount++;
            if (!top.isInstabreak() || !bottomMid.isInstabreak()) barelyStonkCount++;
        }

        if (bottom.isInstabreak()) stonkCount++;
        if (bottom == LayerNodeState.OPEN) openCount++;
        if (bottomMid.isInstabreak()) stonkCount++;
        if (bottomMid == LayerNodeState.OPEN) openCount++;
        if (top.isInstabreak()) stonkCount++;
        if (top == LayerNodeState.OPEN) openCount++;
        if (topMid.isInstabreak()) stonkCount++;
        if (topMid == LayerNodeState.OPEN) openCount++;

        boolean falls = getLayer(x, y-1, z) == LayerNodeState.OPEN;
        boolean highCeiling = getLayer(x, y+4, z) == LayerNodeState.OPEN;


        if (openCount == 4) {
            return NodeState.OPEN;
        }
        if (bottom == LayerNodeState.FORBIDDEN || bottomMid == LayerNodeState.FORBIDDEN || top == LayerNodeState.FORBIDDEN || topMid == LayerNodeState.FORBIDDEN) {
            return NodeState.BLOCKED;
        }
        if (!topMid.isInstabreak()) {
            // if top mid is blocked, then player can't go anywhere, unless, falling...
            if (falls)
                return NodeState.BLOCKED_STONKABLE_FALLING;
            else
                return NodeState.BLOCKED;
        }

        if (barelyStonkCount > 1) {
            return NodeState.BLOCKED;
        }


        if (y % 2 == 0 && bottom.isStair() && openCount == 3 && highCeiling) {
            if (falls) return NodeState.ENTRANCE_STONK_DOWN_FALLING;
            return NodeState.ENTRANCE_STONK_DOWN;
        }
        if (((x % 2 == 0) != (z % 2 == 0)) && y % 2 == 1 && getLayer(x, y-1, z).isStair() &&
                (bottom == LayerNodeState.BLOCKED_ONE_STONK_STAIR_MID || bottom == LayerNodeState.STONKABLE_STAIR_MID) && openCount == 3 && highCeiling) {
            return NodeState.ENTRANCE_STONK_DOWN_ECHEST;
        }

        if (y % 2 != 0 && topMid.isStair() && openCount == 3 && falls) {
            return NodeState.ENTRANCE_STONK_UP;
        }

        // wall
        if (x % 2 != 0 && z % 2 != 0 && y % 2 == 0 && bottom.isBlocked() && openCount == 3 && highCeiling) {
            IBlockState iBlockState1 = getCachedWorld().getBlockState(new BlockPos(x/2.0, y/2-1, z/2.0));
            Block b = iBlockState1.getBlock();
            if (b instanceof BlockWall || b instanceof BlockFence || b instanceof BlockFenceGate) {
                iBlockState1 = getCachedWorld().getBlockState(new BlockPos(x/2.0, y/2, z/2.0));
                b = iBlockState1.getBlock();
                if (b == Blocks.air) {
                    return NodeState.ENTRANCE_TELEPORT_DOWN;
                }
            }
        }

        if (x % 2 != 0 && z % 2 != 0 && y % 2 == 0 && openCount == 1 && topMid == LayerNodeState.OPEN) {
            IBlockState iBlockState1 = getCachedWorld().getBlockState(new BlockPos(x/2.0, y/2, z/2.0));
            Block b = iBlockState1.getBlock();
            if (b instanceof BlockWall || b instanceof BlockFence || b instanceof BlockFenceGate) {
                iBlockState1 = getCachedWorld().getBlockState(new BlockPos(x/2.0, y/2+1, z/2.0));
                b = iBlockState1.getBlock();
                if (!b.getMaterial().blocksMovement()) {
                    iBlockState1 = getCachedWorld().getBlockState(new BlockPos(x/2.0, y/2+2, z/2.0));
                    b = iBlockState1.getBlock();
                    if (!b.getMaterial().blocksMovement()) {
                        if (falls) return NodeState.ENTRANCE_ETHERWARP_FALL;
                        return NodeState.ENTRANCE_ETHERWARP;
                    }
                }
            }
        }

        if (x%2 == 0 && z%2 == 0) {
            return NodeState.BLOCKED; // never go corners while stonking..
        }


        if (x % 2 != 0 && z % 2 != 0 && y % 2 == 0) {
            IBlockState iBlockState1 = getCachedWorld().getBlockState(new BlockPos(x/2.0, y/2-1, z/2.0));
            Block b = iBlockState1.getBlock();
            if (b instanceof BlockWall || b instanceof BlockFence || b instanceof BlockFenceGate) {
                falls = true;
            }
        }

        if (y % 2 == 0 && falls) return NodeState.BLOCKED_STONKABLE_FALLING;
        if (y % 2 == 1 && bottom != LayerNodeState.OPEN || falls) return NodeState.BLOCKED_STONKABLE_FALLING;
        return NodeState.BLOCKED_STONKABLE;
    }

    @AllArgsConstructor @Getter
    public enum LayerNodeState {
        UNCACHED(false, false, false),
        OPEN(false, false, true), // yep, air is instabreakable. I'm doing this to save condition.
        FORBIDDEN(true, false, false),
        BLOCKED_ONE_STONK(true, false, false),
        STONKABLE(true, false, true),
        BLOCKED_ONE_STONK_STAIR_MID(true, false, false),
        STONKABLE_STAIR_MID(true, false, true),
        ENTRANCE_STAIR_STONK(true, true, true),
        ENTRANCE_STAIR_BLOCK(true, true, false),
        OUT_OF_DUNGEON(true, false, false);
        // blocked
        // isStair
        // OneStonk

        public static final int BITS = (int) Math.ceil(Math.log(LayerNodeState.values().length - 1) / Math.log(2));
        public static final LayerNodeState[] VALUES = LayerNodeState.values();

        private boolean blocked;
        private boolean stair;
        private boolean instabreak;
    }


    @AllArgsConstructor
    public enum NodeState {
        UNCACHED(true, true, false, false,false,  new Color(0x550000FF, true)),
        OPEN(false, false, false, true, false, new Color(0x5533FF33, true)),
        // upper block is not insta mine, or there is an unminable block in way
        BLOCKED(true, true, false, false, false, new Color(0x55FF0000, true)),
        // down block is minable, everything else is insta mine or air, and you're not hovering
        BLOCKED_STONKABLE(true, false, false, false,false,  new Color(0x55005500, true)),
        // down block is minable, everything else is insta mine or air, and you're falling.
        BLOCKED_STONKABLE_FALLING(true, false, false, false, true, new Color(0x55002200, true)),
        // downward stonk entrance
        ENTRANCE_STONK_DOWN(true,false, true, false,false,  new Color(0x55FF7700, true)),

        // this weird thing that I learned from val
        ENTRANCE_STONK_DOWN_ECHEST(true, false, true, false, false, new Color(0x55FF7777, true)),

        // downward stonk entrance but falling
        ENTRANCE_STONK_DOWN_FALLING(true,false, true, false, true, new Color(0x55FF7700, true)),
        // upward stonk entrance
        ENTRANCE_STONK_UP(true,false, true, false,false,  new Color(0x55FF3000, true)),
        // downward teleport stonk entrance
        ENTRANCE_TELEPORT_DOWN(true, false, true, false, true, new Color(0x55FF00FF, true)),
        // eterwarp entrance
        ENTRANCE_ETHERWARP(true, false, true, false,false,  new Color(0x55FF0099, true)),
        ENTRANCE_ETHERWARP_FALL(true, false, true, false,true,  new Color(0x55FF5599, true)),

        OUT_OF_DUNGEON(true, true, false, false,false,  new Color(0x550000FF, true)); // always last

        public static final int BITS = (int) Math.ceil(Math.log(NodeState.values().length - 1) / Math.log(2));
        public static final NodeState[] VALUES = NodeState.values();

        @Getter
        private boolean isBlockedNonStonk;
        @Getter
        private boolean isBlockedStonk;
        @Getter
        private boolean stonkEntrance;
        @Getter
        private boolean stonkExit;

        @Getter
        private boolean fall;

        @Getter
        private Color color;
    }

    public LayerNodeState getLayer(int x, int y, int z) {
        if (x < minx || z < minz || x >= maxx || z >= maxz || y < miny || y >= maxy) return LayerNodeState.OUT_OF_DUNGEON;
        int dx = x - minx, dy = y - miny, dz = z - minz;
        int data = oneLayer.read(dx, dy, dz);
        if (data != 0) return LayerNodeState.VALUES[data];
        LayerNodeState val = calculateOneLayerIsBlocked(x, y, z);
        oneLayer.store(dx,dy,dz, val.ordinal());
        return val;
    }
    public NodeState getBlock(int x, int y, int z) {
        if (x < minx || z < minz || x >= maxx || z >= maxz || y < miny || y >= maxy) return NodeState.OUT_OF_DUNGEON;
        int dx = x - minx, dy = y - miny, dz = z - minz;
        int data = whole.read(dx, dy, dz);
        if (data != 0) return NodeState.VALUES[data];
        NodeState val = calculateIsBlocked(x, y, z);
        whole.store(dx,dy,dz, val.ordinal());
        return val;
    }


    public void resetBlock(BlockPos pos) { // I think it can be optimize due to how it is saved in arr
        for (int x = -2; x <= 2; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -2; z <= 2; z++) {
                    resetBlock(pos.getX()*2 + x, pos.getY()*2 + y, pos.getZ()*2 + z);
                }
            }
        }
        for (int x = -2; x <= 2; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -2; z <= 2; z++) {
                    resetBlock2(pos.getX()*2 + x, pos.getY()*2 + y, pos.getZ()*2 + z);
                }
            }
        }

    }
    private void resetBlock(int x, int y, int z) {
        if (x < minx || z < minz || x >= maxx || z >= maxz || y < miny || y >= maxy) return;
        int dx = x - minx, dy = y - miny, dz = z - minz;
        oneLayer.store(dx, dy, dz, calculateOneLayerIsBlocked(x,y,z).ordinal());
    }
    private void resetBlock2(int x, int y, int z) {
        if (x < minx || z < minz || x >= maxx || z >= maxz || y < miny || y >= maxy) return;
        int dx = x - minx, dy = y - miny, dz = z - minz;
        if (whole.store(dx, dy, dz, calculateIsBlocked(x,y,z).ordinal())) {
            blockUpdateId++;
        }
    }
}
