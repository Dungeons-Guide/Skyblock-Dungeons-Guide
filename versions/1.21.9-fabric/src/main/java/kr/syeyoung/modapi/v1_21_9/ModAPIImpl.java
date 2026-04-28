package kr.syeyoung.modapi.v1_21_9;

import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.PathDisplayEngineSettingRegistry;
import kr.syeyoung.modapi.AuthService;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.Platform;
import kr.syeyoung.modapi.audio.USoundHandler;
import kr.syeyoung.modapi.data.UBossBar;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.UEntityPlayerFake;
import kr.syeyoung.modapi.entity.UPlayerSelf;
import kr.syeyoung.modapi.entity.URenderManager;
import kr.syeyoung.modapi.event.EventBus;
import kr.syeyoung.modapi.event.listenerlist.BasicEventBus;
import kr.syeyoung.modapi.fakeserver.FakeServerUtils;
import kr.syeyoung.modapi.gui.UCustomGuiScreen;
import kr.syeyoung.modapi.gui.UGuiScreen;
import kr.syeyoung.modapi.item.IItemStackRegistry;
import kr.syeyoung.modapi.paralleluniverse.scoreboard.UScoreboardManager;
import kr.syeyoung.modapi.paralleluniverse.tablist.UTabList;
import kr.syeyoung.modapi.profiler.UProfiler;
import kr.syeyoung.modapi.rendering.UFontCalculator;
import kr.syeyoung.modapi.rendering.UTextureManager;
import kr.syeyoung.modapi.resources.UResourceManager;
import kr.syeyoung.modapi.resources.UResourcePackRepository;
import kr.syeyoung.modapi.settings.UGameSettings;
import kr.syeyoung.modapi.util.EnumCursor;
import kr.syeyoung.modapi.util.RaycastResult;
import kr.syeyoung.modapi.util.USession;
import kr.syeyoung.modapi.v1_21_9.audio.USoundHandlerImpl;
import kr.syeyoung.modapi.v1_21_9.command.CommandManagerImpl;
import kr.syeyoung.modapi.v1_21_9.entity.UEntityDelegateFactory;
import kr.syeyoung.modapi.v1_21_9.entity.UEntityFakePlayer;
import kr.syeyoung.modapi.v1_21_9.entity.UEntityPlayerSP;
import kr.syeyoung.modapi.v1_21_9.entity.URenderManagerImpl;
import kr.syeyoung.modapi.v1_21_9.fakeserver.BlockAccessibleServerLaunchUtils;
import kr.syeyoung.modapi.v1_21_9.gui.UGuiScreenAdapter;
import kr.syeyoung.modapi.v1_21_9.gui.UNativeGuiScreen;
import kr.syeyoung.modapi.v1_21_9.item.IItemStackRegistryImpl;
import kr.syeyoung.modapi.v1_21_9.map.MapDataManager;
import kr.syeyoung.modapi.v1_21_9.mod.arrowpath.NeoRouteDisplayEngineRegistration;
import kr.syeyoung.modapi.v1_21_9.mod.classic.ClassicPathDisplayEngineRegistration;
import kr.syeyoung.modapi.v1_21_9.paralleluniverse.scoreboard.ScoreboardManager;
import kr.syeyoung.modapi.v1_21_9.paralleluniverse.tab.TabList;
import kr.syeyoung.modapi.v1_21_9.profiler.UProfilerImpl;
import kr.syeyoung.modapi.v1_21_9.render.UFontCalculatorImpl;
import kr.syeyoung.modapi.v1_21_9.render.UTextureManagerImpl;
import kr.syeyoung.modapi.v1_21_9.resources.UResourceManagerImpl;
import kr.syeyoung.modapi.v1_21_9.resources.UResourcePackRepositoryImpl;
import kr.syeyoung.modapi.v1_21_9.settings.UGameSettingsImpl;
import kr.syeyoung.modapi.v1_21_9.util.TextUtils;
import kr.syeyoung.modapi.v1_21_9.util.USessionImpl;
import kr.syeyoung.modapi.v1_21_9.world.BlockStateRegistryImpl;
import kr.syeyoung.modapi.v1_21_9.world.UWorldImpl;
import kr.syeyoung.modapi.world.IBlockAccessible;
import kr.syeyoung.modapi.world.IBlockRegistry;
import kr.syeyoung.modapi.world.IMapUtils;
import kr.syeyoung.modapi.world.UWorld;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.profiler.Profilers;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ModAPIImpl implements ModAPI {

    public ModAPIImpl() {
    }

    public URenderManager getRenderManager() {
        return new URenderManagerImpl(MinecraftClient.getInstance().getEntityRenderDispatcher());
    }

    public UEntity getRenderViewEntity() {
        return MinecraftClient.getInstance().getCameraEntity() == null ? null : UEntityDelegateFactory.createEntityFor(MinecraftClient.getInstance().getCameraEntity());
    }

    public RaycastResult getObjectMouseOver() {
//        MinecraftClient.getInstance().tra
        HitResult hitres = MinecraftClient.getInstance().crosshairTarget;

        if (hitres == null) return new RaycastResult(null, RaycastResult.HitType.MISS, null, null);

        if (hitres instanceof EntityHitResult position) {
            return new RaycastResult(
                    null,
                    RaycastResult.HitType.ENTITY,
                    new Vector3D(position.getPos().x, position.getPos().y, position.getPos().z),
                    UEntityDelegateFactory.createEntityFor(position.getEntity())
            );
        } else if (hitres instanceof BlockHitResult position) {
            return new RaycastResult(
                    position.getPos() == null ? null : new VectorI3D(position.getBlockPos().getX(), position.getBlockPos().getY(), position.getBlockPos().getZ()),
                    RaycastResult.HitType.BLOCK,
                    new Vector3D(position.getPos().x, position.getPos().y, position.getPos().z),
                    null
            );
        } else {
            return new RaycastResult(
                    null,
                    RaycastResult.HitType.MISS,
                    null, null
            );
        }
    }

    public boolean isSinglePlayer() {
        return MinecraftClient.getInstance().isInSingleplayer();
    }

    public UResourceManager getResourceManager() {
        return new UResourceManagerImpl(MinecraftClient.getInstance().getResourceManager());
    }


//    private UProfiler profiler = new UProfilerImpl(Profilers.get());

    public UProfiler getProfiler() {
        return new UProfilerImpl(Profilers.get());
    }

    public UGameSettings getGameSettings() {
        return new UGameSettingsImpl(MinecraftClient.getInstance().options);
    }

    public boolean isCallingFromMinecraftThread() {
        return MinecraftClient.getInstance().isOnThread();
    }

    public UResourcePackRepository getResourcePackRepository() {
        return new UResourcePackRepositoryImpl(MinecraftClient.getInstance().getResourcePackManager());
    }


    public static class PlatformImpl implements Platform {

        @Override
        public String getName() {
            return "fabric";
        }

        @Override
        public String getMinecraftVersion() {
            return "1.21.5";
        }

        @Override
        public String getPlatformVersion() {
            return FabricLoader.getInstance().getModContainer("fabricloader")
                    .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                    .orElse("unknown");
        }

        @Override
        public boolean isOldChat() {
            return false;
        }

        @Override
        public boolean supportCopyClickEvent() {
            return true;
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
        return new USessionImpl(MinecraftClient.getInstance().getSession());
    }

    public USoundHandler getSoundHandler() {
        return new USoundHandlerImpl(MinecraftClient.getInstance().getSoundManager());
    }

    public int getDisplayWidth() {
        return MinecraftClient.getInstance().getWindow().getWidth();
    }

    public int getDisplayHeight() {
        return MinecraftClient.getInstance().getWindow().getHeight();
    }

    public UPlayerSelf getPlayer() {
        return MinecraftClient.getInstance().player == null ? null : new UEntityPlayerSP(MinecraftClient.getInstance().player);
    }

    @Override
    public UWorld getWorld() {
        return MinecraftClient.getInstance().world == null ? null :
                new UWorldImpl(MinecraftClient.getInstance().world, (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry());
    }

    @Override
    public CommandManagerImpl getCommandManager() {
        return commandManager;
    }

    @Getter
    private PacketListener packetInjector = new PacketListener();
    @Getter
    private EventListener eventListener = new EventListener();

    @Override
    public void init() {
//        MinecraftForge.EVENT_BUS.register(packetInjector);
        eventListener.init();
        registry.init();
        commandManager.init();

        PathDisplayEngineSettingRegistry.register(ClassicPathDisplayEngineRegistration.INSTANCE);
        PathDisplayEngineSettingRegistry.register(NeoRouteDisplayEngineRegistration.INSTANCE);

//        try {
//            Set<String> invalid = ReflectionHelper.getPrivateValue(LaunchClassLoader.class, (LaunchClassLoader) Main.class.getClassLoader(), "invalidClasses");
//            ((LaunchClassLoader) Main.class.getClassLoader()).clearNegativeEntries(Sets.newHashSet("org.slf4j.LoggerFactory"));
//            invalid.clear();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        try {
//            List<IResourcePack> resourcePackList = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "defaultResourcePacks", "aA", "field_110449_ao");
//            resourcePackList.add(new DGTexturePack());
//            Minecraft.getMinecraft().refreshResources();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        if (Minecraft.getMinecraft().getNetHandler() != null)
//            Minecraft.getMinecraft().getNetHandler().getNetworkManager().channel().pipeline().addBefore("packet_handler", "dg_packet_handler_2", packetInjector);
    }

    @Override
    public void unload() {
//        MinecraftForge.EVENT_BUS.unregister(packetInjector);
//        CustomNetworkPlayerInfoUnloader.unload();
//
//        commandManager.unregisterCommands();
//        packetInjector.cleanup();

//        try {
//            List<IResourcePack> resourcePackList = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "defaultResourcePacks", "aA", "field_110449_ao");
//            resourcePackList.removeIf(a -> a instanceof DGTexturePack);
//            Minecraft.getMinecraft().refreshResources();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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
                return false; // TODO: impl
            }
        };
    }

    @Override
    public IItemStackRegistry getItemStackRegistry() {
        return new IItemStackRegistryImpl();
    }

    @Override
    public boolean isDevEnv() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public IMapUtils getMapUtils() {
        return MapDataManager.INSTANCE;
    }

    @Override
    public Component getHoveredComponent() {
        Style style = MinecraftClient.getInstance().inGameHud.getChatHud().getTextStyleAt(MinecraftClient.getInstance().mouse.getX(), MinecraftClient.getInstance().mouse.getY());
        if (style == null) return null;
        Text t = Text.literal("").setStyle(style);

        JsonElement element = TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, t).getOrThrow();

        return GsonComponentSerializer.gson().deserializeFromTree(element);
    }

    @Override
    public void disableDefaultChatLogger() { // TODO: impl
//        Logger l = LogManager.getLogger(GuiNewChat.class);
//        if (l instanceof SimpleLogger) {
//            ((SimpleLogger) l).setLevel(Level.OFF);
//        } else if (l instanceof org.apache.logging.log4j.core.Logger) {
//            ((org.apache.logging.log4j.core.Logger) l).setLevel(Level.OFF);
//        }
    }

    public UScoreboardManager getScoreboardManager() {
        return ScoreboardManager.INSTANCE;
    }

    public UTabList getTabList() {
        return TabList.INSTANCE;
    }

    @Override
    public String getKeyDisplayString(int currentKey) {
        return InputUtil.fromKeyCode(new KeyInput(currentKey, currentKey,0)).getLocalizedText().getString();
    }

    @Override
    public void displayGuiScreen(UGuiScreen guiScreen) {
        if (guiScreen == null) MinecraftClient.getInstance().setScreen(null);
        else MinecraftClient.getInstance().setScreen(new UGuiScreenAdapter((UCustomGuiScreen) guiScreen));
    }

    @Override
    public UGuiScreen getCurrentGuiScreen() {
        Screen s = MinecraftClient.getInstance().currentScreen;
        if (s instanceof UGuiScreenAdapter screen) return screen.getDelegate();
        else return UNativeGuiScreen.getUScreen(s);
    }

    @Override
    public double getScaleFactor() {
        return MinecraftClient.getInstance().getWindow().getScaleFactor();
    }

    @Override
    public UFontCalculator getFontCalculator() {
        return UFontCalculatorImpl.INSTANCE; // $$ welp
    }

    @Override
    public UTextureManager getTextureManager() {
        return UTextureManagerImpl.INSTANCE;
    }

    @Override
    public UEntityPlayerFake createFakePlayer(UUID uuid, String name) {
        return new UEntityFakePlayer(new UEntityFakePlayer.FakePlayer(new GameProfile(uuid, name)));
    }

    @Override
    public void purgeCache() {
        // WHAT?
    }

    @Override
    public List<UBossBar> getBossBars() {
//        MinecraftClient.getInstance().getWindow().
        List<UBossBar> bossBars = new ArrayList<>();
        for (Map.Entry<UUID, ClientBossBar> uuidClientBossBarEntry : MinecraftClient.getInstance().inGameHud.getBossBarHud().bossBars.entrySet()) {
            bossBars.add(new UBossBar(TextUtils.fromText(uuidClientBossBarEntry.getValue().getName()), uuidClientBossBarEntry.getValue().getPercent()));
        }
        return bossBars;
    }

    @Override
    public void refreshResources() {
        MinecraftClient.getInstance().reloadResources();
    }

    @Override
    public void exit(int code, boolean hardexit) {
        System.exit(code);
    }

    @Override
    public void setMouseCursor(EnumCursor enumCursor) {
//        MinecraftClient.getInstance().getWindow().setCursor(
//                enumCursor.
//        );
        // TODO: cursor $$
    }

    @Override
    public void setCursorPosition(int cursorX, int cursorY) {
        GLFW.glfwSetCursorPos(MinecraftClient.getInstance().getWindow().getHandle(), cursorX, cursorY);
    }

    @Override
    public AuthService getAuthService() {
        return AuthServiceImpl.INSTANCE;
    }
}
