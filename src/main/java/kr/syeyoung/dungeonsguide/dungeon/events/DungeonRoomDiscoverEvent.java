package kr.syeyoung.dungeonsguide.dungeon.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.UUID;

@Data
@AllArgsConstructor
public class DungeonRoomDiscoverEvent implements DungeonEventData {
    private Point unitPt;
    private int rotation;
    private BlockPos min;
    private int shape;
    private int color;
    private UUID roomUID;
    private String roomName;
    private String roomProc;

    @Override
    public String getEventName() {
        return "ROOM_DISCOVER";
    }
}
