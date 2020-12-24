package kr.syeyoung.dungeonsguide.roomedit.elements;

import kr.syeyoung.dungeonsguide.roomedit.MPanel;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.List;

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
        dec.setBounds(new Rectangle(0,0,bounds.height, bounds.height));
        inc.setBounds(new Rectangle(bounds.width - bounds.height, 0, bounds.height, bounds.height));
        selected.setBounds(new Rectangle(bounds.height, 0, bounds.width - bounds.height - bounds.height, bounds.height));
    }
}
