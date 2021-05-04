package kr.syeyoung.dungeonsguide.dungeon.roomfinder;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.MapProcessor;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonDoor;
import kr.syeyoung.dungeonsguide.dungeon.events.DungeonStateChangeEvent;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonMechanic;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonRoomDoor;
import kr.syeyoung.dungeonsguide.pathfinding.NodeProcessorDungeonRoom;
import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.roomprocessor.ProcessorFactory;
import kr.syeyoung.dungeonsguide.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.roomprocessor.RoomProcessorGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Getter
public class DungeonRoom {
    private final List<Point> unitPoints;
    private final short shape;
    private final byte color;

    private final BlockPos min;
    private final BlockPos max;
    private final Point minRoomPt;

    private final DungeonContext context;

    private final List<DungeonDoor> doors = new ArrayList<DungeonDoor>();

    private DungeonRoomInfo dungeonRoomInfo;

    private final int unitWidth; // X
    private final int unitHeight; // Z

    @Setter
    private int totalSecrets = -1;
    private RoomState currentState = RoomState.DISCOVERED;

    private Map<String, DungeonMechanic> cached = null;
    public Map<String, DungeonMechanic> getMechanics() {
        if (cached == null || EditingContext.getEditingContext() != null) {
            cached = new HashMap<String, DungeonMechanic>(dungeonRoomInfo.getMechanics());
            int index = 0;
            for (DungeonDoor door : doors) {
                if (door.isExist()) cached.put((door.isRequiresKey() ? "withergate" : "gate")+"-"+(++index), new DungeonRoomDoor(door));
            }
        }
        return cached;
    }

    public void setCurrentState(RoomState currentState) {
        context.createEvent(new DungeonStateChangeEvent(unitPoints.get(0), dungeonRoomInfo.getName(), this.currentState, currentState));
        this.currentState = currentState;
    }

    @Getter
    private final PathFinder pathFinder;

    public ScheduledFuture<List<BlockPos>> createEntityPathTo(IBlockAccess blockaccess, Entity entityIn, BlockPos targetPos, float dist) {
        return asyncPathFinder.schedule(() -> {
            PathEntity latest = pathFinder.createEntityPathTo(blockaccess, entityIn, targetPos, dist);
            if (latest != null) {
                List<BlockPos> poses = new ArrayList<>();
                for (int i = 0; i < latest.getCurrentPathLength(); i++) {
                    PathPoint pathPoint = latest.getPathPointFromIndex(i);
                    poses.add(getMin().add(pathPoint.xCoord, pathPoint.yCoord, pathPoint.zCoord));
                }
                return poses;
            }
            return new ArrayList<>();
        }, 0, TimeUnit.MILLISECONDS);
    }

    private static final ScheduledExecutorService asyncPathFinder = Executors.newScheduledThreadPool(2);
    @Getter
    private final NodeProcessorDungeonRoom nodeProcessorDungeonRoom;

    @Getter
    private final Map<String, Object> roomContext = new HashMap<String, Object>();

    @AllArgsConstructor
    @Getter
    public enum RoomState {
        DISCOVERED(0), COMPLETE_WITHOUT_SECRETS(0), FINISHED(0), FAILED(-14);
        private final int scoreModifier;
    }

    private RoomProcessor roomProcessor;

    public DungeonRoom(List<Point> points, short shape, byte color, BlockPos min, BlockPos max, DungeonContext context) {
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

        buildDoors();
        buildRoom();
        nodeProcessorDungeonRoom = new NodeProcessorDungeonRoom(this);
        pathFinder = new PathFinder(nodeProcessorDungeonRoom);
        updateRoomProcessor();
    }

    private static final Set<Vector2d> directions = Sets.newHashSet(new Vector2d(0,16), new Vector2d(0, -16), new Vector2d(16, 0), new Vector2d(-16 , 0));

    private void buildDoors() {
        Set<BlockPos> positions = new HashSet<BlockPos>();
        for (Point p:unitPoints) {
            BlockPos pos = context.getMapProcessor().roomPointToWorldPoint(p).add(16,0,16);
            for (Vector2d vector2d : directions){
                BlockPos doorLoc = pos.add(vector2d.x, 0, vector2d.y);
                if (positions.contains(doorLoc)) positions.remove(doorLoc);
                else positions.add(doorLoc);
            }
        }

        for (BlockPos door : positions) {
            doors.add(new DungeonDoor(context.getWorld(), door));
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
        }
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
            return this.context.getWorld().getChunkFromBlockCoords(pos).getBlock(pos);
        }
        return null;
    }

    public Block getRelativeBlockAt(int x, int y, int z) {
        // validate x y z's
        if (canAccessRelative(x,z)) {
            BlockPos pos = new BlockPos(x,y,z).add(min.getX(),min.getY(),min.getZ());
            return this.context.getWorld().getChunkFromBlockCoords(pos).getBlock(pos);
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
            return this.context.getWorld().getChunkFromBlockCoords(pos).getBlockMetadata(pos);
        }
        return -1;
    }

    public int getAbsoluteBlockDataAt(int x, int y, int z) {
        // validate x y z's
        BlockPos pos = new BlockPos(x,y,z);
        if (canAccessAbsolute(pos)) {
            return this.context.getWorld().getChunkFromBlockCoords(pos).getBlockMetadata(pos);
        }
        return -1;
    }

    public boolean canAccessAbsolute(BlockPos pos) {
        MapProcessor mapProcessor = this.context.getMapProcessor();
        Point roomPt = mapProcessor.worldPointToRoomPoint(pos);
        roomPt.translate(-minRoomPt.x, -minRoomPt.y);

        return (shape >>(roomPt.y *4 +roomPt.x) & 0x1) > 0;
    }
    public boolean canAccessRelative(int x, int z) {
        return  x>= 0 && z >= 0 && (shape >>((z/32) *4 +(x/32)) & 0x1) > 0;
    }
}
