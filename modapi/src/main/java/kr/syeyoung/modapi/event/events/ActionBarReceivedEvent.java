package kr.syeyoung.modapi.event.events;

import kr.syeyoung.modapi.event.UEvent;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;

@AllArgsConstructor
public class ActionBarReceivedEvent extends UEvent {
    public final Component original;
    public Component chat;
}
