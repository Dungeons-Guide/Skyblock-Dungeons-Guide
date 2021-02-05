package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import scala.actors.threadpool.Arrays;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FeatureDungeonScore extends TextHUDFeature {
    public FeatureDungeonScore() {
        super("Dungeon", "Display Current Score", "Calculate and Display current score\nThis data is from pure calculation and can be different from actual score.", "dungeon.stats.score", false, 200, getFontRenderer().FONT_HEIGHT * 4);
        this.setEnabled(false);
        parameters.put("verbose", new FeatureParameter<Boolean>("verbose", "Show each score instead of sum", "Skill: 100 Explore: 58 S->S+(5 tombs) instead of Score: 305", true, "boolean"));
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon();
    }

    private static final java.util.List<StyledText> dummyText=  new ArrayList<StyledText>();
    private static final java.util.List<StyledText> dummyText2=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("Score","scorename"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("305 ","score"));
        dummyText.add(new StyledText("(","brackets"));
        dummyText.add(new StyledText("S+","currentScore"));
        dummyText.add(new StyledText(")","brackets"));



        dummyText2.add(new StyledText("Skill","scorename"));
        dummyText2.add(new StyledText(": ","separator"));
        dummyText2.add(new StyledText("100 ","score"));
        dummyText2.add(new StyledText("(","brackets"));
        dummyText2.add(new StyledText("0 Deaths","etc"));
        dummyText2.add(new StyledText(")\n","brackets"));
        dummyText2.add(new StyledText("Explorer","scorename"));
        dummyText2.add(new StyledText(": ","separator"));
        dummyText2.add(new StyledText("99 ","score"));
        dummyText2.add(new StyledText("(","brackets"));
        dummyText2.add(new StyledText("Rooms O Secrets 39/40","etc"));
        dummyText2.add(new StyledText(")\n","brackets"));
        dummyText2.add(new StyledText("Time","scorename"));
        dummyText2.add(new StyledText(": ","separator"));
        dummyText2.add(new StyledText("100 ","score"));
        dummyText2.add(new StyledText("Bonus","scorename"));
        dummyText2.add(new StyledText(": ","separator"));
        dummyText2.add(new StyledText("0 ","score"));
        dummyText2.add(new StyledText("Total","scorename"));
        dummyText2.add(new StyledText(": ","separator"));
        dummyText2.add(new StyledText("299\n","score"));
        dummyText2.add(new StyledText("S","currentScore"));
        dummyText2.add(new StyledText("->","arrow"));
        dummyText2.add(new StyledText("S+ ","nextScore"));
        dummyText2.add(new StyledText("(","brackets"));;
        dummyText2.add(new StyledText("1 Required 1 crypt","required"));
        dummyText2.add(new StyledText(")","brackets"));

    }

    @Override
    public java.util.List<String> getUsedTextStyle() {
        return Arrays.asList(new String[] {
                "scorename", "separator", "score", "brackets", "etc", "currentScore", "arrow", "nextScore", "required"
        });
    }

    @Override
    public java.util.List<StyledText> getDummyText() {

        if (this.<Boolean>getParameter("verbose").getValue()) {return dummyText2;} else return dummyText;
    }

    @Override
    public java.util.List<StyledText> getText() {
        List<StyledText> actualBit = new ArrayList<StyledText>();

        ScoreCalculation score = calculateScore();
        if (score == null) return new ArrayList<StyledText>();
        int sum = score.time + score.skill + score.explorer + score.bonus;
        if (this.<Boolean>getParameter("verbose").getValue()) {
            actualBit.add(new StyledText("Skill","scorename"));
            actualBit.add(new StyledText(": ","separator"));
            actualBit.add(new StyledText(score.skill+" ","score"));
            actualBit.add(new StyledText("(","brackets"));
            actualBit.add(new StyledText(score.deaths+" Deaths","etc"));
            actualBit.add(new StyledText(")\n","brackets"));
            actualBit.add(new StyledText("Explorer","scorename"));
            actualBit.add(new StyledText(": ","separator"));
            actualBit.add(new StyledText(score.explorer+" ","score"));
            actualBit.add(new StyledText("(","brackets"));
            actualBit.add(new StyledText("Rooms "+(score.fullyCleared ? "O":"X")+ " Secrets "+score.secrets+"/"+score.totalSecrets+(score.totalSecretsKnown ? "": "?"),"etc"));
            actualBit.add(new StyledText(")\n","brackets"));
            actualBit.add(new StyledText("Time","scorename"));
            actualBit.add(new StyledText(": ","separator"));
            actualBit.add(new StyledText(score.time+" ","score"));
            actualBit.add(new StyledText("Bonus","scorename"));
            actualBit.add(new StyledText(": ","separator"));
            actualBit.add(new StyledText(score.bonus+" ","score"));
            actualBit.add(new StyledText("Total","scorename"));
            actualBit.add(new StyledText(": ","separator"));
            actualBit.add(new StyledText(sum+"\n","score"));
            actualBit.addAll(buildRequirement(score));
        } else {
            String letter = getLetter(sum);
            actualBit.add(new StyledText("Score","scorename"));
            actualBit.add(new StyledText(": ","separator"));
            actualBit.add(new StyledText(sum+" ","score"));
            actualBit.add(new StyledText("(","brackets"));
            actualBit.add(new StyledText(letter,"currentScore"));
            actualBit.add(new StyledText(")","brackets"));
        }

        return actualBit;
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

    public int getPercentage() {
        return skyblockStatus.getPercentage();
    }
    public int getCompleteRooms() {
        for (NetworkPlayerInfo networkPlayerInfoIn : Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap()) {
            String name = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
            if (name.startsWith("§r Completed Rooms: §r")) {
                String milestone = TextUtils.stripColor(name).substring(18);
                return Integer.parseInt(milestone);
            }
        }
        return 0;
    }
    public int getTotalRooms() {
        return (int) (100 * (getCompleteRooms() / (double)getPercentage()));
    }
    public int getUndiscoveredPuzzles() {
        int cnt = 0;
        for (NetworkPlayerInfo networkPlayerInfoIn : Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap()) {
            String name = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
            if (name.startsWith("§r ???: ")) {
                cnt ++;
            }
        }
        return cnt;
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
            int totalCompRooms= 0;
            boolean bossroomFound = false;
            for (DungeonRoom dungeonRoom : context.getDungeonRoomList()) {
                if (dungeonRoom.getColor() == 74) bossroomFound = true;
                if (dungeonRoom.getCurrentState() != DungeonRoom.RoomState.DISCOVERED)
                    totalCompRooms += dungeonRoom.getUnitPoints().size();
                if (dungeonRoom.getColor() == 66 && dungeonRoom.getCurrentState() == DungeonRoom.RoomState.DISCOVERED)
                    skill -= 10;
                if (dungeonRoom.getColor() == 74 && dungeonRoom.getCurrentState() == DungeonRoom.RoomState.DISCOVERED)
                    skill += 1;
                skill += dungeonRoom.getCurrentState().getScoreModifier();
            }
            if (!bossroomFound) skill += 1;
            skill -= getUndiscoveredPuzzles() * 10;
            skill -= (getTotalRooms() - totalCompRooms) * 4;
            skill = MathHelper.clamp_int(skill, 0, 100);
        }
        int explorer = 0;
        boolean fullyCleared = false;
        boolean totalSecretsKnown = true;
        int totalSecrets = 0;
        int secrets = 0;
        {
            int completed = 0;
            for (DungeonRoom dungeonRoom : context.getDungeonRoomList()) {
                if (dungeonRoom.getTotalSecrets() != -1)
                    totalSecrets += dungeonRoom.getTotalSecrets();
                else totalSecretsKnown = false;
                completed += dungeonRoom.getUnitPoints().size();
            }
            fullyCleared = completed >= getTotalRooms() && context.getMapProcessor().getUndiscoveredRoom() == 0;
            explorer += MathHelper.clamp_int((int) Math.floor(6.0 / 10.0 * getPercentage()), 0, 60);
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
            time = MathHelper.clamp_int((int) Math.floor(100 - 2.2 * timeModifier), 0, 100);
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
    public List<StyledText> buildRequirement(ScoreCalculation calculation) {
        List<StyledText> actualBit = new ArrayList<StyledText>();
        int current = calculation.time + calculation.bonus + calculation.explorer + calculation.skill;
        String currentLetter = getLetter(current);
        String nextLetter=  getNextLetter(currentLetter);
        if (nextLetter == null) {
            actualBit.add(new StyledText(nextLetter+" Expected","nextScore"));
            return actualBit;
        }
        int req = getScoreRequirement(nextLetter);
        int reqPT2 = req-  current;
        int reqPT = req - current;

        int tombsBreakable = Math.min(5 - calculation.tombs, reqPT);
        reqPT -= tombsBreakable;

        double secretPer = 40.0 / calculation.totalSecrets;
        int secrets = (int) Math.ceil(reqPT / secretPer);

        actualBit.add(new StyledText(currentLetter,"currentScore"));
        actualBit.add(new StyledText("->","arrow"));
        actualBit.add(new StyledText(nextLetter+" ","nextScore"));
        actualBit.add(new StyledText("(","brackets"));;
        actualBit.add(new StyledText(reqPT2+" required "+tombsBreakable+" crypt "+secrets+" secrets","required"));
        actualBit.add(new StyledText(")","brackets"));
        return actualBit;
    }
}
