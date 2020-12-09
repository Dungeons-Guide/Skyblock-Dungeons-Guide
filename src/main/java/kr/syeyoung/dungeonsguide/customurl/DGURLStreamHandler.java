package kr.syeyoung.dungeonsguide.customurl;

import kr.syeyoung.dungeonsguide.Authenticator;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class DGURLStreamHandler extends URLStreamHandler {
    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return new DGURLConnection(url);
    }
}
