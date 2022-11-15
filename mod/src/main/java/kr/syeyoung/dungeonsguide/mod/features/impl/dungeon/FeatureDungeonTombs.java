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

<<<<<<<< HEAD:mod/src/main/java/kr/syeyoung/dungeonsguide/features/impl/dungeon/FeatureDungeonTombs.java
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.features.text.TextStyle;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
========
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.features.text.StyledText;
import kr.syeyoung.dungeonsguide.mod.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.features.text.TextStyle;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
>>>>>>>> origin/breaking-changes-just-working-im-not-putting-all-of-these-into-3.0-but-for-the-sake-of-beta-release-this-thing-exists:mod/src/main/java/kr/syeyoung/dungeonsguide/mod/features/impl/dungeon/FeatureDungeonTombs.java
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureDungeonTombs extends TextHUDFeature {
    public FeatureDungeonTombs() {
        super("Dungeon.HUDs", "Display # of Crypts", "Display how much total crypts have been blown up in a dungeon run", "dungeon.stats.tombs", true, getFontRenderer().getStringWidth("Crypts: 42"), getFontRenderer().FONT_HEIGHT);
        this.setEnabled(false);
        getStyles().add(new TextStyle("title", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("number", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

    public int getTombsFound() {
        for (NetworkPlayerInfo networkPlayerInfoIn : Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap()) {
            String name = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
            if (name.startsWith("§r Crypts: §r§6")) {
                return Integer.parseInt(TextUtils.stripColor(name).substring(9));
            }
        }
        return 0;
    }

    private static final java.util.List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("Crypts","title"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("42","number"));
    }

    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon();
    }

    @Override
    public java.util.List<String> getUsedTextStyle() {
        return Arrays.asList("title", "separator", "number");
    }

    @Override
    public java.util.List<StyledText> getDummyText() {
        return dummyText;
    }

    @Override
    public java.util.List<StyledText> getText() {
        List<StyledText> actualBit = new ArrayList<StyledText>();
        actualBit.add(new StyledText("Crypts","title"));
        actualBit.add(new StyledText(": ","separator"));
        actualBit.add(new StyledText(getTombsFound()+"","number"));
        return actualBit;
    }

}
