package kr.syeyoung.dungeonsguide.utils;

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
