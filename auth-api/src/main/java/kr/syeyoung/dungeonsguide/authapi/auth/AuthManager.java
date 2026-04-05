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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class AuthManager implements AutoCloseable {
    private AuthToken currentToken = new NullToken();
    private final Thread authThread;

    private final List<AuthEventListener> listenerList = new ArrayList<>();
    private final AuthService authService;
    private final AuthAPI authAPI;

    public AuthManager(AuthService service, String userAgent) {
        this("https://v2.dungeons.guide/api", service, userAgent);
    }

    public AuthManager(String baseUrl, AuthService minecraft, String userAgent) {
        this.authService = minecraft;
        this.authAPI = new AuthAPI(baseUrl, userAgent);
        this.authThread = new Thread(this::authLoop);
    }

    public void init() {
        this.authThread.start();
    }

    public void registerListener(AuthEventListener listener) {
        listenerList.add(listener);
    }
    public void unregisterListener(AuthEventListener listener) {
        listenerList.remove(listener);
    }

    public AuthToken getCurrentToken() {
        tokenLock.readLock().lock();
        try {
            return currentToken;
        } finally {
            tokenLock.readLock().unlock();
        }
    }
    public String getWorkingTokenOrNull() {
        AuthToken currentToken = getCurrentToken();
        if (currentToken instanceof DGAuthToken) return currentToken.getToken();
        else return null;
    }

    /**
     * @throws AuthenticationUnavailableException variations of it.
     * @return actual dg token
     */
    public String getWorkingTokenOrThrow() {
        tokenLock.readLock().lock();
        try {
            if (currentToken instanceof DGAuthToken) return currentToken.getToken();
            else if (currentToken instanceof FailedAuthToken)
                throw new AuthFailedException(((FailedAuthToken) currentToken).getException());
            else if (currentToken instanceof NullToken) throw new AuthenticationUnavailableException("Null Token");
            else if (currentToken instanceof PrivacyPolicyRequiredToken) throw new PrivacyPolicyRequiredException();
            throw new IllegalStateException("weird token: " + currentToken);
        } finally {
            tokenLock.readLock().unlock();
        }
    }


    private ReentrantReadWriteLock tokenLock = new ReentrantReadWriteLock();
    private Condition tokenValid = tokenLock.writeLock().newCondition();
    private Condition newToken = tokenLock.writeLock().newCondition();

    public AuthToken waitForWorkingToken() throws InterruptedException {
        tokenLock.writeLock().lock();
        try {
            if (currentToken instanceof DGAuthToken) return currentToken;
            retryAuth();
            while (true) {
                tokenValid.await();
                if (currentToken instanceof DGAuthToken) {
                    return currentToken;
                }
            }
        } finally {
            tokenLock.writeLock().unlock();
        }
    }
    public AuthToken waitForNextToken() throws InterruptedException {
        tokenLock.writeLock().lock();
        try {
            retryAuth();
            while (true) {
                newToken.await();
                if (!(currentToken instanceof NullToken))
                    return currentToken;
            }
        } finally {
            tokenLock.writeLock().unlock();
        }
    }


    private boolean retryAuth = false;
    public void retryAuth() {
        synchronized (this) {
            retryAuth = true;
            this.notifyAll();
        }
    }

    private void authLoop() {
        try {
            while (!Thread.interrupted()) {
                if (!currentToken.isAuthenticated() || retryAuth) {
                    retryAuth = false;
                    authenticate();
                    if (this.currentToken instanceof FailedAuthToken) {
                        Thread.sleep(10000); // retry after 10s.
                    } else {
                        synchronized (this) {
                            this.notifyAll();
                            this.wait();
                        }
                    }
                } else if (!currentToken.getUUID().equals(authService.getCurrentPlayerUUID().toString())) {
                    invalidateToken();
                    authenticate();
                }
            }
        } catch (InterruptedException ignored) {}
    }

    private void invalidateToken() {
        this.currentToken = new NullToken();
        listenerList.forEach(a -> a.onNewAuthToken(currentToken));
    }

    private AuthToken authenticate() {
        tokenLock.writeLock().lock();
        try {
            String token = authAPI.requestAuth(authService.getCurrentPlayerUUID(), authService.getCurrentPlayerUsername());
            byte[] encSecret = AuthAPI.checkSessionAuthenticityAndReturnEncryptedSecret(authService, token);
            currentToken = authAPI.verifyAuth(token, encSecret);

            if (currentToken instanceof DGAuthToken)
                tokenValid.signalAll();
            newToken.signalAll();

            listenerList.forEach(a -> a.onNewAuthToken(currentToken));
        } catch (Exception e) {
            currentToken = new FailedAuthToken(e);
            newToken.signalAll();
            listenerList.forEach(a -> a.onNewAuthToken(currentToken));
            throw new AuthFailedException(e);
        } finally {
            tokenLock.writeLock().unlock();
        }
        return currentToken;
    }


    /**
     * Accept privacy policy
     *
     * @param version version of privacy policy to accept.
     * @throws IllegalStateException if current token is not PrivacyPolicyRequiredToken
     * @throws AuthFailedException if accepting privacy policy failed
     * @return new Auth token
     */
    public AuthToken acceptPrivacyPolicy(long version) throws InterruptedException {
        tokenLock.writeLock().lock();
        try {
            if (!(this.currentToken instanceof PrivacyPolicyRequiredToken)) {
                throw new IllegalStateException("Current token is not PrivacyPolicyRequiredToken");
            }

            try {
                this.currentToken = authAPI.acceptNewPrivacyPolicy(this.currentToken.getToken(), version);
                listenerList.forEach(a -> a.onNewAuthToken(this.currentToken));

                if (currentToken instanceof DGAuthToken)
                    tokenValid.signalAll();
                newToken.signalAll();

            } catch (Exception e) {
                this.currentToken = new FailedAuthToken(e);
                newToken.signalAll();
                listenerList.forEach(a -> a.onNewAuthToken(this.currentToken));
                throw new AuthFailedException(e);
            }
            return this.currentToken;
        } finally {
            tokenLock.writeLock().unlock();
        }
    }

    @Override
    public void close() throws Exception {
        this.authThread.interrupt();
    }
}
