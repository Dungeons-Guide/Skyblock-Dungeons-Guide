package kr.syeyoung.dungeonsguide.party;


import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;

@Data
public class LoadedImage {
    private String url;
    private BufferedImage image;
    private DynamicTexture previewTexture;
    private ResourceLocation resourceLocation;

    public void buildGLThings() {
        previewTexture = new DynamicTexture(image);
        resourceLocation = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("dgurl/"+url, previewTexture);
    }
}
