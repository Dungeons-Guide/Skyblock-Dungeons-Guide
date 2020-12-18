package kr.syeyoung.dungeonsguide.d;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

@AllArgsConstructor
public class b extends URLStreamHandler {
    private kr.syeyoung.dungeonsguide.b a;
    @Override
    protected URLConnection openConnection(URL a) throws IOException {
        return new a(a, this.a);
    }
}
