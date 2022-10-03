package kr.syeyoung.dungeonsguide.auth.authprovider.DgAuth;

import com.mojang.authlib.exceptions.AuthenticationException;
import kr.syeyoung.dungeonsguide.auth.authprovider.AuthProvider;
import kr.syeyoung.dungeonsguide.auth.AuthUtil;

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
    public void init() throws NoSuchAlgorithmException {
        rsaKey = AuthUtil.getKeyPair();
    }

    @Override
    public void authenticate() throws AuthenticationException, IOException, NoSuchAlgorithmException {

        String tempToken = DgAuthUtil.requestAuth(this.authServerUrl);

        DgAuthUtil.checkSessionAuthenticity(tempToken);

        token = DgAuthUtil.verifyAuth(tempToken, rsaKey.getPublic(), authServerUrl);

    }


    @Override
    public AuthProvider createAuthProvider() throws NoSuchAlgorithmException, AuthenticationException, IOException {
        this.init();
        this.authenticate();

        return this;
    }

}
