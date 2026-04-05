package kr.syeyoung.modapi.rendering;

import kr.syeyoung.modapi.data.ResourceIdentifier;

public interface UTextureManager {
    UNativeImageBackedTexture createTexture(String name, int width, int height, boolean clean);

    ResourceIdentifier registerTexture(String name, UNativeImageBackedTexture texture);
}
