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


import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonLeftEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonStartedEvent;
import kr.syeyoung.dungeonsguide.mod.features.text.StyledText;
import kr.syeyoung.dungeonsguide.mod.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.features.text.TextStyle;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureDungeonRealTime extends TextHUDFeature {
    public FeatureDungeonRealTime() {
        super("Dungeon.HUDs", "Display Real Time-Dungeon Time", "Display how much real time has passed since dungeon run started", "dungeon.stats.realtime");
        this.setEnabled(false);
        getStyles().add(new TextStyle("title", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("discriminator", new AColor(0xAA,0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("number", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
    }

    private long started = -1;

    public long getTimeElapsed() {
        return System.currentTimeMillis() - started;
    }

    private static final java.util.List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("Time","title"));
        dummyText.add(new StyledText("(Real)","discriminator"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("-42h","number"));
    }

    @Override
    public boolean isHUDViewable() {
        return started != -1;
    }

    @Override
    public java.util.List<String> getUsedTextStyle() {
        return Arrays.asList("title", "discriminator", "separator", "number");
    }

    @Override
    public java.util.List<StyledText> getDummyText() {
        return dummyText;
    }

    @Override
    public java.util.List<StyledText> getText() {
        List<StyledText> actualBit = new ArrayList<StyledText>();
        actualBit.add(new StyledText("Time","title"));
        actualBit.add(new StyledText("(Real)","discriminator"));
        actualBit.add(new StyledText(": ","separator"));
        actualBit.add(new StyledText(TextUtils.formatTime(getTimeElapsed()),"number"));
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
