package kr.syeyoung.dungeonsguide.features.impl.party.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CachedData<T> {
    private final long expire;
    private final T data;
}
