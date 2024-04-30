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

import com.google.common.collect.Sets;
import com.sun.jna.internal.Cleaner;
import kr.syeyoung.dungeonsguide.launcher.DGInterface;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.commands.CommandDgDebug;
import kr.syeyoung.dungeonsguide.mod.commands.CommandDungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.commands.CommandReparty;
import kr.syeyoung.dungeonsguide.mod.config.Config;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3.ConfigGuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticsManager;
import kr.syeyoung.dungeonsguide.mod.cosmetics.CustomNetworkPlayerInfo;
import kr.syeyoung.dungeonsguide.mod.discord.DiscordIntegrationManager;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonFacade;
import kr.syeyoung.dungeonsguide.mod.events.annotations.EventHandlerRegistry;
import kr.syeyoung.dungeonsguide.mod.events.listener.DungeonListener;
import kr.syeyoung.dungeonsguide.mod.events.listener.PacketInjector;
import kr.syeyoung.dungeonsguide.mod.events.listener.PacketListener;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.guiv2.PassthroughManager;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.fonts.DefaultFontRenderer;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayManager;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import kr.syeyoung.dungeonsguide.mod.resources.DGTexturePack;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderManager;
import kr.syeyoung.dungeonsguide.mod.stomp.StompManager;
import kr.syeyoung.dungeonsguide.mod.utils.AhUtils;
import kr.syeyoung.dungeonsguide.mod.utils.BlockCache;
import kr.syeyoung.dungeonsguide.mod.utils.TimeScoreUtil;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.GLCursors;
import kr.syeyoung.dungeonsguide.mod.wsresource.StaticResourceCache;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.entity.Entity;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.ListenerList;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
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

    @Getter
    private BlockCache blockCache;

    public DungeonsGuide(){
        instance = this;
    }
    private static DungeonsGuide instance;

    public static DungeonsGuide getDungeonsGuide() {
        return instance;
    }

    @Getter
    CommandReparty commandReparty;
    @Getter
    CommandDungeonsGuide commandDungeonsGuide;


    private List<Object> registeredListeners = new ArrayList<>();
    public void registerEventsForge(Object object) {
        registeredListeners.add(object);
        MinecraftForge.EVENT_BUS.register(object);
    }
    private List<ICommand> registeredCommands = new ArrayList<>();
    private List<ExecutorService> executorServices = new ArrayList<>();

    public void registerCommands(ICommand command) {
        registeredCommands.add(command);
        ClientCommandHandler.instance.registerCommand(command);
    }

    public ExecutorService registerExecutorService(ExecutorService executorService) {
        this.executorServices.add(executorService);
        return executorService;
    }

    public ScheduledExecutorService registerExecutorService(ScheduledExecutorService executorService) {
        this.executorServices.add(executorService);
        return executorService;
    }

    private PacketInjector packetInjector;
    public void init(File f) {
        ProgressManager.ProgressBar progressbar = ProgressManager.push("DungeonsGuide", 4);


        progressbar.step("Creating Configuration");

        File configFile = new File(Main.getConfigDir(), "config.json");
        if (!configFile.exists()) {
            Main.getConfigDir().mkdirs();
            firstTimeUsingDG = true;
        }

        Config.f = configFile;
        Minecraft.getMinecraft().getFramebuffer().enableStencil();

        try {
            List<IResourcePack> resourcePackList = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "defaultResourcePacks", "aA", "field_110449_ao");
            resourcePackList.add(new DGTexturePack());
            Minecraft.getMinecraft().refreshResources();
        } catch (Exception e) {
            e.printStackTrace();
        }

        registerEventsForge(this);

        progressbar.step("Registering Events & Commands");

        skyblockStatus = new SkyblockStatus();

        registerEventsForge(skyblockStatus);
        registerEventsForge(ChatTransmitter.INSTANCE);
        registerEventsForge(new BlockCache());
        registerEventsForge(PassthroughManager.INSTANCE);

        FeatureRegistry.getFeatureList();



        try {
            Set<String> invalid = ReflectionHelper.getPrivateValue(LaunchClassLoader.class, (LaunchClassLoader) Main.class.getClassLoader(), "invalidClasses");
            ((LaunchClassLoader) Main.class.getClassLoader()).clearNegativeEntries(Sets.newHashSet("org.slf4j.LoggerFactory"));
            invalid.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }


        this.blockCache = new BlockCache();

        registerEventsForge(new DungeonListener());
        this.dungeonFacade = new DungeonFacade();

        dungeonFacade.init();


        commandDungeonsGuide = new CommandDungeonsGuide();
        CommandDgDebug command = new CommandDgDebug();

        registerCommands(commandDungeonsGuide);
        registerCommands(command);

        registerEventsForge(command);
        registerEventsForge(commandDungeonsGuide);

        registerEventsForge(commandReparty = new CommandReparty());

        registerEventsForge(packetInjector = new PacketInjector());
        registerEventsForge(new PacketListener());
        registerEventsForge(new Keybinds());

        registerEventsForge(PartyManager.INSTANCE);
        registerEventsForge(ChatProcessor.INSTANCE);
        registerEventsForge(StaticResourceCache.INSTANCE);
        registerEventsForge(OverlayManager.getEventHandler());

        registerEventsForge(new AhUtils());


        progressbar.step("Opening connection");
        StompManager.getInstance().init();
        registerEventsForge(cosmeticsManager = new CosmeticsManager());


        progressbar.step("Loading Config");
        try {
            Config.loadConfig(null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (FeatureRegistry.ETC_REPARTY.isEnabled()) {
            registerCommands(commandReparty);
        }
        DiscordIntegrationManager.INSTANCE.isLoaded();


        TimeScoreUtil.init();

        ProgressManager.pop(progressbar);

        VersionInfo.checkAndOpen();





        Minecraft.getMinecraft().refreshResources();

        // Fix Parallel universe not working when player joins hypickle before dg loads
        if (Minecraft.getMinecraft().getNetHandler() != null)
            Minecraft.getMinecraft().getNetHandler().getNetworkManager().channel().pipeline().addBefore("packet_handler", "dg_packet_handler", packetInjector);
    }

    // hotswap fails in dev env due to intellij auto log collection or smth. it holds ref to stacktrace.

    private void transform(AbstractClientPlayer abstractClientPlayer) {
        if (abstractClientPlayer == null) return;
        NetworkPlayerInfo uuidNetworkPlayerInfoEntry = ReflectionHelper.getPrivateValue(AbstractClientPlayer.class,
                abstractClientPlayer,
                "playerInfo", "field_175157_a", "a"
        );
        if (uuidNetworkPlayerInfoEntry instanceof CustomNetworkPlayerInfo) {
            S38PacketPlayerListItem s38PacketPlayerListItem = new S38PacketPlayerListItem();
            NetworkPlayerInfo newInfo = new NetworkPlayerInfo(s38PacketPlayerListItem.new AddPlayerData(
                    uuidNetworkPlayerInfoEntry.getGameProfile(),
                    uuidNetworkPlayerInfoEntry.getResponseTime(),
                    uuidNetworkPlayerInfoEntry.getGameType(),
                    ((CustomNetworkPlayerInfo)uuidNetworkPlayerInfoEntry).getOriginalDisplayName()
            ));
            ReflectionHelper.setPrivateValue(AbstractClientPlayer.class,
                    abstractClientPlayer,
                    newInfo,
                    "playerInfo", "field_175157_a", "a"
            );
        }
    }

    @Override
    public void unload() {
        StompManager.getInstance().cleanup();
        // have FUN!


        for (Object registeredListener : registeredListeners) {
            MinecraftForge.EVENT_BUS.unregister(registeredListener);
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


        Set<ICommand> commands = ReflectionHelper.getPrivateValue(CommandHandler.class, ClientCommandHandler.instance, "commandSet","field_71561_b","field_6467","c");

        for (ICommand registeredCommand : registeredCommands) {
            ClientCommandHandler.instance.getCommands().remove(registeredCommand.getCommandName());
            for (String commandAlias : registeredCommand.getCommandAliases()) {
                ClientCommandHandler.instance.getCommands().remove(commandAlias);
            }
            commands.remove(registeredCommand);
        }

        if (packetInjector != null) packetInjector.cleanup();

        try {
            if (Minecraft.getMinecraft().getRenderManager().livingPlayer instanceof AbstractClientPlayer) {
                AbstractClientPlayer ep = (AbstractClientPlayer) Minecraft.getMinecraft().getRenderManager().livingPlayer;
                transform(ep);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (Minecraft.getMinecraft().pointedEntity instanceof AbstractClientPlayer) {
                AbstractClientPlayer ep = (AbstractClientPlayer) Minecraft.getMinecraft().pointedEntity;
                transform(ep);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        NetHandlerPlayClient netHandlerPlayClient = Minecraft.getMinecraft().getNetHandler();
        if (netHandlerPlayClient == null && (Minecraft.getMinecraft().getRenderManager().livingPlayer) != null
                    && Minecraft.getMinecraft().getRenderManager().livingPlayer instanceof EntityPlayerSP)
            netHandlerPlayClient = ((EntityPlayerSP) Minecraft.getMinecraft().getRenderManager().livingPlayer).sendQueue;

        if (netHandlerPlayClient != null) {
            Map<UUID, NetworkPlayerInfo> playerInfoMap = ReflectionHelper.getPrivateValue(NetHandlerPlayClient.class,
                    netHandlerPlayClient, "playerInfoMap", "field_147310_i", "i");
            for (Map.Entry<UUID, NetworkPlayerInfo> uuidNetworkPlayerInfoEntry : playerInfoMap.entrySet()) {
                if (uuidNetworkPlayerInfoEntry.getValue() instanceof CustomNetworkPlayerInfo) {
                    S38PacketPlayerListItem s38PacketPlayerListItem = new S38PacketPlayerListItem();
                    NetworkPlayerInfo newInfo =  new NetworkPlayerInfo(s38PacketPlayerListItem.new AddPlayerData(
                            uuidNetworkPlayerInfoEntry.getValue().getGameProfile(),
                            uuidNetworkPlayerInfoEntry.getValue().getResponseTime(),
                            uuidNetworkPlayerInfoEntry.getValue().getGameType(),
                            ((CustomNetworkPlayerInfo) uuidNetworkPlayerInfoEntry.getValue()).getOriginalDisplayName()
                    ));
                    playerInfoMap.put(uuidNetworkPlayerInfoEntry.getKey(), newInfo);
                }
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



        World world = Minecraft.getMinecraft().getRenderManager().worldObj;
        if (world != null) {
            for (AbstractClientPlayer entity : world.getEntities(AbstractClientPlayer.class, input -> true)) {
                transform(entity);
            }
            for (AbstractClientPlayer player : world.getPlayers(AbstractClientPlayer.class, input -> true)) {
                transform(player);
            }
            if (world instanceof WorldClient) {
                Set<Entity> list = ReflectionHelper.getPrivateValue(WorldClient.class, (WorldClient) world, "entityList", "field_73032_d", "c");
                for (Entity e : list) {
                    if (e instanceof AbstractClientPlayer) {
                        transform((AbstractClientPlayer) e);
                    }
                }
            }
        }

        try {
            List<IResourcePack> resourcePackList = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "defaultResourcePacks", "aA", "field_110449_ao");
            resourcePackList.removeIf(a -> a instanceof DGTexturePack);
            Minecraft.getMinecraft().refreshResources();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ShaderManager.unload();
        GLCursors.cleanup();
        DiscordIntegrationManager.INSTANCE.cleanup();

        for (ExecutorService executorService : executorServices) {
            executorService.shutdownNow();
        }

        try {
            Cleaner cleaner = Cleaner.getCleaner();
            Thread t = ReflectionHelper.getPrivateValue(Cleaner.class, cleaner, "cleanerThread");
            t.stop();
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
    public void onResourceReload(IResourceManager a) {
        GLCursors.setupCursors();
        DefaultFontRenderer.DEFAULT_RENDERER.onResourceManagerReload();
        ShaderManager.onResourceReload();
    }

    private boolean showedStartUpGuide;
    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent guiOpenEvent){
        if(!showedStartUpGuide){
            showedStartUpGuide = true;

            if(isFirstTimeUsingDG()){
                GuiScreen originalGUI = guiOpenEvent.gui;
                guiOpenEvent.gui = new GuiScreen() {
                    final String welcomeText = "Thank you for installing §eDungeonsGuide§f, the most intelligent skyblock dungeon mod!\nThe gui for relocating GUI Elements and enabling or disabling features can be opened by typing §e/dg\nType §e/dg help §fto view full list of commands offered by dungeons guide!";

                    @Override
                    public void initGui() {
                        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
                        this.buttonList.add(new GuiButton(0, sr.getScaledWidth()/2-100,sr.getScaledHeight()-70 ,"Continue"));
                    }

                    @Override
                    protected void actionPerformed(GuiButton button) throws IOException {
                        super.actionPerformed(button);
                        if (button.id == 0) {
                            Minecraft.getMinecraft().displayGuiScreen(originalGUI);
                        }
                    }

                    @Override
                    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
                        super.drawBackground(1);

                        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
                        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
                        fontRenderer.drawString("§eWelcome To DungeonsGuide", (sr.getScaledWidth()-fontRenderer.getStringWidth("Welcome To DungeonsGuide"))/2,40,0xFFFF0000);
                        int tenth = sr.getScaledWidth() / 10;
                        Gui.drawRect(tenth, 70,sr.getScaledWidth()-tenth, sr.getScaledHeight()-80, 0xFF5B5B5B);

                        String[] split = welcomeText.split("\n");
                        for (int i = 0; i < split.length; i++) {
                            fontRenderer.drawString(split[i].replace("\t", "    "), tenth + 2,i*fontRenderer.FONT_HEIGHT + 72, 0xFFFFFFFF);
                        }

                        super.drawScreen(mouseX, mouseY, partialTicks);
                    }

                };
            }

        }
    }



    public SkyblockStatus getSkyblockStatus() {
        return skyblockStatus;
    }


    @Override
    public Class<? extends GuiScreen> getModConfigGUI() {
        return ConfigGuiScreenAdapter.class;
    }
}
