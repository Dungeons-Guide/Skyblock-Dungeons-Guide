package kr.syeyoung.dungeonsguide.features.impl.etc.ability;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SkyblockAbility {
    private String name;
    private int manaCost;
    private int cooldown;

    private String itemId;
}
