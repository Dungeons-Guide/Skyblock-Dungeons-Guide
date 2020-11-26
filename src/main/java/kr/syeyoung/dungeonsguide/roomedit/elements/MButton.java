package kr.syeyoung.dungeonsguide.roomedit.elements;

import kr.syeyoung.dungeonsguide.roomedit.MPanel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;
import org.w3c.dom.css.Rect;

import java.awt.*;

@Getter
@Setter
public class MButton extends MPanel {
    private String text;

    private Color foreground = Color.white;
    private Color hover = Color.gray;
    private Color clicked = Color.lightGray;
    private Color disabled = Color.darkGray;

    private boolean enabled = true;

    private Runnable onActionPerformed;

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle clip) {
        Dimension bounds = getSize();

        Color bg = backgroundColor;
        if (!enabled) {
            bg = disabled;
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
        if (onActionPerformed != null && new Rectangle(new Point(0,0),getSize()).contains(relMouseX, relMouseY))
            onActionPerformed.run();
    }
}
