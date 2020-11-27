package kr.syeyoung.dungeonsguide.dungeon.mechanics;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import lombok.Data;

@Data
public class DungeonDoor implements Triggered {
    private OffsetPointSet offsetPointSet = new OffsetPointSet();
    private Triggering triggering;
}
