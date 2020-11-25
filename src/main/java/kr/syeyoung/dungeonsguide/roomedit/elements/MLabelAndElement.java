package kr.syeyoung.dungeonsguide.roomedit.elements;

import kr.syeyoung.dungeonsguide.roomedit.MPanel;
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
        if (hover != null && new Rectangle(new Point(0,0),bounds.getSize()).contains(relMousex0, relMousey0)) {
            Gui.drawRect(0,0,bounds.width, bounds.height, hover.getRGB());
        }
    }

    @Override
    protected void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        if (onClick!= null && new Rectangle(new Point(0,0),bounds.getSize()).contains(relMouseX, relMouseY)) {
            onClick.run();
        }
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setSize(new Dimension(parentWidth, bounds.height));
        label.setBounds(new Rectangle(0,0,parentHeight / 3, bounds.height));
        element.setBounds(new Rectangle(parentWidth / 3,0,parentWidth / 3 * 2, bounds.height));
    }

    @Override
    public void onBoundsUpdate() {
        label.setBounds(new Rectangle(0,0,bounds.width / 3, bounds.height));
        element.setBounds(new Rectangle(bounds.width / 3,0,bounds.width / 3 * 2, bounds.height));
    }
}
