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


import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.features.text.TextHUDFeature;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureCurrentPhase extends TextHUDFeature {
    public FeatureCurrentPhase() {
        super("Dungeon.Bossfight", "Display Current Phase", "Displays the current phase of bossfight", "bossfight.phasedisplay", false, getFontRenderer().getStringWidth("Current Phase: fight-2-idk-howlng"), getFontRenderer().FONT_HEIGHT);
        this.setEnabled(true);
        getStyles().add(new TextStyle("title", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("phase", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

    private static final List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("Current Phase","title"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("fight-2","phase"));
    }
    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon() && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() != null && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBossfightProcessor() != null;
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Arrays.asList("title", "separator", "phase");
    }

    @Override
    public List<StyledText> getDummyText() {
        return dummyText;
    }

    @Override
    public List<StyledText> getText() {
        String currentPhsae = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBossfightProcessor().getCurrentPhase();
        List<StyledText> actualBit = new ArrayList<StyledText>();
        actualBit.add(new StyledText("Current Phase","title"));
        actualBit.add(new StyledText(": ","separator"));
        actualBit.add(new StyledText(currentPhsae,"phase"));
        return actualBit;
    }

}
