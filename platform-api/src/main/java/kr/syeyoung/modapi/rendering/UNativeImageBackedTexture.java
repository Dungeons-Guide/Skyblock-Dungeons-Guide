package kr.syeyoung.modapi.rendering;

import java.awt.image.BufferedImage;

public interface UNativeImageBackedTexture {
    public void load(BufferedImage image);
    public void upload();
}
