package kr.syeyoung.dungeonsguide.launcher.auth;

import java.security.KeyPair;
import java.security.interfaces.RSAKey;
import java.time.Instant;

public interface AuthToken {
    boolean isUserVerified();
    boolean hasFullCapability();
    boolean isAuthenticated();

    Instant getExpiryInstant();

    KeyPair getRSAKeyForAuth();

    String getToken();
}
