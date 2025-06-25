package kr.syeyoung.modapi.audio;

import kr.syeyoung.modapi.data.ResourceIdentifier;

public interface USoundHandler {
    void playSoundAtPlayer(ResourceIdentifier identifier, float pitch);
}
