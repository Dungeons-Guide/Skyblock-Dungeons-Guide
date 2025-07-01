package kr.syeyoung.modapi;

import kr.syeyoung.modapi.audio.USoundHandler;
import kr.syeyoung.modapi.resources.UResourcePackRepository;
import kr.syeyoung.modapi.command.UCommandManager;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.UPlayerSelf;
import kr.syeyoung.modapi.entity.URenderManager;
import kr.syeyoung.modapi.event.EventBus;
import kr.syeyoung.modapi.fakeserver.FakeServerUtils;
import kr.syeyoung.modapi.gui.UContainerChest;
import kr.syeyoung.modapi.item.IItemStackRegistry;
import kr.syeyoung.modapi.profiler.UProfiler;
import kr.syeyoung.modapi.resources.UResourceManager;
import kr.syeyoung.modapi.settings.UGameSettings;
import kr.syeyoung.modapi.util.RaycastResult;
import kr.syeyoung.modapi.util.USession;
import kr.syeyoung.modapi.world.IBlockRegistry;
import kr.syeyoung.modapi.world.IMapUtils;
import kr.syeyoung.modapi.world.UWorld;
import net.kyori.adventure.text.Component;

public interface ModAPI {
    String getKeyDisplayString(int currentKey);

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

    UEntity getRenderViewEntity();

    RaycastResult getObjectMouseOver();

    IBlockRegistry getBlockRegistry();

    UCommandManager getCommandManager();

    boolean isSinglePlayer();

    UResourceManager getResourceManager();

    FakeServerUtils getFakeServerUtils();

    IItemStackRegistry getItemStackRegistry();

    boolean isDevEnv();

    UContainerChest extractContainerChest(Object object);

    IMapUtils getMapUtils();

    Component getHoveredComponent();

    UProfiler getProfiler();

    UGameSettings getGameSettings();

    void disableDefaultChatLogger();

    boolean isCallingFromMinecraftThread();

    UResourcePackRepository getResourcePackRepository();
}
