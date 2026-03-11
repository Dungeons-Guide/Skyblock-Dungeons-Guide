package kr.syeyoung.dungeonsguide.authapi.api;

import kr.syeyoung.dungeonsguide.authapi.auth.token.AuthToken;

public interface AuthEventListener {
    public void onNewAuthToken(AuthToken token);
}
