package kr.syeyoung.modapi;

import kr.syeyoung.modapi.audio.USoundHandler;
import kr.syeyoung.modapi.entity.UPlayerSelf;
import kr.syeyoung.modapi.event.EventBus;
import kr.syeyoung.modapi.util.USession;

public interface ModAPI {
    Platform getPlatform();

    EventBus getEventBus();

    static ModAPI getAPI() {
        return ModAPIHolder.modAPI;
    }

    USession getSession();

    USoundHandler getSoundHandler();

    int getDisplayWidth();

    int getDisplayHeight();

    UPlayerSelf getPlayer();
}
