package kr.syeyoung.modapi.v1_8_9.util;

import org.lwjgl.input.Keyboard;

public class KeyboardModernizer {
    public static int getKeyCode() {
        return Keyboard.getEventKey();
    }
    public static int getScanCode() {
        return 0;
    }
    public static int getModifiers() {
        int mod = 0;
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) mod |= 0x1;
        if (Keyboard.isKeyDown(219) || Keyboard.isKeyDown(220)) mod |= 2;
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) mod |= 2;
        if (Keyboard.isKeyDown(Keyboard.KEY_LMETA) || Keyboard.isKeyDown(Keyboard.KEY_RMETA)) mod |= 4;
        return mod;
    }


}
