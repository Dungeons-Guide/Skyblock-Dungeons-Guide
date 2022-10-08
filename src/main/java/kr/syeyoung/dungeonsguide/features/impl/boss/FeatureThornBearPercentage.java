/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.features.impl.boss;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.features.text.TextStyle;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bossfight.BossfightProcessorThorn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureThornBearPercentage extends TextHUDFeature {
    public FeatureThornBearPercentage() {
        super("Bossfight.Floor 4", "Display Spirit Bear Summon Percentage", "Displays spirit bear summon percentage in hud", "bossfight.spiritbear", true, getFontRenderer().getStringWidth("Spirit Bear: 100%"), getFontRenderer().FONT_HEIGHT);
        this.setEnabled(true);
        getStyles().add(new TextStyle("title", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("number", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("unit", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

    private static final java.util.List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("Spirit Bear","title"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("50","number"));
        dummyText.add(new StyledText("%","unit"));
    }
    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon() && skyblockStatus.getContext() != null && skyblockStatus.getContext().getBossfightProcessor() instanceof BossfightProcessorThorn;
    }

    @Override
    public java.util.List<String> getUsedTextStyle() {
        return Arrays.asList("title", "separator", "number", "unit");
    }

    @Override
    public java.util.List<StyledText> getDummyText() {
        return dummyText;
    }

    @Override
    public java.util.List<StyledText> getText() {
        int percentage = (int) (((BossfightProcessorThorn) skyblockStatus.getContext().getBossfightProcessor()).calculatePercentage() * 100);
        List<StyledText> actualBit = new ArrayList<StyledText>();
        actualBit.add(new StyledText("Spirit Bear","title"));
        actualBit.add(new StyledText(": ","separator"));
        actualBit.add(new StyledText(percentage+"","number"));
        actualBit.add(new StyledText("%","unit"));
        return actualBit;
    }

}
