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

package kr.syeyoung.dungeonsguide.config.guiconfig;

import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.MList;

import java.awt.*;

public class MPanelCategory extends MPanel {

    private NestedCategory key;
    private RootConfigPanel rootConfigPanel;

    private MList list;

    public MPanelCategory(NestedCategory nestedCategory, RootConfigPanel rootConfigPanel) {
        this.key = nestedCategory;
        this.rootConfigPanel = rootConfigPanel;

        list = new MList();
        list.setDrawLine(false);
        list.setGap(5);
        add(list);

        for (NestedCategory value : nestedCategory.children().values()) {
            list.add(new MCategory(value, rootConfigPanel));
        }
        if (nestedCategory.parent() != null) {
            String actualCategory = nestedCategory.categoryFull().substring(5);
            if (FeatureRegistry.getFeaturesByCategory().containsKey(actualCategory))
                for (AbstractFeature abstractFeature : FeatureRegistry.getFeaturesByCategory().get(actualCategory)) {
                    MFeature mFeature = new MFeature(abstractFeature, rootConfigPanel);
                    list.add(mFeature);
                    mFeature.setHover(new Color(94, 94, 94, 255));
                }
        }
        list.realignChildren();

    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        super.resize(parentWidth, parentHeight);
        setBounds(new Rectangle(0,0,parentWidth,parentHeight));
        Dimension prefSize = getPreferredSize();
        int hei = prefSize.height;
        setBounds(new Rectangle(0,0,parentWidth,hei));
    }

    @Override
    public void setBounds(Rectangle bounds) {
        super.setBounds(bounds);
        list.setBounds(new Rectangle(5,5,bounds.width- 10, bounds.height - 10));
        list.realignChildren();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension prefSize = list.getPreferredSize();
        int wid = prefSize.width + 10;
        int hei = prefSize.height + 10;
        return new Dimension(wid, hei);
    }
}
