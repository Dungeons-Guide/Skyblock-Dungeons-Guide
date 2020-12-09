package kr.syeyoung.dungeonsguide.customurl;

import kr.syeyoung.dungeonsguide.Authenticator;
import lombok.AllArgsConstructor;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

@AllArgsConstructor
public class DGURLStreamHandlerFactory implements URLStreamHandlerFactory {
    private Authenticator authenticator;
    @Override
    public URLStreamHandler createURLStreamHandler(String s) {
        if ("dungeonsguide".equals(s)) {
            return new DGURLStreamHandler(authenticator);
        }

        return null;
    }
}
