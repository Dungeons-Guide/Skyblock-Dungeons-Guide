/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraftforge.fml.common.ProgressManager;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.*;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Authenticator {
    private KeyPair rsaKey;
    private String token;
    private final ProgressManager.ProgressBar progressBar;

    public String getToken() {
        return token;
    }

    private KeyPair getKeyPair()  {
        KeyPairGenerator a = null;
        try {
            a = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException b) { }
        a.initialize(1024);
        this.rsaKey = a.generateKeyPair();
        return this.rsaKey;
    }

    private PublicKey dgPublicKey;
    private PublicKey getDGPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (dgPublicKey != null) return dgPublicKey;
        X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.decodeBase64("MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAxO89qtwG67jNucQ9Y44c" +
                "IUs/B+5BeJPs7G+RG2gfs4/2+tzF/c1FLDc33M7yKw8aKk99vsBUY9Oo8gxxiEPB" +
                "JitP/qfon2THp94oM77ZTpHlmFoqbZMcKGZVI8yfvEL4laTM8Hw+qh5poQwtpEbK" +
                "Xo47AkxygxJasUnykER2+aSTZ6kWU2D4xiNtFA6lzqN+/oA+NaYfPS0amAvyVlHR" +
                "n/8IuGkxb5RrlqVssQstFnxsJuv88qdGSEqlcKq2tLeg9hb8eCnl2OFzvXmgbVER" +
                "0JaV+4Z02fVG1IlR3Xo1mSit7yIU6++3usRCjx2yfXpnGGJUW5pe6YETjNew3ax+" +
                "FAZ4GePWCdmS7FvBnbbABKo5pE06ZTfDUTCjQlAJQiUgoF6ntMJvQAXPu48Vr8q/" +
                "mTcuZWVnI6CDgyE7nNq3WNoq3397sBzxRohMxuqzl3T19zkfPKF05iV2Ju1HQMW5" +
                "I119bYrmVD240aGESZc20Sx/9g1BFpNzQbM5PGUlWJ0dhLjl2ge4ip2hHciY3OEY" +
                "p2Qy2k+xEdenpKdL+WMRimCQoO9gWe2Tp4NmP5dppDXZgPjXqjZpnGs0Uxs+fXqW" +
                "cwlg3MbX3rFl9so/fhVf4p9oXZK3ve7z5D6XSSDRYECvsKIa08WAxJ/U6n204E/4" +
                "xUF+3ZgFPdzZGn2PU7SsnOsCAwEAAQ=="));
        return dgPublicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    public Authenticator(ProgressManager.ProgressBar progressBar) {
        this.progressBar = progressBar;
        progressBar.step("Generating KeyPair");
        getKeyPair();
    }

    public String authenticateAndDownload(String version) throws IOException, AuthenticationException, NoSuchAlgorithmException, CertificateException, KeyStoreException, KeyManagementException, InvalidKeySpecException, SignatureException {
        Session session = Minecraft.getMinecraft().getSession();
        String sessionToken = session.getToken();

        progressBar.step("Authenticating (1/2)");
        String tempToken = requestAuth(session.getProfile());
        MinecraftSessionService yggdrasilMinecraftSessionService = Minecraft.getMinecraft().getSessionService();
        JsonObject d = getJwtPayload(tempToken);
        String hash = calculateServerHash(Base64.decodeBase64(d.get("sharedSecret").getAsString()),
                Base64.decodeBase64(d.get("publicKey").getAsString()));
        yggdrasilMinecraftSessionService.joinServer(session.getProfile(), sessionToken, hash);
        progressBar.step("Authenticating (2/2)");
        this.token = verifyAuth(tempToken, this.rsaKey.getPublic());
        try {
            progressBar.step("Downloading Jar");
            if (version != null)
                downloadSafe(this.token, "https://dungeons.guide/resource/version?v=" + version, true);
            progressBar.step("Downloading Rooms");
            downloadSafe(this.token, "https://dungeons.guide/resource/roomdata", false);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return this.token;
    }

    public JsonObject getJwtPayload(String jwt) {
        String midPart = jwt.split("\\.")[1].replace("+", "-").replace("/", "_");
        String base64Decode = new String(Base64.decodeBase64(midPart)); // padding
        return (JsonObject) new JsonParser().parse(base64Decode);
    }



    private String requestAuth(GameProfile profile) throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException, KeyManagementException {
        HttpsURLConnection connection = (HttpsURLConnection) new URL("https://dungeons.guide/auth/requestAuth").openConnection();
        connection.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        connection.getOutputStream().write(("{\"uuid\":\""+profile.getId().toString()+"\",\"nickname\":\""+profile.getName()+"\"}").getBytes());
        String payload = String.join("\n", IOUtils.readLines(connection.getErrorStream() == null ? connection.getInputStream() : connection.getErrorStream()));
        if (connection.getResponseCode() >= 400)
            System.out.println("https://dungeons.guide/auth/requestAuth :: Received "+connection.getResponseCode()+" along with\n"+payload);

        JsonObject json = (JsonObject) new JsonParser().parse(payload);

        if (!"ok".equals(json.get("status").getAsString())) {
            return null;
        }
        return json.get("data").getAsString();
    }
    private String verifyAuth(String tempToken, PublicKey clientKey) throws IOException {
        HttpsURLConnection urlConnection = (HttpsURLConnection) new URL("https://dungeons.guide/auth/authenticate").openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);

        urlConnection.getOutputStream().write(("{\"jwt\":\""+tempToken+"\",\"publicKey\":\""+Base64.encodeBase64URLSafeString(clientKey.getEncoded())+"\"}").getBytes());
        String payload = String.join("\n", IOUtils.readLines(urlConnection.getErrorStream() == null ? urlConnection.getInputStream() : urlConnection.getErrorStream()));
        if (urlConnection.getResponseCode() >= 400)
            System.out.println("https://dungeons.guide/auth/authenticate :: Received "+urlConnection.getResponseCode()+" along with\n"+payload);

        JsonObject jsonObject = (JsonObject) new JsonParser().parse(payload);
        if (!"ok".equals(jsonObject.get("status").getAsString())) {
            return null;
        }
        return jsonObject.get("data").getAsString();
    }

    private final HashMap<String, byte[]> loadedResources = new HashMap<String, byte[]>();

    public HashMap<String, byte[]> getResources() {
        return loadedResources;
    }

    private void downloadSafe(String dgToken, String url, boolean isValidateSignature) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, CertificateException, KeyStoreException, KeyManagementException, SignatureException, InvalidKeySpecException {
        HttpsURLConnection dgConnection = (HttpsURLConnection) new URL(url).openConnection();
        dgConnection.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        dgConnection.setRequestProperty("Content-Type", "application/json");
        dgConnection.setRequestMethod("GET");
        dgConnection.setRequestProperty("Authorization", dgToken);
        dgConnection.setDoInput(true);
        dgConnection.setDoOutput(true);

        InputStream inputStream = dgConnection.getInputStream();
        byte[] lengthBytes = new byte[4];
        inputStream.read(lengthBytes);
        int length = ((lengthBytes[0] & 0xFF) << 24) |
                ((lengthBytes[1] & 0xFF) << 16) |
                ((lengthBytes[2] & 0xFF) << 8) |
                ((lengthBytes[3] & 0xFF));
        while (inputStream.available() < length) ;
        byte[] keyPayload = new byte[length];
        inputStream.read(keyPayload);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, this.rsaKey.getPrivate());
        byte[] h = cipher.doFinal(keyPayload);

        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(h, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(h);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);

        cipherInputStream.read(lengthBytes);
        length = ((lengthBytes[0] & 0xFF) << 24) |
                ((lengthBytes[1] & 0xFF) << 16) |
                ((lengthBytes[2] & 0xFF) << 8) |
                ((lengthBytes[3] & 0xFF));

        int totalLen = length;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buff = new byte[256];
        while (totalLen > 0) {
            int len = cipherInputStream.read(buff, 0, Math.min(buff.length,  totalLen));
            totalLen -= len;
            bos.write(buff, 0, len);
        }
        byte[] body = bos.toByteArray();

        byte[] signed = null;
        if (isValidateSignature) {
            progressBar.step("Validating Signature");
            cipherInputStream.read(lengthBytes,0 , 4);
            length = ((lengthBytes[0] & 0xFF) << 24) |
                    ((lengthBytes[1] & 0xFF) << 16) |
                    ((lengthBytes[2] & 0xFF) << 8) |
                    ((lengthBytes[3] & 0xFF));

            totalLen = length;
            bos = new ByteArrayOutputStream();
            while (totalLen > 0) {
                int len = cipherInputStream.read(buff, 0, Math.min(buff.length,  totalLen));
                totalLen -= len;
                bos.write(buff, 0, len);
            }
            signed = bos.toByteArray();

            Signature sign = Signature.getInstance("SHA512withRSA");
            sign.initVerify(getDGPublicKey());
            sign.update(body);
            boolean truth = sign.verify(signed);
            if (!truth) throw new SignatureException("DG SIGNATURE FORGED");
        }

        ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(body));
        ZipEntry zipEntry;
        while ((zipEntry=zipInputStream.getNextEntry()) != null) {
            byte[] buffer = new byte[256];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int p = 0;
            while((p = zipInputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, p);
            }
            this.loadedResources.put(zipEntry.getName(), byteArrayOutputStream.toByteArray());
        }
        dgConnection.disconnect();
    }

    public JsonElement getJsonSecured(String u) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, CertificateException, KeyStoreException, KeyManagementException {
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL(u).openConnection();
        httpsURLConnection.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        httpsURLConnection.setRequestProperty("Content-Type", "application/json");
        httpsURLConnection.setRequestMethod("GET");
        httpsURLConnection.setRequestProperty("Authorization", this.token);
        httpsURLConnection.setDoInput(true);
        httpsURLConnection.setDoOutput(true);

        InputStream inputStream = httpsURLConnection.getInputStream();
        byte[] lengthPayload = new byte[4];
        inputStream.read(lengthPayload);
        int length = ((lengthPayload[0] & 0xFF) << 24) |
                ((lengthPayload[1] & 0xFF) << 16) |
                ((lengthPayload[2] & 0xFF) << 8) |
                ((lengthPayload[3] & 0xFF));
        while (inputStream.available() < length) ;
        byte[] keyPayload = new byte[length];
        inputStream.read(keyPayload);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, this.rsaKey.getPrivate());
        byte[] AESKey = cipher.doFinal(keyPayload);

        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(AESKey, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(AESKey);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);
        cipherInputStream.read(lengthPayload);
        length = ((lengthPayload[0] & 0xFF) << 24) |
                ((lengthPayload[1] & 0xFF) << 16) |
                ((lengthPayload[2] & 0xFF) << 8) |
                ((lengthPayload[3] & 0xFF));
        JsonElement l = new JsonParser().parse(new InputStreamReader(cipherInputStream));
        httpsURLConnection.disconnect();
        return l;
    }

    public String calculateServerHash(byte[] a, byte[] b) throws NoSuchAlgorithmException {
        MessageDigest c = MessageDigest.getInstance("SHA-1");
        c.update("".getBytes());
        c.update(a);
        c.update(b);
        byte[] d = c.digest();
        return new BigInteger(d).toString(16);
    }
}
