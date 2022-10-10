package kr.syeyoung.dungeonsguide.auth;

import com.google.common.base.Throwables;
import com.google.gson.JsonObject;
import com.mojang.authlib.exceptions.AuthenticationException;
import kr.syeyoung.dungeonsguide.auth.authprovider.AuthProvider;
import kr.syeyoung.dungeonsguide.auth.authprovider.DgAuth.DgAuth;
import kr.syeyoung.dungeonsguide.auth.authprovider.DgAuth.DgAuthUtil;
import kr.syeyoung.dungeonsguide.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.events.impl.AuthChangedEvent;
import kr.syeyoung.dungeonsguide.stomp.StompManager;
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


public class AuthManager {
    Logger logger = LogManager.getLogger("AuthManger");

    private static AuthManager instance;

    public static AuthManager getInstance() {
        if(instance == null) instance = new AuthManager();
        return instance;
    }


    @Setter
    private String baseserverurl = "https://dungeons.guide";

    private AuthProvider currentProvider;

    public String getToken() {
        if (currentProvider != null && currentProvider.getToken() != null) {
            return currentProvider.getToken();
        }
        return null;
    }

    public KeyPair getKeyPair(){
        if (currentProvider != null && currentProvider.getToken() != null) {
            return currentProvider.getRsaKey();
        }
        return null;
    }


    boolean initlock = false;

    public void init() {
        if (initlock) {
            logger.info("Cannot init AuthManger twice");
            return;
        }

        reauth();

        initlock = true;


        MinecraftForge.EVENT_BUS.register(this);
        new Thread(() -> {
            while (true) {
                if (getToken() == null) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                JsonObject obj = DgAuthUtil.getJwtPayload(getToken());
                if (!obj.get("uuid").getAsString().replaceAll("-", "").equals(Minecraft.getMinecraft().getSession().getPlayerID())) {
                    shouldReAuth = true;
                }

                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "minecraft session change listener thread").start();
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

        currentProvider = null;
        try {
            currentProvider = new DgAuth(baseserverurl).createAuthProvider();
            if (currentProvider.getToken() == null) {
                shouldReAuth = true;
                currentProvider = null;
                ChatProcessor.INSTANCE.addToReciveChatQueue("§eDungeons Guide §7:: §r§cDG auth failed, trying again in ten seconds", true);
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
            ChatProcessor.INSTANCE.addToReciveChatQueue("§eDungeons Guide §7:: §r§cDG auth failed, trying again in ten seconds", true);
            logger.error("Re-auth failed with message {}, trying again in a ten seconds", String.valueOf(Throwables.getRootCause(e)));
        }

    }


}
