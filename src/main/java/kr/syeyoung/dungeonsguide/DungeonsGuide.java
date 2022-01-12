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
import kr.syeyoung.dungeonsguide.chat.PartyManager;
import kr.syeyoung.dungeonsguide.commands.*;
import kr.syeyoung.dungeonsguide.config.Config;
import kr.syeyoung.dungeonsguide.cosmetics.CosmeticsManager;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.eventlistener.DungeonListener;
import kr.syeyoung.dungeonsguide.eventlistener.FeatureListener;
import kr.syeyoung.dungeonsguide.eventlistener.PacketListener;
import kr.syeyoung.dungeonsguide.events.StompConnectedEvent;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.resources.DGTexturePack;
import kr.syeyoung.dungeonsguide.rpc.RichPresenceManager;
import kr.syeyoung.dungeonsguide.stomp.CloseListener;
import kr.syeyoung.dungeonsguide.stomp.StompClient;
import kr.syeyoung.dungeonsguide.stomp.StompInterface;
import kr.syeyoung.dungeonsguide.utils.AhUtils;
import kr.syeyoung.dungeonsguide.utils.TimeScoreUtil;
import kr.syeyoung.dungeonsguide.wsresource.StaticResourceCache;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DungeonsGuide implements DGInterface, CloseListener {

    private SkyblockStatus skyblockStatus;

    private static DungeonsGuide dungeonsGuide;


    @Getter
    private final Authenticator authenticator;

    @Getter
    private StompInterface stompConnection;
    @Getter
    private CosmeticsManager cosmeticsManager;

    public DungeonsGuide(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public static void sendDebugChat(IChatComponent iChatComponent) {
        if (FeatureRegistry.DEBUG.isEnabled())
            Minecraft.getMinecraft().thePlayer.addChatMessage(iChatComponent);
    }
    @Getter
    CommandReparty commandReparty;


    private final String stompURL = "wss://dungeons.guide/ws";
//    private String stompURL = "ws://localhost/ws";
    public void init(FMLInitializationEvent event) {
        ProgressManager.ProgressBar progressbar = ProgressManager.push("DungeonsGuide", 4);


        try {
            Set<String> invalid = ReflectionHelper.getPrivateValue(LaunchClassLoader.class, (LaunchClassLoader) Main.class.getClassLoader(), "invalidClasses");
            ((LaunchClassLoader) Main.class.getClassLoader()).clearNegativeEntries(Sets.newHashSet("org.slf4j.LoggerFactory"));
            invalid.clear();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        progressbar.step("Registering Events & Commands");
        dungeonsGuide = this;
        skyblockStatus = new SkyblockStatus();

        CommandDungeonsGuide commandDungeonsGuide;
        MinecraftForge.EVENT_BUS.register(new DungeonListener());
        ClientCommandHandler.instance.registerCommand(commandDungeonsGuide = new CommandDungeonsGuide());
        MinecraftForge.EVENT_BUS.register(commandDungeonsGuide);

        commandReparty = new CommandReparty();
        MinecraftForge.EVENT_BUS.register(commandReparty);

        MinecraftForge.EVENT_BUS.register(new FeatureListener());
        MinecraftForge.EVENT_BUS.register(new PacketListener());
        MinecraftForge.EVENT_BUS.register(new Keybinds());

//        MinecraftForge.EVENT_BUS.register(PartyManager.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ChatProcessor.INSTANCE);
        MinecraftForge.EVENT_BUS.register(PartyManager.INSTANCE);
        MinecraftForge.EVENT_BUS.register(StaticResourceCache.INSTANCE);

        AhUtils.registerTimer();

        progressbar.step("Loading Roomdatas");
        try {
            DungeonRoomInfoRegistry.loadAll(configDir);
        } catch (BadPaddingException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | IOException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        progressbar.step("Opening connection");

        cosmeticsManager = new CosmeticsManager();
        MinecraftForge.EVENT_BUS.register(cosmeticsManager);

        try {
            connectStomp();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        progressbar.step("Loading Config");
        try {
            Config.loadConfig( null );
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (FeatureRegistry.ETC_REPARTY.isEnabled())
            ClientCommandHandler.instance.registerCommand(commandReparty);
        if (FeatureRegistry.DISCORD_DONOTUSE.isEnabled())
            System.setProperty("dg.safe", "true");
        MinecraftForge.EVENT_BUS.register(RichPresenceManager.INSTANCE);
        TimeScoreUtil.init();

        ProgressManager.pop(progressbar);
    }
    @Getter
    private boolean firstTimeUsingDG = false;
    public void pre(FMLPreInitializationEvent event) {
        configDir = new File(event.getModConfigurationDirectory(),"dungeonsguide");
        File configFile = new File(configDir, "config.json");
        if (!configFile.exists()) {
            configDir.mkdirs();
            firstTimeUsingDG = true;
        }
        Config.f = configFile;
        Minecraft.getMinecraft().getFramebuffer().enableStencil();

        try {
            List<IResourcePack> resourcePackList = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(),"defaultResourcePacks", "aA", "field_110449_ao");
            resourcePackList.add(new DGTexturePack(authenticator));
            Minecraft.getMinecraft().refreshResources();
        } catch (Throwable t){
            t.printStackTrace();
        }
    }

    @Getter
    private File configDir;


    public SkyblockStatus getSkyblockStatus() {
        return skyblockStatus;
    }

    public static DungeonsGuide getDungeonsGuide() {
        return dungeonsGuide;
    }
    ScheduledExecutorService ex = Executors.newScheduledThreadPool(2);
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Stomp Connection closed, trying to reconnect - "+reason+ " - "+code);
        connectStomp();
    }

    public void connectStomp() {
        ex.schedule(() -> {
            try {
                stompConnection = new StompClient(new URI(stompURL), authenticator.getToken(), DungeonsGuide.this);
                MinecraftForge.EVENT_BUS.post(new StompConnectedEvent(stompConnection));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 5L, TimeUnit.SECONDS);
    }
}
