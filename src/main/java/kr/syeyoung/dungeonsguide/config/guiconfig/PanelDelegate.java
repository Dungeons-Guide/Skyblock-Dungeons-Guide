package kr.syeyoung.dungeonsguide.config.guiconfig;

import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.roomedit.MPanel;

import java.awt.*;

public class PanelDelegate extends MPanel {
    private GuiFeature guiFeature;
    public PanelDelegate(GuiFeature guiFeature) {
        this.guiFeature = guiFeature;
    }

    @Override
    public Rectangle getBounds() {
        return guiFeature.getFeatureRect();
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        guiFeature.drawDemo(partialTicks);
    }

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {

    }

    @Override
    public void mouseReleased(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int state) {

    }

    @Override
    public void mouseClickMove(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int clickedMouseButton, long timeSinceLastClick) {

    }
}
