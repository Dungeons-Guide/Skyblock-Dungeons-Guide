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

import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import lombok.Getter;

public class FeatureDebug extends SimpleFeature {
    @Getter
    private static Boolean trapfix;

    public FeatureDebug() {
        super("Advanced", "Debug", "Toggles debug mode", "debug", false);
//        addParameter("Key", new FeatureParameter<String>("Key", "Secret Key given by syeyoung", "Put the debug enable key here to enable debug mode", "", "string"));

        addParameter("swtich", new FeatureParameter<>("swtich", "Enable Debug", "Enables debug mode", false, "boolean"));


        addParameter("TrapRoomFix", new FeatureParameter<>("TrapRoomFix", "Enable trap", "trap ", false, "boolean", nval -> this.trapfix = nval));
    }


    @Override
    public boolean isEnabled() {
        return this.<Boolean>getParameter("swtich").getValue();
    }


//    @Override
//    public boolean isDisyllable() {
//        return false;
//    }

//    @Override
//    public String getEditRoute(RootConfigPanel rootConfigPanel) {
//        ConfigPanelCreator.map.put("base." + getKey(), () -> {
//            MFeatureEdit featureEdit = new MFeatureEdit(FeatureDebug.this, rootConfigPanel);
//            for (FeatureParameter<?> parameter : getParameters()) {
//                featureEdit.addParameterEdit(parameter.getKey(), new MParameterEdit(FeatureDebug.this, parameter, rootConfigPanel));
//            }
//            featureEdit.addParameterEdit("IsEnabled",
//                    new MParameterEdit(
//                            FeatureDebug.this,
//                            new FeatureParameter<>("Key Status", "Key Status", "Key Enabled? Or not?", "", "idk"),
//                            rootConfigPanel,
//                            new MLabel() {
//                                @Override
//                                public String getText() {
//                                    return isEnabled() ? "Enabled!" : "Incorrect Key";
//                                }
//                            },
//                            (a) -> false
//                    )
//            );
//            return featureEdit;
//        });
//        return "base." + getKey();
//    }
}
