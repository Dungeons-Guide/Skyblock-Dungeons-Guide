package kr.syeyoung.dungeonsguide.features.impl;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class FeatureDungeonScore extends GuiFeature {
    public FeatureDungeonScore() {
        super("Dungeon", "Display Current Score", "Calculate and Display current score\nThis data is from pure calculation and can be different from actual score.", "dungeon.stats.score", false, 200, getFontRenderer().FONT_HEIGHT * 4);
        this.setEnabled(false);
        parameters.put("color", new FeatureParameter<Color>("color", "Color", "Color of text", Color.orange, "color"));
        parameters.put("verbose", new FeatureParameter<Boolean>("verbose", "Show each score instead of sum", "Skill: 100 Explore: 58 S->S+(5 tombs) instead of Score: 305", true, "boolean"));
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
    @Override
    public void drawHUD(float partialTicks) {
        if (!skyblockStatus.isOnDungeon()) return;
        FontRenderer fr = getFontRenderer();
        ScoreCalculation score = calculateScore();
        if (score == null) return;
        int sum = score.time + score.skill + score.explorer + score.bonus;
        if (this.<Boolean>getParameter("verbose").getValue()) {
            String req = buildRequirement(score);
            int rgb = this.<Color>getParameter("color").getValue().getRGB();
            fr.drawString("Skill: "+score.skill+" ("+score.deaths+" Deaths)", 0, 0, rgb);
            fr.drawString("Explorer: "+score.explorer+" (Rooms "+(score.fullyCleared ? "O" : "X") + " secrets "+score.secrets+"/"+score.totalSecrets+(score.totalSecretsKnown ? "": "?")+")", 0, 8, rgb);
            fr.drawString("Time: "+score.time+" Bonus: "+score.bonus+" ::: Total: "+sum, 0, 16, rgb);
            fr.drawString(req, 0, 24, rgb);
        } else {
            double scale = getFeatureRect().getHeight() / fr.FONT_HEIGHT;
            GL11.glScaled(scale, scale, 0);
            String letter = getLetter(sum);
            fr.drawString("Score: "+sum + "("+letter+")", 0,0, this.<Color>getParameter("color").getValue().getRGB());
        }
   }

    @Override
    public void drawDemo(float partialTicks) {
        FontRenderer fr = getFontRenderer();
        if (this.<Boolean>getParameter("verbose").getValue()) {
            int rgb = this.<Color>getParameter("color").getValue().getRGB();
            fr.drawString("Skill: 100 (0 Deaths)", 0, 0, rgb);
            fr.drawString("Explorer: 99 (Rooms O 39/40)", 0, 8, rgb);
            fr.drawString("Time: 100 Bonus: 0 Total: 299", 0, 16, rgb);
            fr.drawString("S->S+ (1 Required 1 tomb)", 0, 24, rgb);
        } else {
            double scale = getFeatureRect().getHeight() / fr.FONT_HEIGHT;
            GL11.glScaled(scale, scale, 0);
            fr.drawString("Score: 305 (S+)", 0,0, this.<Color>getParameter("color").getValue().getRGB());
        }
    }

    @Data
    @AllArgsConstructor
    public static class ScoreCalculation {
        private int skill, explorer, time, bonus, tombs;
        private boolean fullyCleared;
        private int secrets, totalSecrets;
        private boolean totalSecretsKnown;
        private int deaths;
    }

    public ScoreCalculation calculateScore() {
        if (!skyblockStatus.isOnDungeon()) return null;
        DungeonContext context = skyblockStatus.getContext();
        if (context == null) return null;
        if (!context.getMapProcessor().isInitialized()) return null;

        int skill = 100;
        int deaths = 0;
        {
            deaths = FeatureRegistry.DUNGEON_DEATHS.getTotalDeaths();
            skill -= FeatureRegistry.DUNGEON_DEATHS.getTotalDeaths() * 2;
            for (DungeonRoom dungeonRoom : context.getDungeonRoomList()) {
                skill += dungeonRoom.getCurrentState().getScoreModifier();
            }
        }
        int explorer = 0;
        boolean fullyCleared = false;
        boolean totalSecretsKnown = true;
        int totalSecrets = 0;
        int secrets = 0;
        {
            int clearedRooms = 0;
            for (DungeonRoom dungeonRoom : context.getDungeonRoomList()) {
                if (!(dungeonRoom.getCurrentState() == DungeonRoom.RoomState.DISCOVERED)) {
                    clearedRooms ++;
                }
                if (dungeonRoom.getTotalSecrets() != -1)
                    totalSecrets += dungeonRoom.getTotalSecrets();
                else totalSecretsKnown = false;
            }
            fullyCleared = clearedRooms == context.getDungeonRoomList().size() && context.getMapProcessor().getUndiscoveredRoom() == 0;
            explorer += MathHelper.clamp_int((int) Math.floor(60 * (clearedRooms / ((double)context.getDungeonRoomList().size() + context.getMapProcessor().getUndiscoveredRoom()))), 0, 60);
            explorer += MathHelper.clamp_int((int) Math.floor(40 * ((secrets = FeatureRegistry.DUNGEON_SECRETS.getSecretsFound()) / (double)totalSecrets)),0,40);
        }
        int time = 0;
        {
            double timeModifier;
            if (context.getBossRoomEnterSeconds() != -1) {
                timeModifier = Math.max(0, context.getBossRoomEnterSeconds() - 1200);
            } else {
                timeModifier = Math.max(0, FeatureRegistry.DUNGEON_SBTIME.getTimeElapsed() / 1000 - 1200);
            }
            time = (int) Math.floor(100 - 2.2 * timeModifier);
        }
        int bonus = 0;
        int tombs;
        {
            bonus += tombs = MathHelper.clamp_int(FeatureRegistry.DUNGEON_TOMBS.getTombsFound(), 0, 5);
        }

        // amazing thing
        return new ScoreCalculation(skill, explorer, time, bonus, tombs, fullyCleared, secrets, totalSecrets, totalSecretsKnown, deaths);
    }
    public String getLetter(int score) {
        if (score <= 99) return "D";
        if (score <= 159) return "C";
        if (score <= 229) return "B";
        if (score <= 269) return "A";
        if (score <= 299) return "S";
        return "S+";
    }
    public int getScoreRequirement(String letter) {
        if (letter.equals("D")) return 0;
        if (letter.equals("C")) return 100;
        if (letter.equals("B")) return 160;
        if (letter.equals("A")) return 230;
        if (letter.equals("S")) return 270;
        if (letter.equals("S+")) return 300;
        return -1;
    }
    public String getNextLetter(String letter) {
        if (letter.equals("D")) return "C";
        if (letter.equals("C")) return "B";
        if (letter.equals("B")) return "A";
        if (letter.equals("A")) return "S";
        if (letter.equals("S")) return "S+";
        else return null;
    }
    public String buildRequirement(ScoreCalculation calculation) {
        int current = calculation.time + calculation.bonus + calculation.explorer + calculation.skill;
        String currentLetter = getLetter(current);
        String nextLetter=  getNextLetter(currentLetter);
        if (nextLetter == null) return "S+ Expected";
        int req = getScoreRequirement(nextLetter);
        int reqPT2 = req-  current;
        int reqPT = req - current;

        int tombsBreakable = Math.min(5 - calculation.tombs, reqPT);
        reqPT -= tombsBreakable;

        double secretPer = 40.0 / calculation.totalSecrets;
        int secrets = (int) Math.ceil(reqPT / secretPer);

        return currentLetter+"->"+nextLetter+" ("+reqPT2+" Req "+tombsBreakable+" crypts "+secrets+" secrets"+(calculation.totalSecretsKnown ? "" : "?")+")";
    }
}
