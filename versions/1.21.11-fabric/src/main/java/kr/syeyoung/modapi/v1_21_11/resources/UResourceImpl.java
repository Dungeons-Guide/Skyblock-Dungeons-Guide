package kr.syeyoung.modapi.v1_21_11.resources;

import kr.syeyoung.modapi.resources.UResource;
import net.minecraft.resource.Resource;

import java.io.IOException;
import java.io.InputStream;

public class UResourceImpl implements UResource {
    private Resource delegate;

    public UResourceImpl(Resource resource) {
        this.delegate = resource;
    }

    public InputStream getInputStream() throws IOException {
        return delegate.getInputStream();
    }

    @Override
    public String getResourcePackName() {
        return delegate.getPackId();
    }
}
