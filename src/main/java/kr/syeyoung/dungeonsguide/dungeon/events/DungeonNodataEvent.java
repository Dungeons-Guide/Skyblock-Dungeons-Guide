package kr.syeyoung.dungeonsguide.dungeon.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DungeonNodataEvent implements DungeonEventData {
    private String eventName;
}
