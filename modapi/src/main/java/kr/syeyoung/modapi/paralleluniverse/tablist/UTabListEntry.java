package kr.syeyoung.modapi.paralleluniverse.tablist;

import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.util.GameMode;

import java.util.UUID;

public interface UTabListEntry {

    String getFormatted();

    String getEffectiveName();

    int getPing();

    GameMode getGameMode();

    ResourceIdentifier getLocationSkin();

    String getEffectiveWithoutName();

    UUID getUUID();

    String getPlayerName();
}
