package kr.syeyoung.modapi;

import kr.syeyoung.modapi.audio.USoundHandler;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.UPlayerSelf;
import kr.syeyoung.modapi.entity.URenderManager;
import kr.syeyoung.modapi.event.EventBus;
import kr.syeyoung.modapi.util.USession;
import kr.syeyoung.modapi.world.UWorld;

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

    UWorld getWorld();


    void init();

    void unload();

    URenderManager getRenderManager();

    UEntity TEMPWRAP(Object object);
}
