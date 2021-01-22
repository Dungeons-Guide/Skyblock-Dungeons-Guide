package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import com.google.common.base.Predicate;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.WorldRenderListener;
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

import java.awt.*;
import java.util.List;


public class FeatureWarnLowHealth extends GuiFeature {
    public FeatureWarnLowHealth() {
        super("Dungeon", "Low Health Warning", "Warn if someone is on low health", "dungeon.lowhealthwarn", false, 200, 50);
        parameters.put("color", new FeatureParameter<Color>("color", "Color", "Color of playername", Color.yellow, "color"));
        parameters.put("threshold", new FeatureParameter<Integer>("threshold", "Health Threshold", "Health Threshold for this feature to be toggled. default to 500", 500, "integer"));

    }


    private SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();

    @Override
    public void drawDemo(float partialTicks) {
        FontRenderer fr = getFontRenderer();
        double scale = getFeatureRect().getHeight() / fr.FONT_HEIGHT;
        GlStateManager.scale(scale, scale, 0);
        fr.drawString("DungeonsGuide: ", 0,0,this.<Color>getParameter("color").getValue().getRGB());
        fr.drawString("500hp", fr.getStringWidth("DungeonsGuide: "), 0, Color.red.getRGB());
    }

    @Override
    public void drawHUD(float partialTicks) {
        FontRenderer fr = getFontRenderer();
        double scale = getFeatureRect().getHeight() / fr.FONT_HEIGHT;
        GlStateManager.scale(scale, scale, 0);
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
        if (lowestHealth > this.<Integer>getParameter("threshold").getValue()) return;
        fr.drawString(lowestHealthName+": ", 0,0,this.<Color>getParameter("color").getValue().getRGB());
        fr.drawString(lowestHealth+"hp", fr.getStringWidth(lowestHealthName+"DungeonsGuide: "), 0, Color.red.getRGB());
    }
}
