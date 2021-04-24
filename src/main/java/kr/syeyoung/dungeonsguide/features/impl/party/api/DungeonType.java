package kr.syeyoung.dungeonsguide.features.impl.party.api;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public enum DungeonType {
    CATACOMBS("catacombs", "The Catacombs",
            Sets.newHashSet(0,1,2,3,4,5,6,7)),
    MASTER_CATACOMBS("master_catacombs", "MasterMode Catacombs", Sets.newHashSet(
            1,2,3,4,5,6
    ));

    private final String jsonName;
    private final String familiarName;
    private final Set<Integer> validFloors ;
}
