package kr.syeyoung.dungeonsguide;

import kr.syeyoung.dungeonsguide.commands.*;
import kr.syeyoung.dungeonsguide.config.Config;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.eventlistener.DungeonListener;
import kr.syeyoung.dungeonsguide.eventlistener.FeatureListener;
import kr.syeyoung.dungeonsguide.eventlistener.PacketListener;
import kr.syeyoung.dungeonsguide.events.StompConnectedEvent;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.stomp.CloseListener;
import kr.syeyoung.dungeonsguide.stomp.StompClient;
import kr.syeyoung.dungeonsguide.stomp.StompInterface;
import kr.syeyoung.dungeonsguide.utils.AhUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.commons.io.IOUtils;

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

    public void init(FMLInitializationEvent event) {
        ProgressManager.ProgressBar progressbar = ProgressManager.push("DungeonsGuide", 4);



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

        AhUtils.registerTimer();

        progressbar.step("Downloading Roomdatas");
        try {
            DungeonRoomInfoRegistry.loadAll(configDir);
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        Keybinds.register();


        progressbar.step("Opening connection");
        try {
            stompConnection = new StompClient(new URI("wss://dungeonsguide.kro.kr/ws"), authenticator.c(), this);
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
                    stompConnection = new StompClient(new URI("wss://dungeonsguide.kro.kr/ws"), authenticator.c(), e.this);
                    MinecraftForge.EVENT_BUS.post(new StompConnectedEvent(stompConnection));
                } catch (Exception e) {
                    e.printStackTrace();
                    connectStomp();
                }
            }
        }, 5L, TimeUnit.SECONDS);
    }
}
