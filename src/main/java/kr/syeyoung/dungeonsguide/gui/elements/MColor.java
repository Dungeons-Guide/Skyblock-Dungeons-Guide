package kr.syeyoung.dungeonsguide.gui.elements;

import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.client.gui.Gui;

import java.awt.*;

@AllArgsConstructor
@NoArgsConstructor
public class MColor extends MPanel {
    @Getter
    @Setter
    private Color color = Color.white;
    @Getter
    @Setter
    private Dimension size = new Dimension(20,15);
    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        Rectangle rectangle = getBounds();

        int x = (rectangle.width - getSize().width) / 2;
        int y = (rectangle.height - getSize().height) / 2;

        RenderUtils.drawRectSafe(x,y,x+getSize().width,y+getSize().height, getColor().getRGB());
    }
}
