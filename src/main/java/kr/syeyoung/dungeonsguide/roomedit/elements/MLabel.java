package kr.syeyoung.dungeonsguide.roomedit.elements;

import kr.syeyoung.dungeonsguide.roomedit.MPanel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.awt.*;

public class MLabel extends MPanel {
    @Getter
    @Setter
    private String text;

    @Getter
    @Setter
    private Color foreground = Color.white;

    public enum Alignment {
        LEFT, CENTER, RIGHT
    }
    @Getter
    @Setter
    private Alignment alignment= Alignment.LEFT;

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle clip) {
        Dimension bounds = getSize();

        FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
        int width = renderer.getStringWidth(getText());
        int x,y;
        if (alignment == Alignment.CENTER) {
            x = (bounds.width - width) / 2;
             y = (bounds.height - renderer.FONT_HEIGHT) / 2;
        } else if (alignment == Alignment.LEFT) {
             x = 0;
             y = (bounds.height - renderer.FONT_HEIGHT) / 2;
        } else if (alignment == Alignment.RIGHT) {
            x = bounds.width - width;
            y = (bounds.height - renderer.FONT_HEIGHT) / 2;
        } else{
            return;
        }
        renderer.drawString(getText(), x,y, 0xffffff);
    }
}
