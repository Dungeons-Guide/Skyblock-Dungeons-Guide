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

package kr.syeyoung.dungeonsguide.features.impl.secret.mechanicbrowser;

import kr.syeyoung.dungeonsguide.gui.MGui;
import lombok.Getter;

public class MGuiMechanicBrowser extends MGui {
    private FeatureMechanicBrowse featureMechanicBrowse;
    @Getter
    private PanelMechanicBrowser panelMechanicBrowser;
    public MGuiMechanicBrowser(FeatureMechanicBrowse mechanicBrowse) {
        this.featureMechanicBrowse = mechanicBrowse;
        panelMechanicBrowser = new PanelMechanicBrowser(mechanicBrowse);
        getMainPanel().add(panelMechanicBrowser);
    }

    @Override
    public void initGui() {
        super.initGui();
        panelMechanicBrowser.setBounds(featureMechanicBrowse.getFeatureRect().getRectangle());
        panelMechanicBrowser.setScale(featureMechanicBrowse.getScale());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
