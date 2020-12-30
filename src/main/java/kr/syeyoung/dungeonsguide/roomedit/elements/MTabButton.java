package kr.syeyoung.dungeonsguide.roomedit.elements;

import kr.syeyoung.dungeonsguide.roomedit.MPanel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

import java.awt.*;

@Getter
@Setter
public class MTabButton extends MPanel {
    private String text;

    private Color foreground = Color.white;
    private Color hover = new Color(236, 236, 236, 64);
    private Color clicked = new Color(30,30,30,0);
    private Color selected = new Color(0,0,0,255);
    private Color disabled = new Color(0,0,0);

    private boolean enabled = true;

    private MTabbedPane tabbedPane;

    public MTabButton(MTabbedPane tabbedPane, String key) {
        this.tabbedPane = tabbedPane;
        this.text = key;
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle clip) {
        Dimension bounds = getSize();

        Color bg = null;
        if (!enabled) {
            bg = disabled;
        } else if (tabbedPane.getSelectedKey().equals(text)) {
            bg = selected;
        } else if (new Rectangle(new Point(0,0),bounds).contains(relMousex0, relMousey0)) {
            bg = hover;
        }
        if (bg != null)
            Gui.drawRect(0,0,bounds.width, bounds.height, bg.getRGB());

        FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
        int width = renderer.getStringWidth(text);
        int x = (bounds.width - width) / 2;
        int y = (bounds.height - renderer.FONT_HEIGHT) / 2;

        renderer.drawString(text, x,y, foreground.getRGB());
    }

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        if (lastAbsClip.contains(absMouseX, absMouseY)) {
            tabbedPane.setSelectedKey(text);
        }
    }
}
