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

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.features.text.TextStyle;
import kr.syeyoung.dungeonsguide.roomprocessor.bossfight.HealthData;
import kr.syeyoung.dungeonsguide.utils.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureBossHealth extends TextHUDFeature {
    public FeatureBossHealth() {
        super("Bossfight", "Display Boss Health(s)", "Show the health of boss and minibosses in bossfight (Guardians, Priests..)", "bossfight.health", false, getFontRenderer().getStringWidth("The Professor: 4242m"), getFontRenderer().FONT_HEIGHT * 5);
        this.setEnabled(true);
        addParameter("totalHealth", new FeatureParameter<Boolean>("totalHealth", "show total health", "Show total health along with current health", false, "boolean", nval -> totalHealth = nval));
        addParameter("formatHealth", new FeatureParameter<Boolean>("formatHealth", "format health", "1234568 -> 1m", true, "boolean", nval -> formatHealth = nval));
        addParameter("ignoreInattackable", new FeatureParameter<Boolean>("ignoreInattackable", "Don't show health of in-attackable enemy", "For example, do not show guardians health when they're not attackable", false, "boolean", nval -> ignoreInattackable = nval));

        getStyles().add(new TextStyle("title", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("health", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator2", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("maxHealth", new AColor(0x55, 0x55,0xFF,255), new AColor(0, 0,0,0), false));
    }


    boolean totalHealth;
    boolean formatHealth;
    boolean ignoreInattackable;

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();


    @Override
    public boolean doesScaleWithHeight() {
        return false;
    }

    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon() && skyblockStatus.getContext() != null && skyblockStatus.getContext().getBossfightProcessor() != null;
    }

    @Override
    public java.util.List<String> getUsedTextStyle() {
        return Arrays.asList("title", "separator", "health", "separator2", "maxHealth");
    }

    @Override
    public java.util.List<StyledText> getDummyText() {
        List<StyledText> actualBit = new ArrayList<StyledText>();
        addLine(new HealthData("The Professor", 3300000, 5000000, false), actualBit);
        addLine(new HealthData("Chaos Guardian", 500000, 2000000, true), actualBit);
        addLine(new HealthData("Healing Guardian", 1000000, 3000000, true), actualBit);
        addLine(new HealthData("Laser Guardian", 5000000, 5000000, true), actualBit);
        addLine(new HealthData("Giant", 10000000, 20000000, false), actualBit);
        return actualBit;
    }

    public void addLine(HealthData data, List<StyledText> actualBit) {
        if (ignoreInattackable && !data.isAttackable()) return;

        actualBit.add(new StyledText(data.getName(),"title"));
        actualBit.add(new StyledText(": ","separator"));
        actualBit.add(new StyledText( (formatHealth ? TextUtils.format(data.getHealth()) : data.getHealth()) + (totalHealth ? "" : "\n"),"health"));
        if (totalHealth) {
            actualBit.add(new StyledText("/", "separator2"));
            actualBit.add(new StyledText( (formatHealth ? TextUtils.format(data.getMaxHealth()) : data.getMaxHealth()) +"\n","maxHealth"));
        }
    }

    @Override
    public java.util.List<StyledText> getText() {
        List<StyledText> actualBit = new ArrayList<StyledText>();
        List<HealthData> healths = skyblockStatus.getContext().getBossfightProcessor().getHealths();
        for (HealthData heal : healths) {
            addLine(heal, actualBit);
        }
        return actualBit;
    }
}
