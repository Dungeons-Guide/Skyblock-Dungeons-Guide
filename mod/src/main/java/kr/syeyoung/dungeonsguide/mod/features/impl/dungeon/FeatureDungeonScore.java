/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon;


import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.config.types.TCBoolean;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonRoomScaffoldParser;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultTextHUDFeatureStyleFeature;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.NullTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextSpan;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabList;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabListEntry;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import kr.syeyoung.dungeonsguide.mod.utils.TimeScoreUtil;
import kr.syeyoung.dungeonsguide.mod.wsresource.StaticResource;
import kr.syeyoung.dungeonsguide.mod.wsresource.StaticResourceCache;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.util.MathHelper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class FeatureDungeonScore extends TextHUDFeature {
    public FeatureDungeonScore() {
        super("Dungeon.HUDs", "Display Current Score", "Calculate and Display current score\nThis data is from pure calculation and can be different from actual score.", "dungeon.stats.score");
        this.setEnabled(false);
        addParameter("verbose", new FeatureParameter<Boolean>("verbose", "Show each score instead of sum", "Skill: 100 Explore: 58 S->S+(5 tombs) instead of Score: 305", true, TCBoolean.INSTANCE));

        registerDefaultStyle("scorename", DefaultingDelegatingTextStyle.derive("Feature Default - Scorename", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.NAME)));
        registerDefaultStyle("separator", DefaultingDelegatingTextStyle.derive("Feature Default - Separator", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.SEPARATOR)));
        registerDefaultStyle("score", DefaultingDelegatingTextStyle.derive("Feature Default - Score", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.VALUE)));
        registerDefaultStyle("brackets", DefaultingDelegatingTextStyle.derive("Feature Default - Brackets", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.BRACKET)));
        registerDefaultStyle("etc", DefaultingDelegatingTextStyle.derive("Feature Default - Etc", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.EXTRA_INFO)));
        registerDefaultStyle("currentScore", DefaultingDelegatingTextStyle.derive("Feature Default - CurrentScore", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.DEFAULT))
                .setTextShader(new AColor(0xFF, 0xAA,0x00,255)).setBackgroundShader(new AColor(0, 0,0,0)));
        registerDefaultStyle("arrow", DefaultingDelegatingTextStyle.derive("Feature Default - Arrow", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.EXTRA_INFO)));
        registerDefaultStyle("nextScore", DefaultingDelegatingTextStyle.derive("Feature Default - NextScore", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.DEFAULT))
                .setTextShader(new AColor(0xFF, 0xAA,0x00,255)).setBackgroundShader(new AColor(0, 0,0,0)));
        registerDefaultStyle("required", DefaultingDelegatingTextStyle.derive("Feature Default - Required", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.EXTRA_INFO)));
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon();
    }

    @Override
    public TextSpan getDummyText() {

        if (this.<Boolean>getParameter("verbose").getValue()) {
            TextSpan dummyText2 = new TextSpan(new NullTextStyle(), "");
            dummyText2.addChild(new TextSpan(getStyle("scorename"), "Skill"));
            dummyText2.addChild(new TextSpan(getStyle("separator"), ": "));
            dummyText2.addChild(new TextSpan(getStyle("score"), "100 "));
            dummyText2.addChild(new TextSpan(getStyle("brackets"), "("));
            dummyText2.addChild(new TextSpan(getStyle("etc"), "0 Deaths"));
            dummyText2.addChild(new TextSpan(getStyle("brackets"), ")\n"));
            dummyText2.addChild(new TextSpan(getStyle("scorename"), "Explorer"));
            dummyText2.addChild(new TextSpan(getStyle("separator"), ": "));
            dummyText2.addChild(new TextSpan(getStyle("score"), "99 "));
            dummyText2.addChild(new TextSpan(getStyle("brackets"), "("));
            dummyText2.addChild(new TextSpan(getStyle("etc"), "Rooms O Secrets 39/40"));
            dummyText2.addChild(new TextSpan(getStyle("brackets"), ")\n"));
            dummyText2.addChild(new TextSpan(getStyle("scorename"), "Time"));
            dummyText2.addChild(new TextSpan(getStyle("separator"), ": "));
            dummyText2.addChild(new TextSpan(getStyle("score"), "100 "));
            dummyText2.addChild(new TextSpan(getStyle("scorename"), "Bonus"));
            dummyText2.addChild(new TextSpan(getStyle("separator"), ": "));
            dummyText2.addChild(new TextSpan(getStyle("score"), "0 "));
            dummyText2.addChild(new TextSpan(getStyle("scorename"), "Total"));
            dummyText2.addChild(new TextSpan(getStyle("separator"), ": "));
            dummyText2.addChild(new TextSpan(getStyle("score"), "299\n"));
            dummyText2.addChild(new TextSpan(getStyle("currentScore"), "S"));
            dummyText2.addChild(new TextSpan(getStyle("arrow"), "->"));
            dummyText2.addChild(new TextSpan(getStyle("nextScore"), "S+ "));
            dummyText2.addChild(new TextSpan(getStyle("brackets"), "("));
            dummyText2.addChild(new TextSpan(getStyle("required"), "1 Required 1 crypt"));
            dummyText2.addChild(new TextSpan(getStyle("brackets"), ")"));
            return dummyText2;
        } else {
            TextSpan dummyText = new TextSpan(new NullTextStyle(), "");
            dummyText.addChild(new TextSpan(getStyle("scorename"), "Score"));
            dummyText.addChild(new TextSpan(getStyle("separator"), ": "));
            dummyText.addChild(new TextSpan(getStyle("score"), "305 "));
            dummyText.addChild(new TextSpan(getStyle("brackets"), "("));
            dummyText.addChild(new TextSpan(getStyle("currentScore"), "S+"));
            dummyText.addChild(new TextSpan(getStyle("brackets"), ")"));
            return dummyText;
        }
    }

    @Override
    public TextSpan getText() {
        TextSpan actualBit = new TextSpan(new NullTextStyle(), "");

        ScoreCalculation score = calculateScore();
        if (score == null) return new TextSpan(new NullTextStyle(), "");
        int sum = score.time + score.skill + score.explorer + score.bonus;
        if (this.<Boolean>getParameter("verbose").getValue()) {
            actualBit.addChild(new TextSpan(getStyle("scorename"), "Skill"));
            actualBit.addChild(new TextSpan(getStyle("separator"), ": "));
            actualBit.addChild(new TextSpan(getStyle("score"), score.skill + " "));
            actualBit.addChild(new TextSpan(getStyle("brackets"), "("));
            actualBit.addChild(new TextSpan(getStyle("etc"), score.deaths + " Deaths"));
            actualBit.addChild(new TextSpan(getStyle("brackets"), ")\n"));
            actualBit.addChild(new TextSpan(getStyle("scorename"), "Explorer"));
            actualBit.addChild(new TextSpan(getStyle("separator"), ": "));
            actualBit.addChild(new TextSpan(getStyle("score"), score.explorer + " "));
            actualBit.addChild(new TextSpan(getStyle("brackets"), "("));
            actualBit.addChild(new TextSpan(getStyle("etc"), "Rooms " + (score.fullyCleared ? "O" : "X") + " Secrets " + score.secrets + "/" + score.effectiveTotalSecrets +" of "+score.getTotalSecrets() + (score.totalSecretsKnown ? "" : "?")));
            actualBit.addChild(new TextSpan(getStyle("brackets"), ")\n"));
            actualBit.addChild(new TextSpan(getStyle("scorename"), "Time"));
            actualBit.addChild(new TextSpan(getStyle("separator"), ": "));
            actualBit.addChild(new TextSpan(getStyle("score"), score.time + " "));
            actualBit.addChild(new TextSpan(getStyle("scorename"), "Bonus"));
            actualBit.addChild(new TextSpan(getStyle("separator"), ": "));
            actualBit.addChild(new TextSpan(getStyle("score"), score.bonus + " "));
            actualBit.addChild(new TextSpan(getStyle("scorename"), "Total"));
            actualBit.addChild(new TextSpan(getStyle("separator"), ": "));
            actualBit.addChild(new TextSpan(getStyle("score"), sum + "\n"));
            actualBit.addChild(buildRequirement(score));
        } else {
            String letter = getLetter(sum);
            actualBit.addChild(new TextSpan(getStyle("scorename"), "Score"));
            actualBit.addChild(new TextSpan(getStyle("separator"), ": "));
            actualBit.addChild(new TextSpan(getStyle("score"), sum + " "));
            actualBit.addChild(new TextSpan(getStyle("brackets"), "("));
            actualBit.addChild(new TextSpan(getStyle("currentScore"), letter));
            actualBit.addChild(new TextSpan(getStyle("brackets"), ")"));
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

    public int getCompleteRooms() {
        for (TabListEntry tabListEntry : TabList.INSTANCE.getTabListEntries()) {
            String name = tabListEntry.getEffectiveName();
            if (name.startsWith("§r Completed Rooms: §r")) {
                String milestone = TextUtils.stripColor(name).substring(18);
                return Integer.parseInt(milestone);
            }
        }
        return 0;
    }
    public int getTotalRooms() {
        int compRooms = getCompleteRooms();
        if (compRooms == 0) return 100;
        System.out.println(compRooms /  (double) DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getPercentage());
        return (int) Math.round(100 * (compRooms / (double) DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getPercentage()));
    }
    public int getUndiscoveredPuzzles() {
        int cnt = 0;
        for (TabListEntry tabListEntry : TabList.INSTANCE.getTabListEntries()) {
            String name = tabListEntry.getEffectiveName();
            if (name.startsWith("§r ???: ")) {
                cnt ++;
            }
        }
        return cnt;
    }

    public ScoreCalculation calculateScore() {
        if (!skyblockStatus.isOnDungeon()) return null;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return null;
        if (context.getScaffoldParser() == null) return null;


        DungeonRoomScaffoldParser parser = context.getScaffoldParser();
        int skill = 100;
        int deaths = 0;
        {
            int totalCompRooms= 0;
            int roomCnt = 0;
//            boolean bossroomIncomplete = true;
//            boolean traproomIncomplete = context.isTrapRoomGen();
            int incompletePuzzles = getUndiscoveredPuzzles();

            for (DungeonRoom dungeonRoom : parser.getDungeonRoomList()) {
//                if (dungeonRoom.getColor() == 74 && dungeonRoom.getCurrentState() != DungeonRoom.RoomState.DISCOVERED)
//                    bossroomIncomplete = false;
//                if (dungeonRoom.getColor() == 62 && dungeonRoom.getCurrentState() != DungeonRoom.RoomState.DISCOVERED)
//                    traproomIncomplete = false;
                if (dungeonRoom.getCurrentState() != DungeonRoom.RoomState.DISCOVERED)
                    totalCompRooms += dungeonRoom.getUnitPoints().size();
                if (dungeonRoom.getColor() == 66 && (dungeonRoom.getCurrentState() == DungeonRoom.RoomState.DISCOVERED || dungeonRoom.getCurrentState() == DungeonRoom.RoomState.FAILED)) // INCOMPLETE PUZZLE ON MAP
                    incompletePuzzles++;
                roomCnt += dungeonRoom.getUnitPoints().size();
            }
            if (parser.getUndiscoveredRoom() != 0)
                roomCnt = getTotalRooms();
            skill = (int) Math.floor(80.0 * totalCompRooms / roomCnt)+20;
            System.out.println(skill + " / "+totalCompRooms + " / "+ roomCnt);
            skill -=  incompletePuzzles * 10;

            deaths = FeatureRegistry.DUNGEON_DEATHS.getTotalDeaths();
            skill -= FeatureRegistry.DUNGEON_DEATHS.getTotalDeaths() * 2;

            skill = MathHelper.clamp_int(skill, 20, 100);
        }
        int explorer = 0;
        boolean fullyCleared = false;
        boolean totalSecretsKnown = true;
        int totalSecrets = 0;
        int secrets = 0;
        {
            int completed = 0;
            double total = 0;

            for (DungeonRoom dungeonRoom : parser.getDungeonRoomList()) {
                if (dungeonRoom.getCurrentState() != DungeonRoom.RoomState.DISCOVERED && dungeonRoom.getCurrentState() != DungeonRoom.RoomState.FAILED)
                    completed += dungeonRoom.getUnitPoints().size();
                total += dungeonRoom.getUnitPoints().size();
            }

            totalSecrets =  FeatureRegistry.DUNGEON_SECRETS.getTotalSecretsInt() ;
            totalSecretsKnown = FeatureRegistry.DUNGEON_SECRETS.sureOfTotalSecrets();

            fullyCleared = completed >= getTotalRooms() && parser.getUndiscoveredRoom() == 0;
            explorer += MathHelper.clamp_int((int) Math.floor(6.0 / 10.0 * (parser.getUndiscoveredRoom() != 0 ? DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getPercentage() : completed / total * 100)), 0, 60);
            explorer += MathHelper.clamp_int((int) Math.floor(40 * (secrets = FeatureRegistry.DUNGEON_SECRETS.getSecretsFound()) / Math.ceil(totalSecrets * context.getSecretPercentage())),0,40);
        }
        int time = 0;
        {
            int maxTime = context.getMaxSpeed();
//            int timeSec = FeatureRegistry.DUNGEON_SBTIME.getTimeElapsed() / 1000 - maxTime + 480;
//
//            if (timeSec <= 480) time = 100;
//            else if (timeSec <= 580) time = (int) Math.ceil(148 - 0.1 * timeSec);
//            else if (timeSec <= 980) time = (int) Math.ceil(119 - 0.05 * timeSec);
//            else if (timeSec < 3060) time = (int) Math.ceil(3102 - (1/30.0) * timeSec);
//            time = MathHelper.clamp_int(time, 0, 100); // just in case.
            time = TimeScoreUtil.estimate(FeatureRegistry.DUNGEON_SBTIME.getTimeElapsed(), maxTime);
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
        return new ScoreCalculation(skill, explorer, time, bonus, tombs, fullyCleared, secrets, totalSecrets, (int)Math.ceil (totalSecrets * context.getSecretPercentage()), totalSecretsKnown, deaths);
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
    public TextSpan buildRequirement(ScoreCalculation calculation) {
        TextSpan actualBit = new TextSpan(new NullTextStyle(), "");
        int current = calculation.time + calculation.bonus + calculation.explorer + calculation.skill;
        String currentLetter = getLetter(current);
        String nextLetter=  getNextLetter(currentLetter);
        if (nextLetter == null) {
            actualBit.addChild(new TextSpan(getStyle("nextScore"), "S+ Expected"));
            return actualBit;
        }
        int req = getScoreRequirement(nextLetter);
        int reqPT2 = req-  current;
        int reqPT = req - current;

        int tombsBreakable = Math.min(5 - calculation.tombs, reqPT);
        reqPT -= tombsBreakable;

        double secretPer = 40.0 / calculation.effectiveTotalSecrets;
        int secrets = (int) Math.ceil(reqPT / secretPer);

        actualBit.addChild(new TextSpan(getStyle("currentScore"), currentLetter));
        actualBit.addChild(new TextSpan(getStyle("arrow"), "->"));
        actualBit.addChild(new TextSpan(getStyle("nextScore"), nextLetter+" "));
        actualBit.addChild(new TextSpan(getStyle("brackets"), "("));
        actualBit.addChild(new TextSpan(getStyle("required"), reqPT2+" required "+tombsBreakable+" crypt "+secrets+" secrets"));
        actualBit.addChild(new TextSpan(getStyle("brackets"), ")"));
        return actualBit;
    }
}
