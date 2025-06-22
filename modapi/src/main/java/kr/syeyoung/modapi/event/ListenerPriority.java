package kr.syeyoung.modapi.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ListenerPriority {
    FIRST(0), SECOND(1), THIRD(2), FOURTH(3), LAST(4);
    final int priority;

    public static final int SIZE = values().length;
}
