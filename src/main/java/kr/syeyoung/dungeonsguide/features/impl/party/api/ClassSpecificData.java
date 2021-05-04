package kr.syeyoung.dungeonsguide.features.impl.party.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ClassSpecificData<T> {
    private final DungeonClass dungeonClass;
    private final T data;
}
