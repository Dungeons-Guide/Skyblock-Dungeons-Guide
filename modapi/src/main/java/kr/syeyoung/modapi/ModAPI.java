package kr.syeyoung.modapi;

import kr.syeyoung.modapi.event.EventBus;

public interface ModAPI {
    Platform getPlatform();
    EventBus getEventBus();
    static ModAPI getAPI() {
        return ModAPIHolder.modAPI;
    }
}
