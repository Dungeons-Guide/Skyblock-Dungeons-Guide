package kr.syeyoung.dungeonsguide;

import com.google.common.io.Files;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class NetworkClassLoader extends ClassLoader {
    Authenticator authenticator;

    public NetworkClassLoader(Authenticator authenticator, ClassLoader parent) {
        super(parent);
        this.authenticator = authenticator;
    }

    @Override
    public Class findClass(String name) throws ClassNotFoundException {
        byte[] b = new byte[0];
        try {
            b = loadClassFromFile(name);
            return defineClass(name, b, 0, b.length);
        } catch (FileNotFoundException ignored) {
        } catch(BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        throw new ClassNotFoundException();
    }

    private byte[] loadClassFromFile(String fileName) throws BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException {
        InputStream inputStream = authenticator.getInputStream(fileName.replace('.', '/')+ ".class");

        byte[] bytes = new byte[4];
        inputStream.read(bytes);
        int length = ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8 ) |
                ((bytes[3] & 0xFF));

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        int totalLen = 0;
        try {
            byte[] buffer = new byte[128];
            int read = 0;
            while ( (read = inputStream.read(buffer)) != -1 ) {
                totalLen += read;
                byteStream.write(buffer, 0, read);
                if (totalLen >= length) break;;
            }
        } catch (Exception ignored) {}
        byte[] byte1 = byteStream.toByteArray();
        byte[] byte2 = new byte[(int) length];
        System.arraycopy(byte1, 0, byte2, 0, byte2.length);
        inputStream.close();
        return byte2;
    }
}
