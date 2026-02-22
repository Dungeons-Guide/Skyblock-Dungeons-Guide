package v1_21_11.resources;

import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.resources.UResource;
import kr.syeyoung.modapi.resources.UResourceManager;
import kr.syeyoung.modapi.v1_21_5.resources.UResourceImpl;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UResourceManagerImpl implements UResourceManager {
    private ResourceManager delegate;

    public UResourceManagerImpl(ResourceManager delegate) {
        this.delegate = delegate;
    }

    public UResource getResource(ResourceIdentifier location) throws IOException {
        Resource iResource = delegate.getResource(Identifier.of(location.getMod(), location.getLocation())).orElse(null);
        if (iResource == null) return null;
        return new UResourceImpl(iResource);
    }

    @Override
    public List<UResource> getAllResources(ResourceIdentifier location) throws IOException {
        List<Resource> iResource = delegate.getAllResources(Identifier.of(location.getMod(), location.getLocation()));
        if (iResource == null) return Collections.emptyList();
        List<UResource> resources = new ArrayList<>();
        for (Resource resource : iResource) {
            resources.add(new UResourceImpl(resource));
        }
        return resources;
    }
}

