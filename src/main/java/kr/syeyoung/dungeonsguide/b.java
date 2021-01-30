package kr.syeyoung.dungeonsguide;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.*;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class b {
    private KeyPair a;
    private String b;

    public String c() {
        return b;
    }

    private KeyPair a()  {
        KeyPairGenerator a = null;
        try {
            a = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException b) { }
        a.initialize(1024);
        this.a = a.generateKeyPair();
        return this.a;
    }

    public b() {
        a();
    }

    private SSLSocketFactory e() throws NoSuchAlgorithmException, KeyManagementException, CertificateException, KeyStoreException, IOException {
        X509Certificate a = (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(("-----BEGIN CERTIFICATE-----\n" +
                        "MIIEZTCCA02gAwIBAgIQQAF1BIMUpMghjISpDBbN3zANBgkqhkiG9w0BAQsFADA/\n" +
                        "MSQwIgYDVQQKExtEaWdpdGFsIFNpZ25hdHVyZSBUcnVzdCBDby4xFzAVBgNVBAMT\n" +
                        "DkRTVCBSb290IENBIFgzMB4XDTIwMTAwNzE5MjE0MFoXDTIxMDkyOTE5MjE0MFow\n" +
                        "MjELMAkGA1UEBhMCVVMxFjAUBgNVBAoTDUxldCdzIEVuY3J5cHQxCzAJBgNVBAMT\n" +
                        "AlIzMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuwIVKMz2oJTTDxLs\n" +
                        "jVWSw/iC8ZmmekKIp10mqrUrucVMsa+Oa/l1yKPXD0eUFFU1V4yeqKI5GfWCPEKp\n" +
                        "Tm71O8Mu243AsFzzWTjn7c9p8FoLG77AlCQlh/o3cbMT5xys4Zvv2+Q7RVJFlqnB\n" +
                        "U840yFLuta7tj95gcOKlVKu2bQ6XpUA0ayvTvGbrZjR8+muLj1cpmfgwF126cm/7\n" +
                        "gcWt0oZYPRfH5wm78Sv3htzB2nFd1EbjzK0lwYi8YGd1ZrPxGPeiXOZT/zqItkel\n" +
                        "/xMY6pgJdz+dU/nPAeX1pnAXFK9jpP+Zs5Od3FOnBv5IhR2haa4ldbsTzFID9e1R\n" +
                        "oYvbFQIDAQABo4IBaDCCAWQwEgYDVR0TAQH/BAgwBgEB/wIBADAOBgNVHQ8BAf8E\n" +
                        "BAMCAYYwSwYIKwYBBQUHAQEEPzA9MDsGCCsGAQUFBzAChi9odHRwOi8vYXBwcy5p\n" +
                        "ZGVudHJ1c3QuY29tL3Jvb3RzL2RzdHJvb3RjYXgzLnA3YzAfBgNVHSMEGDAWgBTE\n" +
                        "p7Gkeyxx+tvhS5B1/8QVYIWJEDBUBgNVHSAETTBLMAgGBmeBDAECATA/BgsrBgEE\n" +
                        "AYLfEwEBATAwMC4GCCsGAQUFBwIBFiJodHRwOi8vY3BzLnJvb3QteDEubGV0c2Vu\n" +
                        "Y3J5cHQub3JnMDwGA1UdHwQ1MDMwMaAvoC2GK2h0dHA6Ly9jcmwuaWRlbnRydXN0\n" +
                        "LmNvbS9EU1RST09UQ0FYM0NSTC5jcmwwHQYDVR0OBBYEFBQusxe3WFbLrlAJQOYf\n" +
                        "r52LFMLGMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjANBgkqhkiG9w0B\n" +
                        "AQsFAAOCAQEA2UzgyfWEiDcx27sT4rP8i2tiEmxYt0l+PAK3qB8oYevO4C5z70kH\n" +
                        "ejWEHx2taPDY/laBL21/WKZuNTYQHHPD5b1tXgHXbnL7KqC401dk5VvCadTQsvd8\n" +
                        "S8MXjohyc9z9/G2948kLjmE6Flh9dDYrVYA9x2O+hEPGOaEOa1eePynBgPayvUfL\n" +
                        "qjBstzLhWVQLGAkXXmNs+5ZnPBxzDJOLxhF2JIbeQAcH5H0tZrUlo5ZYyOqA7s9p\n" +
                        "O5b85o3AM/OJ+CktFBQtfvBhcJVd9wvlwPsk+uyOy2HI7mNxKKgsBTt375teA2Tw\n" +
                        "UdHkhVNcsAKX1H7GNNLOEADksd86wuoXvg==\n" +
                        "-----END CERTIFICATE-----").getBytes()));

        KeyStore b = KeyStore.getInstance(KeyStore.getDefaultType());
        b.load(null, null);
        b.setCertificateEntry(Integer.toString(1), a);

        TrustManagerFactory c = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        c.init(b);

        SSLContext d = SSLContext.getInstance("TLSv1.2");
        d.init(null, c.getTrustManagers(), null);
        return d.getSocketFactory();
    }

    public String b(boolean jars) throws IOException, AuthenticationException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException, CertificateException, KeyStoreException, KeyManagementException {
        Session a = Minecraft.getMinecraft().getSession();
        String b = a.getToken();

        String c = a(a.getProfile());
        MinecraftSessionService yggdrasilMinecraftSessionService = Minecraft.getMinecraft().getSessionService();
        JsonObject d = a(c);
        String hash = a(Base64.decodeBase64(d.get("sharedSecret").getAsString()),
                Base64.decodeBase64(d.get("publicKey").getAsString()));
        yggdrasilMinecraftSessionService.joinServer(a.getProfile(), b, hash);
        this.b = a(c, this.a.getPublic());
        if (jars)
            b(this.b, "https://dungeonsguide.kro.kr/resource/latest");
        b(this.b, "https://dungeonsguide.kro.kr/resource/roomdata");
        return this.b;
    }

    public JsonObject a(String c) {
        String a = c.split("\\.")[1].replace("+", "-").replace("/", "_");
        String b = new String(Base64.decodeBase64(a)); // padding
        return (JsonObject) new JsonParser().parse(b);
    }



    private String a(GameProfile d) throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException, KeyManagementException {
        HttpsURLConnection a = (HttpsURLConnection) new URL("https://dungeonsguide.kro.kr/auth/requestAuth").openConnection();
        a.setSSLSocketFactory(e());
        a.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        a.setRequestProperty("Content-Type", "application/json");
        a.setRequestMethod("POST");
        a.setDoInput(true);
        a.setDoOutput(true);

        a.getOutputStream().write(("{\"uuid\":\""+d.getId().toString()+"\",\"nickname\":\""+d.getName()+"\"}").getBytes());
        InputStreamReader b = new InputStreamReader(a.getInputStream());
        JsonObject c = (JsonObject) new JsonParser().parse(b);
        if (!"ok".equals(c.get("status").getAsString())) {
            return null;
        }
        return c.get("data").getAsString();
    }
    private String a(String a, PublicKey b) throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException, KeyManagementException {
        HttpsURLConnection c = (HttpsURLConnection) new URL("https://dungeonsguide.kro.kr/auth/authenticate").openConnection();
        c.setSSLSocketFactory(e());
        c.setRequestMethod("POST");
        c.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        c.setRequestProperty("Content-Type", "application/json");
        c.setDoInput(true);
        c.setDoOutput(true);

        c.getOutputStream().write(("{\"jwt\":\""+a+"\",\"publicKey\":\""+Base64.encodeBase64URLSafeString(b.getEncoded())+"\"}").getBytes());
        InputStreamReader d = new InputStreamReader(c.getInputStream());
        JsonObject e = (JsonObject) new JsonParser().parse(d);
        if (!"ok".equals(e.get("status").getAsString())) {
            return null;
        }
        return e.get("data").getAsString();
    }

    private HashMap<String, byte[]> c = new HashMap<String, byte[]>();

    public HashMap<String, byte[]> d() {
        return c;
    }

    private void b(String a, String u) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, CertificateException, KeyStoreException, KeyManagementException {
        HttpsURLConnection b = (HttpsURLConnection) new URL(u).openConnection();
        b.setSSLSocketFactory(e());
        b.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        b.setRequestProperty("Content-Type", "application/json");
        b.setRequestMethod("GET");
        b.setRequestProperty("Authorization", a);
        b.setDoInput(true);
        b.setDoOutput(true);

        InputStream c = b.getInputStream();
        byte[] d = new byte[4];
        c.read(d);
        int f = ((d[0] & 0xFF) << 24) |
                ((d[1] & 0xFF) << 16) |
                ((d[2] & 0xFF) << 8) |
                ((d[3] & 0xFF));
        while (c.available() < f) ;
        byte[] e = new byte[f];
        c.read(e);

        Cipher g = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        g.init(Cipher.DECRYPT_MODE, this.a.getPrivate());
        byte[] h = g.doFinal(e);

        g = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec i = new SecretKeySpec(h, "AES");
        IvParameterSpec j = new IvParameterSpec(h);
        g.init(Cipher.DECRYPT_MODE, i, j);
        CipherInputStream k = new CipherInputStream(c, g);

        k.read(d);
        ZipInputStream l = new ZipInputStream(k);
        ZipEntry m;
        while ((m=l.getNextEntry()) != null) {
            byte[] n = new byte[256];
            ByteArrayOutputStream o = new ByteArrayOutputStream();
            int p = 0;
            while((p = l.read(n)) > 0) {
                o.write(n, 0, p);
            }
            this.c.put(m.getName(), o.toByteArray());
        }
        b.disconnect();
    }
    public JsonElement d(String u) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, CertificateException, KeyStoreException, KeyManagementException {
        HttpsURLConnection b = (HttpsURLConnection) new URL(u).openConnection();
        b.setSSLSocketFactory(e());
        b.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        b.setRequestProperty("Content-Type", "application/json");
        b.setRequestMethod("GET");
        b.setRequestProperty("Authorization", this.b);
        b.setDoInput(true);
        b.setDoOutput(true);

        InputStream c = b.getInputStream();
        byte[] d = new byte[4];
        c.read(d);
        int f = ((d[0] & 0xFF) << 24) |
                ((d[1] & 0xFF) << 16) |
                ((d[2] & 0xFF) << 8) |
                ((d[3] & 0xFF));
        while (c.available() < f) ;
        byte[] e = new byte[f];
        c.read(e);

        Cipher g = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        g.init(Cipher.DECRYPT_MODE, this.a.getPrivate());
        byte[] h = g.doFinal(e);

        g = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec i = new SecretKeySpec(h, "AES");
        IvParameterSpec j = new IvParameterSpec(h);
        g.init(Cipher.DECRYPT_MODE, i, j);
        CipherInputStream k = new CipherInputStream(c, g);
        k.read(d);
        f = ((d[0] & 0xFF) << 24) |
                ((d[1] & 0xFF) << 16) |
                ((d[2] & 0xFF) << 8) |
                ((d[3] & 0xFF));
        JsonElement l = new JsonParser().parse(new InputStreamReader(k));
        b.disconnect();
        return l;
    }

    public String a(byte[] a, byte[] b) throws NoSuchAlgorithmException {
        MessageDigest c = MessageDigest.getInstance("SHA-1");
        c.update("".getBytes());
        c.update(a);
        c.update(b);
        byte[] d = c.digest();
        return new BigInteger(d).toString(16);
    }
}
