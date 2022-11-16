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
import java.util.List;
import java.util.Set;

public class DungeonsGuide implements DGInterface {

    @Getter
    private static boolean firstTimeUsingDG = false;
    Logger logger = LogManager.getLogger("DungeonsGuide");

    public boolean verbose = false;
    private SkyblockStatus skyblockStatus;

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

        MinecraftForge.EVENT_BUS.register(this);

        progressbar.step("Registering Events & Commands");

        skyblockStatus = new SkyblockStatus();

        MinecraftForge.EVENT_BUS.register(skyblockStatus);


        (new FeatureRegistry()).init();

        new ChatTransmitter();

        try {
            Set<String> invalid = ReflectionHelper.getPrivateValue(LaunchClassLoader.class, (LaunchClassLoader) Main.class.getClassLoader(), "invalidClasses");
            ((LaunchClassLoader) Main.class.getClassLoader()).clearNegativeEntries(Sets.newHashSet("org.slf4j.LoggerFactory"));
            invalid.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }


        this.blockCache = new BlockCache();

        this.dungeonFacade = new DungeonFacade();
        dungeonFacade.init();



        TitleRender.getInstance();

        CommandDungeonsGuide commandDungeonsGuide = new CommandDungeonsGuide();
        CommandDgDebug command = new CommandDgDebug();

        ClientCommandHandler.instance.registerCommand(commandDungeonsGuide);
        ClientCommandHandler.instance.registerCommand(command);

        MinecraftForge.EVENT_BUS.register(command);
        MinecraftForge.EVENT_BUS.register(commandDungeonsGuide);

        MinecraftForge.EVENT_BUS.register(commandReparty = new CommandReparty());

        MinecraftForge.EVENT_BUS.register(new FeatureListener());
        MinecraftForge.EVENT_BUS.register(new PacketListener());
        MinecraftForge.EVENT_BUS.register(new Keybinds());

        MinecraftForge.EVENT_BUS.register(PartyManager.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ChatProcessor.INSTANCE);
        MinecraftForge.EVENT_BUS.register(StaticResourceCache.INSTANCE);

        MinecraftForge.EVENT_BUS.register(new AhUtils());


        progressbar.step("Opening connection");
        MinecraftForge.EVENT_BUS.register(cosmeticsManager = new CosmeticsManager());


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

        MinecraftForge.EVENT_BUS.register(RichPresenceManager.INSTANCE);
        TimeScoreUtil.init();

        ProgressManager.pop(progressbar);

    }

    @Override
    public void unload() {
        // have FUN!

//        bar.step("Instantiating...");
//        partialLoad(obtainLoader(configuration));
        throw new UnsupportedOperationException("Who the heck registered events in features?? This will stay unsupported for now");
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
