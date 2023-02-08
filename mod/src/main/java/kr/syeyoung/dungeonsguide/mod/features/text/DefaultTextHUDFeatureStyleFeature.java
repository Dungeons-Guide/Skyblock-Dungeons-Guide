/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.features.text;

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.config.types.TCRTextStyleMap;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefaultTextHUDFeatureStyleFeature extends SimpleFeature {
    public enum Styles {
        NAME, VALUE
    }

    public DefaultTextHUDFeatureStyleFeature() {
        super("Misc", "Quick HUD Style Settings", "Configure the default hud style", "misc.defaulthud");

        registerDefaultStyle(Styles.NAME, DefaultingDelegatingTextStyle.ofDefault().setTextShader(new AColor(0x00, 0xAA,0xAA,255)));
        registerDefaultStyle(Styles.VALUE, DefaultingDelegatingTextStyle.ofDefault().setTextShader(new AColor(0x55, 0xFF,0xFF,255)));
        addParameter("newstyle", new FeatureParameter<>("newstyle", "TextStyle", "", styleMap, new TCRTextStyleMap(), this::updateStyle));
    }

    public DefaultingDelegatingTextStyle getStyle(Styles styles) {
        return styleMap.get(styles.name());
    }


    private Map<String, DefaultingDelegatingTextStyle> defaultStyleMap = new HashMap<>();
    private Map<String, DefaultingDelegatingTextStyle> styleMap = new HashMap<>();
    public void registerDefaultStyle(Styles styles, DefaultingDelegatingTextStyle style) {
        defaultStyleMap.put(styles.name(), style);
    }
    public void updateStyle(Map<String, DefaultingDelegatingTextStyle> map) {
        styleMap.clear();
        Set<String> wasIn = new HashSet<>(map.keySet());
        Set<String> needsToBeIn = new HashSet<>(defaultStyleMap.keySet());
        needsToBeIn.removeAll(wasIn);
        for (Map.Entry<String, DefaultingDelegatingTextStyle> stringDefaultingDelegatingTextStyleEntry : map.entrySet()) {
            if (!defaultStyleMap.containsKey(stringDefaultingDelegatingTextStyleEntry.getKey())) continue;
            DefaultingDelegatingTextStyle newStyle = stringDefaultingDelegatingTextStyleEntry.getValue();
            newStyle.setParent(() -> defaultStyleMap.get(stringDefaultingDelegatingTextStyleEntry.getKey()));
            styleMap.put(stringDefaultingDelegatingTextStyleEntry.getKey(), newStyle);
        }
        for (String s : needsToBeIn) {
            styleMap.put(s, new DefaultingDelegatingTextStyle().setParent(() -> defaultStyleMap.get(s)));
            map.put(s, styleMap.get(s));
        }
    }
}
