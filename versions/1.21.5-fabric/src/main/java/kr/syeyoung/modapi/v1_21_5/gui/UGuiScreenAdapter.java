package kr.syeyoung.modapi.v1_21_5.gui;

import kr.syeyoung.modapi.gui.UCustomGuiScreen;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class UGuiScreenAdapter extends Screen {
    @Getter
    private UCustomGuiScreen delegate;
    public UGuiScreenAdapter(UCustomGuiScreen screen) {
        super(Text.empty());

        this.delegate = screen;
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        delegate.render(new URenderContextImpl(context), deltaTicks);
    }

    @Override
    protected void init() {
        delegate.init();
    }

    @Override
    public void onDisplayed() {
        delegate.onDisplayed();
    }

    @Override
    public void removed() {
        delegate.onRemoved();
    }

    // why do we not call super? Let dg overwrite "ESC" for some cases...

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return delegate.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return delegate.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return delegate.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return delegate.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return delegate.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return delegate.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return delegate.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        delegate.mouseMoved(mouseX, mouseY);
    }
}
