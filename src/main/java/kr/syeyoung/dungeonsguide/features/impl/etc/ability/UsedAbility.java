package kr.syeyoung.dungeonsguide.features.impl.etc.ability;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
public class UsedAbility {
    private SkyblockAbility ability;
    @EqualsAndHashCode.Exclude
    private long cooldownEnd;
}
