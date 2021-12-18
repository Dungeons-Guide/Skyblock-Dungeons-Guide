/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.events.StompConnectedEvent;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.listener.StompConnectedListener;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.features.text.TextStyle;
import kr.syeyoung.dungeonsguide.stomp.StompInterface;
import kr.syeyoung.dungeonsguide.stomp.StompMessageHandler;
import kr.syeyoung.dungeonsguide.stomp.StompPayload;
import kr.syeyoung.dungeonsguide.stomp.StompSubscription;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import kr.syeyoung.dungeonsguide.wsresource.StaticResource;
import kr.syeyoung.dungeonsguide.wsresource.StaticResourceCache;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.MathHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class FeatureDungeonScore extends TextHUDFeature {
    public FeatureDungeonScore() {
        super("Dungeon.Dungeon Information", "Display Current Score", "Calculate and Display current score\nThis data is from pure calculation and can be different from actual score.", "dungeon.stats.score", false, 200, getFontRenderer().FONT_HEIGHT * 4);
        this.setEnabled(false);
        parameters.put("verbose", new FeatureParameter<Boolean>("verbose", "Show each score instead of sum", "Skill: 100 Explore: 58 S->S+(5 tombs) instead of Score: 305", true, "boolean"));

        getStyles().add(new TextStyle("scorename", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("score", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("brackets", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("etc",  new AColor(0xAA,0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("currentScore", new AColor(0xFF, 0xAA,0x00,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("arrow",  new AColor(0xAA,0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("nextScore", new AColor(0xFF, 0xAA,0x00,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("required",  new AColor(0xAA,0xAA,0xAA,255), new AColor(0, 0,0,0), false));
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
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
        dummyText2.add(new StyledText("(","brackets"));
        dummyText2.add(new StyledText("1 Required 1 crypt","required"));
        dummyText2.add(new StyledText(")","brackets"));

    }

    @Override
    public java.util.List<String> getUsedTextStyle() {
        return Arrays.asList("scorename", "separator", "score", "brackets", "etc", "currentScore", "arrow", "nextScore", "required");

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
            actualBit.add(new StyledText("Skill", "scorename"));
            actualBit.add(new StyledText(": ", "separator"));
            actualBit.add(new StyledText(score.skill + " ", "score"));
            actualBit.add(new StyledText("(", "brackets"));
            actualBit.add(new StyledText(score.deaths + " Deaths", "etc"));
            actualBit.add(new StyledText(")\n", "brackets"));
            actualBit.add(new StyledText("Explorer", "scorename"));
            actualBit.add(new StyledText(": ", "separator"));
            actualBit.add(new StyledText(score.explorer + " ", "score"));
            actualBit.add(new StyledText("(", "brackets"));
            actualBit.add(new StyledText("Rooms " + (score.fullyCleared ? "O" : "X") + " Secrets " + score.secrets + "/" + score.effectiveTotalSecrets +" of "+score.getTotalSecrets() + (score.totalSecretsKnown ? "" : "?"), "etc"));
            actualBit.add(new StyledText(")\n", "brackets"));
            actualBit.add(new StyledText("Time", "scorename"));
            actualBit.add(new StyledText(": ", "separator"));
            actualBit.add(new StyledText(score.time + " ", "score"));
            actualBit.add(new StyledText("Bonus", "scorename"));
            actualBit.add(new StyledText(": ", "separator"));
            actualBit.add(new StyledText(score.bonus + " ", "score"));
            actualBit.add(new StyledText("Total", "scorename"));
            actualBit.add(new StyledText(": ", "separator"));
            actualBit.add(new StyledText(sum + "\n", "score"));
            actualBit.addAll(buildRequirement(score));
        } else {
            String letter = getLetter(sum);
            actualBit.add(new StyledText("Score", "scorename"));
            actualBit.add(new StyledText(": ", "separator"));
            actualBit.add(new StyledText(sum + " ", "score"));
            actualBit.add(new StyledText("(", "brackets"));
            actualBit.add(new StyledText(letter, "currentScore"));
            actualBit.add(new StyledText(")", "brackets"));
        }

        return actualBit;
    }

    @Data
    @AllArgsConstructor
    public static class ScoreCalculation {
        private int skill, explorer, time, bonus, tombs;
        private boolean fullyCleared;
        private int secrets, totalSecrets, effectiveTotalSecrets;
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
            boolean traproomFound = false;
            int roomCnt = 0;
            for (DungeonRoom dungeonRoom : context.getDungeonRoomList()) {
                if (dungeonRoom.getColor() == 74) bossroomFound = true;
                if (dungeonRoom.getColor() == 62) traproomFound = true;
                if (dungeonRoom.getCurrentState() != DungeonRoom.RoomState.DISCOVERED)
                    totalCompRooms += dungeonRoom.getUnitPoints().size();
                if (dungeonRoom.getColor() == 66 && dungeonRoom.getCurrentState() == DungeonRoom.RoomState.DISCOVERED) // INCOMPLETE PUZZLE ON MAP
                    skill -= 10;
                if (dungeonRoom.getColor() == 74 && dungeonRoom.getCurrentState() == DungeonRoom.RoomState.DISCOVERED) // INCOMPLETE BOSSROOM YELLOW
                    skill += 1;
                if (dungeonRoom.getColor() == 62 && dungeonRoom.getCurrentState() == DungeonRoom.RoomState.DISCOVERED) // INCOMPLETE TRAP ROOM
                    skill += 1;

                skill += dungeonRoom.getCurrentState().getScoreModifier();

                roomCnt += dungeonRoom.getUnitPoints().size();
            }
            if (!bossroomFound) skill += 1;
            if (!traproomFound && context.isTrapRoomGen()) skill += 1;
            skill -= getUndiscoveredPuzzles() * 10;
            if (context.getMapProcessor().getUndiscoveredRoom() == 0) {
                skill -= Math.max(0, (roomCnt - totalCompRooms) * 4);
            } else {
                skill -= Math.max(0, (getTotalRooms() - totalCompRooms) * 4);
            }
            skill = MathHelper.clamp_int(skill, 0, 100);
        }
        int explorer = 0;
        boolean fullyCleared = false;
        boolean totalSecretsKnown = true;
        int totalSecrets = 0;
        int secrets = 0;
        {
            int completed = 0;
            double total = 0;

            for (DungeonRoom dungeonRoom : context.getDungeonRoomList()) {
                if (dungeonRoom.getCurrentState() != DungeonRoom.RoomState.DISCOVERED && dungeonRoom.getCurrentState() != DungeonRoom.RoomState.FAILED)
                    completed += dungeonRoom.getUnitPoints().size();
                total += dungeonRoom.getUnitPoints().size();
            }

            totalSecrets =  FeatureRegistry.DUNGEON_SECRETS.getTotalSecretsInt() ;
            totalSecretsKnown = FeatureRegistry.DUNGEON_SECRETS.sureOfTotalSecrets();

            fullyCleared = completed >= getTotalRooms() && context.getMapProcessor().getUndiscoveredRoom() == 0;
            explorer += MathHelper.clamp_int((int) Math.floor(6.0 / 10.0 * (context.getMapProcessor().getUndiscoveredRoom() != 0 ? getPercentage() : completed / total * 100)), 0, 60);
            explorer += MathHelper.clamp_int((int) Math.floor(40 * (secrets = FeatureRegistry.DUNGEON_SECRETS.getSecretsFound()) / (totalSecrets * context.getSecretPercentage())),0,40);
        }
        int time = 0;
        {
            int maxTime = context.getMaxSpeed();
            int timeSec = FeatureRegistry.DUNGEON_SBTIME.getTimeElapsed() / 1000;

            if (timeSec <= maxTime) time = 100;
            else if (timeSec <= maxTime+100) time = (int) Math.ceil(232 - 0.1 * timeSec);
            else if (timeSec <= maxTime+500) time = (int) Math.ceil(161 - 0.05 * timeSec);
            else if (timeSec < maxTime+2600) time = (int) Math.ceil(392/3.0 - (1/30.0) * timeSec);
        }
        int bonus = 0;
        int tombs;
        {
            bonus += tombs = MathHelper.clamp_int(FeatureRegistry.DUNGEON_TOMBS.getTombsFound(), 0, 5);
            if (context.isGotMimic()) bonus += 2;
            CompletableFuture<StaticResource> staticResourceCompletableFuture = StaticResourceCache.INSTANCE.getResource(StaticResourceCache.BONUS_SCORE);
            if (staticResourceCompletableFuture.isDone()) {
                try {
                    bonus += Integer.parseInt(staticResourceCompletableFuture.get().getValue().trim());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }

        // amazing thing
        return new ScoreCalculation(skill, explorer, time, bonus, tombs, fullyCleared, secrets, totalSecrets, (int) (totalSecrets * context.getSecretPercentage()), totalSecretsKnown, deaths);
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
            actualBit.add(new StyledText("S+ Expected","nextScore"));
            return actualBit;
        }
        int req = getScoreRequirement(nextLetter);
        int reqPT2 = req-  current;
        int reqPT = req - current;

        int tombsBreakable = Math.min(5 - calculation.tombs, reqPT);
        reqPT -= tombsBreakable;

        double secretPer = 40.0 / calculation.effectiveTotalSecrets;
        int secrets = (int) Math.ceil(reqPT / secretPer);

        actualBit.add(new StyledText(currentLetter,"currentScore"));
        actualBit.add(new StyledText("->","arrow"));
        actualBit.add(new StyledText(nextLetter+" ","nextScore"));
        actualBit.add(new StyledText("(","brackets"));
        actualBit.add(new StyledText(reqPT2+" required "+tombsBreakable+" crypt "+secrets+" secrets","required"));
        actualBit.add(new StyledText(")","brackets"));
        return actualBit;
    }
}
