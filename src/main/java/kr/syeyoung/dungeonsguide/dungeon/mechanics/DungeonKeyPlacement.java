package kr.syeyoung.dungeonsguide.dungeon.mechanics;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import lombok.Data;

@Data
public class DungeonKeyPlacement implements DungeonMechanic, Triggering {
    private OffsetPoint keySlot;
    private DungeonKey dungeonKey;

    private Triggered triggered;
}
