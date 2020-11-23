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

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks) {
        Dimension bounds = getSize();

        FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
        int width = renderer.getStringWidth(text);
        int x = (bounds.width - width) / 2;
        int y = (bounds.height - renderer.FONT_HEIGHT) / 2;

        renderer.drawString(text, x,y, foreground.getRGB());
    }
}
