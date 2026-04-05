package kr.syeyoung.modapi.event.events;

import kr.syeyoung.modapi.event.UEvent;
import kr.syeyoung.modapi.paralleluniverse.tablist.UTabListEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter @AllArgsConstructor
public class TabListUpdateEvent extends UEvent {
    private final Action action;
    private final List<? extends UTabListEntry> entries;

    public enum Action {
        ADD_PLAYER, REMOVE_PLAYER
    }
}
