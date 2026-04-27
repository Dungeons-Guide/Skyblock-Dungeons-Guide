package kr.syeyoung.modapi.v1_21_9.resources;

import kr.syeyoung.modapi.resources.UResourcePackEntry;
import kr.syeyoung.modapi.resources.UResourcePackRepository;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;

import java.util.ArrayList;
import java.util.List;

public class UResourcePackRepositoryImpl implements UResourcePackRepository {
    private ResourcePackManager delegate;

    public UResourcePackRepositoryImpl(ResourcePackManager delegate) {
        this.delegate = delegate;
    }

    public List<UResourcePackEntry> getRepositoryEntries() {
        List<UResourcePackEntry> entries = new ArrayList<>();
        for (ResourcePackProfile repositoryEntry : delegate.getEnabledProfiles()) {
            entries.add(new UResourcePackEntryImpl(repositoryEntry));
        }
        return entries;
    }
}
