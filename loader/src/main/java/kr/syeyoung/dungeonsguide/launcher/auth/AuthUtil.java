package kr.syeyoung.dungeonsguide.launcher.auth;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.*;

public class AuthUtil {
    private AuthUtil() {}

    public static KeyPair getKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator a = null;
        a = KeyPairGenerator.getInstance("RSA");
        a.initialize(1024);
        return a.generateKeyPair();
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
