package kr.syeyoung.modapi.v1_8_9.resources;

import kr.syeyoung.modapi.resources.UResource;
import net.minecraft.client.resources.IResource;

import java.io.InputStream;

public class UResourceImpl implements UResource {
    private IResource delegate;

    public UResourceImpl(IResource resource) {
        this.delegate = resource;
    }

    public InputStream getInputStream() {
        return delegate.getInputStream();
    }

    @Override
    public String getResourcePackName() {
        return delegate.getResourcePackName();
    }
}
