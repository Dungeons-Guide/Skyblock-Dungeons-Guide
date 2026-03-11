package kr.syeyoung.modapi.v1_21_11.gui;

import kr.syeyoung.modapi.gui.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;

@AllArgsConstructor @Getter
public class UNativeGuiScreen implements UGuiScreen {
    private final Screen handle;

    public static class UGuiScreenChestImpl extends UNativeGuiScreen implements UGuiScreenChest {
        private GenericContainerScreen containerScreen;
        public UGuiScreenChestImpl(GenericContainerScreen handle) {
            super(handle);
            this.containerScreen = handle;
        }

        @Override
        public UContainerChest getContainer() {
            return new UContainerChestImpl(containerScreen.getScreenHandler(), containerScreen.getTitle());
        }

        @Override
        public UContainerSlot getSlotUnderMouse() {
            return new UContainerSlotImpl(containerScreen.focusedSlot);
        }
    }

    public static class UGuiScreenChatImpl extends UNativeGuiScreen implements UGuiScreenChat {
        public UGuiScreenChatImpl(Screen handle) {
            super(handle);
        }
    }

    public static UNativeGuiScreen getUScreen(Screen screen) {
        if (screen == null) return null;
        if (screen instanceof ChatScreen screen1) return new UGuiScreenChatImpl(screen1);
        else if (screen instanceof GenericContainerScreen screen1) return new UGuiScreenChestImpl(screen1);
        else return new UNativeGuiScreen(screen);
    }
}
