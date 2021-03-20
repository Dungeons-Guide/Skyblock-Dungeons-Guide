package kr.syeyoung.dungeonsguide.features.listener;

import net.minecraft.network.play.server.S45PacketTitle;

public interface TitleListener {
    void onTitle(S45PacketTitle renderPlayerEvent);
}
