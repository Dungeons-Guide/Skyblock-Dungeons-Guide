package kr.syeyoung.modapi.paralleluniverse.scoreboard;

import net.kyori.adventure.text.Component;

public interface UScore {
    Component getPlayerName();
    int getScore();
    String getVisibleName();
    String getJustTeam();
}
