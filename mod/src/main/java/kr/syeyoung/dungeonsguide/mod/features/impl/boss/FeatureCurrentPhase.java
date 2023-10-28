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
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultTextHUDFeatureStyleFeature;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.NullTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextSpan;

public class FeatureCurrentPhase extends TextHUDFeature {
    public FeatureCurrentPhase() {
        super("Bossfight", "Display Current Phase", "Displays the current phase of bossfight", "bossfight.phasedisplay");
        this.setEnabled(true);
        registerDefaultStyle("title", DefaultingDelegatingTextStyle.derive("Feature Default - Title", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.NAME)));
        registerDefaultStyle("separator", DefaultingDelegatingTextStyle.derive("Feature Default - Separator", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.SEPARATOR)));
        registerDefaultStyle("phase", DefaultingDelegatingTextStyle.derive("Feature Default - Phase", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.VALUE)));
    }

    @Override
    public boolean isHUDViewable() {
        return SkyblockStatus.isOnDungeon() && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() != null && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBossfightProcessor() != null;
    }

    @Override
    public TextSpan getDummyText() {
        TextSpan textSpan = new TextSpan(new NullTextStyle(), "");
        textSpan.addChild(new TextSpan(getStyle("title"), "Current Phase"));
        textSpan.addChild(new TextSpan(getStyle("separator"), ": "));
        textSpan.addChild(new TextSpan(getStyle("phase"), "fight-2"));
        return textSpan;
    }

    @Override
    public TextSpan getText() {
        String currentPhase = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBossfightProcessor().getCurrentPhase();
        TextSpan actualBit = new TextSpan(new NullTextStyle(), "");
        actualBit.addChild(new TextSpan(getStyle("title"), "Current Phase"));
        actualBit.addChild(new TextSpan(getStyle("separator"), ": "));
        actualBit.addChild(new TextSpan(getStyle("phase"), currentPhase));
        return actualBit;
    }

}
