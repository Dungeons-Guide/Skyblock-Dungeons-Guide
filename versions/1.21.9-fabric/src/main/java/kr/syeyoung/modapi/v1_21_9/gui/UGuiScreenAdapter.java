package kr.syeyoung.modapi.v1_21_9.gui;

import kr.syeyoung.modapi.gui.UCustomGuiScreen;
import kr.syeyoung.modapi.v1_21_9.render.UGuiRenderContextImpl;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
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
        delegate.render(new UGuiRenderContextImpl(context), deltaTicks);
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
    public boolean keyPressed(KeyInput input) {
        return delegate.keyPressed(input.key(), input.scancode(), input.modifiers());
    }

    @Override
    public boolean keyReleased(KeyInput input) {
        return delegate.keyReleased(input.key(), input.scancode(), input.modifiers());
    }

    @Override
    public boolean charTyped(CharInput input) {
        return delegate.charTyped((char) input.codepoint(), input.modifiers());
    }

    @Override
    public boolean mouseClicked(Click input, boolean doubled) {
        double sc = MinecraftClient.getInstance().getWindow().getScaleFactor();
        return delegate.mouseClicked(input.x() * sc, input.y() * sc, input.button());
    }

    @Override
    public boolean mouseReleased(Click input) {
        double sc = MinecraftClient.getInstance().getWindow().getScaleFactor();
        return delegate.mouseReleased(input.x() * sc, input.y() * sc, input.button());
    }

    @Override
    public boolean mouseDragged(Click input, double deltaX, double deltaY) {
        double sc = MinecraftClient.getInstance().getWindow().getScaleFactor();
        return delegate.mouseDragged(input.x() * sc, input.y() * sc, input.button(), deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double sc = MinecraftClient.getInstance().getWindow().getScaleFactor();
        return delegate.mouseScrolled(mouseX * sc, mouseY * sc, horizontalAmount, verticalAmount);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        double sc = MinecraftClient.getInstance().getWindow().getScaleFactor();
        delegate.mouseMoved(mouseX * sc, mouseY * sc);
    }
}
