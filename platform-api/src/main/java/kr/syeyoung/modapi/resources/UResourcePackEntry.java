package kr.syeyoung.modapi.resources;

import kr.syeyoung.modapi.data.ResourceIdentifier;

public interface UResourcePackEntry {
    String getResourcePackName();

    ResourceIdentifier bindAndGetIdentifier();
}
