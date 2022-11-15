package kr.syeyoung.dungeonsguide.launcher.auth.token;

import java.security.KeyPair;
import java.time.Instant;

public class FailedAuthToken implements AuthToken {
    private final Throwable exeption;

    public FailedAuthToken(Throwable exception) {
        this.exeption = exception;
    }

    @Override
    public boolean isUserVerified() {
        return false;
    }

    @Override
    public boolean hasFullCapability() {
        return false;
    }

    @Override
    public boolean isAuthenticated() {
        return false;
    }

    @Override
    public Instant getExpiryInstant() {
        return Instant.MIN;
    }

    @Override
    public String getToken() {
        return null;
    }

    public Throwable getException() {
        return exeption;
    }
}
