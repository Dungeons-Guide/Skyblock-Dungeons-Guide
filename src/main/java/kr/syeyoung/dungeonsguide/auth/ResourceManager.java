package kr.syeyoung.dungeonsguide.auth;

import lombok.Setter;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ResourceManager {

    @Setter
    private String baseUrl;
    @Setter
    private String BASE64_X509ENCODEDKEYSPEC;
    private final HashMap<String, byte[]> loadedResources = new HashMap<>();


    private static ResourceManager instance;
    public static ResourceManager getInstance() {
        if(instance == null) {
            instance = new ResourceManager();
            MinecraftForge.EVENT_BUS.register(instance);
        }
        return instance;
    }

    private ResourceManager() {
    }

    public Map<String, byte[]> getResources() {
        return loadedResources;
    }


    public void downloadAssets(String version){
        try {
            // version not being null indicates that the user is "premium"
            // so we download the special version
            if (version != null)
                downloadSafe( baseUrl + "/resource/version?v=" + version, true);
            downloadSafe(baseUrl + "/resource/roomdata", false);
        } catch (Exception t) {
            t.printStackTrace();
        }

    }

    private void downloadSafe(String url, boolean isValidateSignature) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, SignatureException, InvalidKeySpecException {
        HttpsURLConnection dgConnection = (HttpsURLConnection) new URL(url).openConnection();
        dgConnection.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        dgConnection.setRequestProperty("Content-Type", "application/json");
        dgConnection.setRequestMethod("GET");
        dgConnection.setRequestProperty("Authorization", AuthManager.getInstance().getToken());
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
        cipher.init(Cipher.DECRYPT_MODE, AuthManager.getInstance().getKeyPair().getPrivate());
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

        byte[] signed;
        if (isValidateSignature) {
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
            sign.initVerify(getPublicKey(BASE64_X509ENCODEDKEYSPEC));
            sign.update(body);
            boolean truth = sign.verify(signed);
            if (!truth) throw new SignatureException("DG SIGNATURE FORGED");
        }

        ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(body));
        ZipEntry zipEntry;
        while ((zipEntry=zipInputStream.getNextEntry()) != null) {
            byte[] buffer = new byte[256];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int p;
            while((p = zipInputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, p);
            }
            this.loadedResources.put(zipEntry.getName(), byteArrayOutputStream.toByteArray());
        }
    }


    public static PublicKey getPublicKey(String base64X509EncodedKeySpec) throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.decodeBase64(base64X509EncodedKeySpec));

        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

}
