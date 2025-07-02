package kr.syeyoung.modapi.v1_21_5.resources;

import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.resources.UResourcePackEntry;
import net.minecraft.resource.ResourcePackProfile;

public class UResourcePackEntryImpl implements UResourcePackEntry {
    private ResourcePackProfile delegate;

    public UResourcePackEntryImpl(ResourcePackProfile delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getResourcePackName() {
        return delegate.getDisplayName().getString();
    }

    public ResourceIdentifier bindAndGetIdentifier() { // TODO: IMPL!!
        return null;
//        delegate.
//        delegate.bindTexturePackIcon(Minecraft.getMinecraft().getTextureManager());
//        ResourceLocation resourceLocation = ReflectionHelper.getPrivateValue(ResourcePackRepository.Entry.class, delegate, "locationTexturePackIcon", "field_5260", "f");
//        return new ResourceIdentifier(resourceLocation.getResourceDomain(), resourceLocation.getResourcePath());
    }
}
