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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DGURLConnection extends URLConnection {
    private Authenticator authenticator;
    protected DGURLConnection(URL url, Authenticator authenticator) {
        super(url);
        connected = false;
        this.authenticator = authenticator;
    }

    @Override
    public void connect() throws IOException {
    }
    @Override
    public InputStream getInputStream() throws IOException {
        String path  = url.getPath().substring(1);
        if (!authenticator.getDynamicResources().containsKey(path)) throw new FileNotFoundException();
        return new ByteArrayInputStream(authenticator.getDynamicResources().get(path));
    }
}
