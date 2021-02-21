package kr.syeyoung.dungeonsguide.dungeon.events;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.awt.*;

@Data
@AllArgsConstructor
public class DungeonCryptBrokenEvent implements DungeonEventData {
    private int prevCrypts;
    private int newCrypts;

    @Override
    public String getEventName() {
        return "CRYPTS_CHANGE";
    }
}
