/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.config.guiconfig.old;

import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.*;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.util.*;
import java.util.List;

public class FeatureEditPane extends MPanel {
    private final List<AbstractFeature> features;

    private final List<MFeature> le = new ArrayList<MFeature>();

    private final GuiConfig config;

    private MTextField textField;
    private String search = "";

    public FeatureEditPane(List<AbstractFeature> features, GuiConfig config) {
        this.features = features;
        this.config = config;
        buildElements();

    }


    public void buildElements() {
        for (AbstractFeature feature : features) {
            MFeature mFeature = new MFeature(feature, config);
            mFeature.setHover(new Color(94, 94, 94, 255));
            le.add(mFeature);
            add(mFeature);
        }

        textField = new MTextField() {
            @Override
            public void edit(String str) {
                offsetY = 0;
                search = str;
            }
        };
        textField.setText("");
        textField.setBounds(new Rectangle(getBounds().width - 200, 0, 200, 20));
        add(textField);
    }
    @Override
    public void onBoundsUpdate() {
        for (MPanel panel :getChildComponents()){
            panel.setSize(new Dimension(getBounds().width, panel.getPreferredSize().height));
        }
        textField.setBounds(new Rectangle(getBounds().width - 200, 0, 200, 20));
    }
    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(5,1,parentWidth-10,parentHeight-2));
    }

    @Override
    public List<MPanel> getChildComponents() {
        List<MPanel> comp = new ArrayList<MPanel>();
        comp.add(textField);
        for (MFeature feature:le) {
            if (feature.getFeature().getName().toLowerCase().contains(search.toLowerCase()))
                comp.add(feature);
        }
        return comp;
    }

    private int offsetY = 0;
    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        int heights = 0;
        for (MPanel panel:getChildComponents()) {
            panel.setPosition(new Point(panel.getBounds().x, -offsetY + heights));
            heights += panel.getBounds().height + 5;
        }
    }



    @Override
    public void mouseScrolled(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {
        if (scrollAmount > 0) offsetY -= 20;
        else if (scrollAmount < 0) offsetY += 20;
        if (offsetY < 0) offsetY = 0;
    }
}
