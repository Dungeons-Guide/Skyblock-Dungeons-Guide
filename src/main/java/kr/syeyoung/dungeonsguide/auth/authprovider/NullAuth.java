package kr.syeyoung.dungeonsguide.auth.authprovider;

import com.mojang.authlib.exceptions.AuthenticationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class NullAuth implements AuthProvider {

    Logger logger = LogManager.getLogger("NullAuth");

    @Override
    public void init() throws NoSuchAlgorithmException {
        logger.info("Initialising nothing");
    }

    @Override
    public void authenticate() throws AuthenticationException, IOException, NoSuchAlgorithmException {
        logger.info("Authenticating... something");
    }

    @Override
    public String getToken() {
        return "TOKEN";
    }

    @Override
    public KeyPair getRsaKey() {
        return new KeyPair(new PublicKey() {
            @Override
            public String getAlgorithm() {
                return null;
            }

            @Override
            public String getFormat() {
                return null;
            }

            @Override
            public byte[] getEncoded() {
                return new byte[0];
            }
        }, new PrivateKey() {
            @Override
            public String getAlgorithm() {
                return null;
            }

            @Override
            public String getFormat() {
                return null;
            }

            @Override
            public byte[] getEncoded() {
                return new byte[0];
            }
        });
    }

    @Override
    public AuthProvider createAuthProvider() throws NoSuchAlgorithmException, AuthenticationException, IOException {
        return new NullAuth();
    }

}
