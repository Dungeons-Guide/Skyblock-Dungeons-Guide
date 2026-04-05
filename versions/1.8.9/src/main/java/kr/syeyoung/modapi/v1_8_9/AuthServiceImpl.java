package kr.syeyoung.modapi.v1_8_9;

import com.mojang.authlib.exceptions.AuthenticationException;
import kr.syeyoung.dungeonsguide.authapi.api.AuthService;
import net.minecraft.client.Minecraft;

import java.util.UUID;

public class AuthServiceImpl implements AuthService  {
    public static final AuthServiceImpl INSTANCE = new AuthServiceImpl();


    @Override
    public void mojangAuth(String serverId) {
        try {
            Minecraft.getMinecraft().getSessionService().joinServer(
                    Minecraft.getMinecraft().getSession().getProfile(),
                    Minecraft.getMinecraft().getSession().getToken(),
                    serverId
            );
        } catch (AuthenticationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UUID getCurrentPlayerUUID() {
        return Minecraft.getMinecraft().getSession().getProfile().getId();
    }

    @Override
    public String getCurrentPlayerUsername() {
        return Minecraft.getMinecraft().getSession().getProfile().getName();
    }
}
