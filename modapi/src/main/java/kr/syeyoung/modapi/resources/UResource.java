package kr.syeyoung.modapi.resources;

import java.io.InputStream;

public interface UResource {
    InputStream getInputStream();
    String getResourcePackName();
}
