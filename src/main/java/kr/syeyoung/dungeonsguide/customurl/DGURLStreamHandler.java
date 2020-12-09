package kr.syeyoung.dungeonsguide.customurl;

import kr.syeyoung.dungeonsguide.Authenticator;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

@AllArgsConstructor
public class DGURLStreamHandler extends URLStreamHandler {
    private Authenticator authenticator;
    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return new DGURLConnection(url, authenticator);
    }
}
