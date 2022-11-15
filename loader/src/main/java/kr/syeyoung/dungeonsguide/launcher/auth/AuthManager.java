package kr.syeyoung.dungeonsguide.launcher.auth;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonObject;
import com.mojang.authlib.exceptions.AuthenticationException;
import kr.syeyoung.dungeonsguide.launcher.auth.authprovider.AuthProvider;
import kr.syeyoung.dungeonsguide.launcher.auth.authprovider.DgAuth.DgAuth;
import kr.syeyoung.dungeonsguide.launcher.auth.authprovider.DgAuth.DgAuthUtil;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.events.impl.AuthChangedEvent;
import kr.syeyoung.dungeonsguide.mod.stomp.StompManager;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.concurrent.*;


public class AuthManager {
    Logger logger = LogManager.getLogger("AuthManger");

    private static AuthManager INSTANCE;

    public static AuthManager getInstance() {
        if(INSTANCE == null) INSTANCE = new AuthManager();
        return INSTANCE;
    }

    @Setter
    private String baseserverurl = "https://dungeons.guide";

    private AuthToken currentToken = new NullToken();

    public String getToken() {
        return currentToken.getToken();
    }


    public KeyPair getKeyPair(){
        return currentToken.getRSAKeyForAuth();
    }


    private volatile boolean initlock = false;

    public void init() {
        if (initlock) {
            logger.info("Cannot init AuthManger twice");
            return;
        }

        reauth();

        initlock = true;


        MinecraftForge.EVENT_BUS.register(this);

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("DgAuth Pool").build();
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, namedThreadFactory);
        scheduler.scheduleAtFixedRate(() -> {
            if (getToken() != null) {
                JsonObject obj = DgAuthUtil.getJwtPayload(getToken());
                if (!obj.get("uuid").getAsString().replace("-", "").equals(Minecraft.getMinecraft().getSession().getPlayerID())) {
                    shouldReAuth = true;
                }
            }


        }, 10,2000, TimeUnit.MILLISECONDS);
    }

    boolean shouldReAuth = true;
    int tickCounter;

    @SubscribeEvent
    public void onTickClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        if (tickCounter % 200 == 0) {
            tickCounter = 0;
            reauth();
        }
        tickCounter++;
    }

    public boolean isPlebUser(){
        return Objects.equals(getInstance().getPlanType(), "OPENSOURCE");
    }

    public String getPlanType(){
        if(getToken() == null) return null;


       JsonObject jwt = DgAuthUtil.getJwtPayload(getToken());

       if(!jwt.has("plan")) return null;

       return jwt.get("plan").getAsString();

    }

    void reauth() {
        if (!shouldReAuth) return;
        shouldReAuth = false;
        currentToken = new NullToken();
        try {
            currentProvider = new DgAuth(baseserverurl).createAuthProvider();
            if (currentProvider.getToken() == null) {
                shouldReAuth = true;
                currentProvider = null;
                ChatTransmitter.addToQueue("§eDungeons Guide §7:: §r§cDG auth failed, trying again in ten seconds", true);
                logger.info("DG auth failed, trying again in a second");
            } else {
                // RE-AUTHed SUCCESSFULLY HOORAY
                // for some reason the forge events don't work in pre init, so I call the callback directly
                StompManager.getInstance().init();
                MinecraftForge.EVENT_BUS.post(new AuthChangedEvent());
            }
        } catch (NoSuchAlgorithmException | AuthenticationException | IOException e) {

            shouldReAuth = true;
            currentProvider = null;
            ChatTransmitter.addToQueue("§eDungeons Guide §7:: §r§cDG auth failed, trying again in ten seconds", true);
            logger.error("Re-auth failed with message {}, trying again in a ten seconds", String.valueOf(Throwables.getRootCause(e)));
        }

    }


}
