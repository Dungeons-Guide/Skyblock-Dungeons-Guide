/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import kr.syeyoung.dungeonsguide.launcher.DGInterface;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.gui.screen.GuiDisplayer;
import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.commands.CommandDgDebug;
import kr.syeyoung.dungeonsguide.mod.commands.CommandDungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.commands.CommandRegistrationHelper;
import kr.syeyoung.dungeonsguide.mod.config.Config;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3.ConfigGuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.config.onboarding.OnboardingPage;
import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticsManager;
import kr.syeyoung.dungeonsguide.mod.discord.DiscordIntegrationManager;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonFacade;
import kr.syeyoung.dungeonsguide.mod.events.annotations.EventHandlerRegistry;
import kr.syeyoung.dungeonsguide.mod.events.listener.DungeonListener;
import kr.syeyoung.dungeonsguide.mod.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.gui.GuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.gui.PassthroughManager;
import kr.syeyoung.dungeonsguide.mod.gui.elements.GlobalHUDScale;
import kr.syeyoung.dungeonsguide.mod.gui.elements.richtext.fonts.DefaultFontRenderer;
import kr.syeyoung.dungeonsguide.mod.gui.xml.DomElementRegistry;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayManager;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import kr.syeyoung.dungeonsguide.mod.player.PlayerManager;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderManager;
import kr.syeyoung.dungeonsguide.mod.stomp.StompManager;
import kr.syeyoung.dungeonsguide.mod.utils.TimeScoreUtil;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.GLCursors;
import kr.syeyoung.dungeonsguide.mod.wsresource.StaticResourceCache;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.command.UCommandContext;
import kr.syeyoung.modapi.event.AnnotatedListenerHelper;
import kr.syeyoung.modapi.event.ListenerRegistration;
import kr.syeyoung.modapi.event.SubscribeEvent;
import kr.syeyoung.modapi.event.events.ClientTickEvent;
import kr.syeyoung.modapi.event.events.RegisterCommandEvent;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.ListenerList;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DungeonsGuide implements DGInterface {

    @Getter
    private static boolean firstTimeUsingDG = false;
    Logger logger = LogManager.getLogger("DungeonsGuide");

    public boolean verbose = false;
    private SkyblockStatus skyblockStatus;

    @Getter
    private File tempDir = new File(Main.getConfigDir(), "tmp");

    @Getter
    public static final ThreadGroup THREAD_GROUP = new ThreadGroup("Dungeons Guide");

    public static final DefaultThreadFactory THREAD_FACTORY = new DefaultThreadFactory();

    static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            group = THREAD_GROUP;
            namePrefix = "pool-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    @Getter
    private CosmeticsManager cosmeticsManager;
    @Getter
    private DungeonFacade dungeonFacade;

    public DungeonsGuide(){
        instance = this;
    }
    private static DungeonsGuide instance;

    public static DungeonsGuide getDungeonsGuide() {
        return instance;
    }


    private List<Object> registeredListeners = new ArrayList<>();
    private List<ListenerRegistration> registeredMODAPIListeners = new ArrayList<>();
    public void registerEventsForge(Object object) {
        registeredListeners.add(object);
        MinecraftForge.EVENT_BUS.register(object);
        registeredMODAPIListeners.addAll(AnnotatedListenerHelper.registerListeners(ModAPI.getAPI().getEventBus(), object));

    }
    private List<ExecutorService> executorServices = new ArrayList<>();

    public ExecutorService registerExecutorService(ExecutorService executorService) {
        this.executorServices.add(executorService);
        return executorService;
    }

    public ScheduledExecutorService registerExecutorService(ScheduledExecutorService executorService) {
        this.executorServices.add(executorService);
        return executorService;
    }

    public void init(File f) {
        ProgressManager.ProgressBar progressbar = ProgressManager.push("DungeonsGuide", 5);


        ModAPI.getAPI().init();


        progressbar.step("Creating Configuration");

        tempDir.mkdirs();

        File configFile = new File(Main.getConfigDir(), "config.json");
        if (!configFile.exists()) {
            Main.getConfigDir().mkdirs();
            firstTimeUsingDG = true;
        }

        Config.f = configFile;
        Minecraft.getMinecraft().getFramebuffer().enableStencil();

        registerEventsForge(this);

        progressbar.step("Loading Native Libraries");

        try {
            NativeLoader.extractLibraryAndLoad("waterboard");
            NativeLoader.extractLibraryAndLoad("dptsp");
        } catch (IOException | UnsatisfiedLinkError | RuntimeException e) {
            e.printStackTrace();
        }

        progressbar.step("Registering Events & Commands");

        skyblockStatus = new SkyblockStatus();

        registerEventsForge(skyblockStatus);
        registerEventsForge(ChatTransmitter.INSTANCE);
        registerEventsForge(PassthroughManager.INSTANCE);

        FeatureRegistry.getFeatureList();






        registerEventsForge(new DungeonListener());
        this.dungeonFacade = new DungeonFacade();

        dungeonFacade.init();
        registerEventsForge(new Keybinds());

        registerEventsForge(PartyManager.INSTANCE);
        registerEventsForge(ChatProcessor.INSTANCE);
        registerEventsForge(PlayerManager.INSTANCE);
        registerEventsForge(StaticResourceCache.INSTANCE);
        registerEventsForge(OverlayManager.getEventHandler());


        progressbar.step("Opening connection");
        StompManager.getInstance().init();
        registerEventsForge(cosmeticsManager = new CosmeticsManager());


        progressbar.step("Loading Config");
        try {
            Config.loadConfig(null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        DiscordIntegrationManager.INSTANCE.isLoaded();

        for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
            abstractFeature.init();
        }


        TimeScoreUtil.init();

        ProgressManager.pop(progressbar);

        VersionInfo.checkAndOpen();

        Minecraft.getMinecraft().refreshResources();

        ModAPI.getAPI().getCommandManager().requestCommandReload();

        // Fix Parallel universe not working when player joins hypickle before dg loads
        if (firstTimeUsingDG) {
            GuiDisplayer.INSTANCE.displayGui(new GuiScreenAdapter(new GlobalHUDScale(new OnboardingPage("pages/front.gui")), null, false));
        }
    }

    // hotswap fails in dev env due to intellij auto log collection or smth. it holds ref to stacktrace.


    @Override
    public void unload() {

        ModAPI.getAPI().unload();

        StompManager.getInstance().cleanup();
        // have FUN!


        for (Object registeredListener : registeredListeners) {
            MinecraftForge.EVENT_BUS.unregister(registeredListener);
        }

        for (ListenerRegistration registeredMODAPIListener : registeredMODAPIListeners) {
            ModAPI.getAPI().getEventBus().unregisterListener(registeredMODAPIListener);
        }
        EventHandlerRegistry.unregisterListeners();

        List<ListenerList> all = ReflectionHelper.getPrivateValue(ListenerList.class, null, "allLists");
        int busId = ReflectionHelper.getPrivateValue(EventBus.class, MinecraftForge.EVENT_BUS, "busID");
        for (ListenerList listenerList : all) {
            Object[] list = ReflectionHelper.getPrivateValue(ListenerList.class, listenerList, "lists");
            Object inst = list[busId];
            try {
                Method m = inst.getClass().getDeclaredMethod("buildCache"); // refresh cache
                m.setAccessible(true);
                m.invoke(inst);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        Map<ResourceLocation, ITextureObject> mapTextureObjects = ReflectionHelper.getPrivateValue(TextureManager.class, Minecraft.getMinecraft().getTextureManager(), "mapTextureObjects", "field_110585_a", "b");
        for (ITextureObject value : mapTextureObjects.values()) {
            if (value instanceof ThreadDownloadImageData) {
                ReflectionHelper.setPrivateValue(ThreadDownloadImageData.class,(ThreadDownloadImageData) value, null, "imageBuffer", "field_110563_c", "k");
            }
        }
        Set<ResourceLocation> toRemove = new HashSet<>();
        for (Map.Entry<ResourceLocation, ITextureObject> resourceLocationITextureObjectEntry : mapTextureObjects.entrySet()) {
            if (resourceLocationITextureObjectEntry.getKey().getResourceDomain().equalsIgnoreCase("dungeonsguide"))
                toRemove.add(resourceLocationITextureObjectEntry.getKey());
        }
        for (ResourceLocation resourceLocation : toRemove) {
            ITextureObject textureObject = mapTextureObjects.remove(resourceLocation);
        }


        ShaderManager.unload();
        GLCursors.cleanup();
        DiscordIntegrationManager.INSTANCE.cleanup();

        for (ExecutorService executorService : executorServices) {
            executorService.shutdownNow();
        }

//        try {
//            Cleaner cleaner = Cleaner.getCleaner();
//            Thread t = ReflectionHelper.getPrivateValue(Cleaner.class, cleaner, "cleanerThread");
//            t.stop();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

        THREAD_GROUP.interrupt();
        THREAD_GROUP.stop();

        try {
            Thread.sleep(2000); // This is requirement for all the threads to finish within 1 second. or reference leak.
        } catch (InterruptedException e) {
        }
        THREAD_GROUP.destroy();

    }

    @Override
    public void onResourceReload(IResourceManager a) {
        GLCursors.setupCursors();
        DefaultFontRenderer.DEFAULT_RENDERER.onResourceManagerReload();
        ShaderManager.onResourceReload();
        DomElementRegistry.onResourceManagerReload();

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        byte[] glypthWidths = ReflectionHelper.getPrivateValue(FontRenderer.class, fontRenderer, "glyphWidth", "field_78287_e", "field_2819", "e");
        for (int i = 0; i < 255; i++) {
            glypthWidths[0xed00 + i] = 14;
        }
        glypthWidths[0xed02] = 1;
    }


    public SkyblockStatus getSkyblockStatus() {
        return skyblockStatus;
    }


    @Override
    public Class<? extends GuiScreen> getModConfigGUI() {
        return ConfigGuiScreenAdapter.class;
    }


    private LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

    public void runNextTick(Runnable r) {
        try {
            tasks.offer(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent tickEvent) {
        while (!tasks.isEmpty()) {
            try {
                tasks.poll().run();
            } catch (Exception e) {
                FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandEvent commandEvent) {
        CommandRegistrationHelper.registerCommands(commandEvent.getCommandManager(), new CommandDungeonsGuide());
        CommandRegistrationHelper.registerCommands(commandEvent.getCommandManager(), new CommandDgDebug());

        CommandNode<UCommandContext> cmd = commandEvent.getCommandManager().getCommandNode("dg");

        for (String alias : Arrays.asList("dg", "dungeonsguide", "dungeonguide", "deegee", "던전가이드", "던전안내")) {
            commandEvent.getCommandManager().registerCommand(
                    LiteralArgumentBuilder.<UCommandContext>literal(alias).redirect(cmd)
            );
        }
    }
}
