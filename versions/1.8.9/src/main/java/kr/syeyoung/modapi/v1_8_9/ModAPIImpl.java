package kr.syeyoung.modapi.v1_8_9;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.Platform;
import kr.syeyoung.modapi.audio.USoundHandler;
import kr.syeyoung.modapi.paralleluniverse.scoreboard.UScoreboardManager;
import kr.syeyoung.modapi.paralleluniverse.tablist.UTabList;
import kr.syeyoung.modapi.resources.UResourcePackRepository;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.UPlayerSelf;
import kr.syeyoung.modapi.entity.URenderManager;
import kr.syeyoung.modapi.event.EventBus;
import kr.syeyoung.modapi.event.listenerlist.BasicEventBus;
import kr.syeyoung.modapi.fakeserver.FakeServerUtils;
import kr.syeyoung.modapi.gui.UContainerChest;
import kr.syeyoung.modapi.item.IItemStackRegistry;
import kr.syeyoung.modapi.profiler.UProfiler;
import kr.syeyoung.modapi.resources.UResourceManager;
import kr.syeyoung.modapi.settings.UGameSettings;
import kr.syeyoung.modapi.util.RaycastResult;
import kr.syeyoung.modapi.util.USession;
import kr.syeyoung.modapi.v1_8_9.audio.USoundHandlerImpl;
import kr.syeyoung.modapi.v1_8_9.client.renderer.entity.URenderManagerImpl;
import kr.syeyoung.modapi.v1_8_9.paralleluniverse.scoreboard.ScoreboardManager;
import kr.syeyoung.modapi.v1_8_9.paralleluniverse.tab.TabList;
import kr.syeyoung.modapi.v1_8_9.resources.UResourcePackRepositoryImpl;
import kr.syeyoung.modapi.v1_8_9.command.CommandManagerImpl;
import kr.syeyoung.modapi.v1_8_9.entity.UEntityDelegateFactory;
import kr.syeyoung.modapi.v1_8_9.entity.UEntityPlayerSP;
import kr.syeyoung.modapi.v1_8_9.fakeserver.BlockAccessibleServerLaunchUtils;
import kr.syeyoung.modapi.v1_8_9.gui.UContainerChestImpl;
import kr.syeyoung.modapi.v1_8_9.item.IItemStackRegistryImpl;
import kr.syeyoung.modapi.v1_8_9.map.MapDataManager;
import kr.syeyoung.modapi.v1_8_9.profiler.UProfilerImpl;
import kr.syeyoung.modapi.v1_8_9.resources.DGTexturePack;
import kr.syeyoung.modapi.v1_8_9.resources.UResourceManagerImpl;
import kr.syeyoung.modapi.v1_8_9.settings.UGameSettingsImpl;
import kr.syeyoung.modapi.v1_8_9.util.CustomNetworkPlayerInfoUnloader;
import kr.syeyoung.modapi.v1_8_9.util.USessionImpl;
import kr.syeyoung.modapi.v1_8_9.world.BlockStateRegistryImpl;
import kr.syeyoung.modapi.v1_8_9.world.UWorldImpl;
import kr.syeyoung.modapi.world.IBlockAccessible;
import kr.syeyoung.modapi.world.IBlockRegistry;
import kr.syeyoung.modapi.world.IMapUtils;
import kr.syeyoung.modapi.world.UWorld;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.legacyimpl.NBTLegacyHoverEventSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.event.HoverEvent;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.simple.SimpleLogger;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ModAPIImpl implements ModAPI {
    Minecraft delegate; // dummy to trick. TODO

    public ModAPIImpl() {
    }

    public URenderManager getRenderManager() {
        return new URenderManagerImpl(Minecraft.getMinecraft().getRenderManager());
    }

    public UEntity getRenderViewEntity() {
        return Minecraft.getMinecraft().getRenderViewEntity() == null ? null : UEntityDelegateFactory.createEntityFor(Minecraft.getMinecraft().getRenderViewEntity());
    }

    public RaycastResult getObjectMouseOver() {
        MovingObjectPosition position = Minecraft.getMinecraft().objectMouseOver;

        if (position == null) return new RaycastResult(null, RaycastResult.HitType.MISS, null, null);
        return new RaycastResult(
                position.getBlockPos() == null ? null : new VectorI3D(position.getBlockPos().getX(), position.getBlockPos().getY(), position.getBlockPos().getZ()),
                position.typeOfHit == MovingObjectPosition.MovingObjectType.MISS ? RaycastResult.HitType.MISS :
                        position.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY ? RaycastResult.HitType.ENTITY :
                                RaycastResult.HitType.BLOCK,
                position.hitVec == null ? null : new Vector3D(position.hitVec.xCoord, position.hitVec.yCoord, position.hitVec.zCoord),
                position.entityHit == null ? null : UEntityDelegateFactory.createEntityFor(position.entityHit)
        );
    }

    public boolean isSinglePlayer() {
        return Minecraft.getMinecraft().isSingleplayer();
    }

    public UResourceManager getResourceManager() {
        return new UResourceManagerImpl(Minecraft.getMinecraft().getResourceManager());
    }


    private UProfiler profiler = new UProfilerImpl(Minecraft.getMinecraft().mcProfiler);

    public UProfiler getProfiler() {
        return profiler;
    }

    public UGameSettings getGameSettings() {
        return new UGameSettingsImpl(Minecraft.getMinecraft().gameSettings);
    }

    public boolean isCallingFromMinecraftThread() {
        return Minecraft.getMinecraft().isCallingFromMinecraftThread();
    }

    public UResourcePackRepository getResourcePackRepository() {
        return new UResourcePackRepositoryImpl(Minecraft.getMinecraft().getResourcePackRepository());
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

        @Override
        public boolean isOldChat() {
            return true;
        }

        @Override
        public boolean supportCopyClickEvent() {
            return false;
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
        return Minecraft.getMinecraft().thePlayer == null ? null : new UEntityPlayerSP(Minecraft.getMinecraft().thePlayer);
    }

    @Override
    public UWorld getWorld() {
        return Minecraft.getMinecraft().theWorld == null ? null : new UWorldImpl(Minecraft.getMinecraft().theWorld, (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry());
    }

    @Override
    public CommandManagerImpl getCommandManager() {
        return commandManager;
    }

    private PacketInjector packetInjector = new PacketInjector();
    private EventListener eventListener = new EventListener();

    @Override
    public void init() {
        MinecraftForge.EVENT_BUS.register(packetInjector);
        eventListener.register();
        registry.init();


        try {
            Set<String> invalid = ReflectionHelper.getPrivateValue(LaunchClassLoader.class, (LaunchClassLoader) Main.class.getClassLoader(), "invalidClasses");
            ((LaunchClassLoader) Main.class.getClassLoader()).clearNegativeEntries(Sets.newHashSet("org.slf4j.LoggerFactory"));
            invalid.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            List<IResourcePack> resourcePackList = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "defaultResourcePacks", "aA", "field_110449_ao");
            resourcePackList.add(new DGTexturePack());
            Minecraft.getMinecraft().refreshResources();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Minecraft.getMinecraft().getNetHandler() != null)
            Minecraft.getMinecraft().getNetHandler().getNetworkManager().channel().pipeline().addBefore("packet_handler", "dg_packet_handler_2", packetInjector);
    }

    @Override
    public void unload() {
        MinecraftForge.EVENT_BUS.unregister(packetInjector);
        CustomNetworkPlayerInfoUnloader.unload();
        eventListener.unregister();

        commandManager.unregisterCommands();
        packetInjector.cleanup();

        try {
            List<IResourcePack> resourcePackList = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "defaultResourcePacks", "aA", "field_110449_ao");
            resourcePackList.removeIf(a -> a instanceof DGTexturePack);
            Minecraft.getMinecraft().refreshResources();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BlockStateRegistryImpl registry = new BlockStateRegistryImpl();
    private CommandManagerImpl commandManager = new CommandManagerImpl();


    @Override
    public IBlockRegistry getBlockRegistry() {
        return registry;
    }

    @Override
    public FakeServerUtils getFakeServerUtils() {
        return new FakeServerUtils() {
            @Override
            public void launchFakeServerAndJoin(IBlockAccessible accessible) {
                BlockAccessibleServerLaunchUtils.launchDungeonServerAndJoin(accessible);
            }

            @Override
            public boolean isRunning() {
                return BlockAccessibleServerLaunchUtils.isDungeonIntegratedServerRunning();
            }
        };
    }

    @Override
    public UContainerChest extractContainerChest(Object object) {
        if (object instanceof GuiChest) {
            return new UContainerChestImpl((ContainerChest) ((GuiChest) object).inventorySlots);
        }
        return null;
    }

    @Override
    public IItemStackRegistry getItemStackRegistry() {
        return new IItemStackRegistryImpl();
    }

    @Override
    public boolean isDevEnv() {
        return (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
    }

    @Override
    public IMapUtils getMapUtils() {
        return MapDataManager.INSTANCE;
    }

    @Override
    public Component getHoveredComponent() {
        IChatComponent ichatcomponent = Minecraft.getMinecraft().ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());
        if (ichatcomponent == null) return null;
        if (ichatcomponent.getChatStyle() == null) return null;
        if (ichatcomponent.getChatStyle().getChatHoverEvent() == null) return null;
        HoverEvent hoverEvent = ichatcomponent.getChatStyle().getChatHoverEvent();
        if (hoverEvent.getAction() != HoverEvent.Action.SHOW_ITEM) return null;
        try {
            net.kyori.adventure.text.event.HoverEvent.ShowItem showItem = NBTLegacyHoverEventSerializer.get().deserializeShowItem(
                    Component.text(hoverEvent.getValue().getUnformattedText())
            );
            return Component.text(ichatcomponent.getUnformattedText()).hoverEvent(net.kyori.adventure.text.event.HoverEvent.showItem(showItem));
        } catch (IOException e) {
            return null;
        }

//        String json = IChatComponent.Serializer.componentToJson(ichatcomponent);
//        return GsonComponentSerializer.colorDownsamplingGson().deserialize(json); apparently adventure has a bug where it is unable to deserialize legacy hover event. welp. #890. but I'm in a rush to impl 1.21 so let me just hack a solution.
    }

    @Override
    public void disableDefaultChatLogger() {
        Logger l = LogManager.getLogger(GuiNewChat.class);
        if (l instanceof SimpleLogger) {
            ((SimpleLogger) l).setLevel(Level.OFF);
        } else if (l instanceof org.apache.logging.log4j.core.Logger) {
            ((org.apache.logging.log4j.core.Logger) l).setLevel(Level.OFF);
        }
    }

    public UScoreboardManager getScoreboardManager() {
        return ScoreboardManager.INSTANCE;
    }

    public UTabList getTabList() {
        return TabList.INSTANCE;
    }

    @Override
    public String getKeyDisplayString(int currentKey) {
        return GameSettings.getKeyDisplayString(currentKey);
    }
}
