package kr.syeyoung.dungeonsguide.launcher.auth.authprovider.DgAuth;

import com.mojang.authlib.exceptions.AuthenticationException;
import kr.syeyoung.dungeonsguide.launcher.auth.AuthUtil;
import kr.syeyoung.dungeonsguide.launcher.auth.authprovider.AuthProvider;

import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

public class DgAuth implements AuthProvider {

    private final String authServerUrl;

    public DgAuth(String authServerUrl){
        this.authServerUrl = authServerUrl;
    }

    private String token;
    private KeyPair rsaKey;

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public KeyPair getRsaKey() {
        return rsaKey;
    }


    @Override
    public AuthProvider createAuthProvider() throws NoSuchAlgorithmException, AuthenticationException, IOException {
        this.rsaKey = AuthUtil.getKeyPair();

        String tempToken = DgAuthUtil.requestAuth(this.authServerUrl);

        DgAuthUtil.checkSessionAuthenticity(tempToken);

        this.token = DgAuthUtil.verifyAuth(tempToken, rsaKey.getPublic(), authServerUrl);

        return this;
    }

}
