/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
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

package kr.syeyoung.dungeonsguide.launcher.authentication;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.exceptions.PrivacyPolicyRequiredException;
import kr.syeyoung.dungeonsguide.launcher.exceptions.TokenExpiredException;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import sun.reflect.Reflection;

import javax.crypto.*;
import javax.net.ssl.*;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Authenticator {
    private String token;
    private Instant validThru;
    @Getter
    private TokenStatus tokenStatus = TokenStatus.UNAUTHENTICATED;

    private final SecureRandom secureRandom = new SecureRandom();

    private Lock authenticationLock = new ReentrantLock();

    static {
        Reflection.registerFieldsToFilter(Authenticator.class, "token"); // Please do not touch this field. I know there is a way to block it completely, but I won't do it here.
    }

    public String getRawToken() {
        return token;
    }
    public String getUnexpiredToken() {
        if (tokenStatus != TokenStatus.AUTHENTICATED) throw new IllegalStateException("Token is not available");
        long expiry = getJwtPayload(token).getLong("exp");
        if (System.currentTimeMillis() >= expiry-2000 || tokenStatus == TokenStatus.EXPIRED) {
            tokenStatus = TokenStatus.EXPIRED;
            try {
                repeatAuthenticate(5);
            } catch (Throwable t) {
                Main.getMain().setLastError(t);
                throw new TokenExpiredException();
            }
        }
        return token;
    }


    private byte[] generateSharedSecret() {
        byte[] bts = new byte[32];
        secureRandom.nextBytes(bts);
        return bts;
    }

    public String repeatAuthenticate(int tries) {
        int cnt = 0;
        while(true) {
            try {
                reauthenticate();
                break;
            } catch (IOException | AuthenticationException | NoSuchAlgorithmException e) {
                if (cnt == tries) throw new RuntimeException(e);
                try {
                    Thread.sleep((long) Math.max(Math.pow(2, tries)* 100, 1000 * 10));
                } catch (InterruptedException ex) {}
            }
            cnt++;
        }
        return token;
    }
    public String reauthenticate() throws IOException, AuthenticationException, NoSuchAlgorithmException {
        try {
            authenticationLock.lock();

            MinecraftSessionService yggdrasilMinecraftSessionService = Minecraft.getMinecraft().getSessionService();
            Session session = Minecraft.getMinecraft().getSession();

            tokenStatus = TokenStatus.UNAUTHENTICATED;
            token = null;
            token = requestAuth(session.getProfile().getId(), session.getProfile().getName());
            JSONObject d = getJwtPayload(token);

            byte[] sharedSecret = generateSharedSecret();


            String hash = calculateServerHash(sharedSecret,
                    Base64.decodeBase64(d.getString("publicKey")));

            byte[] encodedSharedSecret;
            try {
                Cipher cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.ENCRYPT_MODE, KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.decodeBase64(d.getString("publicKey")))));
                encodedSharedSecret = cipher.doFinal(sharedSecret);
            } catch (NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
                     InvalidKeySpecException |
                     InvalidKeyException e) {
                throw new RuntimeException(e);
            }

            yggdrasilMinecraftSessionService.joinServer(session.getProfile(), session.getToken(), hash); // Sent to "MOJANG" Server.
            JSONObject furtherStuff = verifyAuth(token, encodedSharedSecret);
            token = furtherStuff.getString("jwt");
            if ("TOS_PRIVACY_POLICY_ACCEPT_REQUIRED".equals(furtherStuff.getString("result"))) {
                tokenStatus = TokenStatus.PP_REQUIRED;
                throw new PrivacyPolicyRequiredException();
            }
            tokenStatus = TokenStatus.AUTHENTICATED;
            return this.token;
        } finally {
            authenticationLock.unlock();
        }
    }

    public String acceptLatestTOS() throws IOException {
        try {
            authenticationLock.lock();
            if (tokenStatus != TokenStatus.PP_REQUIRED) throw new IllegalStateException("Already accepted TOS");
            JSONObject furtherStuff = acceptPrivacyPolicy(token);
            token = furtherStuff.getString("jwt");
            if ("TOS_PRIVACY_POLICY_ACCEPT_REQUIRED".equals(furtherStuff.getString("result"))) {
                tokenStatus = TokenStatus.PP_REQUIRED;
                throw new PrivacyPolicyRequiredException();
            }
            tokenStatus = TokenStatus.AUTHENTICATED;
            return this.token;
        } finally {
            authenticationLock.unlock();
        }
    }

    public JSONObject getJwtPayload(String jwt) {
        String midPart = jwt.split("\\.")[1].replace("+", "-").replace("/", "_");
        String base64Decode = new String(Base64.decodeBase64(midPart)); // padding
        return new JSONObject(base64Decode);
    }

    private String requestAuth(UUID uuid, String nickname) throws IOException {
        HttpsURLConnection urlConnection = (HttpsURLConnection) request("POST", "/auth/v2/requestAuth");
        urlConnection.setRequestProperty("Content-Type", "application/json");

        urlConnection.getOutputStream().write(("{\"uuid\":\""+uuid.toString()+"\",\"nickname\":\""+nickname+"\"}").getBytes());
        try (InputStream is = obtainInputStream(urlConnection)) {
            String payload = String.join("\n", IOUtils.readLines(is));
            if (urlConnection.getResponseCode() != 200)
                System.out.println("/auth/requestAuth :: Received " + urlConnection.getResponseCode() + " along with\n" + payload);

            JSONObject json = new JSONObject(payload);

            if ("Success".equals(json.getString("status"))) {
                return json.getString("data");
            } else {
                throw new AuthServerException(json);
            }
        }
    }
    private JSONObject verifyAuth(String tempToken, byte[] secret) throws IOException {
        HttpsURLConnection urlConnection = (HttpsURLConnection) request("POST", "/auth/v2/authenticate");

        urlConnection.getOutputStream().write(("{\"jwt\":\""+tempToken+"\",\"sharedSecret\":\""+Base64.encodeBase64URLSafeString(secret)+"}").getBytes());
        try (InputStream is = obtainInputStream(urlConnection)) {
            String payload = String.join("\n", IOUtils.readLines(is));
            if (urlConnection.getResponseCode() != 200)
                System.out.println("/auth/authenticate :: Received " + urlConnection.getResponseCode() + " along with\n" + payload);

            JSONObject jsonObject = new JSONObject(payload);
            if (!"Success".equals(jsonObject.getString("status"))) {
                throw new AuthServerException(jsonObject);
            }
            return jsonObject.getJSONObject("data");
        }
    }
    private JSONObject acceptPrivacyPolicy(String tempToken) throws IOException {
        HttpsURLConnection urlConnection = (HttpsURLConnection) request("POST", "/auth/v2/acceptPrivacyPolicy");

        urlConnection.getOutputStream().write(tempToken.getBytes());
        try (InputStream is = obtainInputStream(urlConnection)) {
            String payload = String.join("\n", IOUtils.readLines(is));
            if (urlConnection.getResponseCode() != 200)
                System.out.println("/auth/authenticate :: Received " + urlConnection.getResponseCode() + " along with\n" + payload);

            JSONObject jsonObject = new JSONObject(payload);
            if (!"Success".equals(jsonObject.getString("status"))) {
                throw new AuthServerException(jsonObject);
            }
            return jsonObject.getJSONObject("data");
        }
    }


    private String calculateServerHash(byte[] a, byte[] b) throws NoSuchAlgorithmException {
        MessageDigest c = MessageDigest.getInstance("SHA-1");
        c.update("".getBytes());
        c.update(a);
        c.update(b);
        byte[] d = c.digest();
        return new BigInteger(d).toString(16);
    }

    public InputStream obtainInputStream(HttpURLConnection huc) {
        InputStream inputStream = null;
        try {
            inputStream = huc.getInputStream();
        } catch (Exception e) {
            inputStream = huc.getErrorStream();
        }
        return inputStream;
    }
    public HttpURLConnection request(String method, String url) throws IOException {
        HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(Main.DOMAIN+url).openConnection();
        urlConnection.setRequestMethod(method);
        urlConnection.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setAllowUserInteraction(true);
        String token = getUnexpiredToken();
        if (tokenStatus == TokenStatus.AUTHENTICATED)
            urlConnection.setRequestProperty("Authorization", "Bearer "+token);
        return urlConnection;
    }
}
