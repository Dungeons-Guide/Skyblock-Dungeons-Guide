package kr.syeyoung.modapi.event.events;

import kr.syeyoung.modapi.event.UEvent;
import kr.syeyoung.modapi.item.UItemStack;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter @AllArgsConstructor
public class WindowUpdateEvent extends UEvent {
    private int windowId;
    private List<SlotUpdate> slotUpdateList;
    private boolean singleUpdate;

    @Getter @AllArgsConstructor
    public static class SlotUpdate {
        private int slotId;
        private UItemStack itemStack;
    }
}
