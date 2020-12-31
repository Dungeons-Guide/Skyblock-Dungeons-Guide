package kr.syeyoung.dungeonsguide.features.listener;

import net.minecraftforge.client.event.ClientChatReceivedEvent;

public interface ChatListener {
    void onChat(ClientChatReceivedEvent clientChatReceivedEvent);
}
