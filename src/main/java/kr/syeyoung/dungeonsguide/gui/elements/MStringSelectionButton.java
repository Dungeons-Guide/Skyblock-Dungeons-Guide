package kr.syeyoung.dungeonsguide.gui.elements;

import kr.syeyoung.dungeonsguide.gui.MPanel;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.List;

@Getter
@Setter
public class MStringSelectionButton extends MPanel {

    private List<String> possible;
    private int selectedIndex;

    private MButton dec;
    private MButton inc;
    private MLabel selected;

    @Getter
    @Setter
    private Runnable onUpdate;

    public MStringSelectionButton(final List<String> possible, String defaultValue) {
        this.possible = possible;
        selectedIndex = possible.indexOf(defaultValue);
        if (selectedIndex == -1) selectedIndex = 0;

        dec = new MButton(); dec.setText("<"); add(dec);
        inc = new MButton(); inc.setText(">"); add(inc);
        selected = new MLabel(); updateSelected(); add(selected);

        dec.setOnActionPerformed(new Runnable() {
            @Override
            public void run() {
                selectedIndex++;
                if (selectedIndex >= possible.size()) selectedIndex = 0;
                updateSelected();
                onUpdate.run();
            }
        });
        inc.setOnActionPerformed(new Runnable() {
            @Override
            public void run() {
                selectedIndex --;
                if (selectedIndex < 0) selectedIndex = possible.size() - 1;
                updateSelected();
                onUpdate.run();
            }
        });
    }

    public String selectionToDisplay(String selection) {
        return selection;
    }

    public String getSelected() {
        if (possible.size() == 0) return null;
        return possible.get(selectedIndex);
    }

    private void updateSelected() {
        if (possible.size() == 0) selected.setText("-Empty-");
        else selected.setText(selectionToDisplay(possible.get(selectedIndex)));
    }

    @Override
    public void onBoundsUpdate() {
        dec.setBounds(new Rectangle(0,0,getBounds().height, getBounds().height));
        inc.setBounds(new Rectangle(getBounds().width - getBounds().height, 0, getBounds().height, getBounds().height));
        selected.setBounds(new Rectangle(getBounds().height, 0, getBounds().width - getBounds().height - getBounds().height, getBounds().height));
    }
}
