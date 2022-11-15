package kr.syeyoung.dungeonsguide.launcher.auth.token;

import java.security.KeyPair;
import java.security.interfaces.RSAKey;
import java.time.Instant;

public interface AuthToken {
    boolean isUserVerified();
    boolean hasFullCapability();
    boolean isAuthenticated();

    Instant getExpiryInstant();

    default String getUID() {return null;}
    default String getUUID() {return null;}
    default String getUsername() {return null;}

    String getToken();
}
