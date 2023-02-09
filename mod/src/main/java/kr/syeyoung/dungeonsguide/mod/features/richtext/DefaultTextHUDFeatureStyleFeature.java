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

package kr.syeyoung.dungeonsguide.mod.features.richtext;

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.config.types.TCRTextStyleMap;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.richtext.config.WidgetTextStyleConfig;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextSpan;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefaultTextHUDFeatureStyleFeature extends SimpleFeature {
    public enum Styles {
        DEFAULT, NAME, VALUE, SEPARATOR, FRACTION, EXTRA_INFO, TOTAL, BRACKET, WARNING
    }

    public DefaultTextHUDFeatureStyleFeature() {
        super("Misc", "Quick HUD Style Settings", "Configure the default hud style", "misc.defaulthud");

        registerDefaultStyle(Styles.DEFAULT, DefaultingDelegatingTextStyle.ofDefault("Global Text"));
        registerDefaultStyle(Styles.NAME, DefaultingDelegatingTextStyle.derive("Global Default - Name", () -> getStyle(Styles.DEFAULT)).setTextShader(new AColor(0x00, 0xAA,0xAA,255)));
        registerDefaultStyle(Styles.VALUE, DefaultingDelegatingTextStyle.derive("Global Default - Value", () -> getStyle(Styles.DEFAULT)).setTextShader(new AColor(0x55, 0xFF,0xFF,255)));
        registerDefaultStyle(Styles.SEPARATOR, DefaultingDelegatingTextStyle.derive("Global Default - Separator", () -> getStyle(Styles.DEFAULT)).setTextShader(new AColor(0x55, 0x55,0x55,255)));
        registerDefaultStyle(Styles.EXTRA_INFO, DefaultingDelegatingTextStyle.derive("Global Default - Extra Info", () -> getStyle(Styles.DEFAULT)).setTextShader(new AColor(0xAA,0xAA,0xAA,255)));
        registerDefaultStyle(Styles.BRACKET, DefaultingDelegatingTextStyle.derive("Global Default - Bracket", () -> getStyle(Styles.DEFAULT)).setTextShader(new AColor(0x55, 0x55,0x55,255)));
        registerDefaultStyle(Styles.WARNING, DefaultingDelegatingTextStyle.derive("Global Default - Warning", () -> getStyle(Styles.DEFAULT)).setTextShader(new AColor(0xFF, 0x69,0x17,255)));

        registerDefaultStyle(Styles.FRACTION, DefaultingDelegatingTextStyle.derive("Global Default - Fraction", () -> getStyle(Styles.SEPARATOR)));
        registerDefaultStyle(Styles.TOTAL, DefaultingDelegatingTextStyle.derive("Global Default - Total", () -> getStyle(Styles.VALUE)));
        addParameter("newstyle", new FeatureParameter<>("newstyle", "TextStyle", "", styleMap, new TCRTextStyleMap(), this::updateStyle)
                .setWidgetGenerator((param) -> new WidgetTextStyleConfig(getDummyText(), styleMap)));
    }

    public TextSpan getDummyText() {
        TextSpan rootSpan = new TextSpan(new NullTextStyle(), "");
        rootSpan.addChild(new TextSpan(getStyle(Styles.DEFAULT), "Default\n"));
        rootSpan.addChild(new TextSpan(getStyle(Styles.NAME), "Name"));
        rootSpan.addChild(new TextSpan(getStyle(Styles.SEPARATOR), ": "));
        rootSpan.addChild(new TextSpan(getStyle(Styles.VALUE), "Value"));
        rootSpan.addChild(new TextSpan(getStyle(Styles.FRACTION), "/"));
        rootSpan.addChild(new TextSpan(getStyle(Styles.TOTAL), "Total "));
        rootSpan.addChild(new TextSpan(getStyle(Styles.BRACKET), "("));
        rootSpan.addChild(new TextSpan(getStyle(Styles.EXTRA_INFO), "Misc Info"));
        rootSpan.addChild(new TextSpan(getStyle(Styles.BRACKET), ")\n"));
        rootSpan.addChild(new TextSpan(getStyle(Styles.EXTRA_INFO), "Additional Data\n"));
        rootSpan.addChild(new TextSpan(getStyle(Styles.WARNING), "WARNING!!"));
        return rootSpan;
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
            newStyle.setName("User Setting of "+defaultStyleMap.get(stringDefaultingDelegatingTextStyleEntry.getKey()).name);
            newStyle.setParent(() -> defaultStyleMap.get(stringDefaultingDelegatingTextStyleEntry.getKey()));
            styleMap.put(stringDefaultingDelegatingTextStyleEntry.getKey(), newStyle);
        }
        for (String s : needsToBeIn) {
            styleMap.put(s, DefaultingDelegatingTextStyle.derive("User Setting of "+defaultStyleMap.get(s).name, () -> defaultStyleMap.get(s)));
            map.put(s, styleMap.get(s));
        }
    }
}
