/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraftforge.fml.common.ProgressManager;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.*;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class b {
    private KeyPair a;
    private String b;
    private final ProgressManager.ProgressBar p;

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

    private PublicKey d;
    private PublicKey e() throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (d != null) return d;
        X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.decodeBase64("MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAxO89qtwG67jNucQ9Y44c" +
                "IUs/B+5BeJPs7G+RG2gfs4/2+tzF/c1FLDc33M7yKw8aKk99vsBUY9Oo8gxxiEPB" +
                "JitP/qfon2THp94oM77ZTpHlmFoqbZMcKGZVI8yfvEL4laTM8Hw+qh5poQwtpEbK" +
                "Xo47AkxygxJasUnykER2+aSTZ6kWU2D4xiNtFA6lzqN+/oA+NaYfPS0amAvyVlHR" +
                "n/8IuGkxb5RrlqVssQstFnxsJuv88qdGSEqlcKq2tLeg9hb8eCnl2OFzvXmgbVER" +
                "0JaV+4Z02fVG1IlR3Xo1mSit7yIU6++3usRCjx2yfXpnGGJUW5pe6YETjNew3ax+" +
                "FAZ4GePWCdmS7FvBnbbABKo5pE06ZTfDUTCjQlAJQiUgoF6ntMJvQAXPu48Vr8q/" +
                "mTcuZWVnI6CDgyE7nNq3WNoq3397sBzxRohMxuqzl3T19zkfPKF05iV2Ju1HQMW5" +
                "I119bYrmVD240aGESZc20Sx/9g1BFpNzQbM5PGUlWJ0dhLjl2ge4ip2hHciY3OEY" +
                "p2Qy2k+xEdenpKdL+WMRimCQoO9gWe2Tp4NmP5dppDXZgPjXqjZpnGs0Uxs+fXqW" +
                "cwlg3MbX3rFl9so/fhVf4p9oXZK3ve7z5D6XSSDRYECvsKIa08WAxJ/U6n204E/4" +
                "xUF+3ZgFPdzZGn2PU7SsnOsCAwEAAQ=="));
        return d = KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    public b(ProgressManager.ProgressBar p) {
        this.p = p;
        p.step("Generating KeyPair");
        a();
    }

    public String b(String e) throws IOException, AuthenticationException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException, CertificateException, KeyStoreException, KeyManagementException, InvalidKeySpecException, SignatureException {
        Session a = Minecraft.getMinecraft().getSession();
        String b = a.getToken();

        p.step("Authenticating (1/2)");
        String c = a(a.getProfile());
        MinecraftSessionService yggdrasilMinecraftSessionService = Minecraft.getMinecraft().getSessionService();
        JsonObject d = a(c);
        String hash = a(Base64.decodeBase64(d.get("sharedSecret").getAsString()),
                Base64.decodeBase64(d.get("publicKey").getAsString()));
        yggdrasilMinecraftSessionService.joinServer(a.getProfile(), b, hash);
        p.step("Authenticating (2/2)");
        this.b = a(c, this.a.getPublic());
        try {
            p.step("Downloading Jar");
            if (e != null)
                b(this.b, "https://dungeons.guide/resource/version?v=" + e, true);
            p.step("Downloading Rooms");
            b(this.b, "https://dungeons.guide/resource/roomdata", false);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return this.b;
    }

    public JsonObject a(String c) {
        String a = c.split("\\.")[1].replace("+", "-").replace("/", "_");
        String b = new String(Base64.decodeBase64(a)); // padding
        return (JsonObject) new JsonParser().parse(b);
    }



    private String a(GameProfile d) throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException, KeyManagementException {
        HttpsURLConnection a = (HttpsURLConnection) new URL("https://dungeons.guide/auth/requestAuth").openConnection();
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
        HttpsURLConnection c = (HttpsURLConnection) new URL("https://dungeons.guide/auth/authenticate").openConnection();
        c.setRequestMethod("POST");
        c.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        c.setRequestProperty("Content-Type", "application/json");
        c.setDoInput(true);
        c.setDoOutput(true);

        c.getOutputStream().write(("{\"jwt\":\""+a+"\",\"publicKey\":\""+Base64.encodeBase64URLSafeString(b.getEncoded())+"\"}").getBytes());
        c.getResponseCode();
        InputStreamReader d = new InputStreamReader(c.getInputStream());
        JsonObject e = (JsonObject) new JsonParser().parse(d);
        if (!"ok".equals(e.get("status").getAsString())) {
            return null;
        }
        return e.get("data").getAsString();
    }

    private final HashMap<String, byte[]> c = new HashMap<String, byte[]>();

    public HashMap<String, byte[]> d() {
        return c;
    }

    private void b(String a, String u, boolean v) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, CertificateException, KeyStoreException, KeyManagementException, SignatureException, InvalidKeySpecException {
        HttpsURLConnection b = (HttpsURLConnection) new URL(u).openConnection();
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
        f = ((d[0] & 0xFF) << 24) |
                ((d[1] & 0xFF) << 16) |
                ((d[2] & 0xFF) << 8) |
                ((d[3] & 0xFF));

        int totalLen = f;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buff = new byte[256];
        while (totalLen > 0) {
            int len = k.read(buff, 0, Math.min(buff.length,  totalLen));
            totalLen -= len;
            bos.write(buff, 0, len);
        }
        byte[] payload = bos.toByteArray();

        byte[] signed = null;
        if (v) {
            p.step("Validating Signature");
            k.read(d,0 , 4);
            f = ((d[0] & 0xFF) << 24) |
                    ((d[1] & 0xFF) << 16) |
                    ((d[2] & 0xFF) << 8) |
                    ((d[3] & 0xFF));

            totalLen = f;
            bos = new ByteArrayOutputStream();
            while (totalLen > 0) {
                int len = k.read(buff, 0, Math.min(buff.length,  totalLen));
                totalLen -= len;
                bos.write(buff, 0, len);
            }
            signed = bos.toByteArray();

            Signature sign = Signature.getInstance("SHA512withRSA");
            sign.initVerify(e());
            sign.update(payload);
            boolean truth = sign.verify(signed);
            if (!truth) throw new SignatureException("DG SIGNATURE FORGED");
        }

        ZipInputStream l = new ZipInputStream(new ByteArrayInputStream(payload));
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
