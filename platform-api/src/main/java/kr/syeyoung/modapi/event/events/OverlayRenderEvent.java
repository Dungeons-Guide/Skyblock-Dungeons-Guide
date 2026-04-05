package kr.syeyoung.modapi.event.events;

import kr.syeyoung.modapi.event.UEvent;
import kr.syeyoung.modapi.rendering.UGuiRenderContext;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public class OverlayRenderEvent extends UEvent {
    private final float partialTicks;
    private final UGuiRenderContext renderContext;
}
