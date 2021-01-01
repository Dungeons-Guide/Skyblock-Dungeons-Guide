package kr.syeyoung.dungeonsguide.roomedit.elements;

import kr.syeyoung.dungeonsguide.roomedit.MPanel;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public class MFloatSelectionButton extends MPanel {

    private float data;

    private MButton dec;
    private MButton inc;
    private MTextField selected;

    @Getter
    @Setter
    private Runnable onUpdate;

    public MFloatSelectionButton(float data2) {
        this.data = data2;

        dec = new MButton(); dec.setText("<"); add(dec);
        inc = new MButton(); inc.setText(">"); add(inc);
        selected = new MTextField() {
            @Override
            public String getText() {
                return data +"";
            }
            @Override
            public void edit(String str) {
                try {
                    data = Float.parseFloat(str);
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

    public float getSelected() {
        return data;
    }

    public void updateSelected() {
        selected.setText(data+"");
    }

    @Override
    public void onBoundsUpdate() {
        dec.setBounds(new Rectangle(0,0,getBounds().height, getBounds().height));
        inc.setBounds(new Rectangle(getBounds().width - getBounds().height, 0, getBounds().height, getBounds().height));
        selected.setBounds(new Rectangle(getBounds().height, 0, getBounds().width - getBounds().height - getBounds().height, getBounds().height));
    }
}
