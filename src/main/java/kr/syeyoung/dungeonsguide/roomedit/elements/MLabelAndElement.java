package kr.syeyoung.dungeonsguide.roomedit.elements;

import kr.syeyoung.dungeonsguide.roomedit.MPanel;

import java.awt.*;

public class MLabelAndElement extends MPanel {
    private MLabel label;
    private MPanel element;

    public MLabelAndElement(String label, MPanel element) {
        this.add(this.label = new MLabel());
        this.label.setText(label);
        this.add(element);
        this.element = element;
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
