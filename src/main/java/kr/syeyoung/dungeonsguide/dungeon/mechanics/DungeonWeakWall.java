package kr.syeyoung.dungeonsguide.dungeon.mechanics;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import lombok.Data;

@Data
public class DungeonWeakWall implements DungeonMechanic {
    OffsetPointSet weakWalls = new OffsetPointSet();
}
