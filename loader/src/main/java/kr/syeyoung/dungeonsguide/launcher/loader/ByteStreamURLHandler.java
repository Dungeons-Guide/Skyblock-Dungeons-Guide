package kr.syeyoung.dungeonsguide.launcher.loader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class ByteStreamURLHandler extends URLStreamHandler {
    private InputStreamGenerator converter;
    public ByteStreamURLHandler(InputStreamGenerator converter) {
        this.converter = converter;
    }
    public interface InputStreamGenerator {
        InputStream convert(String name);
    }

    public class ByteStreamURLConnection extends URLConnection {

        /**
         * Constructs a URL connection to the specified URL. A connection to
         * the object referenced by the URL is not created.
         *
         * @param url the specified URL.
         */
        protected ByteStreamURLConnection(URL url) {
            super(url);
            connected = false;
        }

        @Override
        public void connect() throws IOException {
            connected = true;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return converter.convert(url.getPath());
        }
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return new ByteStreamURLConnection(u);
    }
}
