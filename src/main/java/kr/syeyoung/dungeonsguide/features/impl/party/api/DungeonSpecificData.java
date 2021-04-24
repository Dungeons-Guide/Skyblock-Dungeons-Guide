package kr.syeyoung.dungeonsguide.features.impl.party.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DungeonSpecificData<T> {
    private final DungeonType type;
    private final T data;
}
