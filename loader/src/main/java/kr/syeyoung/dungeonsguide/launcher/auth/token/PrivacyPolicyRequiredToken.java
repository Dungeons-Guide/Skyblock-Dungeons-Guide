package kr.syeyoung.dungeonsguide.launcher.auth.token;

import kr.syeyoung.dungeonsguide.launcher.auth.DgAuthUtil;
import org.json.JSONObject;

import java.time.Instant;

public class PrivacyPolicyRequiredToken implements AuthToken {
    private String token;
    private JSONObject parsed;

    public PrivacyPolicyRequiredToken(String token) {
        this.token = token;
        this.parsed = DgAuthUtil.getJwtPayload(token);
    }

    @Override
    public boolean isUserVerified() {
        return true;
    }

    @Override
    public boolean hasFullCapability() {
        return false;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public Instant getExpiryInstant() {
        return Instant.ofEpochSecond(Long.parseLong(parsed.getString("exp")));
    }

    @Override
    public String getUID() {
        return parsed.getString("userid");
    }

    @Override
    public String getUUID() {
        return parsed.getString("uuid");
    }

    @Override
    public String getUsername() {
        return parsed.getString("nickname");
    }

    @Override
    public String getToken() {
        return token;
    }
}
