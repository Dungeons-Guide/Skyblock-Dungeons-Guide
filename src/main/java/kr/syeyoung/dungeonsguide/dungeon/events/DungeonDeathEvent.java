package kr.syeyoung.dungeonsguide.dungeon.events;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.awt.*;

@Data
@AllArgsConstructor
public class DungeonDeathEvent implements DungeonEventData {
    private String playerName;
    private String message;
    private int cnt;

    @Override
    public String getEventName() {
        return "PLAYER_DEATH";
    }
}
