package kr.syeyoung.dungeonsguide.features.listener;

import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public interface KeyInputListener {
    void onKeyInput(GuiScreenEvent.KeyboardInputEvent keyboardInputEvent);
}
