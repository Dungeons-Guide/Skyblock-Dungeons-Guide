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

package kr.syeyoung.dungeonsguide;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.commands.CommandDgDebug;
import kr.syeyoung.dungeonsguide.commands.CommandDungeonsGuide;
import kr.syeyoung.dungeonsguide.commands.CommandReparty;
import kr.syeyoung.dungeonsguide.config.Config;
import kr.syeyoung.dungeonsguide.cosmetics.CosmeticsManager;
import kr.syeyoung.dungeonsguide.discord.rpc.RichPresenceManager;
import kr.syeyoung.dungeonsguide.dungeon.DungeonGodObject;
import kr.syeyoung.dungeonsguide.events.listener.FeatureListener;
import kr.syeyoung.dungeonsguide.events.listener.PacketListener;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.party.PartyManager;
import kr.syeyoung.dungeonsguide.utils.AhUtils;
import kr.syeyoung.dungeonsguide.utils.TimeScoreUtil;
import kr.syeyoung.dungeonsguide.utils.TitleRender;
import kr.syeyoung.dungeonsguide.utils.cursor.GLCursors;
import kr.syeyoung.dungeonsguide.wsresource.StaticResourceCache;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Set;

public class DungeonsGuide {

    public boolean verbose = false;
    private SkyblockStatus skyblockStatus;

    @Getter
    private CosmeticsManager cosmeticsManager;

    Logger logger = LogManager.getLogger("DungeonsGuide");
    @Getter
    private DungeonGodObject dungeonGodObject;

    private DungeonsGuide() {
    }


    private static DungeonsGuide instance;

    public static DungeonsGuide getDungeonsGuide() {
        if (instance == null) instance = new DungeonsGuide();
        return instance;
    }

    public static void sendDebugChat(IChatComponent iChatComponent) {
        if(FeatureRegistry.DEBUG == null) return;
        if (FeatureRegistry.DEBUG.isEnabled())
            Minecraft.getMinecraft().thePlayer.addChatMessage(iChatComponent);
    }

    public static void sendDebugChat(String text) {
        sendDebugChat(new ChatComponentText(text));
    }

    @Getter
    CommandReparty commandReparty;


    public void init() {

        ProgressManager.ProgressBar progressbar = ProgressManager.push("DungeonsGuide", 4);

        try {
            Set<String> invalid = ReflectionHelper.getPrivateValue(LaunchClassLoader.class, (LaunchClassLoader) Main.class.getClassLoader(), "invalidClasses");
            ((LaunchClassLoader) Main.class.getClassLoader()).clearNegativeEntries(Sets.newHashSet("org.slf4j.LoggerFactory"));
            invalid.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }

        progressbar.step("Registering Events & Commands");


        this.dungeonGodObject = new DungeonGodObject();
        dungeonGodObject.init();

        skyblockStatus = new SkyblockStatus();

        TitleRender.getInstance();

        CommandDungeonsGuide commandDungeonsGuide = new CommandDungeonsGuide();
        CommandDgDebug command = new CommandDgDebug();

        ClientCommandHandler.instance.registerCommand(commandDungeonsGuide);
        ClientCommandHandler.instance.registerCommand(command);

        MinecraftForge.EVENT_BUS.register(command);
        MinecraftForge.EVENT_BUS.register(commandDungeonsGuide);

        commandReparty = new CommandReparty();
        MinecraftForge.EVENT_BUS.register(commandReparty);

        MinecraftForge.EVENT_BUS.register(new FeatureListener());
        MinecraftForge.EVENT_BUS.register(new PacketListener());
        MinecraftForge.EVENT_BUS.register(new Keybinds());

        MinecraftForge.EVENT_BUS.register(PartyManager.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ChatProcessor.INSTANCE);
        MinecraftForge.EVENT_BUS.register(StaticResourceCache.INSTANCE);

        MinecraftForge.EVENT_BUS.register(new AhUtils());


        progressbar.step("Opening connection");
        cosmeticsManager = new CosmeticsManager();
        MinecraftForge.EVENT_BUS.register(cosmeticsManager);


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

        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(resourceManager -> GLCursors.setupCursors());
    }


    public SkyblockStatus getSkyblockStatus() {
        return skyblockStatus;
    }




}
