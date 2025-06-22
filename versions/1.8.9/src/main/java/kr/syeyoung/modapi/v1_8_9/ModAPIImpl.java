package kr.syeyoung.modapi.v1_8_9;

import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.ModAPIHolder;
import kr.syeyoung.modapi.Platform;
import kr.syeyoung.modapi.event.EventBus;
import kr.syeyoung.modapi.event.listenerlist.BasicEventBus;
import net.minecraftforge.common.ForgeVersion;

public class ModAPIImpl implements ModAPI {
    public ModAPIImpl() {}


    public static class PlatformImpl implements Platform {

        @Override
        public String getName() {
            return "forge";
        }

        @Override
        public String getMinecraftVersion() {
            return ForgeVersion.mcVersion;
        }

        @Override
        public String getPlatformVersion() {
            return ForgeVersion.getVersion();
        }

        public static final PlatformImpl INST = new PlatformImpl();
    }

    private static final EventBus defaultEventBus = new BasicEventBus();

    @Override
    public Platform getPlatform() {
        return PlatformImpl.INST;
    }

    @Override
    public EventBus getEventBus() {
        return defaultEventBus;
    }
}
