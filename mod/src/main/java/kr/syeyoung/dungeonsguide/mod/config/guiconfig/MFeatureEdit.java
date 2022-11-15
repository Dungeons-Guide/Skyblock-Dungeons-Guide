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

package kr.syeyoung.dungeonsguide.mod.config.guiconfig;


import kr.syeyoung.dungeonsguide.mod.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MButton;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MList;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MModalConfirmation;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MFeatureEdit extends MPanel {
    private MList list;
    private MButton goBack, resetToDefault;
    private RootConfigPanel rootConfigPanel;
    private AbstractFeature abstractFeature;

    private Map<String, MPanel> parameterEdits = new HashMap<>();

    public MFeatureEdit(AbstractFeature abstractFeature, RootConfigPanel rootConfigPanel) {
        this.abstractFeature = abstractFeature;
        this.rootConfigPanel = rootConfigPanel;
        list = new MList();
        list.setGap(5);
        list.setDrawLine(false);
        add(list);

        goBack = new MButton();
        goBack.setText("< Go Back");
        goBack.setOnActionPerformed(rootConfigPanel::goBack);
        add(goBack);
        resetToDefault = new MButton();
        resetToDefault.setText("Reset To Default");
        resetToDefault.setForeground(Color.red);
        resetToDefault.setOnActionPerformed(() -> {
            openResetConfirmation();
        });
        add(resetToDefault);
    }

    public void openResetConfirmation() {
        MModalConfirmation mModal = new MModalConfirmation("Are you sure?",
                "Resetting to default will reset your configuration for the selected feature to default",
                () -> {
            for (FeatureParameter parameter : abstractFeature.getParameters()) {
                parameter.setToDefault();
            }
            abstractFeature.onParameterReset();
            rootConfigPanel.invalidatePage(abstractFeature.getEditRoute(rootConfigPanel));
            }, () -> {});
        mModal.setScale(getScale());
        mModal.getYes().setBorder(0xFFFF0000);
        mModal.getYes().setText("Yes, Reset it");
        mModal.getNo().setText("Cancel");
        mModal.open(MFeatureEdit.this);
    }

    public void addParameterEdit(String name, MPanel paramEdit) {
        parameterEdits.put(name, paramEdit);
        list.add(paramEdit);
    }
    public MPanel removeParameterEdit(String name) {
        MPanel panel = parameterEdits.remove(name);
        list.remove(panel);
        return panel;
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
        goBack.setBounds(new Rectangle(5,5,75,15));
        resetToDefault.setBounds(new Rectangle(bounds.width - 105, 5, 100, 15));

        list.setBounds(new Rectangle(5,25,bounds.width - 10, bounds.height - 10));
        list.realignChildren();

    }

    @Override
    public Dimension getPreferredSize() {
        Dimension listPref = list.getPreferredSize();
        return new Dimension(listPref.width + 10, listPref.height + 30);
    }
}
