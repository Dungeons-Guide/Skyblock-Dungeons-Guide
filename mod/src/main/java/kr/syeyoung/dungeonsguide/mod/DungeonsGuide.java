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
import kr.syeyoung.dungeonguide.loader.DGInterface;
import kr.syeyoung.dungeonguide.loader.LoaderAPI;
import kr.syeyoung.dungeonsguide.authapi.auth.AuthManager;
import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.commands.CommandDgDebug;
import kr.syeyoung.dungeonsguide.mod.commands.CommandDungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.commands.CommandRegistrationHelper;
import kr.syeyoung.dungeonsguide.mod.config.Config;
import kr.syeyoung.dungeonsguide.mod.config.onboarding.OnboardingPage;
import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticsManager;
import kr.syeyoung.dungeonsguide.mod.discord.DiscordIntegrationManager;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonFacade;
import kr.syeyoung.dungeonsguide.mod.events.annotations.EventHandlerRegistry;
import kr.syeyoung.dungeonsguide.mod.events.listener.DungeonListener;
import kr.syeyoung.dungeonsguide.mod.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.gui.CustomGuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.gui.elements.GlobalHUDScale;
import kr.syeyoung.dungeonsguide.mod.gui.xml.DomElementRegistry;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayManager;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import kr.syeyoung.dungeonsguide.mod.player.PlayerManager;
import kr.syeyoung.dungeonsguide.mod.stomp.StompManager;
import kr.syeyoung.dungeonsguide.mod.utils.GuiDisplayer;
import kr.syeyoung.dungeonsguide.mod.utils.TimeScoreUtil;
import kr.syeyoung.dungeonsguide.mod.wsresource.StaticResourceCache;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.command.UCommandContext;
import kr.syeyoung.modapi.event.AnnotatedListenerHelper;
import kr.syeyoung.modapi.event.ListenerRegistration;
import kr.syeyoung.modapi.event.SubscribeEvent;
import kr.syeyoung.modapi.event.events.ClientTickEvent;
import kr.syeyoung.modapi.event.events.RegisterCommandEvent;
import kr.syeyoung.modapi.event.events.ResourceReloadEvent;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DungeonsGuide implements DGInterface {
    public static final String DOMAIN = "https://v2.dungeons.guide/api";

    @Getter
    private static boolean firstTimeUsingDG = false;
    Logger logger = LogManager.getLogger("DungeonsGuide");

    public boolean verbose = false;
    private SkyblockStatus skyblockStatus;

    private File configDir;
    @Getter
    private File tempDir;
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


    private List<ListenerRegistration> registeredMODAPIListeners = new ArrayList<>();
    public void registerEventsForge(Object object) {
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

    @Getter
    private LoaderAPI loaderAPI;

    @Getter
    private AuthManager authManager;

    public void init(File f, LoaderAPI loaderAPI) {
//        ProgressManager.ProgressBar progressbar = ProgressManager.push("DungeonsGuide", 5); $$ PROGRESS
        this.loaderAPI = loaderAPI;
        this.configDir = f;

        this.authManager = new AuthManager(DOMAIN, ModAPI.getAPI().getAuthService(), "DungeonsGuide/"+VersionInfo.VERSION);
        this.authManager.init();
        ModAPI.getAPI().init();


//        progressbar.step("Creating Configuration");

        File configFile = new File(configDir, "config.json");
        if (!configFile.exists()) {
            f.mkdirs();
            firstTimeUsingDG = true;
        }
        tempDir = new File(configDir, "tmp");
        tempDir.mkdirs();


        Config.f = configFile;

        registerEventsForge(this);

//        progressbar.step("Loading Native Libraries");

        try {
            NativeLoader.extractLibraryAndLoad("waterboard");
            NativeLoader.extractLibraryAndLoad("dptsp");
        } catch (IOException | UnsatisfiedLinkError | RuntimeException e) {
            e.printStackTrace();
        }

//        progressbar.step("Registering Events & Commands");

        skyblockStatus = new SkyblockStatus();

        registerEventsForge(skyblockStatus);
        registerEventsForge(ChatTransmitter.INSTANCE);

        FeatureRegistry.getFeatureList();


        registerEventsForge(new DungeonListener());
        this.dungeonFacade = new DungeonFacade();

        dungeonFacade.init();

        registerEventsForge(PartyManager.INSTANCE);
        registerEventsForge(ChatProcessor.INSTANCE);
        registerEventsForge(PlayerManager.INSTANCE);
        registerEventsForge(StaticResourceCache.INSTANCE);
        registerEventsForge(OverlayManager.getEventHandler());


//        progressbar.step("Opening connection");
        StompManager.getInstance().init();
        registerEventsForge(cosmeticsManager = new CosmeticsManager());


//        progressbar.step("Loading Config");
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

//        ProgressManager.pop(progressbar);

        VersionInfo.checkAndOpen();

        ModAPI.getAPI().refreshResources();

        ModAPI.getAPI().getCommandManager().requestCommandReload();

        // Fix Parallel universe not working when player joins hypickle before dg loads
        if (firstTimeUsingDG) {
            GuiDisplayer.INSTANCE.displayGui(new CustomGuiScreenAdapter(new GlobalHUDScale(new OnboardingPage("pages/front.gui")), null, false));
        }
    }

    // hotswap fails in dev env due to intellij auto log collection or smth. it holds ref to stacktrace.


    @Override
    public void unload() {

        ModAPI.getAPI().unload();

        StompManager.getInstance().cleanup();
        // have FUN!


        for (ListenerRegistration registeredMODAPIListener : registeredMODAPIListeners) {
            ModAPI.getAPI().getEventBus().unregisterListener(registeredMODAPIListener);
        }
        EventHandlerRegistry.unregisterListeners();


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

        try {
            this.authManager.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        THREAD_GROUP.interrupt();
        THREAD_GROUP.stop();

        try {
            Thread.sleep(2000); // This is requirement for all the threads to finish within 1 second. or reference leak.
        } catch (InterruptedException e) {
        }
        THREAD_GROUP.destroy();
    }

    @Override
    public void onResourceReload() {
        DomElementRegistry.onResourceManagerReload();

        ModAPI.getAPI().getEventBus().fireEvent(new ResourceReloadEvent());
    }


    public SkyblockStatus getSkyblockStatus() {
        return skyblockStatus;
    }


//    @Override
//    public Class<? extends GuiScreen> getModConfigGUI() {
//        return null; // $$
//    }


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
