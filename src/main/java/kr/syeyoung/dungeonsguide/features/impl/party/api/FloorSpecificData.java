package kr.syeyoung.dungeonsguide.features.impl.party.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FloorSpecificData<T> {
    private final int floor;
    private final T data;
}
