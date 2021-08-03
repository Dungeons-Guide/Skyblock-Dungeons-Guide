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

package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.features.text.TextStyle;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FeatureWarnLowHealth extends TextHUDFeature {
    public FeatureWarnLowHealth() {
        super("Dungeon.Teammates", "Low Health Warning", "Warn if someone is on low health", "dungeon.lowhealthwarn", false, 500, 20);
        parameters.put("threshold", new FeatureParameter<Integer>("threshold", "Health Threshold", "Health Threshold for this feature to be toggled. default to 500", 500, "integer"));
        getStyles().add(new TextStyle("title", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("number", new AColor(0xFF, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("unit", new AColor(0xFF, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        setEnabled(false);
    }


    private final SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();


    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon();
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Arrays.asList("title", "separator", "number", "unit");
    }

    private static final java.util.List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("DungeonsGuide","title"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("500","number"));
        dummyText.add(new StyledText("hp","unit"));
    }

    @Override
    public List<StyledText> getDummyText() {
        return dummyText;
    }

    @Override
    public List<StyledText> getText() {
        String lowestHealthName = "";
        int lowestHealth = 999999999;
        Scoreboard scoreboard = Minecraft.getMinecraft().thePlayer.getWorldScoreboard();
        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        for (Score sc : scoreboard.getSortedScores(objective)) {
            ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(sc.getPlayerName());
            String line = ScorePlayerTeam.formatPlayerName(scorePlayerTeam, sc.getPlayerName()).trim();
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
        if (lowestHealth > this.<Integer>getParameter("threshold").getValue()) return new ArrayList<StyledText>();

        List<StyledText> actualBit = new ArrayList<StyledText>();
        actualBit.add(new StyledText(lowestHealthName,"title"));
        actualBit.add(new StyledText(": ","separator"));
        actualBit.add(new StyledText(lowestHealth+"","number"));
        actualBit.add(new StyledText("hp","unit"));
        return actualBit;
    }
}
