package kr.syeyoung.dungeonsguide.auth.authprovider.impl;

import com.mojang.authlib.exceptions.AuthenticationException;
import kr.syeyoung.dungeonsguide.auth.authprovider.AuthProvider;
import kr.syeyoung.dungeonsguide.auth.authprovider.AuthProviderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

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
    public void init() throws NoSuchAlgorithmException {
        rsaKey = AuthProviderUtil.getKeyPair();
    }

    @Override
    public void authenticate(Minecraft mc) throws AuthenticationException, IOException, NoSuchAlgorithmException {
        Session session = mc.getSession();

        String tempToken  = AuthProviderUtil.checkSessionAuthenticity(session, authServerUrl);
        token = AuthProviderUtil.verifyAuth(tempToken, rsaKey.getPublic(), authServerUrl);

    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public KeyPair getRsaKey() {
        return rsaKey;
    }

}
