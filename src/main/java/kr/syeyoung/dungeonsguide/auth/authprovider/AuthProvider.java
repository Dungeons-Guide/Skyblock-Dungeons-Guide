package kr.syeyoung.dungeonsguide.auth.authprovider;

import com.mojang.authlib.exceptions.AuthenticationException;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

public interface AuthProvider {
    void init() throws NoSuchAlgorithmException;
    void authenticate(Minecraft mc) throws AuthenticationException, IOException, NoSuchAlgorithmException;
    String getToken();

    KeyPair getRsaKey();
}
