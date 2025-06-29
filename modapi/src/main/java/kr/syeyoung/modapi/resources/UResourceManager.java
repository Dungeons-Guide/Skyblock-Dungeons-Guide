package kr.syeyoung.modapi.resources;

import kr.syeyoung.modapi.data.ResourceIdentifier;

import java.io.IOException;

public interface UResourceManager {
    UResource getResource(ResourceIdentifier location) throws IOException;
}
