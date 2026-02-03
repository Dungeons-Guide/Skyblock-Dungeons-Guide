package kr.syeyoung.modapi.v1_8_9.render;

import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.rendering.UNativeImageBackedTexture;
import kr.syeyoung.modapi.rendering.UTextureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;

public class UTextureManagerImpl implements UTextureManager {
    public static final UTextureManagerImpl INSTANCE = new UTextureManagerImpl();

    private UTextureManagerImpl() {}

    @Override
    public UNativeImageBackedTexture createTexture(String name, int width, int height, boolean clean) {
        DynamicTexture texture = new DynamicTexture(width, height);
        return new UNativeImageBackedTextureImpl(texture);
    }

    @Override
    public ResourceIdentifier registerTexture(String name, UNativeImageBackedTexture texture) {
        ResourceLocation location = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(name, ((UNativeImageBackedTextureImpl) texture).texture);
        return new ResourceIdentifier(location.getResourceDomain(), location.getResourcePath());
    }

    public static class UNativeImageBackedTextureImpl implements UNativeImageBackedTexture {
        private final DynamicTexture texture;
        public UNativeImageBackedTextureImpl(DynamicTexture texture) {
            this.texture = texture;
        }

        @Override
        public void load(BufferedImage image) {
            image.getRGB(0, 0, image.getWidth(), image.getHeight(), texture.getTextureData(), 0, image.getWidth());
        }

        @Override
        public void upload() {
            texture.updateDynamicTexture();
        }
    }
}
