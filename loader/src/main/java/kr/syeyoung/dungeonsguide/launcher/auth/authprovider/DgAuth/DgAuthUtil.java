package kr.syeyoung.dungeonsguide.launcher.auth.authprovider.DgAuth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class DgAuthUtil {
    private DgAuthUtil(){}

    public static String requestAuth(String baseurl) throws IOException {
        GameProfile profile = Minecraft.getMinecraft().getSession().getProfile();

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

    public static void checkSessionAuthenticity(String tempToken) throws NoSuchAlgorithmException, AuthenticationException {
        JsonObject d = getJwtPayload(tempToken);
        byte[] sharedSecret = Base64.decodeBase64(d.get("sharedSecret").getAsString());
        byte[] publicKey =Base64.decodeBase64(d.get("publicKey").getAsString());
        String hash = calculateServerHash(sharedSecret, publicKey);

        Session session = Minecraft.getMinecraft().getSession();
        MinecraftSessionService yggdrasilMinecraftSessionService = Minecraft.getMinecraft().getSessionService();
        yggdrasilMinecraftSessionService.joinServer(session.getProfile(), session.getToken(), hash);
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
}
