package kr.syeyoung.modapi;

import kr.syeyoung.modapi.data.Entity;
import kr.syeyoung.modapi.data.World;
import kr.syeyoung.modapi.events.EventBus;

public interface ModAPI {
    Platform getPlatform();

    World getPlayerWorld();
    Entity getPlayer();

    EventBus getEventBus();
}
