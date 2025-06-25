package kr.syeyoung.modapi.v1_8_9;

import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.Platform;
import kr.syeyoung.modapi.audio.USoundHandler;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.UPlayerSelf;
import kr.syeyoung.modapi.entity.URenderManager;
import kr.syeyoung.modapi.event.EventBus;
import kr.syeyoung.modapi.event.listenerlist.BasicEventBus;
import kr.syeyoung.modapi.util.USession;
import kr.syeyoung.modapi.v1_8_9.audio.USoundHandlerImpl;
import kr.syeyoung.modapi.v1_8_9.client.renderer.entity.URenderManagerImpl;
import kr.syeyoung.modapi.v1_8_9.entity.UEntityDelegateFactory;
import kr.syeyoung.modapi.v1_8_9.entity.UEntityPlayerSP;
import kr.syeyoung.modapi.v1_8_9.util.USessionImpl;
import kr.syeyoung.modapi.v1_8_9.world.UWorldImpl;
import kr.syeyoung.modapi.world.UWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;

public class ModAPIImpl implements ModAPI {
    Minecraft delegate; // dummy to trick. TODO

    public ModAPIImpl() {
    }

    public URenderManager getRenderManager() {
        return new URenderManagerImpl(delegate.getRenderManager());
    }


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

    @Override
    public USession getSession() {
        return new USessionImpl(Minecraft.getMinecraft().getSession());
    }

    public USoundHandler getSoundHandler() {
        return new USoundHandlerImpl(Minecraft.getMinecraft().getSoundHandler());
    }

    public int getDisplayWidth() {
        return Minecraft.getMinecraft().displayWidth;
    }

    public int getDisplayHeight() {
        return Minecraft.getMinecraft().displayHeight;
    }

    public UPlayerSelf getPlayer() {
        return delegate.thePlayer == null ? null : new UEntityPlayerSP(delegate.thePlayer);
    }

    @Override
    public UWorld getWorld() {
        return delegate.theWorld == null ? null : new UWorldImpl(delegate.theWorld);
    }

    @Override
    public UEntity TEMPWRAP(Object object) {
        return UEntityDelegateFactory.createEntityFor((Entity) object);
    }

    private PacketInjector packetInjector = new PacketInjector();
    private EventListener eventListener = new EventListener();

    @Override
    public void init() {
        MinecraftForge.EVENT_BUS.register(packetInjector);
        MinecraftForge.EVENT_BUS.register(eventListener);

        if (Minecraft.getMinecraft().getNetHandler() != null)
            Minecraft.getMinecraft().getNetHandler().getNetworkManager().channel().pipeline().addBefore("packet_handler", "dg_packet_handler", packetInjector);
    }

    @Override
    public void unload() {
        MinecraftForge.EVENT_BUS.unregister(packetInjector);
        MinecraftForge.EVENT_BUS.unregister(eventListener);

        packetInjector.cleanup();
    }



}
