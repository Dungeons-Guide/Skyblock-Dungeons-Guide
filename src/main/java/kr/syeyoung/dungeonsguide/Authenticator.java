package kr.syeyoung.dungeonsguide;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.*;
import java.security.*;
import java.util.UUID;

public class Authenticator {
    @Getter
    private KeyPair keyPair;
    @Getter
    private String token;
    private KeyPair generate1024RSAKey()  {
        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        generator.initialize(1024);
        keyPair = generator.generateKeyPair();
        return  keyPair;
    }

    public Authenticator() {
        generate1024RSAKey();
    }

    private static final String DOMAIN = "http://localhost:8080/";

    public String authenticate() throws IOException, AuthenticationException, NoSuchAlgorithmException {
        Session session = Minecraft.getMinecraft().getSession();
        String token = session.getToken();

        String jwt = requestAuth(session.getProfile());
        MinecraftSessionService yggdrasilMinecraftSessionService = Minecraft.getMinecraft().getSessionService();
        JsonObject jwt2 = parseJWT(jwt);
        String hash = calculateAuthHash(DatatypeConverter.parseBase64Binary(jwt2.get("sharedSecret").getAsString()),
                DatatypeConverter.parseBase64Binary(jwt2.get("publicKey").getAsString()));
        yggdrasilMinecraftSessionService.joinServer(session.getProfile(), token, hash);
        this.token = requestAuth2(jwt, keyPair.getPublic());
        return this.token;
    }

    public JsonObject parseJWT(String jwt) {
        String payload = jwt.split("\\.")[1].replace("+", "-").replace("/", "_");
        String json = new String(DatatypeConverter.parseBase64Binary(payload));
        return (JsonObject) new JsonParser().parse(json);
    }

    private String requestAuth(GameProfile profile) throws IOException {
        HttpURLConnection huc = (HttpURLConnection) new URL(DOMAIN+"auth/requestAuth").openConnection();
        huc.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        huc.setRequestProperty("Content-Type", "application/json");
        huc.setRequestMethod("POST");
        huc.setDoInput(true);
        huc.setDoOutput(true);

        huc.getOutputStream().write(("{\"uuid\":\""+profile.getId().toString()+"\",\"nickname\":\""+profile.getName()+"\"}").getBytes());
        InputStreamReader inputStreamReader = new InputStreamReader(huc.getInputStream());
        JsonObject object = (JsonObject) new JsonParser().parse(inputStreamReader);
        if (!"ok".equals(object.get("status").getAsString())) {
            return null;
        }
        return object.get("data").getAsString();
    }
    private String requestAuth2(String token, PublicKey publicKey) throws IOException {
        HttpURLConnection huc = (HttpURLConnection) new URL(DOMAIN+"auth/authenticate").openConnection();
        huc.setRequestMethod("POST");
        huc.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        huc.setRequestProperty("Content-Type", "application/json");
        huc.setDoInput(true);
        huc.setDoOutput(true);

        huc.getOutputStream().write(("{\"jwt\":\""+token+"\",\"publicKey\":\""+DatatypeConverter.printBase64Binary(publicKey.getEncoded())+"\"}").getBytes());
        InputStreamReader inputStreamReader = new InputStreamReader(huc.getInputStream());
        JsonObject object = (JsonObject) new JsonParser().parse(inputStreamReader);
        if (!"ok".equals(object.get("status").getAsString())) {
            return null;
        }
        return object.get("data").getAsString();
    }
    public String calculateAuthHash(byte[] sharedSecret, byte[] pk) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update("".getBytes());
        md.update(sharedSecret);
        md.update(pk);
        byte[] result = md.digest();
        return new BigInteger(result).toString(16);
    }
}
