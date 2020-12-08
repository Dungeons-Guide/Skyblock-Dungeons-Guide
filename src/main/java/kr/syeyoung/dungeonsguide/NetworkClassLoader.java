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
        InputStream inputStream = authenticator.getInputStream(fileName.replace('.', '/')+ ".class");
        long length = 0;
        {
            for (int i = 4; i >= 0; i--) {
                length |= (inputStream.read() & 0xFF) << i * 8;
            }
        }

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        int nextValue = 0;
        try {
            byte[] buffer = new byte[1024];
            while ( (inputStream.read(buffer)) != -1 ) {
                byteStream.write(buffer);
            }
        } catch (Exception e) {}
        byte[] byte1 = byteStream.toByteArray();
        byte[] byte2 = new byte[(int) length];
        System.arraycopy(byte1, 0, byte2, 0, byte2.length);

        return byte2;
    }
}
