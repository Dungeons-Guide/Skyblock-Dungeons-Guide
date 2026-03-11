/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.authapi.auth;

import kr.syeyoung.dungeonsguide.authapi.api.AuthEventListener;
import kr.syeyoung.dungeonsguide.authapi.api.AuthService;
import kr.syeyoung.dungeonsguide.authapi.auth.token.*;
import kr.syeyoung.dungeonsguide.authapi.exceptions.auth.AuthFailedException;
import kr.syeyoung.dungeonsguide.authapi.exceptions.auth.AuthenticationUnavailableException;
import kr.syeyoung.dungeonsguide.authapi.exceptions.auth.PrivacyPolicyRequiredException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.Throwables;

import java.util.List;


public class AuthManager {
    Logger logger = LogManager.getLogger("AuthManger");

    private boolean shouldAuthNotif = true;
    private AuthToken currentToken = new NullToken();

    private List<AuthEventListener> listenerList;
    private final AuthService authService;
    private final String baseUrl;
    private final String userAgent;
    private final AuthAPI authAPI;

    public AuthManager(AuthService service, String userAgent) {
        this("https://v2.dungeons.guide/api", service, userAgent);
    }

    public AuthManager(String baseUrl, AuthService minecraft, String userAgent) {
        this.baseUrl = baseUrl;
        this.authService = minecraft;
        this.userAgent = userAgent;
        this.authAPI = new AuthAPI(baseUrl, userAgent);
    }


    public AuthToken getToken() {
        return currentToken;
    }
    public String getWorkingTokenOrNull() {
        if (currentToken instanceof DGAuthToken) return currentToken.getToken();
        else return null;
    }

    /**
     * @throws AuthenticationUnavailableException variations of it.
     * @return actual dg token
     */
    public String getWorkingTokenOrThrow() {
        if (currentToken instanceof DGAuthToken) return currentToken.getToken();
        else if (currentToken instanceof FailedAuthToken) throw new AuthFailedException(((FailedAuthToken) currentToken).getException());
        else if (currentToken instanceof NullToken) throw new AuthenticationUnavailableException("Null Token");
        else if (currentToken instanceof PrivacyPolicyRequiredToken) throw new PrivacyPolicyRequiredException();
        throw new IllegalStateException("weird token: "+currentToken);
    }

    private volatile boolean reauthLock = false;

    AuthToken reAuth() {
        if (reauthLock) {
            while (reauthLock) ;
            return currentToken;
        }

        reauthLock = true;

        try {
            String token = authAPI.requestAuth(authService.getCurrentPlayerUUID(), authService.getCurrentPlayerUsername());
            byte[] encSecret = AuthAPI.checkSessionAuthenticityAndReturnEncryptedSecret(authService, token);
            currentToken = authAPI.verifyAuth(token, encSecret);
            listenerList.forEach(a -> a.onNewAuthToken(currentToken));
        } catch (Exception e) {
            currentToken = new FailedAuthToken(e);
            listenerList.forEach(a -> a.onNewAuthToken(currentToken));

            logger.error("Re-auth failed with message {}, trying again in a 2 seconds", String.valueOf(Throwables.getRootCause(e)));
            throw new AuthFailedException(e);
        } finally {
            reauthLock = false;
        }
        return currentToken;
    }

    private volatile boolean accepting = false;
    public synchronized void acceptPrivacyPolicy(long version) {
        if (accepting) return;
        accepting = true;
        acceptPrivacyPolicy0(version);
        accepting = false;
    }

    private AuthToken acceptPrivacyPolicy0(long version) {
        if (reauthLock) {
            while(reauthLock);
            return currentToken;
        }

        if (currentToken instanceof PrivacyPolicyRequiredToken) {
            reauthLock = true;
            try {
                currentToken = authAPI.acceptNewPrivacyPolicy(currentToken.getToken(), version);
                listenerList.forEach(a -> a.onNewAuthToken(currentToken));
            } catch (Exception e) {
                currentToken = new FailedAuthToken(e);
                listenerList.forEach(a -> a.onNewAuthToken(currentToken));
                logger.error("Accepting the Privacy Policy failed with message {}, trying again in a 2 seconds", String.valueOf(Throwables.getRootCause(e)));
                throw new AuthFailedException(e);
            } finally {
                reauthLock = false;
            }
        }
        return currentToken;
    }
}
