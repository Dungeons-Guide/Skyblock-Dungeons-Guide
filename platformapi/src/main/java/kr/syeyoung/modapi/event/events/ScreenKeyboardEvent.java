package kr.syeyoung.modapi.event.events;

import kr.syeyoung.modapi.event.UEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public abstract class ScreenKeyboardEvent extends UEvent {
    private int key;
    private int scancode;
    private int modifiers;

    public static class KeyPressed extends ScreenKeyboardEvent {

        public KeyPressed(int key, int scancode, int modifiers) {
            super(key, scancode, modifiers);
        }
    }

    public static class KeyReleased extends ScreenKeyboardEvent {

        public KeyReleased(int key, int scancode, int modifiers) {
            super(key, scancode, modifiers);
        }
    }
}
