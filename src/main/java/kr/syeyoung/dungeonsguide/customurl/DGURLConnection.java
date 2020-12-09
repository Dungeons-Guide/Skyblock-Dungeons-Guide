package kr.syeyoung.dungeonsguide.customurl;

import kr.syeyoung.dungeonsguide.Authenticator;
import kr.syeyoung.dungeonsguide.DungeonsGuideMain;
import lombok.SneakyThrows;
import net.minecraft.launchwrapper.LaunchClassLoader;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class DGURLConnection extends URLConnection {
    private static final String DOMAIN = "http://localhost:8080/";

    private Authenticator authenticator;
    protected DGURLConnection(URL url, Authenticator authenticator) {
        super(url);
        connected = false;
        this.authenticator = authenticator;
    }

    @Override
    public void setUseCaches(boolean b) {
        checkExist = !b;
    }
    private boolean checkExist = false;
    private boolean exists= false;

    @Override
    public void connect() throws IOException {
        try {
            if (!(url.getPath().contains("kr/syeyoung") || url.getPath().contains("roomdata"))) return;
            this.connected = true;

            boolean classLoader = false;
            for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
                if (ste.getClassName().equals("net.minecraft.launchwrapper.LaunchClassLoader")) {
                    classLoader = true;
                }
            }


            System.out.println("loading " + url.getPath().substring(1) + " called from classloader " + classLoader);

            HttpURLConnection huc = (HttpURLConnection) new URL(DOMAIN + "resource/resource?class=" + URLEncoder.encode(url.getPath().substring(1))).openConnection();
            huc.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
            huc.setRequestProperty("Content-Type", "application/json");
            huc.setRequestMethod(checkExist ? "HEAD" : "GET");
            huc.setRequestProperty("Authorization", (url.getUserInfo() == null && classLoader) ? authenticator.getToken() : url.getUserInfo());
            huc.setDoInput(true);
            huc.setDoOutput(true);
            System.out.println("Resp Code::" + huc.getResponseCode() + "/ " + checkExist);

            exists = huc.getResponseCode() != 404;
            if (checkExist) {
                return;
            }

            InputStream inputStream = huc.getInputStream();
            byte[] bytes = new byte[4];
            inputStream.read(bytes);
            int len = ((bytes[0] & 0xFF) << 24) |
                    ((bytes[1] & 0xFF) << 16) |
                    ((bytes[2] & 0xFF) << 8) |
                    ((bytes[3] & 0xFF));
            while (inputStream.available() < len) ;
            byte[] pubKey = new byte[len];
            inputStream.read(pubKey);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            byte[] byteEncrypted = pubKey;
            cipher.init(Cipher.DECRYPT_MODE, authenticator.getKeyPair().getPrivate());
            byte[] bytePlain = cipher.doFinal(byteEncrypted);

            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(bytePlain, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(bytePlain);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);

            cipherInputStream.read(bytes);
            int length = ((bytes[0] & 0xFF) << 24) |
                    ((bytes[1] & 0xFF) << 16) |
                    ((bytes[2] & 0xFF) << 8) |
                    ((bytes[3] & 0xFF));
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            int totalLen = 0;
            try {
                byte[] buffer = new byte[128];
                int read = 0;
                while ((read = cipherInputStream.read(buffer)) != -1) {
                    totalLen += read;
                    byteStream.write(buffer, 0, read);
                    if (totalLen >= length) break;
                }
            } catch (Exception ignored) {
            }
            byte[] byte1 = byteStream.toByteArray();
            byte[] byte2 = new byte[(int) length];
            System.arraycopy(byte1, 0, byte2, 0, byte2.length);
            cipherInputStream.close();
            inputStream.close();
            this.inputStream = new ByteArrayInputStream(byte2);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
    private InputStream inputStream;

    @Override
    public InputStream getInputStream() throws IOException {
        if (!connected) connect();
        if (checkExist && !exists) throw new FileNotFoundException();
        return inputStream;
    }
}
