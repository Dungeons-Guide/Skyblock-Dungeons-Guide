package kr.syeyoung.dungeonsguide.dungeon.mechanics;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class DungeonSecret implements DungeonMechanic {
    private OffsetPoint secretPoint = new OffsetPoint(0,0,0);
    private SecretType secretType = SecretType.CHEST;

    public static enum SecretType {
        BAT, CHEST, ITEM_DROP
    }
}
