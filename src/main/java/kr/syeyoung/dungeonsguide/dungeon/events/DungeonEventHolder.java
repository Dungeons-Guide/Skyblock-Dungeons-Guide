package kr.syeyoung.dungeonsguide.dungeon.events;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Data
public class DungeonEventHolder implements Serializable {
    private long date;
    private Set<String> players;
    private List<DungeonEvent> eventDataList;
}
