package kr.syeyoung.dungeonsguide.customurl;

import kr.syeyoung.dungeonsguide.Authenticator;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class DGURLStreamHandlerFactory implements URLStreamHandlerFactory {
    @Override
    public URLStreamHandler createURLStreamHandler(String s) {
        if ("dungeonsguide".equals(s)) {
            return new DGURLStreamHandler();
        }

        return null;
    }
}
