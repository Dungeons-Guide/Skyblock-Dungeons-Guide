package kr.syeyoung.modapi.entity;

import kr.syeyoung.modapi.gui.UContainer;
import kr.syeyoung.modapi.item.UInventoryPlayer;
import kr.syeyoung.modapi.util.GameMode;
import net.kyori.adventure.audience.Audience;

public interface UPlayerSelf extends UEntityPlayer, Audience {
    String getClientBrand();
    UInventoryPlayer getInventory();
    UContainer getOpenContainer();

    void setOpenContainer(UContainer guiChest);

    void sendMessageToServer(String message);

    GameMode getGameMode();
}
