package kr.syeyoung.modapi.v1_8_9.resources;

import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.resources.UResourcePackEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class UResourcePackEntryImpl implements UResourcePackEntry {
    private ResourcePackRepository.Entry delegate;

    public UResourcePackEntryImpl(ResourcePackRepository.Entry delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getResourcePackName() {
        return delegate.getResourcePackName();
    }

    public ResourceIdentifier bindAndGetIdentifier() {
        delegate.bindTexturePackIcon(Minecraft.getMinecraft().getTextureManager());
        ResourceLocation resourceLocation = ReflectionHelper.getPrivateValue(ResourcePackRepository.Entry.class, delegate, "locationTexturePackIcon", "field_5260", "f");
        return new ResourceIdentifier(resourceLocation.getResourceDomain(), resourceLocation.getResourcePath());
    }
}
