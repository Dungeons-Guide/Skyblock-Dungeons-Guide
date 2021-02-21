package kr.syeyoung.dungeonsguide.dungeon.events;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.awt.*;

@Data
@AllArgsConstructor
public class DungeonSecretCountChangeEvent implements DungeonEventData {
    private int prevCount;
    private int newCount;
    private int totalSecret;
    private boolean sureTotalSecret;

    @Override
    public String getEventName() {
        return "SECRET_CNT_CHANGE";
    }
}
