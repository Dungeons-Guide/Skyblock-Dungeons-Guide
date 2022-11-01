package kr.syeyoung.dungeonsguide.auth.authprovider;

import com.mojang.authlib.exceptions.AuthenticationException;

import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

public interface AuthProvider {
    String getToken();

    KeyPair getRsaKey();


    AuthProvider createAuthProvider() throws NoSuchAlgorithmException, AuthenticationException, IOException;
}
