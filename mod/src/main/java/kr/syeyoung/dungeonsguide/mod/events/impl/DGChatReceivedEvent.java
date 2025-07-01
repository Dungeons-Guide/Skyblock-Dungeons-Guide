package kr.syeyoung.dungeonsguide.mod.events.impl;

import kr.syeyoung.modapi.event.Cancelable;
import kr.syeyoung.modapi.event.UEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.kyori.adventure.text.Component;

@Data @AllArgsConstructor
public class DGChatReceivedEvent extends UEvent implements Cancelable {
    private final String formattedText;
    private final Component originalComponent;


    private boolean canceled;
    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = true;
    }
}
