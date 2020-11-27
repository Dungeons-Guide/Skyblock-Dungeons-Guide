package kr.syeyoung.dungeonsguide.dungeon.mechanics;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import lombok.Data;

@Data
public class DungeonLever implements DungeonMechanic {
    private OffsetPoint lever = new OffsetPoint(0,0,0);

    private Triggered triggered;
}
