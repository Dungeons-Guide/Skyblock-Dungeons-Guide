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

import kr.syeyoung.dungeonsguide.authapi.api.AuthService;
import kr.syeyoung.dungeonsguide.authapi.auth.token.AuthToken;
import kr.syeyoung.dungeonsguide.authapi.auth.token.DGAuthToken;
import kr.syeyoung.dungeonsguide.authapi.auth.token.PrivacyPolicyRequiredToken;
import kr.syeyoung.dungeonsguide.authapi.exceptions.http.AuthServerException;
import kr.syeyoung.dungeonsguide.authapi.exceptions.http.ResponseParsingException;
import kr.syeyoung.dungeonsguide.authapi.util.LetsEncrypt;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.UUID;
import java.util.stream.Collectors;

public class AuthAPI {
    private static final SecureRandom random = new SecureRandom();

    private final String baseUrl;
    private final String userAgent;
    public AuthAPI(String baseUrl, String userAgent) {
        this.baseUrl = baseUrl;
        this.userAgent = userAgent;
    }




    private static String getResponse(HttpURLConnection connection) throws IOException {
        connection.getResponseCode();
        InputStream toRead = connection.getErrorStream();
        if (toRead == null)
            toRead = connection.getInputStream();
        try (InputStream read = toRead) {
            return new BufferedReader(new InputStreamReader(read, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
        }
    }

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
        String payload = getResponse(connection);

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
        }
    }

    public String requestAuth(UUID uuid, String name) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(baseUrl + "/auth/v2/requestAuth").openConnection();
        connection.setSSLSocketFactory(LetsEncrypt.LETS_ENCRYPT);
        connection.setRequestProperty("User-Agent", userAgent);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(1000);
        connection.setReadTimeout(3000);
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        connection.getOutputStream().write(("{\"uuid\":\""+uuid.toString()+"\",\"nickname\":\""+name+"\"}").getBytes());

        return getResponse(connection, String.class);
    }

    public static byte[] checkSessionAuthenticityAndReturnEncryptedSecret(AuthService service, String tempToken) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        byte[] sharedSecret = new byte[16];
        byte[] result;
        byte[] publicKey;
        try {
            JSONObject d = getJwtPayload(tempToken);
            random.nextBytes(sharedSecret);
            publicKey = Base64.getDecoder().decode(d.getString("publicKey"));

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, AuthUtil.getPublicKey(publicKey));
            result = cipher.doFinal(sharedSecret);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse publicKey, generate shared secret, then encrypt it.", e);
        }

        String hash = calculateServerHash(sharedSecret, publicKey);


        service.mojangAuth(hash);
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
    public AuthToken verifyAuth(String tempToken, byte[] encSecret) throws IOException {
        HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(baseUrl + "/auth/v2/authenticate").openConnection();
        urlConnection.setSSLSocketFactory(LetsEncrypt.LETS_ENCRYPT);
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("User-Agent", userAgent);
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setConnectTimeout(1000);
        urlConnection.setReadTimeout(3000);
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);

        urlConnection.getOutputStream().write(("{\"jwt\":\""+tempToken+"\",\"sharedSecret\":\""+Base64.getEncoder().encodeToString(encSecret)+"\"}").getBytes());

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

    public AuthToken acceptNewPrivacyPolicy(String tempToken, long version) throws IOException {
        HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(baseUrl + "/auth/v2/acceptPrivacyPolicy").openConnection();
        urlConnection.setSSLSocketFactory(LetsEncrypt.LETS_ENCRYPT);
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("User-Agent", userAgent);
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
        String base64Decode = new String(Base64.getDecoder().decode(midPart)); // padding
        return new JSONObject(base64Decode);
    }

    private static String calculateServerHash(byte[] a, byte[] b) throws NoSuchAlgorithmException {
        MessageDigest c = MessageDigest.getInstance("SHA-1");
        c.update("".getBytes());
        c.update(a);
        c.update(b);
        byte[] d = c.digest();
        return new BigInteger(d).toString(16);
    }
}
