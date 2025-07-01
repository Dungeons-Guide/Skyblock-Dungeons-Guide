package kr.syeyoung.modapi.event.events;

import kr.syeyoung.modapi.event.UEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;

@AllArgsConstructor @Getter @Setter
public class TabNameFormatEvent extends UEvent {
    private final String name;
    private final String teamFormat;
    private final Component formatted;
    private String displayName;
}
