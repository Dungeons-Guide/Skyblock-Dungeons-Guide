package kr.syeyoung.modapi.v1_21_11.render;

import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.rendering.UNativeImageBackedTexture;
import kr.syeyoung.modapi.rendering.UTextureManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class UTextureManagerImpl implements UTextureManager {
    public static final UTextureManagerImpl INSTANCE = new UTextureManagerImpl();

    private UTextureManagerImpl() {}

    @Override
    public UNativeImageBackedTexture createTexture(String name, int width, int height, boolean clean) {
        NativeImageBackedTexture texture = new NativeImageBackedTexture(name, width, height, clean);
        return new UNativeImageBackedTextureImpl(texture);
    }

    @Override
    public ResourceIdentifier registerTexture(String name, UNativeImageBackedTexture texture) {
        MinecraftClient.getInstance().getTextureManager().registerTexture(
                Identifier.of("dungeonsguide","dyn/"+ name), ((UNativeImageBackedTextureImpl)texture).texture
        );
        return new ResourceIdentifier("dungeonsguide","dyn/"+  name);
    }

    public static class UNativeImageBackedTextureImpl implements UNativeImageBackedTexture {
        private NativeImageBackedTexture texture;
        public UNativeImageBackedTextureImpl(NativeImageBackedTexture texture) {
            this.texture = texture;
        }

        @Override
        public void load(BufferedImage image) {
//            texture.getImage().
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ImageIO.write(image, "PNG", baos);
                texture.setImage(NativeImage.read(baos.toByteArray()));
            } catch (IOException e) { throw new RuntimeException(e); }
        }

        @Override
        public void upload() {
            texture.upload();
        }
    }
}
