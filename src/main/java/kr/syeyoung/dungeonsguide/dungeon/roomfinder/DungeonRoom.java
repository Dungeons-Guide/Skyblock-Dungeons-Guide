package kr.syeyoung.dungeonsguide.dungeon.roomfinder;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonDoor;
import lombok.Getter;
import net.minecraft.util.BlockPos;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class DungeonRoom {
    private final List<Point> unitPoints;
    private final short shape;
    private final byte color;

    private final BlockPos min;

    private final DungeonContext context;

    private final List<DungeonDoor> doors = new ArrayList<DungeonDoor>();

    private DungeonRoomInfo dungeonRoomInfo;

    public DungeonRoom(List<Point> points, short shape, byte color, BlockPos min, DungeonContext context) {
        this.unitPoints = points;
        this.shape = shape;
        this.color = color;
        this.min = min;
        this.context = context;
        buildDoors();
        buildRoom();
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
    }
}
