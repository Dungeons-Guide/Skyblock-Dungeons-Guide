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

package kr.syeyoung.dungeonsguide.features.impl.advanced;

import com.google.common.base.Supplier;
import kr.syeyoung.dungeonsguide.config.guiconfig.ConfigPanelCreator;
import kr.syeyoung.dungeonsguide.config.guiconfig.MFeatureEdit;
import kr.syeyoung.dungeonsguide.config.guiconfig.MParameterEdit;
import kr.syeyoung.dungeonsguide.config.guiconfig.RootConfigPanel;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.MLabel;

public class FeatureDebug extends SimpleFeature {
    public FeatureDebug() {
        super("Advanced", "Debug", "Toggles debug mode", "debug", false);
        parameters.put("Key", new FeatureParameter<String>("Key", "Secret Key given by syeyoung", "Put the debug enable key here to enable debug mode", "","string"));
    }
    @Override
    public boolean isEnabled() {
        return "just hide it".equals(this.<String>getParameter("Key").getValue());
    }
    @Override
    public boolean isDisyllable() {
        return false;
    }

    @Override
    public String getEditRoute(RootConfigPanel rootConfigPanel) {
        ConfigPanelCreator.map.put("base." + getKey() , new Supplier<MPanel>() {
            @Override
            public MPanel get() {
                MFeatureEdit featureEdit = new MFeatureEdit(FeatureDebug.this, rootConfigPanel);
                for (FeatureParameter parameter: getParameters()) {
                    featureEdit.addParameterEdit(parameter.getKey(), new MParameterEdit(FeatureDebug.this, parameter, rootConfigPanel));
                }
                featureEdit.addParameterEdit("IsEnabled", new MParameterEdit(FeatureDebug.this, new FeatureParameter("Key Status", "Key Status", "Key Enabled? Or not?", "", "idk"), rootConfigPanel, new MLabel() {
                    @Override
                    public String getText() {
                        return isEnabled() ? "Enabled!" : "Incorrect Key";
                    }
                }, (a) -> false));
                return featureEdit;
            }
        });
        return "base." + getKey() ;
    }
}
