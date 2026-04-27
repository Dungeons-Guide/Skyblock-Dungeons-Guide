package kr.syeyoung.modapi.v1_21_9;

import com.mojang.authlib.exceptions.AuthenticationException;
import kr.syeyoung.modapi.AuthService;
import net.minecraft.client.MinecraftClient;

import java.util.UUID;

public class AuthServiceImpl implements AuthService {
    public static final AuthServiceImpl INSTANCE = new AuthServiceImpl();


    @Override
    public void mojangAuth(String serverId) {
        try {
            MinecraftClient.getInstance().getApiServices().sessionService().joinServer(
                    MinecraftClient.getInstance().getGameProfile().id(),
                    MinecraftClient.getInstance().getSession().getAccessToken(),
                    serverId
            );
        } catch (AuthenticationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UUID getCurrentPlayerUUID() {
        return MinecraftClient.getInstance().getGameProfile().id();
    }

    @Override
    public String getCurrentPlayerUsername() {
        return MinecraftClient.getInstance().getGameProfile().name();
    }
}
