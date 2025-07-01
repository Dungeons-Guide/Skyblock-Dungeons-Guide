package kr.syeyoung.modapi.resources;

import kr.syeyoung.modapi.data.ResourceIdentifier;

import java.io.IOException;
import java.util.List;

public interface UResourceManager {
    UResource getResource(ResourceIdentifier location) throws IOException;
    List<UResource> getAllResources(ResourceIdentifier location) throws IOException;
}
