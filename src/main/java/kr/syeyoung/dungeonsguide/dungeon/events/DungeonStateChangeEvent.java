package kr.syeyoung.dungeonsguide.dungeon.events;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.UUID;

@Data
@AllArgsConstructor
public class DungeonStateChangeEvent implements DungeonEventData {
    private Point unitPt;
    private String roomName;
    private DungeonRoom.RoomState from;
    private DungeonRoom.RoomState to;

    @Override
    public String getEventName() {
        return "ROOM_STATE_CHANGE";
    }
}
