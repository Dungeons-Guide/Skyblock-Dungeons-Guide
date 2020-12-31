package kr.syeyoung.dungeonsguide.config.guiconfig;

import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.roomedit.MPanel;
import net.minecraft.client.gui.Gui;
import org.w3c.dom.css.Rect;

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
    public void render(int absMousex, int absMousey, int relMouseX, int relMouseY, float partialTicks, Rectangle scissor) {
        guiFeature.drawDemo(partialTicks);

        Gui.drawRect(0,0, 3, 3, 0x55777777);
        Gui.drawRect(0, getBounds().height - 3, 3, 3, 0x55777777);
        Gui.drawRect(getBounds().width - 3,0, 3, 3, 0x55777777);
        Gui.drawRect(getBounds().width - 3,getBounds().height - 3, 3, 3, 0x55777777);
        if (lastAbsClip.contains(absMousex, absMousey)) {
            if (relMouseX < 3 && relMouseY < 3) {
                Gui.drawRect(0,0, 3, 3, 0x55FFFFFF);
            } else if (relMouseX < 3 && relMouseY > getBounds().height - 3) {
                Gui.drawRect(0, getBounds().height - 3, 3, 3, 0x55FFFFFF);
            } else if (relMouseX > getBounds().width - 3 && relMouseY < getBounds().height - 3) {
                Gui.drawRect(getBounds().width - 3,getBounds().height - 3, 3, 3, 0x55FFFFFF);
            } else if (relMouseX > getBounds().width - 3 && relMouseY < 3) {
                Gui.drawRect(getBounds().width - 3,0, 3, 3, 0x55FFFFFF);
            } else {
                Gui.drawRect(0,0, getBounds().width, getBounds().height, 0x55FFFFFF);
            }
        }
    }

    private int selectedPart = 0;

    private int lastX = 0;
    private int lastY = 0;


    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        if (!lastAbsClip.contains(absMouseX, absMouseY)) return;
        if (relMouseX < 3 && relMouseY < 3) {
            selectedPart = 0;
        } else if (relMouseX < 3 && relMouseY > getBounds().height - 3) {
            selectedPart = 2;
        } else if (relMouseX > getBounds().width - 3 && relMouseY < getBounds().height - 3) {
            selectedPart = 3;
        } else if (relMouseX > getBounds().width - 3 && relMouseY < 3) {
            selectedPart = 1;
        } else {
            selectedPart = -1;
        }
        lastX = absMouseX;
        lastY = absMouseY;
    }

    @Override
    public void mouseReleased(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int state) {
        selectedPart = 0;
    }

    @Override
    public void mouseClickMove(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int clickedMouseButton, long timeSinceLastClick) {
        int dx = absMouseX - lastX;
        int dy = absMouseY - lastY;
        if (selectedPart > 0) {
            boolean revChangeX = (selectedPart & 0x1) == 0;
            boolean revChangeY = (selectedPart & 0x2) == 0;
            Rectangle rectangle = guiFeature.getFeatureRect().getBounds();
            int prevWidth = rectangle.width;
            int prevHeight= rectangle.height;

            rectangle.width = prevWidth + dx;
            rectangle.height = prevHeight + dy;

            if (guiFeature.isKeepRatio()) {
                double ratio = guiFeature.getDefaultRatio();
                int width1 = rectangle.width;
                int height1 = (int) (width1 / ratio);

                int width2 = (int) (rectangle.height * ratio);
                int height2 = rectangle.height;

                if (width1 * height1 < width2 * height2) {
                    rectangle.width = width1;
                    rectangle.height = height1;
                } else {
                    rectangle.width = width2;
                    rectangle.height= height2;
                }
            }
            if (revChangeX) rectangle.x -= (rectangle.width - prevWidth);
            if (revChangeY) rectangle.y -= (rectangle.height - prevHeight);

            guiFeature.setFeatureRect(rectangle);
        } else if (selectedPart == -1){
            Rectangle rectangle = guiFeature.getFeatureRect().getBounds();
            rectangle.translate(dx, dy);
            guiFeature.setFeatureRect(rectangle);
        }
    }
}
