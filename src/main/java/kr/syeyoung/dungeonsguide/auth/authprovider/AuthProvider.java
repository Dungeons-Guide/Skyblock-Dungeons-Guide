package kr.syeyoung.dungeonsguide.auth.authprovider;

import com.mojang.authlib.exceptions.AuthenticationException;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

public interface AuthProvider {
    void init() throws NoSuchAlgorithmException;
    void authenticate() throws AuthenticationException, IOException, NoSuchAlgorithmException;
    String getToken();

    KeyPair getRsaKey();


    AuthProvider createAuthProvider() throws NoSuchAlgorithmException, AuthenticationException, IOException;
}
