package kr.syeyoung.modapi.paralleluniverse.tablist;

import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.util.GameMode;

public interface UTabListEntry {

    String getFormatted();

    String getEffectiveName();

    String getPing();

    GameMode getGameMode();

    ResourceIdentifier getLocationSkin();

    String getEffectiveWithoutName();
}
