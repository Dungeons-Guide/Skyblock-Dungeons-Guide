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
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bossfight.BossfightProcessorSadan;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.entity.boss.BossStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureTerracotaTimer extends TextHUDFeature {
    public FeatureTerracotaTimer() {
        super("Bossfight.Floor 6", "Display Terracotta phase timer", "Displays Terracotta phase timer", "bossfight.terracota", true, getFontRenderer().getStringWidth("Terracottas: 1m 99s"), getFontRenderer().FONT_HEIGHT);
        this.setEnabled(true);
        getStyles().add(new TextStyle("title", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("time", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

    private static final List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("Terracottas","title"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("1m 99s","time"));
    }
    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon() && skyblockStatus.getContext() != null && skyblockStatus.getContext().getBossfightProcessor() instanceof BossfightProcessorSadan &&
                "fight-1".equalsIgnoreCase(skyblockStatus.getContext().getBossfightProcessor().getCurrentPhase());
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Arrays.asList("title", "separator", "number", "unit");
    }

    @Override
    public List<StyledText> getDummyText() {
        return dummyText;
    }

    @Override
    public List<StyledText> getText() {
        List<StyledText> actualBit = new ArrayList<StyledText>();
        actualBit.add(new StyledText("Terracottas","title"));
        actualBit.add(new StyledText(": ","separator"));
        actualBit.add(new StyledText(TextUtils.formatTime((long) (BossStatus.healthScale * 1000 * 60 * 1.5)),"time"));
        return actualBit;
    }

}
