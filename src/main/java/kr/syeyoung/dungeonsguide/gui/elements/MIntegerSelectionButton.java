package kr.syeyoung.dungeonsguide.gui.elements;

import kr.syeyoung.dungeonsguide.gui.MPanel;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public class MIntegerSelectionButton extends MPanel {

    private int data;

    private MButton dec;
    private MButton inc;
    private MTextField selected;

    @Getter
    @Setter
    private Runnable onUpdate;

    public MIntegerSelectionButton(int data2) {
        this.data = data2;

        dec = new MButton(); dec.setText("<"); add(dec);
        inc = new MButton(); inc.setText(">"); add(inc);
        selected = new MTextField() {
            @Override
            public void edit(String str) {
                try {
                    data = Integer.parseInt(str);
                    onUpdate.run();
                } catch (Exception e) {}
            }
        }; updateSelected(); add(selected);

        dec.setOnActionPerformed(new Runnable() {
            @Override
            public void run() {
                data--;
                updateSelected();
                onUpdate.run();
            }
        });
        inc.setOnActionPerformed(new Runnable() {
            @Override
            public void run() {
                data ++;
                updateSelected();
                onUpdate.run();
            }
        });
    }

    public int getSelected() {
        return data;
    }

    private void updateSelected() {
        selected.setText(data+"");
    }

    @Override
    public void onBoundsUpdate() {
        dec.setBounds(new Rectangle(0,0,getBounds().height, getBounds().height));
        inc.setBounds(new Rectangle(getBounds().width - getBounds().height, 0, getBounds().height, getBounds().height));
        selected.setBounds(new Rectangle(getBounds().height, 0, getBounds().width - getBounds().height - getBounds().height, getBounds().height));
    }
}
