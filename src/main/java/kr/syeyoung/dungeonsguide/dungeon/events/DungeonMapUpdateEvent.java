package kr.syeyoung.dungeonsguide.dungeon.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DungeonMapUpdateEvent implements DungeonEventData {
    private byte[] map;

    @Override
    public String getEventName() {
        return "MAP_UPDATE";
    }
}
