package kr.syeyoung.dungeonsguide.launcher.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.auth.token.AuthToken;
import kr.syeyoung.dungeonsguide.launcher.auth.token.DGAuthToken;
import kr.syeyoung.dungeonsguide.launcher.auth.token.PrivacyPolicyRequiredToken;
import kr.syeyoung.dungeonsguide.launcher.exceptions.AuthServerException;
import kr.syeyoung.dungeonsguide.launcher.exceptions.PrivacyPolicyRequiredException;
import kr.syeyoung.dungeonsguide.launcher.exceptions.ResponseParsingException;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.stream.Collectors;

public class DgAuthUtil {
    private static final SecureRandom random = new SecureRandom();
    private DgAuthUtil(){}


    private static <T> DGResponse<T> getResponse(HttpsURLConnection connection, Class<T> data) throws IOException {
        connection.getResponseCode();
        InputStream toRead = connection.getErrorStream();
        if (toRead == null)
            toRead = connection.getInputStream();
        String payload = IOUtils.readLines(toRead).stream().collect(Collectors.joining("\n"));

        try {
            JSONObject json = new JSONObject(payload);
            return new DGResponse(
                    json.getString("status"),
                    (T) json.get("data"),
                    json.getString("errorMessage"),
                    json.getString("qrCode")
            );
        }   catch (Exception e) {
            throw new ResponseParsingException(payload, e.getMessage());
        } finally {
            toRead.close();
        }

    }

    public static String requestAuth() throws IOException {
        GameProfile profile = Minecraft.getMinecraft().getSession().getProfile();

        HttpsURLConnection connection = (HttpsURLConnection) new URL(Main.DOMAIN + "/auth/requestAuth").openConnection();
        connection.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        connection.getOutputStream().write(("{\"uuid\":\""+profile.getId().toString()+"\",\"nickname\":\""+profile.getName()+"\"}").getBytes());

        DGResponse<String> preToken = getResponse(connection, String.class);
        if (!"SUCCESS".equals(preToken.getStatus())) {
            throw new AuthServerException(preToken);
        }

        return preToken.getData();
    }

    public static byte[] checkSessionAuthenticityAndReturnEncryptedSecret(String tempToken) throws NoSuchAlgorithmException, AuthenticationException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        JSONObject d = getJwtPayload(tempToken);
        byte[] sharedSecret = new byte[16];
        random.nextBytes(sharedSecret);
        byte[] publicKey =Base64.decodeBase64(d.getString("publicKey"));

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, AuthUtil.getPublicKey(publicKey));
        byte[] result = cipher.doFinal(sharedSecret);


        String hash = calculateServerHash(sharedSecret, publicKey);

        Session session = Minecraft.getMinecraft().getSession();
        MinecraftSessionService yggdrasilMinecraftSessionService = Minecraft.getMinecraft().getSessionService();
        yggdrasilMinecraftSessionService.joinServer(session.getProfile(), session.getToken(), hash);

        return result;
    }

    public static AuthToken verifyAuth(String tempToken, byte[] encSecret) throws IOException {
        HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(Main.DOMAIN + "/auth/authenticate").openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);

        urlConnection.getOutputStream().write(("{\"jwt\":\""+tempToken+"\",\"sharedSecret\":\""+Base64.encodeBase64URLSafeString(encSecret)+"\"}").getBytes());

        DGResponse<JSONObject> postToken = getResponse(urlConnection, JSONObject.class);
        if (!"SUCCESS".equals(postToken.getStatus())) {
            throw new AuthServerException(postToken);
        }
        JSONObject data = postToken.getData();
        if (data.getString("result").equals("TOS_PRIVACY_POLICY_ACCEPT_REQUIRED")) {
            return new PrivacyPolicyRequiredToken(data.getString("jwt"));
        } else if (data.getString("result").equals("SUCCESSFUL")) {
            return new DGAuthToken(data.getString("jwt"));
        } else {
            throw new AuthServerException(postToken);
        }
    }

    public static AuthToken acceptNewPrivacyPolicy(String tempToken) throws IOException {
        HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(Main.DOMAIN + "/auth/acceptPrivacyPolicy").openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);

        urlConnection.getOutputStream().write(tempToken.getBytes());

        DGResponse<JSONObject> postToken = getResponse(urlConnection, JSONObject.class);
        if (!"SUCCESS".equals(postToken.getStatus())) {
            throw new AuthServerException(postToken);
        }
        JSONObject data = postToken.getData();
        if (data.getString("result").equals("TOS_PRIVACY_POLICY_ACCEPT_REQUIRED")) {
            return new PrivacyPolicyRequiredToken(data.getString("jwt"));
        } else if (data.getString("result").equals("SUCCESSFUL")) {
            return new DGAuthToken(data.getString("jwt"));
        } else {
            throw new AuthServerException(postToken);
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
