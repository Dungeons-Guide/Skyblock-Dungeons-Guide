package kr.syeyoung.dungeonsguide;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class NetworkClassLoader extends ClassLoader {
    Authenticator authenticator;

    public NetworkClassLoader(Authenticator authenticator) {
        super();
        this.authenticator = authenticator;
    }

    @Override
    public Class findClass(String name) throws ClassNotFoundException {
        byte[] b = new byte[0];
        try {
            b = loadClassFromFile(name);
            return defineClass(name, b, 0, b.length);
        } catch (BadPaddingException e) {
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
        byte[] buffer;
        InputStream inputStream = authenticator.getInputStream(fileName.replace('.', '/')+ ".class");
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        int nextValue = 0;
        try {
            while ( (nextValue = inputStream.read()) != -1 ) {
                byteStream.write(nextValue);
            }
        } catch (Exception e) {}
        buffer = byteStream.toByteArray();
        return buffer;
    }
}
