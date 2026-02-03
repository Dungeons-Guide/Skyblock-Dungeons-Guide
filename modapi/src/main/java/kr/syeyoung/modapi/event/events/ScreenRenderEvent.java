package kr.syeyoung.modapi.event.events;

import kr.syeyoung.modapi.event.UEvent;
import kr.syeyoung.modapi.gui.UGuiScreen;
import kr.syeyoung.modapi.rendering.URenderContext;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public abstract class ScreenRenderEvent extends UEvent {
    private final UGuiScreen gui;
    private final float partialTicks;
    private final URenderContext renderContext;

    public static class Pre extends ScreenRenderEvent {
        public Pre(UGuiScreen gui, float partialTicks, URenderContext renderContext) {
            super(gui, partialTicks, renderContext);
        }
    }

    public static class Post extends ScreenRenderEvent {
        public Post(UGuiScreen gui, float partialTicks, URenderContext renderContext) {
            super(gui, partialTicks, renderContext);
        }
    }
}
