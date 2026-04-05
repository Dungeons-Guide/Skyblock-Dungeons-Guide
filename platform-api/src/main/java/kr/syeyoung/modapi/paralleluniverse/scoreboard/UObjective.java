package kr.syeyoung.modapi.paralleluniverse.scoreboard;

import net.kyori.adventure.text.Component;

import java.util.SortedSet;

public interface UObjective {
    String getObjectiveName();
    Component getDisplayName();

    SortedSet<? extends UScore> getScores();

    String getRenderType();
}
