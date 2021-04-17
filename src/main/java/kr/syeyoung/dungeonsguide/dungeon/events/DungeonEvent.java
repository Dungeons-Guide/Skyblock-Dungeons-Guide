package kr.syeyoung.dungeonsguide.dungeon.events;

import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import lombok.Data;

import java.io.Serializable;

@Data
public class DungeonEvent implements Serializable {
    private long UTCTime = System.currentTimeMillis();
    private long realTimeElapsed;
    private long skyblockTimeElapsed;

    private String eventName;
    private DungeonEventData data;

    public DungeonEvent(DungeonEventData eventData){
        this.data = eventData;
        this.realTimeElapsed = FeatureRegistry.DUNGEON_REALTIME.getTimeElapsed();
        this.skyblockTimeElapsed = FeatureRegistry.DUNGEON_SBTIME.getTimeElapsed();
        this.eventName = eventData.getEventName();
    }
}
