package kr.syeyoung.dungeonsguide.features.listener;

import kr.syeyoung.dungeonsguide.events.StompConnectedEvent;

public interface StompConnectedListener {
    void onStompConnected(StompConnectedEvent event);
}
