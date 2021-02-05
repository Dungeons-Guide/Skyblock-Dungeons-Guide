package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.events.DungeonEndedEvent;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.listener.*;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.lwjgl.opengl.GL11;
import scala.actors.threadpool.Arrays;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FeatureDungeonSBTime extends TextHUDFeature {

    private SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();

    public FeatureDungeonSBTime() {
        super("Dungeon", "Display Ingame Dungeon Time", "Display how much time skyblock thinks has passed since dungeon run started", "dungeon.stats.igtime", true, getFontRenderer().getStringWidth("Time(IG): 1h 59m 59s"), getFontRenderer().FONT_HEIGHT);
        this.setEnabled(false);
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

    private static final java.util.List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("Time","title"));
        dummyText.add(new StyledText("(Ig)","discriminator"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("-42h","number"));
    }

    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon();
    }

    @Override
    public java.util.List<String> getUsedTextStyle() {
        return Arrays.asList(new String[] {
                "title", "discriminator", "separator", "number"
        });
    }

    @Override
    public java.util.List<StyledText> getDummyText() {
        return dummyText;
    }

    @Override
    public java.util.List<StyledText> getText() {
        List<StyledText> actualBit = new ArrayList<StyledText>();
        actualBit.add(new StyledText("Time","title"));
        actualBit.add(new StyledText("(Ig)","discriminator"));
        actualBit.add(new StyledText(": ","separator"));
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
        actualBit.add(new StyledText(time,"number"));
        return actualBit;
    }

}
