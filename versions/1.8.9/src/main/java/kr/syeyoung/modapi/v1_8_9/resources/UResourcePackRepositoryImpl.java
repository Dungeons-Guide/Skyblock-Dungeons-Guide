package kr.syeyoung.modapi.v1_8_9.resources;

import kr.syeyoung.modapi.resources.UResourcePackEntry;
import kr.syeyoung.modapi.resources.UResourcePackRepository;
import net.minecraft.client.resources.ResourcePackRepository;

import java.util.ArrayList;
import java.util.List;

public class UResourcePackRepositoryImpl implements UResourcePackRepository {
    private ResourcePackRepository delegate;

    public UResourcePackRepositoryImpl(ResourcePackRepository delegate) {
        this.delegate = delegate;
    }

    public List<UResourcePackEntry> getRepositoryEntries() {
        List<UResourcePackEntry> entries = new ArrayList<>();
        for (ResourcePackRepository.Entry repositoryEntry : delegate.getRepositoryEntries()) {
            entries.add(new UResourcePackEntryImpl(repositoryEntry));
        }
        return entries;
    }
}
