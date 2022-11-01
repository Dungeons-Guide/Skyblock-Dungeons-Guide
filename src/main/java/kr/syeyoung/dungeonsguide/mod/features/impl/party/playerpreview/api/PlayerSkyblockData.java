package kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api;

import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.playerprofile.PlayerProfile;
import lombok.Data;

@Data
public class PlayerSkyblockData {
    PlayerProfile[] playerProfiles;
    int lastestprofileArrayIndex;
}
