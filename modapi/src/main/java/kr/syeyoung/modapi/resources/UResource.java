package kr.syeyoung.modapi.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface UResource {
    InputStream getInputStream() throws IOException;
    String getResourcePackName();

    Map<String, Object> getMetadata(String animation);
}
