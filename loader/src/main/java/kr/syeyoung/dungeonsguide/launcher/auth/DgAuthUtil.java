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

package kr.syeyoung.dungeonsguide.launcher.auth;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.auth.token.AuthToken;
import kr.syeyoung.dungeonsguide.launcher.auth.token.DGAuthToken;
import kr.syeyoung.dungeonsguide.launcher.auth.token.PrivacyPolicyRequiredToken;
import kr.syeyoung.dungeonsguide.launcher.exceptions.http.AuthServerException;
import kr.syeyoung.dungeonsguide.launcher.exceptions.http.ResponseParsingException;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.stream.Collectors;

public class DgAuthUtil {
    private static final SecureRandom random = new SecureRandom();
    private DgAuthUtil(){}

    /**
     *
     * @param connection
     * @param data
     * @return
     * @param <T>
     * @throws IOException when stuff wrong
     * @throws ResponseParsingException failed to parse generic response
     * @throws AuthServerException auth server returned FAILURE
     */
    private static <T> T getResponse(HttpURLConnection connection, Class<T> data) throws IOException {
        connection.getResponseCode();
        InputStream toRead = connection.getErrorStream();
        if (toRead == null)
            toRead = connection.getInputStream();
        String payload = IOUtils.readLines(toRead).stream().collect(Collectors.joining("\n"));

        try {
            JSONObject json = new JSONObject(payload);
            DGResponse<T> response = new DGResponse<>(
                    connection.getResponseCode(),
                    json.getString("status"),
                    json.isNull("data") ? null:(T) json.get("data"),
                    json.isNull("errorMessage") ?null: json.getString("errorMessage"),
                    json.isNull("qrCode") ? null:  json.getString("qrCode")
            );

            if (!"Success".equals(response.getStatus())) {
                throw new AuthServerException(response);
            }

            return (T) response.getData();
        } catch (AuthServerException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseParsingException(payload, e);
        } finally {
            toRead.close();
        }
    }

    public static String requestAuth() throws IOException {
        GameProfile profile = Minecraft.getMinecraft().getSession().getProfile();

        HttpURLConnection connection = (HttpURLConnection) new URL(Main.DOMAIN + "/auth/v2/requestAuth").openConnection();
        connection.setRequestProperty("User-Agent", "DungeonsGuide/4.0");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(1000);
        connection.setReadTimeout(3000);
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        connection.getOutputStream().write(("{\"uuid\":\""+profile.getId().toString()+"\",\"nickname\":\""+profile.getName()+"\"}").getBytes());

        return getResponse(connection, String.class);
    }

    public static byte[] checkSessionAuthenticityAndReturnEncryptedSecret(String tempToken) throws NoSuchAlgorithmException, AuthenticationException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        byte[] sharedSecret = new byte[16];
        byte[] result;
        byte[] publicKey;
        try {
            JSONObject d = getJwtPayload(tempToken);
            random.nextBytes(sharedSecret);
            publicKey = Base64.decodeBase64(d.getString("publicKey"));

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, AuthUtil.getPublicKey(publicKey));
            result = cipher.doFinal(sharedSecret);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse publicKey, generate shared secret, then encrypt it.", e);
        }

        String hash = calculateServerHash(sharedSecret, publicKey);

        Session session = Minecraft.getMinecraft().getSession();
        MinecraftSessionService yggdrasilMinecraftSessionService = Minecraft.getMinecraft().getSessionService();
        yggdrasilMinecraftSessionService.joinServer(session.getProfile(), session.getToken(), hash);

        return result;
    }

    /**
     *
     * @param tempToken
     * @param encSecret
     * @return
     * @throws IOException when io error happens
     * @throws ResponseParsingException when fails to parse exception
     * @throws AuthServerException when auth server throws error
     */
    public static AuthToken verifyAuth(String tempToken, byte[] encSecret) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL(Main.DOMAIN + "/auth/v2/authenticate").openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("User-Agent", "DungeonsGuide/4.0");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setConnectTimeout(1000);
        urlConnection.setReadTimeout(3000);
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);

        urlConnection.getOutputStream().write(("{\"jwt\":\""+tempToken+"\",\"sharedSecret\":\""+Base64.encodeBase64String(encSecret)+"\"}").getBytes());

        JSONObject data = getResponse(urlConnection, JSONObject.class);
        try {
            if (data.getString("result").equals("TOS_PRIVACY_POLICY_ACCEPT_REQUIRED")) {
                return new PrivacyPolicyRequiredToken(data.getString("jwt"));
            } else if (data.getString("result").equals("SUCCESSFUL")) {
                return new DGAuthToken(data.getString("jwt"));
            } else {
                throw new UnsupportedOperationException("Unknown auth result");
            }
        } catch (Exception e) {
            throw new ResponseParsingException(data.toString(), e);
        }
    }

    public static AuthToken acceptNewPrivacyPolicy(String tempToken, long version) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL(Main.DOMAIN + "/auth/v2/acceptPrivacyPolicy").openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("User-Agent", "DungeonsGuide/4.0");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setConnectTimeout(1000);
        urlConnection.setReadTimeout(3000);
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);

        urlConnection.getOutputStream().write(("{\"jwt\": \""+tempToken+"\", \"version\": "+version+"}").getBytes());

        JSONObject data = getResponse(urlConnection, JSONObject.class);
        try {
            if (data.getString("result").equals("TOS_PRIVACY_POLICY_ACCEPT_REQUIRED")) {
                return new PrivacyPolicyRequiredToken(data.getString("jwt"));
            } else if (data.getString("result").equals("SUCCESSFUL")) {
                return new DGAuthToken(data.getString("jwt"));
            } else {
                throw new UnsupportedOperationException("Unknown auth result");
            }
        } catch (Exception e) {
            throw new ResponseParsingException(data.toString(), e);
        }
    }
    public static JSONObject getJwtPayload(String jwt) {
        String midPart = jwt.split("\\.")[1].replace("+", "-").replace("/", "_");
        String base64Decode = new String(Base64.decodeBase64(midPart)); // padding
        return new JSONObject(base64Decode);
    }

    public static String calculateServerHash(byte[] a, byte[] b) throws NoSuchAlgorithmException {
        MessageDigest c = MessageDigest.getInstance("SHA-1");
        c.update("".getBytes());
        c.update(a);
        c.update(b);
        byte[] d = c.digest();
        return new BigInteger(d).toString(16);
    }
}
