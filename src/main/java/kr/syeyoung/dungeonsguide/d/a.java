package kr.syeyoung.dungeonsguide.d;

import kr.syeyoung.dungeonsguide.b;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class a extends URLConnection {
    private final b a;
    protected a(URL b, b a) {
        super(b);
        connected = false;
        this.a = a;
    }

    @Override
    public void connect() throws IOException {
    }
    @Override
    public InputStream getInputStream() throws IOException {
        if (a != null) {
            String path = url.getPath().substring(1);
            if (!a.d().containsKey(path)) throw new FileNotFoundException();
            return new ByteArrayInputStream(a.d().get(path));
        } else if (url.getPath().contains("roomdata")){
            return a.class.getResourceAsStream(url.getPath());
        }
        throw new FileNotFoundException();
    }
}
