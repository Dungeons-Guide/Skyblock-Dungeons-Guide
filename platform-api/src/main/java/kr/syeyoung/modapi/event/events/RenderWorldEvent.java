package kr.syeyoung.modapi.event.events;

import kr.syeyoung.modapi.event.UEvent;
import kr.syeyoung.modapi.rendering.UWorldRenderContext;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public class RenderWorldEvent extends UEvent {
    private final float partialTicks;
    private final UWorldRenderContext context;
}
