package kr.syeyoung.dungeonsguide.features.listener;

import net.minecraftforge.client.event.GuiScreenEvent;

public interface GuiClickListener {
    void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre mouseInputEvent);
}
