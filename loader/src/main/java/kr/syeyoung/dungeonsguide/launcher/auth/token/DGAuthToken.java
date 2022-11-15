package kr.syeyoung.dungeonsguide.launcher.auth.token;

import kr.syeyoung.dungeonsguide.launcher.auth.AuthUtil;
import kr.syeyoung.dungeonsguide.launcher.auth.DgAuthUtil;
import org.json.JSONObject;

import java.time.Instant;

public class DGAuthToken implements AuthToken {
    private String token;
    private JSONObject parsed;

    public DGAuthToken(String token) {
        this.token = token;
        this.parsed = DgAuthUtil.getJwtPayload(token);
    }

    @Override
    public boolean isUserVerified() {
        return true;
    }

    @Override
    public boolean hasFullCapability() {
        return true;
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
