package kr.syeyoung.modapi.event.events;

import kr.syeyoung.modapi.event.UEvent;
import kr.syeyoung.modapi.gui.UGuiScreen;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public class ScreenInitEvent extends UEvent {
    private final UGuiScreen gui;
}
