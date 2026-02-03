package kr.syeyoung.modapi.event.events;

import kr.syeyoung.modapi.entity.UEntityLiving;
import kr.syeyoung.modapi.event.Cancelable;
import kr.syeyoung.modapi.event.UEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class RenderLivingEvent extends UEvent implements Cancelable {
    @Getter
    private final UEntityLiving entity;
    private boolean isCanceled;

    @Override
    public boolean isCanceled() {
        return isCanceled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.isCanceled = canceled;
    }
}
