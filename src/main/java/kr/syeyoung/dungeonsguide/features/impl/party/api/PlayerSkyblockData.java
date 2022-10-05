package kr.syeyoung.dungeonsguide.features.impl.party.api;

import lombok.Data;

@Data
public class PlayerSkyblockData {
    PlayerProfile[] playerProfiles;
    int lastestprofileArrayIndex;
}
