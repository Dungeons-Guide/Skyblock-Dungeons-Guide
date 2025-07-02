package kr.syeyoung.modapi.resources;

import java.io.IOException;
import java.io.InputStream;

public interface UResource {
    InputStream getInputStream() throws IOException;
    String getResourcePackName();
}
