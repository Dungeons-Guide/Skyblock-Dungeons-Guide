package kr.syeyoung.modapi.v1_8_9.resources;

import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.resources.UResource;
import kr.syeyoung.modapi.resources.UResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UResourceManagerImpl implements UResourceManager {
    private IResourceManager delegate;

    public UResourceManagerImpl(IResourceManager delegate) {
        this.delegate = delegate;
    }

    public UResource getResource(ResourceIdentifier location) throws IOException {
        IResource iResource = delegate.getResource(new ResourceLocation(location.getMod(), location.getLocation()));
        if (iResource == null) return null;
        return new UResourceImpl(iResource);
    }

    @Override
    public List<UResource> getAllResources(ResourceIdentifier location) throws IOException {
        List<IResource> iResource = delegate.getAllResources(new ResourceLocation(location.getMod(), location.getLocation()));
        if (iResource == null) return Collections.emptyList();
        List<UResource> resources = new ArrayList<>();
        for (IResource resource : iResource) {
            resources.add(new UResourceImpl(resource));
        }
        return resources;
    }
}

