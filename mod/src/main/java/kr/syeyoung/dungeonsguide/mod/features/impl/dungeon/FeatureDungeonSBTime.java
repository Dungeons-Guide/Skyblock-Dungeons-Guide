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

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.text.DefaultTextHUDFeatureStyleFeature;
import kr.syeyoung.dungeonsguide.mod.features.text.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.text.NullTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextSpan;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;

import java.util.Collection;

public class FeatureDungeonSBTime extends TextHUDFeature {

    private final SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

    public FeatureDungeonSBTime() {
        super("Dungeon.HUDs", "Display Ingame Dungeon Time", "Display how much time skyblock thinks has passed since dungeon run started", "dungeon.stats.igtime");
        this.setEnabled(false);
        registerDefaultStyle("title", DefaultingDelegatingTextStyle.derive(() -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.NAME)));
        registerDefaultStyle("discriminator", DefaultingDelegatingTextStyle.ofDefault().setTextShader(new AColor(0xAA,0xAA,0xAA,255)).setBackgroundShader(new AColor(0, 0,0,0)));
        registerDefaultStyle("separator", DefaultingDelegatingTextStyle.ofDefault().setTextShader(new AColor(0x55, 0x55,0x55,255)).setBackgroundShader(new AColor(0, 0,0,0)));
        registerDefaultStyle("number", DefaultingDelegatingTextStyle.derive(() -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.VALUE)));
    }

    public int getTimeElapsed() {
        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        Collection<Score> scores = scoreboard.getSortedScores(objective);
        String time = "idkyet";
        for (Score sc:scores) {
            ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(sc.getPlayerName());
            String strippedLine = TextUtils.keepScoreboardCharacters(TextUtils.stripColor(ScorePlayerTeam.formatPlayerName(scorePlayerTeam, sc.getPlayerName()))).trim();
            if (strippedLine.startsWith("Time Elapsed: ")) {
                time = strippedLine.substring(14);
            }
        }
        time = time.replace(" ", "");
        int hour = time.indexOf('h') == -1 ? 0 : Integer.parseInt(time.substring(0, time.indexOf('h')));
        if (time.contains("h")) time = time.substring(time.indexOf('h') + 1);
        int minute = time.indexOf('m') == -1 ? 0 : Integer.parseInt(time.substring(0, time.indexOf('m')));
        if (time.contains("m")) time = time.substring(time.indexOf('m') + 1);
        int second = time.indexOf('s') == -1 ? 0 : Integer.parseInt(time.substring(0, time.indexOf('s')));

        int time2 = hour * 60 * 60 + minute * 60 + second;
        return time2 * 1000;
    }


    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon();
    }


    @Override
    public TextSpan getDummyText() {
        TextSpan dummyText = new TextSpan(new NullTextStyle(), "");
        dummyText.addChild(new TextSpan(getStyle("title"), "Time "));
        dummyText.addChild(new TextSpan(getStyle("discriminator"), "(Ig)"));
        dummyText.addChild(new TextSpan(getStyle("separator"), ": "));
        dummyText.addChild(new TextSpan(getStyle("number"), "-42h"));
        return dummyText;
    }

    @Override
    public TextSpan getText() {
        TextSpan actualBit = new TextSpan(new NullTextStyle(), "");
        actualBit.addChild(new TextSpan(getStyle("title"), "Time"));
        actualBit.addChild(new TextSpan(getStyle("discriminator"), "(Ig)"));
        actualBit.addChild(new TextSpan(getStyle("separator"), ": "));
        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        Collection<Score> scores = scoreboard.getSortedScores(objective);
        String time = "unknown";
        for (Score sc:scores) {
            ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(sc.getPlayerName());
            String strippedLine = TextUtils.keepScoreboardCharacters(TextUtils.stripColor(ScorePlayerTeam.formatPlayerName(scorePlayerTeam, sc.getPlayerName()))).trim();
            if (strippedLine.startsWith("Time Elapsed: ")) {
                time = strippedLine.substring(14);
            }
        }
        actualBit.addChild(new TextSpan(getStyle("number"), time));
        return actualBit;
    }

}
