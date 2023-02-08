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

package kr.syeyoung.dungeonsguide.mod.features.impl.boss;


import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.config.types.TCBoolean;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.HealthData;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.text.DefaultTextHUDFeatureStyleFeature;
import kr.syeyoung.dungeonsguide.mod.features.text.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.text.NullTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextSpan;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;

import java.util.List;

public class FeatureBossHealth extends TextHUDFeature {
    public FeatureBossHealth() {
        super("Dungeon.Bossfight", "Display Boss(es) Health", "Show the health of boss and minibosses in bossfight (Guardians, Priests..)", "bossfight.health");
        this.setEnabled(true);
        addParameter("totalHealth", new FeatureParameter<Boolean>("totalHealth", "show total health", "Show total health along with current health", false, TCBoolean.INSTANCE, nval -> totalHealth = nval));
        addParameter("formatHealth", new FeatureParameter<Boolean>("formatHealth", "format health", "1234568 -> 1m", true, TCBoolean.INSTANCE, nval -> formatHealth = nval));
        addParameter("ignoreInattackable", new FeatureParameter<Boolean>("ignoreInattackable", "Don't show health of in-attackable enemy", "For example, do not show guardians health when they're not attackable", false, TCBoolean.INSTANCE, nval -> ignoreInattackable = nval));

        registerDefaultStyle("title", DefaultingDelegatingTextStyle.derive(() -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.NAME)));
        registerDefaultStyle("separator", DefaultingDelegatingTextStyle.derive(() -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.SEPARATOR)));
        registerDefaultStyle("health", DefaultingDelegatingTextStyle.derive(() -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.VALUE)));
        registerDefaultStyle("separator2", DefaultingDelegatingTextStyle.derive(() -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.FRACTION)));
        registerDefaultStyle("maxHealth", DefaultingDelegatingTextStyle.derive(() -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.TOTAL)));
    }


    boolean totalHealth;
    boolean formatHealth;
    boolean ignoreInattackable;


    @Override
    public boolean isHUDViewable() {
        return SkyblockStatus.isOnDungeon() && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() != null && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBossfightProcessor() != null;
    }

    @Override
    public TextSpan getDummyText() {
        TextSpan actualBit = new TextSpan(new NullTextStyle(), "");
        addLine(new HealthData("The Professor", 3300000, 5000000, false), actualBit);
        addLine(new HealthData("Chaos Guardian", 500000, 2000000, true), actualBit);
        addLine(new HealthData("Healing Guardian", 1000000, 3000000, true), actualBit);
        addLine(new HealthData("Laser Guardian", 5000000, 5000000, true), actualBit);
        addLine(new HealthData("Giant", 10000000, 20000000, false), actualBit);
        return actualBit;
    }

    public void addLine(HealthData data, TextSpan actualBit) {
        if (ignoreInattackable && !data.isAttackable()) return;

        actualBit.addChild(new TextSpan(getStyle("title"), data.getName()));
        actualBit.addChild(new TextSpan(getStyle("separator"), ": "));
        actualBit.addChild(new TextSpan(getStyle("health"), (formatHealth ? TextUtils.format(data.getHealth()) : data.getHealth()) + (totalHealth ? "" : "\n")));
        if (totalHealth) {
            actualBit.addChild(new TextSpan(getStyle("separator2"), "/"));
            actualBit.addChild(new TextSpan(getStyle("maxHealth"), (formatHealth ? TextUtils.format(data.getMaxHealth()) : data.getMaxHealth()) +"\n"));
        }
    }

    @Override
    public TextSpan getText() {
        TextSpan actualBit = new TextSpan(new NullTextStyle(), "");
        List<HealthData> healths = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBossfightProcessor().getHealths();
        for (HealthData heal : healths) {
            addLine(heal, actualBit);
        }
        return actualBit;
    }
}
