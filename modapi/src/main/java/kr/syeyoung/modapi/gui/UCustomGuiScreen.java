package kr.syeyoung.modapi.gui;

import kr.syeyoung.modapi.rendering.UGuiRenderContext;

public interface UCustomGuiScreen extends UGuiScreen {
    void init();
    void render(UGuiRenderContext context, float deltaTicks);

    default void onDisplayed() {}
    default void onRemoved() {}

    default void mouseMoved(double mouseX, double mouseY) { }
    default boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
    default boolean mouseReleased(double mouseX, double mouseY, int button) { return false; }
    default boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {  return false; }
    default boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) { return false; }
    default boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
    default boolean keyReleased(int keyCode, int scanCode, int modifiers) { return false; }
    default boolean charTyped(char chr, int modifiers) { return false;}
}
