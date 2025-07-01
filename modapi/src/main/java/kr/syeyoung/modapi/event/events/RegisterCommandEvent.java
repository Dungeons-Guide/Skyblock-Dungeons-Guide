package kr.syeyoung.modapi.event.events;

import kr.syeyoung.modapi.command.UCommandManager;
import kr.syeyoung.modapi.event.UEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class RegisterCommandEvent extends UEvent {
    private UCommandManager commandManager;
}
