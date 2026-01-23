package kr.syeyoung.modapi.event.events;

import kr.syeyoung.modapi.event.Cancelable;
import kr.syeyoung.modapi.event.UEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public abstract class ScreenMouseEvent extends UEvent {
    private final double mouseX;
    private final double mouseY;
    public static class MouseClicked extends ScreenMouseEvent implements Cancelable {
        @Getter private final int eventButton;

        private boolean isCanceled;

        public MouseClicked(double mouseX, double mouseY, int eventButton, boolean isCanceled) {
            super(mouseX, mouseY);
            this.eventButton = eventButton;
            this.isCanceled = isCanceled;
        }

        @Override
        public boolean isCanceled() {
            return isCanceled;
        }

        @Override
        public void setCanceled(boolean canceled) {
            this.isCanceled = canceled;
        }
    }
    public static class MouseReleased extends ScreenMouseEvent {
        @Getter private final int eventButton;

        private boolean isCanceled;

        public MouseReleased(double mouseX, double mouseY, int eventButton) {
            super(mouseX, mouseY);
            this.eventButton = eventButton;
        }
    }
    public static class MouseMoved extends ScreenMouseEvent {
        public MouseMoved(double mouseX, double mouseY) {
            super(mouseX, mouseY);
        }
    }
    public static class MouseDragged extends ScreenMouseEvent {
        @Getter private final int dx;
        @Getter private final int dy;
        @Getter private final int eventButton;

        public MouseDragged(double mouseX, double mouseY, int dx, int dy, int eventButton) {
            super(mouseX, mouseY);
            this.dx = dx;
            this.dy = dy;
            this.eventButton = eventButton;
        }
    }
    public static class MouseScrolled extends ScreenMouseEvent implements Cancelable {
        @Getter private final double scrollX;
        @Getter private final double scrollY;
        
        private boolean isCanceled;

        public MouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, boolean isCanceled) {
            super(mouseX, mouseY);
            this.scrollX = scrollX;
            this.scrollY = scrollY;
            this.isCanceled = isCanceled;
        }

        @Override
        public boolean isCanceled() {
            return isCanceled;
        }

        @Override
        public void setCanceled(boolean canceled) {
            this.isCanceled = canceled;
        }
    }
}
