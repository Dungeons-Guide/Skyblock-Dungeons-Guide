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
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.BossfightProcessorThorn;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultTextHUDFeatureStyleFeature;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.NullTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextSpan;

public class FeatureThornBearPercentage extends TextHUDFeature {
    public FeatureThornBearPercentage() {
        super("Bossfight Helper.Floor 4", "Display Spirit Bear Summon Percentage", "Displays spirit bear summon percentage in hud", "bossfight.spiritbear");
        this.setEnabled(true);
        registerDefaultStyle("title", DefaultingDelegatingTextStyle.derive("Feature Default - Title", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.NAME)));
        registerDefaultStyle("separator", DefaultingDelegatingTextStyle.derive("Feature Default - Separator", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.SEPARATOR)));
        registerDefaultStyle("number", DefaultingDelegatingTextStyle.derive("Feature Default - Number", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.VALUE)));
        registerDefaultStyle("unit", DefaultingDelegatingTextStyle.derive("Feature Default - Unit", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.VALUE)));
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon() && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() != null && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBossfightProcessor() instanceof BossfightProcessorThorn;
    }
    @Override
    public TextSpan getDummyText() {
        TextSpan dummyText = new TextSpan(new NullTextStyle(), "");
        dummyText.addChild(new TextSpan(getStyle("title"), "Spirit Bear"));
        dummyText.addChild(new TextSpan(getStyle("separator"), ": "));
        dummyText.addChild(new TextSpan(getStyle("number"), "50"));
        dummyText.addChild(new TextSpan(getStyle("unit"), "%"));
        return dummyText;
    }

    @Override
    public TextSpan getText() {
        int percentage = (int) (((BossfightProcessorThorn) DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBossfightProcessor()).calculatePercentage() * 100);
        TextSpan actualBit = new TextSpan(new NullTextStyle(), "");
        actualBit.addChild(new TextSpan(getStyle("title"), "Spirit Bear"));
        actualBit.addChild(new TextSpan(getStyle("separator"), ": "));
        actualBit.addChild(new TextSpan(getStyle("number"), percentage+""));
        actualBit.addChild(new TextSpan(getStyle("unit"), "%"));
        return actualBit;
    }

}
