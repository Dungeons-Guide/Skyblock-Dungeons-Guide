package kr.syeyoung.dungeonsguide.gui.elements;

import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Gui;

import java.awt.*;

public class MLabelAndElement extends MPanel {
    private MLabel label;
    private MPanel element;

    @Getter @Setter
    private Color hover;
    @Getter @Setter
    private Runnable onClick;

    public MLabelAndElement(String label, MPanel element) {
        this.add(this.label = new MLabel());
        this.label.setText(label);
        this.add(element);
        this.element = element;
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        if (hover != null && new Rectangle(new Point(0,0),getBounds().getSize()).contains(relMousex0, relMousey0)) {
            RenderUtils.drawRectSafe(0,0,getBounds().width, getBounds().height, hover.getRGB());
        }
    }

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        if (onClick!= null && lastAbsClip.contains(absMouseX, absMouseY)) {
            onClick.run();
        }
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setSize(new Dimension(parentWidth, getBounds().height));
        label.setBounds(new Rectangle(0,0,parentHeight / 3, getBounds().height));
        element.setBounds(new Rectangle(parentWidth / 3,0,parentWidth / 3 * 2, getBounds().height));
    }

    @Override
    public void onBoundsUpdate() {
        label.setBounds(new Rectangle(0,0,getBounds().width / 3, getBounds().height));
        element.setBounds(new Rectangle(getBounds().width / 3,0,getBounds().width / 3 * 2, getBounds().height));
    }
}
