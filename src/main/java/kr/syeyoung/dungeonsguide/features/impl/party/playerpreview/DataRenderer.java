package kr.syeyoung.dungeonsguide.features.impl.party.playerpreview;

import kr.syeyoung.dungeonsguide.features.impl.party.api.PlayerProfile;

import java.awt.*;

public interface DataRenderer {
    Dimension renderData(PlayerProfile playerProfile);
}
