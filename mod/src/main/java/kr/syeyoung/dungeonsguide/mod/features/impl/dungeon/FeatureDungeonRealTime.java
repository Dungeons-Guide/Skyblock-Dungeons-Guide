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


import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonLeftEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonStartedEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultTextHUDFeatureStyleFeature;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.NullTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextSpan;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;

public class FeatureDungeonRealTime extends TextHUDFeature {
    public FeatureDungeonRealTime() {
        super("Dungeon HUD.In Dungeon HUD", "Display Real Time-Dungeon Time", "Display how much real time has passed since dungeon run started", "dungeon.stats.realtime");
        this.setEnabled(false);
        registerDefaultStyle("title", DefaultingDelegatingTextStyle.derive("Feature Default - Title", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.NAME)));
        registerDefaultStyle("discriminator", DefaultingDelegatingTextStyle.derive("Feature Default - Discriminator", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.EXTRA_INFO)));
        registerDefaultStyle("separator", DefaultingDelegatingTextStyle.derive("Feature Default - Separator", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.SEPARATOR)));
        registerDefaultStyle("number", DefaultingDelegatingTextStyle.derive("Feature Default - Number", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.VALUE)));
    }

    private long started = -1;

    public long getTimeElapsed() {
        return System.currentTimeMillis() - started;
    }


    @Override
    public boolean isHUDViewable() {
        return started != -1;
    }


    @Override
    public TextSpan getDummyText() {
        TextSpan dummyText = new TextSpan(new NullTextStyle(), "");
        dummyText.addChild(new TextSpan(getStyle("title"), "Time "));
        dummyText.addChild(new TextSpan(getStyle("discriminator"), "(Real)"));
        dummyText.addChild(new TextSpan(getStyle("separator"), ": "));
        dummyText.addChild(new TextSpan(getStyle("number"), "-42h"));
        return dummyText;
    }

    @Override
    public TextSpan getText() {
        TextSpan actualBit = new TextSpan(new NullTextStyle(), "");
        actualBit.addChild(new TextSpan(getStyle("title"), "Time"));
        actualBit.addChild(new TextSpan(getStyle("discriminator"), "(Real)"));
        actualBit.addChild(new TextSpan(getStyle("separator"), ": "));
        actualBit.addChild(new TextSpan(getStyle("number"), TextUtils.formatTime(getTimeElapsed())));
        return actualBit;
    }

    @DGEventHandler
    public void onDungeonStart(DungeonStartedEvent event) {
        started= System.currentTimeMillis();
    }

    @DGEventHandler(ignoreDisabled = true)
    public void onDungeonQuit(DungeonLeftEvent event) {
        started = -1;
    }
}
