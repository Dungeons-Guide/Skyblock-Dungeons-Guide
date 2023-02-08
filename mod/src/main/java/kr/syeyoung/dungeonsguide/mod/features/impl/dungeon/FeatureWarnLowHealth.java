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

package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon;


import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.config.types.TCInteger;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.text.DefaultTextHUDFeatureStyleFeature;
import kr.syeyoung.dungeonsguide.mod.features.text.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.text.NullTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextSpan;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.Objective;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.Score;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.ScoreboardManager;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;


public class FeatureWarnLowHealth extends TextHUDFeature {
    public FeatureWarnLowHealth() {
        super("Dungeon.Teammates", "Low Health Warning", "Warn if someone is on low health", "dungeon.lowhealthwarn");
        addParameter("threshold", new FeatureParameter<Integer>("threshold", "Health Threshold", "Health Threshold for this feature to be toggled. default to 500", 500, TCInteger.INSTANCE));
        registerDefaultStyle("title", DefaultingDelegatingTextStyle.derive(() -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.NAME)));
        registerDefaultStyle("separator", DefaultingDelegatingTextStyle.derive(() -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.SEPARATOR)));
        registerDefaultStyle("number", DefaultingDelegatingTextStyle.ofDefault().setTextShader(new AColor(0xFF, 0x55,0x55,255)).setBackgroundShader(new AColor(0, 0,0,0)));
        registerDefaultStyle("unit", DefaultingDelegatingTextStyle.ofDefault().setTextShader(new AColor(0xFF, 0x55,0x55,255)).setBackgroundShader(new AColor(0, 0,0,0)));
        setEnabled(false);
    }


    private final SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();


    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon();
    }

    @Override
    public TextSpan getDummyText() {
        TextSpan dummyText = new TextSpan(new NullTextStyle(), "");
        dummyText.addChild(new TextSpan(getStyle("title"), "DungeonsGuide"));
        dummyText.addChild(new TextSpan(getStyle("separator"), ": "));
        dummyText.addChild(new TextSpan(getStyle("number"), "500"));
        dummyText.addChild(new TextSpan(getStyle("unit"), "hp"));
        return dummyText;
    }

    @Override
    public TextSpan getText() {
        String lowestHealthName = "";
        int lowestHealth = 999999999;
        Objective objective = ScoreboardManager.INSTANCE.getSidebarObjective();
        if (objective == null) return new TextSpan(new NullTextStyle(), "");
        for (Score sc : objective.getScores()) {
            String line = sc.getVisibleName();
            String stripped = TextUtils.keepScoreboardCharacters(TextUtils.stripColor(line));
            if (line.contains("[") && line.endsWith("❤")) {
                String name = stripped.split(" ")[stripped.split(" ").length - 2];
                int health = Integer.parseInt(stripped.split(" ")[stripped.split(" ").length - 1]);
                if (health < lowestHealth) {
                    lowestHealth = health;
                    lowestHealthName = name;
                }
            }
        }
        if (lowestHealth > this.<Integer>getParameter("threshold").getValue()) return new TextSpan(new NullTextStyle(), "");

        TextSpan actualBit = new TextSpan(new NullTextStyle(), "");
        actualBit.addChild(new TextSpan(getStyle("title"), lowestHealthName));
        actualBit.addChild(new TextSpan(getStyle("separator"), ": "));
        actualBit.addChild(new TextSpan(getStyle("number"), lowestHealth+""));
        actualBit.addChild(new TextSpan(getStyle("unit"), "hp"));
        return actualBit;
    }
}
