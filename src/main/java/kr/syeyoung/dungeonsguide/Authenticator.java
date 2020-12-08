package kr.syeyoung.dungeonsguide;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
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
        DecodedJWT jwt2 = JWT.decode(jwt);
        String hash = calculateAuthHash(DatatypeConverter.parseBase64Binary(jwt2.getClaim("sharedSecret").asString()),
                DatatypeConverter.parseBase64Binary(jwt2.getClaim("publicKey").asString()));
        yggdrasilMinecraftSessionService.joinServer(session.getProfile(), token, hash);
        token = requestAuth2(jwt, keyPair.getPublic());
        return token;
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
    public InputStream getInputStream(String resource) throws IOException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException {
        HttpURLConnection huc = (HttpURLConnection) new URL(DOMAIN+"resource/resource?class="+ URLEncoder.encode(resource)).openConnection();
        huc.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        huc.setRequestProperty("Content-Type", "application/json");
        huc.setRequestProperty("Authorization", token);
        huc.setDoInput(true);
        huc.setDoOutput(true);

        InputStream inputStream = huc.getInputStream();
        byte[] bytes = new byte[4];
        inputStream.read(bytes);
        int len = ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8 ) |
                ((bytes[3] & 0xFF));

        byte[] pubKey = new byte[len];
        inputStream.read(pubKey);

        Cipher cipher = Cipher.getInstance("RSA");
        byte[] byteEncrypted = pubKey;
        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        byte[] bytePlain = cipher.doFinal(byteEncrypted);

        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(bytePlain, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(bytePlain);
        cipher.init(Cipher.DECRYPT_MODE,keySpec,ivSpec);
        return new CipherInputStream(inputStream, cipher);
    }
}
