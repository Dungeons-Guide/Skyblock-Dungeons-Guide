package kr.syeyoung.dungeonsguide.customurl;

import kr.syeyoung.dungeonsguide.Authenticator;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class DGURLStreamHandlerFactory implements URLStreamHandlerFactory {
    private Authenticator authenticator;
    public DGURLStreamHandlerFactory(Authenticator authenticator) {
        this.authenticator = authenticator;
    }
    @Override
    public URLStreamHandler createURLStreamHandler(String s) {
        if ("dungeonsguide".equals(s)) {
            return new DGURLStreamHandler(authenticator);
        }

        return null;
    }
}
