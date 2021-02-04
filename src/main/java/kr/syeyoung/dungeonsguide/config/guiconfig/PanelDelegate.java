package kr.syeyoung.dungeonsguide.config.guiconfig;

import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

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
        GlStateManager.pushMatrix();
        guiFeature.drawDemo(partialTicks);
        GlStateManager.popMatrix();

        Gui.drawRect(0,0, 3, 3, 0xFFBBBBBB);
        Gui.drawRect(0, getBounds().height - 3, 3, getBounds().height, 0xFFBBBBBB);
        Gui.drawRect(getBounds().width - 3,0, getBounds().width, 3, 0xFFBBBBBB);
        Gui.drawRect(getBounds().width - 3,getBounds().height - 3, getBounds().width, getBounds().height, 0xFFBBBBBB);
        if (lastAbsClip.contains(absMousex, absMousey)) {
            if (relMouseX < 3 && relMouseY < 3) {
                Gui.drawRect(0,0, 3, 3, 0x55FFFFFF);
            } else if (relMouseX < 3 && relMouseY > getBounds().height - 3) {
                Gui.drawRect(0, getBounds().height - 3, 3, getBounds().height, 0x55FFFFFF);
            } else if (relMouseX > getBounds().width - 3 && relMouseY > getBounds().height - 3) {
                Gui.drawRect(getBounds().width - 3,getBounds().height - 3, getBounds().width, getBounds().height, 0x55FFFFFF);
            } else if (relMouseX > getBounds().width - 3 && relMouseY < 3) {
                Gui.drawRect(getBounds().width - 3,0, getBounds().width, 3, 0x55FFFFFF);
            } else if (selectedPart == -2){
                Gui.drawRect(0,0, getBounds().width, getBounds().height, 0x55FFFFFF);
            }
        }
    }

    private int selectedPart = -2;

    private int lastX = 0;
    private int lastY = 0;


    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        if (!lastAbsClip.contains(absMouseX, absMouseY)) return;
        if (relMouseX < 3 && relMouseY < 3) {
            selectedPart = 0;
        } else if (relMouseX < 3 && relMouseY > getBounds().height - 3) {
            selectedPart = 2;
        } else if (relMouseX > getBounds().width - 3 && relMouseY > getBounds().height - 3) {
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
        if (selectedPart >= -1) {
            int minWidth;
            int minHeight;
            if (guiFeature.isKeepRatio()) {
                minHeight = (int) Math.max(8, 8 / guiFeature.getDefaultRatio());
                minWidth = (int) (guiFeature.getDefaultRatio() * minHeight);
            } else {
                minWidth = 8;
                minHeight = 8;
            }
            Rectangle rectangle = guiFeature.getFeatureRect().getBounds();
            if (rectangle.width < minWidth || rectangle.height < minHeight) {
                rectangle.width = minWidth;
                rectangle.height= minHeight;
            }

            if (rectangle.x < 0) rectangle.x = 0;
            if (rectangle.y < 0) rectangle.y = 0;

            guiFeature.setFeatureRect(rectangle);
        }

        selectedPart = -2;


    }

    @Override
    public void mouseClickMove(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int clickedMouseButton, long timeSinceLastClick) {
        int dx = absMouseX - lastX;
        int dy = absMouseY - lastY;
        if (selectedPart >= 0) {
            boolean revChangeX = (selectedPart & 0x1) == 0;
            boolean revChangeY = (selectedPart & 0x2) == 0;
            Rectangle rectangle = guiFeature.getFeatureRect().getBounds();
            int prevWidth = rectangle.width;
            int prevHeight= rectangle.height;

            rectangle.width = prevWidth + (revChangeX ? -1 : 1) * dx;
            rectangle.height = prevHeight + (revChangeY ? - 1: 1 ) * dy;

            if (guiFeature.isKeepRatio()) {
                double ratio = guiFeature.getDefaultRatio();
                int width1 = rectangle.width;
                int height1 = (int) (width1 / ratio);

                int width2 = (int) (rectangle.height * ratio);
                int height2 = rectangle.height;

                if (ratio >= 1) {
                    rectangle.width = width1;
                    rectangle.height = height1;
                } else {
                    rectangle.width = width2;
                    rectangle.height = height2;
                }
            }


            if (revChangeX) rectangle.x -= (rectangle.width - prevWidth );
            if (revChangeY) rectangle.y -= (rectangle.height - prevHeight);

            lastX = absMouseX;
            lastY = absMouseY;

            if (rectangle.height < 0) {
                rectangle.height = -rectangle.height;
                rectangle.y -= rectangle.height;
                selectedPart = selectedPart ^ 0x2;
                lastY += revChangeY ? 3 : 3;
            }

            if (rectangle.width < 0) {
                rectangle.width = -rectangle.width;
                rectangle.x -= rectangle.width;
                selectedPart = selectedPart ^ 0x1;
                lastX += revChangeX ? 3 : 3;
            }

            guiFeature.setFeatureRect(rectangle);
        } else if (selectedPart == -1){
            Rectangle rectangle = guiFeature.getFeatureRect().getBounds();
            rectangle.translate(dx, dy);
            guiFeature.setFeatureRect(rectangle);
            lastX = absMouseX;
            lastY = absMouseY;
        }
    }
}
