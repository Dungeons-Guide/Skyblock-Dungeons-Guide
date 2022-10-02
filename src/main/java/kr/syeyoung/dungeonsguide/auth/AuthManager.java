package kr.syeyoung.dungeonsguide.auth;

import com.google.gson.JsonObject;
import com.mojang.authlib.exceptions.AuthenticationException;
import kr.syeyoung.dungeonsguide.auth.authprovider.AuthProvider;
import kr.syeyoung.dungeonsguide.auth.authprovider.AuthProviderUtil;
import kr.syeyoung.dungeonsguide.auth.authprovider.impl.DgAuth;
import kr.syeyoung.dungeonsguide.events.AuthChangedEvent;
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

    String getToken() {
        if (currentProvider != null && currentProvider.getToken() != null) {
            return currentProvider.getToken();
        }
        return null;
    }

    KeyPair getKeyPair(){
        if (currentProvider != null && currentProvider.getToken() != null) {
            return currentProvider.getRsaKey();
        }
        return null;
    }


    boolean initlock = false;

    void init() {
        if (initlock) {
            logger.info("Cannot init AuthManger twice");
            return;
        }
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

                JsonObject obj = AuthProviderUtil.getJwtPayload(getToken());
                if (!obj.get("uuid").getAsString().equals(Minecraft.getMinecraft().getSession().getPlayerID())) {
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

    boolean shouldReAuth;
    int tickCounter;

    @SubscribeEvent
    public void onTickClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        if (tickCounter % 20 == 0) {
            tickCounter = 0;
            reauth();
        }
        tickCounter++;

    }

    void reauth() {
        if (!shouldReAuth) return;

        shouldReAuth = false;

        currentProvider = null;
        try {
            currentProvider = createAuthProvider();
            if (currentProvider.getToken() == null) {
                shouldReAuth = true;
                currentProvider = null;
            } else {
                // RE-AUTH 'ed SUCCESSFULLY HOORAY
                MinecraftForge.EVENT_BUS.post(new AuthChangedEvent());
            }
        } catch (NoSuchAlgorithmException | AuthenticationException | IOException e) {
            e.printStackTrace();
            shouldReAuth = true;
            currentProvider = null;
        }

    }


    AuthProvider createAuthProvider() throws NoSuchAlgorithmException, AuthenticationException, IOException {
        AuthProvider auth = new DgAuth(baseserverurl);

        auth.init();
        auth.authenticate(Minecraft.getMinecraft());

        return auth;
    }


}
