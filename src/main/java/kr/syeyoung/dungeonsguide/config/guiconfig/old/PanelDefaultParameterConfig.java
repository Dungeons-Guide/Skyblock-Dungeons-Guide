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

import kr.syeyoung.dungeonsguide.config.guiconfig.old.GuiConfig;
import kr.syeyoung.dungeonsguide.config.guiconfig.old.MParameter;
import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.MTooltip;
import kr.syeyoung.dungeonsguide.gui.elements.MTooltipText;

import java.util.List;
import java.awt.*;
import java.util.Arrays;
import java.util.Set;

public class PanelDefaultParameterConfig extends MPanel {

    private AbstractFeature feature;

    @Override
    public void onBoundsUpdate() {
        for (MPanel childComponent : getChildComponents()) {
            childComponent.setSize(new Dimension(getBounds().width - 10, childComponent.getSize().height));
        }
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(0,0,parentWidth, parentHeight));
    }

    private final GuiConfig config;
    public PanelDefaultParameterConfig(final GuiConfig config, AbstractFeature feature, List<MPanel> pre, Set<String> ignore) {
        this.config = config;
        for (MPanel mPanel : pre) {
            add(mPanel);
        }
        for (FeatureParameter parameter: feature.getParameters()) {
            if (ignore.contains(parameter.getKey())) continue;
            add(new MParameter(feature, parameter, config));
        }
        setBackgroundColor(new Color(38, 38, 38, 255));
    }


    MPanel within;
    MTooltip mTooltip;
    MParameter lastWithin;
    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        int heights = 0;
        within = null;
        for (MPanel panel:getChildComponents()) {
            panel.setPosition(new Point(5, -offsetY + heights + 5));
            heights += panel.getBounds().height;

            if (panel.getBounds().contains(relMousex0,relMousey0)) within = panel;
        }
        if (within instanceof MParameter) {
            FeatureParameter feature = ((MParameter) within).getParameter();
            if (lastWithin != within) {
                if (mTooltip != null) mTooltip.close();
                    mTooltip = new MTooltipText(Arrays.asList(feature.getDescription().split("\n")));
                mTooltip.open(this);
            }
        } else {
            if (mTooltip != null) {
                mTooltip.close();
                mTooltip = null;
            }
        }
    }


    public int offsetY = 0;

    @Override
    public void mouseScrolled(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {
        for (MPanel childComponent : getChildComponents()) {
            if (!(childComponent instanceof MParameter) && childComponent.getBounds().contains(relMouseX0, relMouseY0)) return;
        }
        if (scrollAmount > 0) offsetY -= 20;
        else if (scrollAmount < 0) offsetY += 20;
        if (offsetY < 0) offsetY = 0;
    }

}
