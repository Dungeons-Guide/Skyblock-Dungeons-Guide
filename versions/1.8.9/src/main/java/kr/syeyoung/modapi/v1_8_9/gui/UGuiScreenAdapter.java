package kr.syeyoung.modapi.v1_8_9.gui;

import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.modapi.gui.UCustomGuiScreen;
import kr.syeyoung.modapi.v1_8_9.render.UGuiRenderContextImpl;
import kr.syeyoung.modapi.v1_8_9.util.KeyboardModernizer;
import kr.syeyoung.modapi.v1_8_9.util.RenderUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.event.KeyEvent;
import java.io.IOException;

public class UGuiScreenAdapter extends GuiScreen  {
    @Getter
    private UCustomGuiScreen delegate;
    public UGuiScreenAdapter(UCustomGuiScreen delegate) {
        this.delegate = delegate;
    }

    @Override
    public void initGui() {
        delegate.init();
        delegate.onDisplayed();
    }

    @Override
    public void onGuiClosed() {
        delegate.onRemoved();
    }

    private float partialTickTracker = 0;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float deltaTick = partialTicks - partialTickTracker;
        partialTickTracker = partialTicks;
        RenderUtils.preRenderGui();
        delegate.render(
                new UGuiRenderContextImpl(), deltaTick < 0 ? deltaTick + 1 : deltaTick // Yes i'm aware this is not actually partial ticks. $$
        );
        RenderUtils.postRenderGui();
    }

    private int touchValue;
    private int eventButton;
    private long lastMouseEvent;


    private int lastX, lastY;


    @Override
    public void handleMouseInput() throws IOException {
        int i = Mouse.getEventX();
        int j = this.mc.displayHeight - Mouse.getEventY();
        int k = Mouse.getEventButton();

        if (i != lastX && j != lastY) {
            delegate.mouseMoved(i, j);
        }
        if (Mouse.getEventButtonState()) {
            if (this.mc.gameSettings.touchscreen && this.touchValue++ > 0) {
                return;
            }

            this.eventButton = k;
            this.lastMouseEvent = Minecraft.getSystemTime();
            delegate.mouseClicked(i, j, this.eventButton);
        } else if (k != -1) {
            if (this.mc.gameSettings.touchscreen && --this.touchValue > 0) {
                return;
            }

            this.eventButton = -1;
            delegate.mouseReleased(i, j, this.eventButton);
        } else if (this.eventButton != -1 && this.lastMouseEvent > 0L) {
            long l = Minecraft.getSystemTime() - this.lastMouseEvent;
            delegate.mouseDragged(i, j, this.eventButton, i - lastX, j - lastY);
        }


        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            try {
                delegate.mouseScrolled(i, j, wheel, wheel);
            } catch (Exception e) {
                FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
            }
        }
        lastX = i;
        lastY = j;
    }

    public boolean isPrintableChar( char c ) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of( c );
        return (!Character.isISOControl(c)) &&
                c != KeyEvent.CHAR_UNDEFINED &&
                block != null &&
                block != Character.UnicodeBlock.SPECIALS &&
                block != Character.UnicodeBlock.PRIVATE_USE_AREA;
    }

    public void handleKeyboardInput() throws IOException {
        // modifiers

        if (Keyboard.getEventKeyState()) {
            if (!Keyboard.isRepeatEvent())
                delegate.keyPressed(KeyboardModernizer.getKeyCode(), KeyboardModernizer.getScanCode(), KeyboardModernizer.getModifiers());
            if (isPrintableChar(Keyboard.getEventCharacter()))
                delegate.charTyped(Keyboard.getEventCharacter(), KeyboardModernizer.getModifiers());
        } else {
            delegate.keyReleased(KeyboardModernizer.getKeyCode(), KeyboardModernizer.getScanCode(), KeyboardModernizer.getModifiers());
        }

        this.mc.dispatchKeypresses();
    }

    @Override
    public void handleInput() throws IOException {
        Keyboard.enableRepeatEvents(true); // I hope it's a temporary solution NEU Incompat. ?
        super.handleInput();
    }
}
