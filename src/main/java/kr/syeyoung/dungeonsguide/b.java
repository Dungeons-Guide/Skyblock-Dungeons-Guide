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
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.*;
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

    private static final String DOMAIN = "http://localhost:8080/";

    public String b() throws IOException, AuthenticationException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException {
        Session a = Minecraft.getMinecraft().getSession();
        String b = a.getToken();

        String c = a(a.getProfile());
        MinecraftSessionService yggdrasilMinecraftSessionService = Minecraft.getMinecraft().getSessionService();
        JsonObject d = a(c);
        String hash = a(DatatypeConverter.parseBase64Binary(d.get("sharedSecret").getAsString()),
                DatatypeConverter.parseBase64Binary(d.get("publicKey").getAsString()));
        yggdrasilMinecraftSessionService.joinServer(a.getProfile(), b, hash);
        this.b = a(c, this.a.getPublic());
        b(this.b);
        return this.b;
    }

    public JsonObject a(String c) {
        String a = c.split("\\.")[1].replace("+", "-").replace("/", "_");
        String b = new String(DatatypeConverter.parseBase64Binary(a));
        return (JsonObject) new JsonParser().parse(b);
    }

    private String a(GameProfile d) throws IOException {
        HttpURLConnection a = (HttpURLConnection) new URL(DOMAIN+"auth/requestAuth").openConnection();
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
    private String a(String a, PublicKey b) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(DOMAIN+"auth/authenticate").openConnection();
        c.setRequestMethod("POST");
        c.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        c.setRequestProperty("Content-Type", "application/json");
        c.setDoInput(true);
        c.setDoOutput(true);

        c.getOutputStream().write(("{\"jwt\":\""+a+"\",\"publicKey\":\""+DatatypeConverter.printBase64Binary(b.getEncoded())+"\"}").getBytes());
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

    private void b(String a) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        HttpURLConnection b = (HttpURLConnection) new URL(DOMAIN + "resource/jar").openConnection();
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

    public String a(byte[] a, byte[] b) throws NoSuchAlgorithmException {
        MessageDigest c = MessageDigest.getInstance("SHA-1");
        c.update("".getBytes());
        c.update(a);
        c.update(b);
        byte[] d = c.digest();
        return new BigInteger(d).toString(16);
    }
}
