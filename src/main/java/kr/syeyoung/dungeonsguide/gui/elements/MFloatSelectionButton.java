/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.gui.elements;

import kr.syeyoung.dungeonsguide.gui.MPanel;
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

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(-1,15);
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
