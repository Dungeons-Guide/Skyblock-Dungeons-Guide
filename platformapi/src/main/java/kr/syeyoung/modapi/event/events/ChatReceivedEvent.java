package kr.syeyoung.modapi.event.events;

import kr.syeyoung.modapi.event.Cancelable;
import kr.syeyoung.modapi.event.UEvent;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;

@AllArgsConstructor
public class ChatReceivedEvent extends UEvent implements Cancelable {
    public final Component original;
    public Component chat;

    public boolean isSystem;


    private boolean canceled;
    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled =canceled;
    }
}
