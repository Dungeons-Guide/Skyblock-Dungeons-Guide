/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.commands.CommandDungeonsGuide;
import kr.syeyoung.dungeonsguide.commands.CommandReparty;
import kr.syeyoung.dungeonsguide.launcher.authentication.Authenticator;
import kr.syeyoung.dungeonsguide.launcher.DGInterface;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.chat.PartyManager;
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
import kr.syeyoung.dungeonsguide.utils.cursor.GLCursors;
import kr.syeyoung.dungeonsguide.wsresource.StaticResourceCache;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
    CommandDungeonsGuide commandDungeonsGuide;


    private final String stompURL = "wss://dungeons.guide/ws";
//    private String stompURL = "ws://localhost/ws";
    public void init(File resourceDir) {
        ProgressManager.ProgressBar progressbar = ProgressManager.push("DungeonsGuide", 4);

        configDir = new File(resourceDir,"dungeonsguide");
        File configFile = new File(configDir, "config.json");
        if (!configFile.exists()) {
            configDir.mkdirs();
            firstTimeUsingDG = true;
        }
        Config.f = configFile;
        Minecraft.getMinecraft().getFramebuffer().enableStencil();


        try {
            List<IResourcePack> resourcePackList = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(),"defaultResourcePacks", "aA", "field_110449_ao");
            resourcePackList.add(new DGTexturePack());
            Minecraft.getMinecraft().refreshResources();
        } catch (Throwable t){
            t.printStackTrace();
        }


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

        registerEvents(new DungeonListener());
        ClientCommandHandler.instance.registerCommand(commandDungeonsGuide = new CommandDungeonsGuide());
        registerEvents(commandDungeonsGuide);

        commandReparty = new CommandReparty();
        registerEvents(commandReparty);

        registerEvents(new FeatureListener());
        registerEvents(new PacketListener());
        registerEvents(new Keybinds());

        registerEvents(ChatProcessor.INSTANCE);
        registerEvents(PartyManager.INSTANCE);
        registerEvents(StaticResourceCache.INSTANCE);

        AhUtils.registerTimer();

        progressbar.step("Loading Roomdatas");
        try {
            DungeonRoomInfoRegistry.loadAll(configDir);
        } catch (BadPaddingException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | IOException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        progressbar.step("Opening connection");

        cosmeticsManager = new CosmeticsManager();
        registerEvents(cosmeticsManager);

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
        registerEvents(RichPresenceManager.INSTANCE);
        TimeScoreUtil.init();

        ProgressManager.pop(progressbar);

        ((IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(resourceManager -> GLCursors.setupCursors());
    }
    @Getter
    private boolean firstTimeUsingDG = false;
    private List<Object> listeners = new ArrayList<>();

    private void registerEvents(Object obj) {
        listeners.add(obj);
        registerEvents(obj);
    }

    @Override
    public void onResourceReload(IResourceManager a) {
        GLCursors.setupCursors();
    }

    @Override
    public void unload() {
        ClientCommandHandler.instance.getCommands().remove(commandReparty.getCommandName());
        ClientCommandHandler.instance.getCommands().remove(commandDungeonsGuide.getCommandName());
        listeners.forEach(MinecraftForge.EVENT_BUS::unregister);
        stompConnection.disconnect();
        List<IResourcePack> resourcePackList = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(),"defaultResourcePacks", "aA", "field_110449_ao");
        resourcePackList.removeIf(a -> a instanceof DGTexturePack);
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
                stompConnection = new StompClient(new URI(stompURL), authenticator.getUnexpiredToken(), DungeonsGuide.this);
                MinecraftForge.EVENT_BUS.post(new StompConnectedEvent(stompConnection));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 5L, TimeUnit.SECONDS);
    }
}
