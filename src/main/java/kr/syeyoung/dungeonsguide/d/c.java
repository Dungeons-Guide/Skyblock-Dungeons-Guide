package kr.syeyoung.dungeonsguide.d;

import lombok.AllArgsConstructor;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

@AllArgsConstructor
public class c implements URLStreamHandlerFactory {
    private kr.syeyoung.dungeonsguide.b a;
    @Override
    public URLStreamHandler createURLStreamHandler(String a) {
        if ("z".equals(a)) {
            return new b(this.a);
        }
        return null;
    }
}
