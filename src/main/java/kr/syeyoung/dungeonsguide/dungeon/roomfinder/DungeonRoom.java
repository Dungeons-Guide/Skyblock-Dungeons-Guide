package kr.syeyoung.dungeonsguide.dungeon.roomfinder;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.MapProcessor;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonDoor;
import kr.syeyoung.dungeonsguide.pathfinding.NodeProcessorDungeonRoom;
import kr.syeyoung.dungeonsguide.roomprocessor.ProcessorFactory;
import kr.syeyoung.dungeonsguide.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.roomprocessor.RoomProcessorGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.util.BlockPos;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.*;
import java.util.List;

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
    @Setter
    private RoomState currentState = RoomState.DISCOVERED;

    @Getter
    private PathFinder pathFinder;
    @Getter
    private NodeProcessorDungeonRoom nodeProcessorDungeonRoom;

    @AllArgsConstructor
    @Getter
    public static enum RoomState {
        DISCOVERED(0), COMPLETE_WITHOUT_SECRETS(0), FINISHED(0), FAILED(-14);
        private int scoreModifier;
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
        updateRoomProcessor();
        nodeProcessorDungeonRoom = new NodeProcessorDungeonRoom(this);
        pathFinder = new PathFinder(nodeProcessorDungeonRoom);
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
        if (dungeonRoomInfo == null)
            dungeonRoomInfo = roomMatcher.createNew();

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
        return (shape >>((z/32) *4 +(x/32)) & 0x1) > 0;
    }
}
