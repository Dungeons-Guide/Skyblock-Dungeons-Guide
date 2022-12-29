/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;

import java.util.function.Consumer;

public class ScoreBoardUtils {

    public static void forEachLine(Consumer<String> lineConsumer){
        Scoreboard scoreboard = Minecraft.getMinecraft().thePlayer.getWorldScoreboard();
        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        for (Score sc : scoreboard.getSortedScores(objective)) {
            ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(sc.getPlayerName());
            String line = ScorePlayerTeam.formatPlayerName(scorePlayerTeam, sc.getPlayerName()).trim();
            String stripped = TextUtils.keepScoreboardCharacters(TextUtils.stripColor(line));
//            if (line.contains("[") && line.endsWith("❤")) {
//                String name = stripped.split(" ")[stripped.split(" ").length - 2];
//                int health = Integer.parseInt(stripped.split(" ")[stripped.split(" ").length - 1]);
//                if (health < lowestHealth) {
//                    lowestHealth = health;
//                    lowestHealthName = name;
//                }
//            }

            lineConsumer.accept(line);

        }
    }

    public static void forEachLineClean(Consumer<String> lineConsumer){
        Scoreboard scoreboard = Minecraft.getMinecraft().thePlayer.getWorldScoreboard();
        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        for (Score sc : scoreboard.getSortedScores(objective)) {
            ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(sc.getPlayerName());
            String line = ScorePlayerTeam.formatPlayerName(scorePlayerTeam, sc.getPlayerName()).trim();
            String stripped = TextUtils.keepScoreboardCharacters(TextUtils.stripColor(line));
//            if (line.contains("[") && line.endsWith("❤")) {
//                String name = stripped.split(" ")[stripped.split(" ").length - 2];
//                int health = Integer.parseInt(stripped.split(" ")[stripped.split(" ").length - 1]);
//                if (health < lowestHealth) {
//                    lowestHealth = health;
//                    lowestHealthName = name;
//                }
//            }

            lineConsumer.accept(stripped);

        }
    }




}
