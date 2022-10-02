package kr.syeyoung.dungeonsguide.auth.authprovider;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import kr.syeyoung.dungeonsguide.auth.AuthManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.security.*;

public class AuthProviderUtil {
    private AuthProviderUtil() {}

    public static KeyPair getKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator a = null;
        a = KeyPairGenerator.getInstance("RSA");
        a.initialize(1024);
        return a.generateKeyPair();
    }

    public static String checkSessionAuthenticity(Session session, String baseurl) throws IOException, NoSuchAlgorithmException, AuthenticationException {
        String tempToken = requestAuth(session.getProfile(), baseurl);
        MinecraftSessionService yggdrasilMinecraftSessionService = Minecraft.getMinecraft().getSessionService();
        assert tempToken != null;
        JsonObject d = getJwtPayload(tempToken);
        String hash = calculateServerHash(Base64.decodeBase64(d.get("sharedSecret").getAsString()),
                Base64.decodeBase64(d.get("publicKey").getAsString()));
        yggdrasilMinecraftSessionService.joinServer(session.getProfile(), session.getToken(), hash);
        return tempToken;
    }

    private static String requestAuth(GameProfile profile, String baseurl) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(baseurl + "/auth/requestAuth").openConnection();
        connection.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        connection.getOutputStream().write(("{\"uuid\":\""+profile.getId().toString()+"\",\"nickname\":\""+profile.getName()+"\"}").getBytes());
        String payload = String.join("\n", IOUtils.readLines(connection.getErrorStream() == null ? connection.getInputStream() : connection.getErrorStream()));

        JsonObject json = (JsonObject) new JsonParser().parse(payload);

        if (!"ok".equals(json.get("status").getAsString())) {
            return null;
        }
        return json.get("data").getAsString();
    }

    public static JsonObject getJwtPayload(String jwt) {
        String midPart = jwt.split("\\.")[1].replace("+", "-").replace("/", "_");
        String base64Decode = new String(Base64.decodeBase64(midPart)); // padding
        return (JsonObject) new JsonParser().parse(base64Decode);
    }

    public static String calculateServerHash(byte[] a, byte[] b) throws NoSuchAlgorithmException {
        MessageDigest c = MessageDigest.getInstance("SHA-1");
        c.update("".getBytes());
        c.update(a);
        c.update(b);
        byte[] d = c.digest();
        return new BigInteger(d).toString(16);
    }

    public static String verifyAuth(String tempToken, PublicKey clientKey, String baseurl) throws IOException {
        HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(baseurl + "/auth/authenticate").openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);

        urlConnection.getOutputStream().write(("{\"jwt\":\""+tempToken+"\",\"publicKey\":\""+Base64.encodeBase64URLSafeString(clientKey.getEncoded())+"\"}").getBytes());
        String payload = String.join("\n", IOUtils.readLines(urlConnection.getErrorStream() == null ? urlConnection.getInputStream() : urlConnection.getErrorStream()));

        JsonObject jsonObject = (JsonObject) new JsonParser().parse(payload);
        if (!"ok".equals(jsonObject.get("status").getAsString())) {
            return null;
        }
        return jsonObject.get("data").getAsString();
    }

    public static JsonElement getJsonSecured(String u) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException{

        int length = 0;
        CipherInputStream cipherInputStream = null;

        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL(u).openConnection();
        httpsURLConnection.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        httpsURLConnection.setRequestProperty("Content-Type", "application/json");
        httpsURLConnection.setRequestMethod("GET");
        httpsURLConnection.setRequestProperty("Authorization", AuthManager.getInstance().getToken());
        httpsURLConnection.setDoInput(true);
        httpsURLConnection.setDoOutput(true);

        InputStream inputStream = httpsURLConnection.getInputStream();
        byte[] lengthPayload = new byte[4];
        inputStream.read(lengthPayload);
        length = ((lengthPayload[0] & 0xFF) << 24) |
                ((lengthPayload[1] & 0xFF) << 16) |
                ((lengthPayload[2] & 0xFF) << 8) |
                ((lengthPayload[3] & 0xFF));
        while (inputStream.available() < length) ;
        byte[] keyPayload = new byte[length];
        inputStream.read(keyPayload);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, AuthManager.getInstance().getKeyPair().getPrivate());
        byte[] AESKey = cipher.doFinal(keyPayload);

        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(AESKey, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(AESKey);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        cipherInputStream = new CipherInputStream(inputStream, cipher);
        cipherInputStream.read(lengthPayload);
        length = ((lengthPayload[0] & 0xFF) << 24) |
                ((lengthPayload[1] & 0xFF) << 16) |
                ((lengthPayload[2] & 0xFF) << 8) |
                ((lengthPayload[3] & 0xFF));

        httpsURLConnection.disconnect();

        return new JsonParser().parse(new InputStreamReader(cipherInputStream));
    }
}
