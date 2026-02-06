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
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonBreakableWallState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonRoomDoor2State;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonRoomDoorState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonTombState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.dataprovider.DungeonDoor;
import kr.syeyoung.dungeonsguide.mod.dungeon.dataprovider.EDungeonDoorType;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.SerializableBlockPos;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.impl.DungeonRoomMatchEvent;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.impl.DungeonStateChangeEvent;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.ProcessorFactory;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.RoomProcessorGenerator;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.ArrayBackedBlockMap;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.DRIBackedBlockMap;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.WorldBackedBlockMap;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.Pair;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.world.BlockType;
import kr.syeyoung.modapi.world.UBlockState;
import kr.syeyoung.modapi.world.UChunk;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class DungeonRoom  {
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

    @Getter(AccessLevel.NONE)
    private Map<String, DungeonMechanicState> _mechanics = null;

    @Setter
    private ArrayBackedBlockMap roomWorld;

    private WorldBackedBlockMap coordinateMap;

    public DungeonRoom(Set<Point> points, short shape, byte color, VectorI3D min, VectorI3D max, DungeonContext context, Set<Pair<Vector2d, EDungeonDoorType>> doorsAndStates) {
        this.unitPoints = points;
        this.color = color;
        this.context = context;
        roomBounds = new RoomBounds(shape, min, max);

        minRoomPt = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (Point pt : unitPoints) {
            if (pt.x < minRoomPt.x) minRoomPt.x = pt.x;
            if (pt.y < minRoomPt.y) minRoomPt.y = pt.y;
        }


        this.doorsAndStates = doorsAndStates;
        tryRematch();
    }

    public DungeonRoom(DungeonContext context) {
        if (!(context.getWorld() instanceof DRIBackedBlockMap)) {
            throw new IllegalArgumentException("This constructor only applicable for DRIWorld based DungeonContext");
        }
        DRIBackedBlockMap driWorld = (DRIBackedBlockMap) context.getWorld();

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

        context.getScaffoldParser().insertRoom(this);

        this.color = this.dungeonRoomInfo.getColor();
        this.context = context;
        roomBounds = new RoomBounds(this.dungeonRoomInfo.getShape(), new VectorI3D(0, 70, 0), new VectorI3D(dungeonRoomInfo.getBlocks()[0].length - 1, 70, dungeonRoomInfo.getBlocks().length - 1));

        minRoomPt = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (Point pt : unitPoints) {
            if (pt.x < minRoomPt.x) minRoomPt.x = pt.x;
            if (pt.y < minRoomPt.y) minRoomPt.y = pt.y;
        }


        this.doorsAndStates = new HashSet<>();
        this.roomMatcher = new RoomMatcher(this);
        this.roomMatcher.setMatch(dungeonRoomInfo);
        this.roomMatcher.setRotation(0);

        totalSecrets = dungeonRoomInfo.getTotalSecrets();

        HashSet<VectorI3D> poses = new HashSet<>();
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
        coordinateMap = new WorldBackedBlockMap(driWorld, roomBounds.getMin().getX()-3, 0, roomBounds.getMin().getZ()-3, roomBounds.getMax().getX()+3, 256, roomBounds.getMax().getZ()+3);
        roomWorld = new ArrayBackedBlockMap(roomBounds.getMin().getX()-3, 0, roomBounds.getMin().getZ()-3, roomBounds.getMax().getX()+3, 256, roomBounds.getMax().getZ()+3);
        roomWorld.migrateFromWorld(context.getWorld());

    }


    public ArrayBackedBlockMap getRoomWorld() {
        if (this.roomWorld != null) return roomWorld;


        int minZChunk = roomBounds.getMin().getZ() >> 4;
        int minXChunk = roomBounds.getMin().getX() >> 4;
        int maxZChunk = roomBounds.getMax().getZ() >> 4;
        int maxXChunk = roomBounds.getMax().getX() >> 4;

        for (int z = minZChunk; z <= maxZChunk; z++) {
            for (int x = minXChunk; x <= maxXChunk; x++) {
                if (!getRoomBounds().canAccessAbsolute(new VectorI3D(x * 16,0, z*16)) && !getRoomBounds().canAccessAbsolute(new VectorI3D(x * 16+15,0, z*16+15))
                && !getRoomBounds().canAccessAbsolute(new VectorI3D(x * 16+15,0, z*16)) && !getRoomBounds().canAccessAbsolute(new VectorI3D(x * 16,0, z*16+15))) {
                    continue;
                }
                UChunk c = getContext().getWorld().getChunkAt(x,z);
                if (c.isEmpty()) {
                    throw new IllegalStateException("Chunk not loaded: "+x+"/"+z);
                }
            }
        }

        roomWorld = new ArrayBackedBlockMap(roomBounds.getMin().getX()-3, 0, roomBounds.getMin().getZ()-3, roomBounds.getMax().getX()+3, 256, roomBounds.getMax().getZ()+3);
        roomWorld.migrateFromWorld(context.getWorld());

        coordinateMap = new WorldBackedBlockMap(roomWorld, roomBounds.getMin().getX()-3, 0, roomBounds.getMin().getZ()-3, roomBounds.getMax().getX()+3, 256, roomBounds.getMax().getZ()+3);



        return this.roomWorld = roomWorld;
    }

    public Map<String, DungeonMechanicState> getMechanics() {
        if (dungeonRoomInfo == null) return Collections.EMPTY_MAP;
        if (_mechanics == null/* || EditingContext.getEditingContext() != null $$ ROOMEDIT */) {
            _mechanics = new HashMap<>();
            for (Map.Entry<String, DungeonMechanicData> stringDungeonMechanicDataEntry : dungeonRoomInfo.getMechanics().entrySet()) {
                _mechanics.put(stringDungeonMechanicDataEntry.getKey(), stringDungeonMechanicDataEntry.getValue().createState(this));
            }
            int index = 0;
            if (_mechanics.values().stream().noneMatch(a -> a instanceof DungeonRoomDoor2State)) {
                for (DungeonDoor door : doors) {
                    if (door.getType().isExist())
                        _mechanics.put((door.getType().getName()) + "-" + (++index), new DungeonRoomDoorState(this, door));
                }
            }
        }
        return _mechanics;
    }

    @AllArgsConstructor
    @Getter
    public enum RoomState {
        DISCOVERED(0), COMPLETE_WITHOUT_SECRETS(0), FINISHED(0), FAILED(-14);
        private final int scoreModifier;
    }

    public void setCurrentState(RoomState currentState) {
        context.getRecorder().createEvent(new DungeonStateChangeEvent(unitPoints.iterator().next(),
                dungeonRoomInfo == null ? null : dungeonRoomInfo.getName(), this.currentState, currentState));
        this.currentState = currentState;
    }

    private static final ExecutorService roomMatcherThread = DungeonsGuide.getDungeonsGuide().registerExecutorService(Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setThreadFactory(DungeonsGuide.THREAD_FACTORY)
                    .setNameFormat("DG-RoomMatcher-%d").build()));


    private RoomProcessor roomProcessor;

    private Set<Pair<Vector2d, EDungeonDoorType>> doorsAndStates;

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
        getRoomWorld();
        buildRoom();
        buildDoors(doorsAndStates);

        DungeonsGuide.getDungeonsGuide().runNextTick(() -> {
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

    private void buildDoors(Set<Pair<Vector2d, EDungeonDoorType>> doorsAndStates) {
            Set<Pair<VectorI3D, EDungeonDoorType>> positions = new HashSet<>();
            VectorI3D pos = context.getScaffoldParser().getDungeonMapLayout().roomPointToWorldPoint(minRoomPt).add(16, 0, 16);
            for (Pair<Vector2d, EDungeonDoorType> doorsAndState : doorsAndStates) {
                Vector2d vector2d = doorsAndState.getFirst();
                VectorI3D neu = pos.add((int) (vector2d.x * 32), 0, (int) (vector2d.y * 32));
                positions.add(new Pair<>(neu, doorsAndState.getSecond()));
            }

            for (Pair<VectorI3D, EDungeonDoorType> door : positions) {
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
                    getRoomMatcher().getRotation(), new SerializableBlockPos(roomBounds.getMin()),
                    new SerializableBlockPos(roomBounds.getMax()), getRoomBounds().getShape(), getColor(),
                    dungeonRoomInfo.getUuid(),
                    dungeonRoomInfo.getName(),
                    dungeonRoomInfo.getProcessorId()));
        }
        ChatTransmitter.sendDebugChat("New Map matched! shape: " + getRoomBounds().getShape() + " color: " +getColor() + " unitPos: " + unitPoints.iterator().next().x + "," + unitPoints.iterator().next().y);
        ChatTransmitter.sendDebugChat("New Map matched! mapMin: " + roomBounds.getMin() + " mapMx: " + roomBounds.getMax());
        ChatTransmitter.sendDebugChat("New Map matched! id: " + dungeonRoomInfo.getUuid() + " name: " + dungeonRoomInfo.getName() +" proc: "+dungeonRoomInfo.getProcessorId());


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

    public UBlockState getRelativeUBlockStateAt(int x, int y, int z) {
        // validate x y z's
        if (getRoomBounds().canAccessRelative(x,z)) {
            return getRoomWorld().getBlockStateAt(x+roomBounds.getMinX(),y+roomBounds.getMin().getY(),z + roomBounds.getMin().getZ());
        }
        return ModAPI.getAPI().getBlockRegistry().oneFromWellknown(BlockType.BEDROCK);
    }

    public VectorI3D getRelativeBlockPosAt(int x, int y, int z) {
        return new VectorI3D(x,y,z).add(roomBounds.getMin().getX(), roomBounds.getMin().getY(), roomBounds.getMin().getZ());
    }

    public Vector3D getRelativeVec3At(double x, double y, double z) {
        return new Vector3D(x,y,z).add(roomBounds.getMin().getX(), roomBounds.getMin().getY(), roomBounds.getMin().getZ());
    }

    public void chunkUpdate(int cx, int cz) {
        UChunk uChunk = context.getWorld().getChunkAt(cx, cz);
        if (uChunk == null) return;
        roomWorld.updateChunk(uChunk);
    }
}
