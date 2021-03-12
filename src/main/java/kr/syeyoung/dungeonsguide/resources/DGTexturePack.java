package kr.syeyoung.dungeonsguide.resources;

import kr.syeyoung.dungeonsguide.a;
import kr.syeyoung.dungeonsguide.b;
import kr.syeyoung.dungeonsguide.e;
import lombok.AllArgsConstructor;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

@AllArgsConstructor
public class DGTexturePack implements IResourcePack {

    private b authenticator;

    @Override
    public InputStream getInputStream(ResourceLocation location) throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("/assets/dg/"+location.getResourcePath());
        if (inputStream != null) return inputStream;
        return new ByteArrayInputStream(authenticator.d().get("assets/dg/"+location.getResourcePath()));
    }

    @Override
    public boolean resourceExists(ResourceLocation location) {
        return authenticator.d().containsKey("assets/dg/"+location.getResourcePath())
        || this.getClass().getResourceAsStream("/assets/dg/"+location.getResourcePath()) != null;
    }

    @Override
    public Set<String> getResourceDomains() {
        return Collections.singleton("dungeonsguide");
    }

    @Override
    public <T extends IMetadataSection> T getPackMetadata(IMetadataSerializer p_135058_1_, String p_135058_2_) throws IOException {
        return null;
    }

    @Override
    public BufferedImage getPackImage() throws IOException {
        return new BufferedImage(512,512, BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public String getPackName() {
        return "Dungeons Guide Default Pack";
    }
}
