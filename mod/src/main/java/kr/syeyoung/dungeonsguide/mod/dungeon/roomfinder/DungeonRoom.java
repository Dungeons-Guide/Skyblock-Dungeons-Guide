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
import kr.syeyoung.dungeonsguide.mod.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonBreakableWallState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonRoomDoor2State;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.AbstractAction;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.AbstractActionMove;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.AtomicAction;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAG;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGNode;
import kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder.DungeonDoor;
import kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder.EDungeonDoorType;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.SerializableBlockPos;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.impl.DungeonRoomMatchEvent;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.impl.DungeonStateChangeEvent;
import kr.syeyoung.dungeonsguide.mod.dungeon.mocking.DRIWorld;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.pathfindcache.PathfindPrecalculation;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.pathfindcache.PathfindResultRegistry;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.pathfindcache.RoomPreset;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.world.CachedWorld;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.world.EditableChunkCache;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.ProcessorFactory;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.RoomProcessorGenerator;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.*;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.FeaturePathfindStrategy;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.AdditionalInfoCaculatedDungeonRoomInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;

@Getter
public class DungeonRoom implements IPathfindWorld {
    private final Set<Point> unitPoints;
    private final RoomBounds roomBounds;
    private final byte color;

    private final Point minRoomPt;

    private final DungeonContext context;

    private final List<DungeonDoor> doors = new ArrayList<>();

    private DungeonRoomInfo dungeonRoomInfo;

    @Setter
    private int totalSecrets = -1;
    private RoomState currentState = RoomState.DISCOVERED;

    private Map<String, DungeonMechanicState> cached = null;

    @Setter
    private World cachedWorld;
    private WorldBackedCoordinateMap coordinateMap;
    private EditableChunkCache chunkCache;

    public World getCachedWorld() {
        if (this.cachedWorld != null) return cachedWorld;


        int minZChunk = roomBounds.getMin().getZ() >> 4;
        int minXChunk = roomBounds.getMin().getX() >> 4;
        int maxZChunk = roomBounds.getMax().getZ() >> 4;
        int maxXChunk = roomBounds.getMax().getX() >> 4;

        for (int z = minZChunk; z <= maxZChunk; z++) {
            for (int x = minXChunk; x <= maxXChunk; x++) {
                if (!getRoomBounds().canAccessAbsolute(new BlockPos(x * 16,0, z*16)) && !getRoomBounds().canAccessAbsolute(new BlockPos(x * 16+15,0, z*16+15))
                && !getRoomBounds().canAccessAbsolute(new BlockPos(x * 16+15,0, z*16)) && !getRoomBounds().canAccessAbsolute(new BlockPos(x * 16,0, z*16+15))) {
                    continue;
                }
                Chunk c = getContext().getWorld().getChunkFromChunkCoords(x,z);
                if (c.isEmpty()) {
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
                    throw new IllegalStateException("Chunk not loaded: "+x+"/"+z);
                }
            }
        }

        this.chunkCache = new EditableChunkCache(getContext().getWorld(), roomBounds.getMin().add(-3, 0, -3), roomBounds.getMax().add(3,0,3), 0);
        CachedWorld cachedWorld =  new CachedWorld(chunkCache, context.getWorld().provider);

        coordinateMap = new WorldBackedCoordinateMap(cachedWorld, roomBounds.getMin().getX()-3, 0, roomBounds.getMin().getZ()-3, roomBounds.getMax().getX()+3, 256, roomBounds.getMax().getZ()+3);


        return this.cachedWorld = cachedWorld;
    }
    public Map<String, DungeonMechanicState> getMechanics() {
        if (cached == null || EditingContext.getEditingContext() != null) {
            cached = new HashMap<>();
            for (Map.Entry<String, DungeonMechanicData> stringDungeonMechanicDataEntry : dungeonRoomInfo.getMechanics().entrySet()) {
                cached.put(stringDungeonMechanicDataEntry.getKey(), stringDungeonMechanicDataEntry.getValue().createState(this));
            }
            int index = 0;
            for (DungeonDoor door : doors) {
                if (door.getType().isExist()) cached.put((door.getType().getName())+"-"+(++index), new DungeonRoomDoorState(this, door));
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
        if (activePathfind.containsKey(pos.center())) {
            WeakReference<PathfinderExecutor> executorWeakReference = activePathfind.get(pos.center());
            PathfinderExecutor executor = executorWeakReference.get();
            if (executor != null) {
                return executor;
            }
        }
        if (true)
            return null;
        PathfinderExecutor executor;
        if (pathfindStrategy == FeaturePathfindStrategy.PathfindStrategy.A_STAR_FINE_GRID_SMART) {
            executor = new PathfinderExecutor(new FineGridStonkingBFS(algorithmSetting), pos, this);
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
    private static final ExecutorService pathfindLoaderThread = DungeonsGuide.getDungeonsGuide().registerExecutorService(Executors.newFixedThreadPool(8,
            new ThreadFactoryBuilder()
                    .setThreadFactory(DungeonsGuide.THREAD_FACTORY)
                    .setNameFormat("DG-PathfindLoader-%d").build()));

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
        this.color = color;
        this.context = context;
        roomBounds = new RoomBounds(shape, min, max);

        minRoomPt = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (Point pt : unitPoints) {
            if (pt.x < minRoomPt.x) minRoomPt.x = pt.x;
            if (pt.y < minRoomPt.y) minRoomPt.y = pt.y;
        }



        minx = min.getX() * 2 + 2; miny = 0; minz = min.getZ() * 2 + 2;
        maxx = max.getX() * 2 + 2; maxy = 255 * 2 + 2; maxz = max.getZ() * 2 + 2;

        lenx = maxx - minx;
        leny = maxy - miny;
        lenz = maxz - minz;

        this.doorsAndStates = doorsAndStates;
        tryRematch();
    }

    public DungeonRoom(DungeonContext context) {
        if (!(context.getWorld() instanceof DRIWorld)) {
            throw new IllegalArgumentException("This constructor only applicable for DRIWorld based DungeonContext");
        }
        DRIWorld driWorld = (DRIWorld) context.getWorld();

        this.dungeonRoomInfo = driWorld.getDungeonRoomInfo();
        this.unitPoints = new HashSet<>();
        for (int dy = 0; dy < 4; dy++) {
            for (int dx = 0; dx < 4; dx++) {
                boolean isSet = ((this.dungeonRoomInfo.getShape()>> (dy * 4 + dx)) & 0x1) != 0;
                if (isSet) {
                    unitPoints.add(new Point(dx, dy));
                }
            }
        }

        context.getScaffoldParser().getDungeonRoomList().add(this);
        for (Point p : unitPoints) {
            context.getScaffoldParser().getRoomMap().put(p, this);
        }

        this.color = this.dungeonRoomInfo.getColor();
        this.context = context;
        roomBounds = new RoomBounds(this.dungeonRoomInfo.getShape(), new BlockPos(0, 70, 0), new BlockPos(dungeonRoomInfo.getBlocks()[0].length - 1, 70, dungeonRoomInfo.getBlocks().length - 1));


        minRoomPt = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (Point pt : unitPoints) {
            if (pt.x < minRoomPt.x) minRoomPt.x = pt.x;
            if (pt.y < minRoomPt.y) minRoomPt.y = pt.y;
        }




        minx = roomBounds.getMin().getX() * 2 + 2; miny = 0; minz = roomBounds.getMin().getZ() * 2 + 2;
        maxx = roomBounds.getMax().getX() * 2 + 2; maxy = 255 * 2 + 2; maxz = roomBounds.getMax().getZ() * 2 + 2;

        lenx = maxx - minx;
        leny = maxy - miny;
        lenz = maxz - minz;

        this.doorsAndStates = new HashSet<>();
        this.cachedWorld = driWorld;
        this.roomMatcher = new RoomMatcher(this);
        this.roomMatcher.setMatch(dungeonRoomInfo);
        this.roomMatcher.setRotation(0);

        roomPreset = context.getPreset().getRoomPreset(dungeonRoomInfo.getUuid());
        algorithmSetting = roomPreset.getEffectiveAlgorithmSetting(dungeonRoomInfo);
        totalSecrets = dungeonRoomInfo.getTotalSecrets();

        HashSet<BlockPos> poses = new HashSet<>();
        for (DungeonMechanicState value : getMechanics().values()) {
            if (value instanceof DungeonTombState) {
                for (OffsetPoint offsetPoint : ((DungeonTombState) value).blockedPoints()) {
                    poses.add(offsetPoint.getBlockPos(this));
                }
            } else if (value instanceof DungeonBreakableWallState) {
                for (OffsetPoint offsetPoint : ((DungeonBreakableWallState) value).blockedPoints()) {
                    poses.add(offsetPoint.getBlockPos(this));
                }
            }
        }
        coordinateMap = new WorldBackedCoordinateMap(driWorld, roomBounds.getMin().getX()-3, 0, roomBounds.getMin().getZ()-3, roomBounds.getMax().getX()+3, 256, roomBounds.getMax().getZ()+3);

        instaBreak = new InstaBreakFactorCalculatingCoordinateMap(coordinateMap, algorithmSetting);
        enderpearl = new BitCachingCoordinateMap<>(new PearlCalculatingCoordinateMap(coordinateMap, roomBounds), PearlCalculatingCoordinateMap.PearlLandType.VALUES, PearlCalculatingCoordinateMap.PearlLandType.BLOCKED);
        whole = new BitCachingCoordinateMap<>(new CollisionStateCalculatingCoordinateMap(coordinateMap, poses, instaBreak, roomBounds), CollisionStateCalculatingCoordinateMap.CollisionState.VALUES, CollisionStateCalculatingCoordinateMap.CollisionState.BLOCKED);
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
        if (getDungeonRoomInfo().getMechanics().values().stream().noneMatch(a -> a instanceof DungeonRoomDoor2State)) {
            Set<Tuple<BlockPos, EDungeonDoorType>> positions = new HashSet<>();
            BlockPos pos = context.getScaffoldParser().getDungeonMapLayout().roomPointToWorldPoint(minRoomPt).add(16, 0, 16);
            for (Tuple<Vector2d, EDungeonDoorType> doorsAndState : doorsAndStates) {
                Vector2d vector2d = doorsAndState.getFirst();
                BlockPos neu = pos.add(vector2d.x * 32, 0, vector2d.y * 32);
                positions.add(new Tuple<>(neu, doorsAndState.getSecond()));
            }

            for (Tuple<BlockPos, EDungeonDoorType> door : positions) {
                doors.add(new DungeonDoor(context.getWorld(), door.getFirst(), door.getSecond()));
            }
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
                    getRoomMatcher().getRotation(), new SerializableBlockPos(roomBounds.getMin()),
                    new SerializableBlockPos(roomBounds.getMax()), getRoomBounds().getShape(), getColor(),
                    dungeonRoomInfo.getUuid(),
                    dungeonRoomInfo.getName(),
                    dungeonRoomInfo.getProcessorId()));
        }
        ChatTransmitter.sendDebugChat(new ChatComponentText("New Map matched! shape: " + getRoomBounds().getShape() + " color: " +getColor() + " unitPos: " + unitPoints.iterator().next().x + "," + unitPoints.iterator().next().y));
        ChatTransmitter.sendDebugChat(new ChatComponentText("New Map matched! mapMin: " + roomBounds.getMin() + " mapMx: " + roomBounds.getMax()));
        ChatTransmitter.sendDebugChat(new ChatComponentText("New Map matched! id: " + dungeonRoomInfo.getUuid() + " name: " + dungeonRoomInfo.getName() +" proc: "+dungeonRoomInfo.getProcessorId()));


        this.dungeonRoomInfo = dungeonRoomInfo;
        totalSecrets = dungeonRoomInfo.getTotalSecrets();

        roomPreset = context.getPreset().getRoomPreset(dungeonRoomInfo.getUuid());
        algorithmSetting = roomPreset.getEffectiveAlgorithmSetting(dungeonRoomInfo);

        HashSet<BlockPos> poses = new HashSet<>();
        for (DungeonMechanicState value : getMechanics().values()) {
                        if (value instanceof DungeonTombState) {
                            for (OffsetPoint offsetPoint : ((DungeonTombState) value).blockedPoints()) {
                                poses.add(offsetPoint.getBlockPos(this));
                            }
                        } else if (value instanceof DungeonBreakableWallState) {
                            for (OffsetPoint offsetPoint : ((DungeonBreakableWallState) value).blockedPoints()) {
                                poses.add(offsetPoint.getBlockPos(this));
                            }
                        }
                    }

        instaBreak = new InstaBreakFactorCalculatingCoordinateMap(coordinateMap, algorithmSetting);
        enderpearl = new BitCachingCoordinateMap<>(new PearlCalculatingCoordinateMap(coordinateMap, roomBounds), PearlCalculatingCoordinateMap.PearlLandType.VALUES, PearlCalculatingCoordinateMap.PearlLandType.BLOCKED);
        whole = new BitCachingCoordinateMap<>(new CollisionStateCalculatingCoordinateMap(coordinateMap, poses, instaBreak, roomBounds), CollisionStateCalculatingCoordinateMap.CollisionState.VALUES, CollisionStateCalculatingCoordinateMap.CollisionState.BLOCKED);


        Set<String> pathfinders = roomPreset.getPrecalculations();
        if (pathfinders != null) {
            for (String precalcId : pathfinders) {
                loadPrecalculated(precalcId);
            }
        }


        // build tsp cache.
        ActionDAG dag = AdditionalInfoCaculatedDungeonRoomInfo.buildReferencingAllPossibleThings(this);
        List<AbstractActionMove> listOfMoves = new ArrayList<>();
        for (ActionDAGNode actionDAGNode : dag.getAllNodes()) {
            if (actionDAGNode.getAction() instanceof AtomicAction) {
                for (AbstractAction actionInAtomicAction : ((AtomicAction) actionDAGNode.getAction()).getActions()) {
                    if (actionInAtomicAction instanceof AbstractActionMove) {
                        listOfMoves.add((AbstractActionMove) actionInAtomicAction);
                    }
                }
            } else if (actionDAGNode.getAction() instanceof AbstractActionMove) {
                listOfMoves.add((AbstractActionMove) actionDAGNode.getAction());
            }
        }

        List<OffsetVec3> vec3 = new ArrayList<>();
        for (AbstractActionMove listOfMove : listOfMoves) {
            vec3.add(listOfMove.getTargetVec3());
        }

        long start = System.currentTimeMillis();

        tspCache = new TSPCache(this, vec3, Collections.EMPTY_LIST);
        for (PathfindPrecalculation value : idCalculation.values()) {
            try {
                tspCache.addToCache(value);
            } catch (IOException e) { e.printStackTrace(); }
        }
        ChatTransmitter.sendDebugChat("Building TSP Cache took "+(System.currentTimeMillis() - start)+"ms");

    }

    @Getter
    private TSPCache tspCache;

    private final Map<String, WeakReference<PathfinderExecutor>> idExecutor = new HashMap<>();
    private final Map<String, PathfindPrecalculation> idCalculation = new HashMap<>();
    public void loadPrecalculated(String id) {
        PathfindPrecalculation cachedPathfinder = PathfindResultRegistry.getINSTANCE().getById(id);
        if (cachedPathfinder == null) return;
        if (idCalculation.containsKey(id)) return;
        idCalculation.put(cachedPathfinder.getTargetHash(), cachedPathfinder);
    }

    public PathfindPrecalculation loadPrecalculatedUnloadedByHash(String hash) {
        if (!idCalculation.containsKey(hash)) {
            if (nextShowedWarning < System.currentTimeMillis()) {
                ChatTransmitter.addToQueue("§eDungeons Guide §7:: §cPrecalculation "+hash+" in room "+dungeonRoomInfo.getName()+" is §4§lMISSING §cin currently applied preset §e"+roomPreset.getParent().getPresetName()+"§c. There may be some problems in pathfinding. Please add precalculations at /dg -> Pathfinding & Secrets -> Precalculations");
                nextShowedWarning = System.currentTimeMillis() + 30000L;
            }
            return null;
        }

        return idCalculation.get(hash);
    }

    private long nextShowedWarning = 0;
    public synchronized PathfinderExecutor loadPrecalculatedByHash(String hash) {
        if (!idCalculation.containsKey(hash)) {
            if (nextShowedWarning < System.currentTimeMillis()) {
                ChatTransmitter.addToQueue("§eDungeons Guide §7:: §cPrecalculation "+hash+" in room "+dungeonRoomInfo.getName()+" is §4§lMISSING §cin currently applied preset §e"+roomPreset.getParent().getPresetName()+"§c. There may be some problems in pathfinding. Please add precalculations at /dg -> Pathfinding & Secrets -> Precalculations");
                nextShowedWarning = System.currentTimeMillis() + 30000L;
            }
            return null;
        }

        if (idExecutor.containsKey(hash)) {
            WeakReference<PathfinderExecutor> executorSoftReference = idExecutor.get(hash);
            PathfinderExecutor executor = executorSoftReference.get();
            if (executor != null) return executor;
            idExecutor.remove(hash);
        };

        System.out.println("LOADING:: "+hash);
        PathfindPrecalculation precalculation = idCalculation.get(hash);

        try {
            IPathfinder pathfinder = precalculation.createPathfinder(getRoomMatcher().getRotation());
            PathfinderExecutor executor1 = new PathfinderExecutor(pathfinder, BoundingBox.of(AxisAlignedBB.fromBounds(0,0,0,0,0,0)), this);
            idExecutor.put(precalculation.getTargetHash(), new WeakReference<>(executor1));
            executor1.doStep();
            return executor1;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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
        if (getRoomBounds().canAccessAbsolute(pos)) {
            return getCachedWorld().getBlockState(pos).getBlock();
        }
        return null;
    }

    public Block getRelativeBlockAt(int x, int y, int z) {
        // validate x y z's
        if (getRoomBounds().canAccessRelative(x,z)) {
            BlockPos pos = new BlockPos(x,y,z).add(roomBounds.getMin().getX(), roomBounds.getMin().getY(), roomBounds.getMin().getZ());
            return getCachedWorld().getBlockState(pos).getBlock();
        }
        return null;
    }

    public BlockPos getRelativeBlockPosAt(int x, int y, int z) {
        BlockPos pos = new BlockPos(x,y,z).add(roomBounds.getMin().getX(), roomBounds.getMin().getY(), roomBounds.getMin().getZ());
        return pos;
    }

    public Vec3 getRelativeVec3At(double x, double y, double z) {
        Vec3 pos = new Vec3(x,y,z).addVector(roomBounds.getMin().getX(), roomBounds.getMin().getY(), roomBounds.getMin().getZ());
        return pos;
    }

    public int getRelativeBlockDataAt(int x, int y, int z) {
        // validate x y z's
        if (getRoomBounds().canAccessRelative(x,z)) {
            BlockPos pos = new BlockPos(x,y,z).add(roomBounds.getMin().getX(), roomBounds.getMin().getY(), roomBounds.getMin().getZ());
            IBlockState iBlockState = getCachedWorld().getBlockState(pos);
            return iBlockState.getBlock().getMetaFromState(iBlockState);
        }
        return -1;
    }


    private BitCachingCoordinateMap<PearlCalculatingCoordinateMap.PearlLandType> enderpearl;
    private BitCachingCoordinateMap<CollisionStateCalculatingCoordinateMap.CollisionState> whole;
    private InstaBreakFactorCalculatingCoordinateMap instaBreak;

    // These values are doubled
    private final int minx;
    private final int miny;
    private final int minz;
    private final int maxx;
    private final int maxy;
    private final int maxz;
    private final int lenx, leny, lenz;

    private AlgorithmSetting algorithmSetting;
    private RoomPreset roomPreset;



    public boolean isInstabreak(int x, int y, int z) {
//        if (x < minx || z < minz || x >= maxx || z >= maxz || y < miny || y+4 >= maxy) return false;
        // TODO: what to do with this sanity check?
        if (x%2 != 0 && z%2 != 0) return false;

        return instaBreak.getBlock(x/2, y/2, z/2).getFactor()  == 0;
    }



    @Override
    public IBlockState getActualBlock(int x, int y, int z) {
        return getCachedWorld().getBlockState(new BlockPos(x,y,z));
    }

    public CollisionStateCalculatingCoordinateMap.CollisionState getBlock(int x, int y, int z) {
        return whole.getBlock(x, y, z);
    }

    public PearlCalculatingCoordinateMap.PearlLandType getPearl(int x, int y, int z) {
        return enderpearl.getBlock(x, y, z);
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
                    whole.update(pos.getX() * 2 + x, pos.getY() * 2 + y, pos.getZ() *2 + z);
                    enderpearl.update(pos.getX() * 2 + x, pos.getY() * 2 + y, pos.getZ() *2 + z);
                }
            }
        }
    }

    public void chunkUpdate(int cx, int cz) {
        if (!chunkCache.isManaged(cx, cz)) {
            return;
        }
        chunkCache.updateChunk(new BlockPos(cx*16+8, 0, cz*16+8));

        for (int x = 0; x < 16; x ++) { // fix pf not going through big block updates
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 255; y++) {
                    whole.update(cx * 16 + x, y, cz * 16 + z);
                    enderpearl.update(cx * 16 + x, y, cz * 16 + z);
                }
            }
        }
    }
}
