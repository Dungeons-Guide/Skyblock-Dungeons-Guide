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
import kr.syeyoung.dungeonsguide.launcher.DGInterface;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.commands.CommandDgDebug;
import kr.syeyoung.dungeonsguide.mod.commands.CommandDungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.commands.CommandReparty;
import kr.syeyoung.dungeonsguide.mod.config.Config;
import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticsManager;
import kr.syeyoung.dungeonsguide.mod.discord.rpc.RichPresenceManager;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonFacade;
import kr.syeyoung.dungeonsguide.mod.events.listener.DungeonListener;
import kr.syeyoung.dungeonsguide.mod.events.listener.FeatureListener;
import kr.syeyoung.dungeonsguide.mod.events.listener.PacketListener;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import kr.syeyoung.dungeonsguide.mod.resources.DGTexturePack;
import kr.syeyoung.dungeonsguide.mod.utils.AhUtils;
import kr.syeyoung.dungeonsguide.mod.utils.BlockCache;
import kr.syeyoung.dungeonsguide.mod.utils.TimeScoreUtil;
import kr.syeyoung.dungeonsguide.mod.utils.TitleRender;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.GLCursors;
import kr.syeyoung.dungeonsguide.mod.wsresource.StaticResourceCache;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.command.ICommand;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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


    private List<Object> registeredListeners = new ArrayList<>();
    public void registerEventsForge(Object object) {
        registeredListeners.add(object);
        MinecraftForge.EVENT_BUS.register(object);
    }
    private List<ICommand> registeredCommands = new ArrayList<>();

    public void registerCommands(ICommand command) {
        registeredCommands.add(command);
        ClientCommandHandler.instance.registerCommand(command);
    }


    public void init(File f) {
        ClassLoader orignalLoader = Thread.currentThread().getContextClassLoader();

        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
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
        registerEventsForge(TitleRender.getInstance());

        (new FeatureRegistry()).init();


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



        TitleRender.getInstance();

        CommandDungeonsGuide commandDungeonsGuide = new CommandDungeonsGuide();
        CommandDgDebug command = new CommandDgDebug();

        registerCommands(commandDungeonsGuide);
        registerCommands(command);

        registerEventsForge(command);
        registerEventsForge(commandDungeonsGuide);

        registerEventsForge(commandReparty = new CommandReparty());

        registerEventsForge(new FeatureListener());
        registerEventsForge(new PacketListener());
        registerEventsForge(new Keybinds());

        registerEventsForge(PartyManager.INSTANCE);
        registerEventsForge(ChatProcessor.INSTANCE);
        registerEventsForge(StaticResourceCache.INSTANCE);

        registerEventsForge(new AhUtils());


        progressbar.step("Opening connection");
        registerEventsForge(cosmeticsManager = new CosmeticsManager());


        progressbar.step("Loading Config");
        try {
            Config.loadConfig(null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (FeatureRegistry.ETC_REPARTY.isEnabled()) {
            ClientCommandHandler.instance.registerCommand(commandReparty);
        }

        if (FeatureRegistry.DISCORD_DONOTUSE.isEnabled()) {
            System.setProperty("dg.safe", "true");
        }

        registerEventsForge(RichPresenceManager.INSTANCE);
        TimeScoreUtil.init();

        ProgressManager.pop(progressbar);

        Thread.currentThread().setContextClassLoader(orignalLoader);
    }

    @Override
    public void unload() {
        // have FUN!


        for (Object registeredListener : registeredListeners) {
            MinecraftForge.EVENT_BUS.unregister(registeredListener);
        }
        Set<ICommand> commands = ReflectionHelper.getPrivateValue(ClientCommandHandler.class, ClientCommandHandler.instance, "commandSet");

        for (ICommand registeredCommand : registeredCommands) {
            ClientCommandHandler.instance.getCommands().remove(registeredCommand.getCommandName());
            for (String commandAlias : registeredCommand.getCommandAliases()) {
                ClientCommandHandler.instance.getCommands().remove(commandAlias);
            }
            commands.remove(registeredCommand);
        }
        THREAD_GROUP.interrupt();
        THREAD_GROUP.stop();
        try {
            Thread.sleep(1000); // This is requirement for all the threads to finish within 1 second. or reference leak.
        } catch (InterruptedException e) {
        }
        THREAD_GROUP.destroy();
    }

    @Override
    public void onResourceReload(IResourceManager a) {
        GLCursors.setupCursors();
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




}
