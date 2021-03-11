package kr.syeyoung.dungeonsguide;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.commands.*;
import kr.syeyoung.dungeonsguide.config.Config;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.eventlistener.DungeonListener;
import kr.syeyoung.dungeonsguide.eventlistener.FeatureListener;
import kr.syeyoung.dungeonsguide.eventlistener.PacketListener;
import kr.syeyoung.dungeonsguide.events.StompConnectedEvent;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.party.PartyInviteViewer;
import kr.syeyoung.dungeonsguide.party.PartyManager;
import kr.syeyoung.dungeonsguide.resources.DGTexturePack;
import kr.syeyoung.dungeonsguide.stomp.CloseListener;
import kr.syeyoung.dungeonsguide.stomp.StompClient;
import kr.syeyoung.dungeonsguide.stomp.StompInterface;
import kr.syeyoung.dungeonsguide.utils.AhUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.command.ICommand;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.launchwrapper.LogWrapper;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class e implements c, CloseListener {

    private SkyblockStatus skyblockStatus;

    private static e dungeonsGuide;


    @Getter
    private b authenticator;

    @Getter
    private StompInterface stompConnection;

    public e(b authenticator) {
        this.authenticator = authenticator;
    }

    public static void sendDebugChat(IChatComponent iChatComponent) {
        if (FeatureRegistry.DEBUG.isEnabled())
            Minecraft.getMinecraft().thePlayer.addChatMessage(iChatComponent);
    }
    @Getter
    CommandReparty commandReparty;


    private String stompURL = "wss://dungeonsguide.kro.kr/ws";
//    private String stompURL = "ws://localhost/ws";
    public void init(FMLInitializationEvent event) {
        ProgressManager.ProgressBar progressbar = ProgressManager.push("DungeonsGuide", 4);


        try {
            Set<String> invalid = ReflectionHelper.getPrivateValue(LaunchClassLoader.class, (LaunchClassLoader) a.class.getClassLoader(), "invalidClasses");
            ((LaunchClassLoader)a.class.getClassLoader()).clearNegativeEntries(Sets.newHashSet("org.slf4j.LoggerFactory"));
            for (String s : invalid) {
                System.out.println(s+" in invalid");
            }
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
        if (FeatureRegistry.ETC_REPARTY.isEnabled())
            ClientCommandHandler.instance.registerCommand(commandReparty);

        MinecraftForge.EVENT_BUS.register(commandReparty);
        MinecraftForge.EVENT_BUS.register(new FeatureListener());
        MinecraftForge.EVENT_BUS.register(new PacketListener());
        MinecraftForge.EVENT_BUS.register(new Keybinds());

        RichPresenceManager.INSTANCE.setup();
        MinecraftForge.EVENT_BUS.register(RichPresenceManager.INSTANCE);
        MinecraftForge.EVENT_BUS.register(PartyManager.INSTANCE);
        MinecraftForge.EVENT_BUS.register(PartyInviteViewer.INSTANCE);

        AhUtils.registerTimer();

        progressbar.step("Downloading Roomdatas");
        try {
            DungeonRoomInfoRegistry.loadAll(configDir);
        } catch (BadPaddingException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | IOException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        Keybinds.register();

        progressbar.step("Opening connection");
        try {
            stompConnection = new StompClient(new URI(stompURL), authenticator.c(), this);
            MinecraftForge.EVENT_BUS.post(new StompConnectedEvent(stompConnection));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        progressbar.step("Loading Config");
        try {
            Config.loadConfig( null );
        } catch (IOException e) {
            e.printStackTrace();
        }


        ProgressManager.pop(progressbar);
    }
    public void pre(FMLPreInitializationEvent event) {
        configDir = new File(event.getModConfigurationDirectory(),"dungeonsguide");
        File configFile = new File(configDir, "config.json");
        if (!configFile.exists()) {
            configDir.mkdirs();
        }
        Config.f = configFile;
        Minecraft.getMinecraft().getFramebuffer().enableStencil();

        try {
            List<IResourcePack> resourcePackList = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(),"defaultResourcePacks");
            resourcePackList.add(new DGTexturePack(authenticator));
        } catch (Throwable t){
            t.printStackTrace();
        }
    }
    private void copy(InputStream inputStream, File f) throws IOException {
        FileOutputStream fos = new FileOutputStream(f);
        IOUtils.copy(inputStream, fos);
        fos.flush();
        fos.close();
        inputStream.close();
    }

    private void combineConfig(Configuration saved, Configuration newest) {
    }

    @Getter
    private File configDir;


    public SkyblockStatus getSkyblockStatus() {
        return (SkyblockStatus) skyblockStatus;
    }

    public static e getDungeonsGuide() {
        return dungeonsGuide;
    }
    ScheduledExecutorService ex = Executors.newScheduledThreadPool(2);
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Stomp Connection closed, trying to reconnect - "+reason+ " - "+code);
        connectStomp();
    }

    public void connectStomp() {
        ex.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    stompConnection = new StompClient(new URI(stompURL), authenticator.c(), e.this);
                    MinecraftForge.EVENT_BUS.post(new StompConnectedEvent(stompConnection));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 5L, TimeUnit.SECONDS);
    }
}
