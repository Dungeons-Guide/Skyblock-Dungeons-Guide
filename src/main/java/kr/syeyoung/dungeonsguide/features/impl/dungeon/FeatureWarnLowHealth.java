package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import com.google.common.base.Predicate;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.WorldRenderListener;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import scala.actors.threadpool.Arrays;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class FeatureWarnLowHealth extends TextHUDFeature {
    public FeatureWarnLowHealth() {
        super("Dungeon", "Low Health Warning", "Warn if someone is on low health", "dungeon.lowhealthwarn", false, 200, 50);
        parameters.put("threshold", new FeatureParameter<Integer>("threshold", "Health Threshold", "Health Threshold for this feature to be toggled. default to 500", 500, "integer"));
    }


    private SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();


    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon();
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Arrays.asList(new String[] {
                "title", "separator", "number", "unit"
        });
    }

    @Override
    public void drawDemo(float partialTicks) {
        FontRenderer fr = getFontRenderer();
        double scale = getFeatureRect().getHeight() / fr.FONT_HEIGHT;
        GlStateManager.scale(scale, scale, 0);
        super.drawDemo(partialTicks);
    }

    @Override
    public void drawHUD(float partialTicks) {
        FontRenderer fr = getFontRenderer();
        double scale = getFeatureRect().getHeight() / fr.FONT_HEIGHT;
        GlStateManager.scale(scale, scale, 0);
        super.drawHUD(partialTicks);
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
            if (line.endsWith("❤")) {
                String name = stripped.split(" ")[1];
                int health = Integer.parseInt(stripped.split(" ")[2]);
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
