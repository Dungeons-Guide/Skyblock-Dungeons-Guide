package kr.syeyoung.dungeonsguide.features.impl;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.features.listener.TickListener;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Collection;

public class FeatureDungeonSBTime extends GuiFeature implements TickListener {
    public FeatureDungeonSBTime() {
        super("Dungeon", "Display Ingame Dungeon Time", "Display how much time skyblock thinks has passed since dungeon run started", "dungeon.stats.igtime", true, getFontRenderer().getStringWidth("Time(IG): 1h 59m 59s"), getFontRenderer().FONT_HEIGHT);
        this.setEnabled(false);
        parameters.put("color", new FeatureParameter<Color>("color", "Color", "Color of text", Color.orange, "color"));
    }

    private long started = -1;
    @Override
    public void drawHUD(float partialTicks) {
        if (started == -1) return;
        FontRenderer fr = getFontRenderer();
        double scale = getFeatureRect().getHeight() / fr.FONT_HEIGHT;
        GL11.glScaled(scale, scale, 0);
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
        fr.drawString("Time(Ig): "+time, 0,0, this.<Color>getParameter("color").getValue().getRGB());
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
    public void drawDemo(float partialTicks) {
        FontRenderer fr = getFontRenderer();
        double scale = getFeatureRect().getHeight() / fr.FONT_HEIGHT;
        GL11.glScaled(scale, scale, 0);

        fr.drawString("Time(Ig): -42h", 0,0, this.<Color>getParameter("color").getValue().getRGB());
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
    private boolean wasInDungeon = false;
    @Override
    public void onTick() {
        if (wasInDungeon && !skyblockStatus.isOnDungeon()) {
            if (skyblockStatus.isOnSkyblock()) started = -1;
        } else if (!wasInDungeon && skyblockStatus.isOnDungeon()) {
            started = System.currentTimeMillis();
        }
        wasInDungeon = skyblockStatus.isOnDungeon();
    }
}
