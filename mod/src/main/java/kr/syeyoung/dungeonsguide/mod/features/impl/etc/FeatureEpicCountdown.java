/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.features.impl.etc;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.TCBoolean;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.Objective;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.Score;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.ScoreboardManager;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.Objects;

/**
 * CREDITS FOR THE COUNTDOWN SOUNDTRACK: <a href="https://www.youtube.com/watch?v=acCqrA-JxAw">...</a>
 */
public class FeatureEpicCountdown extends SimpleFeature {

    static long updatedAt;
    static int secondsLeft;
    private static boolean cleanChat;
    private boolean sfxenabled;

    int actualSecondsLeft;

    public FeatureEpicCountdown() {
        super("Dungeon HUD", "Epic Dungeon Start Countdown", "Shows a cool dungeon start instead of the chat messages", "etc.dungeoncountdown", true);
        addParameter("cleanchat", new FeatureParameter<>("cleanchat", "Clean Dungeon Chat", "^^^", true, TCBoolean.INSTANCE, nval -> cleanChat = nval));
        addParameter("sounds", new FeatureParameter<>("sounds", "Countdown SFX", "^^^", true, TCBoolean.INSTANCE, nval -> sfxenabled = nval));

        lastSec = GO_TEXT;
    }

    @DGEventHandler
    public void processChat(ClientChatReceivedEvent receivedEvent) {
        String txt = receivedEvent.message.getFormattedText();

        // TODO: make a good chat remover with configurable chats, search chats recieved and stuff, but not for now
        if(cleanChat){
            if(txt.startsWith("§e[NPC] §bMort§f: §rTalk to me to change your class and ready up.§r")){
                receivedEvent.setCanceled(true);
            }
            if(txt.startsWith("§r§aYour active Potion Effects have been paused and stored.")){
                receivedEvent.setCanceled(true);
            }
            if(txt.startsWith("§e[NPC] §bMort§f: §rGood luck.§r")){
                receivedEvent.setCanceled(true);
            }
            if(txt.startsWith("§e[NPC] §bMort§f: §rYou should find it useful if you get lost.§r")){
                receivedEvent.setCanceled(true);
            }
            if(TextUtils.stripColor(txt).contains("[NPC] Mort: Here, I found this map")){
                receivedEvent.setCanceled(true);
            }
            if(txt.startsWith("§r§a[Berserk] §r§f")){ // huh? wtf?
                receivedEvent.setCanceled(true);
            }
        }


        if (txt.startsWith("§r§aDungeon starts in")) {
            String striped = TextUtils.stripColor(txt);

            String secondsStr = striped.replace("Dungeon starts in ", "");
            secondsStr = secondsStr.replace(" seconds.", "");
            secondsStr = secondsStr.replace(" second.", "");

            secondsLeft = Integer.parseInt(secondsStr);
            updatedAt = System.currentTimeMillis();

            receivedEvent.setCanceled(true);
        }
    }

    private static final String GO_TEXT = "GO!!!";
    private String lastSec;

    @DGEventHandler
    public void onTick(DGTickEvent event){
        if(!isEnabled() || !SkyblockStatus.isOnDungeon()) return;


        Objective objective = ScoreboardManager.INSTANCE.getSidebarObjective();
        if (objective != null) {
            for (Score score : objective.getScores()) {
                String line = TextUtils.stripColor(score.getJustTeam());
                if(line.contains("Starting in:")){
                    String time = line.replace("Starting in: ", "").replace("§r", "");
                    if (time.contains(":")) {
                        secondsLeft = Integer.parseInt(time.split(":")[0]) * 60 + Integer.parseInt(time.split(":")[1]);
                        updatedAt = System.currentTimeMillis();
                    } else if(!time.isEmpty()){
                        secondsLeft = Integer.parseInt(time);
                        updatedAt = System.currentTimeMillis();
                    }
                }
            }
        }
        long timePassed = System.currentTimeMillis() - updatedAt;

        long secs = timePassed / 1000;

        int actualSecondsPassed = (int) secs;
        actualSecondsLeft = secondsLeft - actualSecondsPassed;
        if (actualSecondsLeft <= 0) {
            if(!Objects.equals(lastSec, GO_TEXT)){
                lastSec = GO_TEXT;
                Minecraft.getMinecraft().ingameGUI.displayTitle(lastSec, "", 2, 25, 15);
            }
            return;
        }

        String string = "§c" + actualSecondsLeft;

        if(!Objects.equals(string, lastSec)){
            if(actualSecondsLeft == 3   && sfxenabled){
                Minecraft.getMinecraft().thePlayer.playSound("skyblock_dungeons_guide:readysetgo", 1F, 1F);
            }
            if(actualSecondsLeft > 5){
                Minecraft.getMinecraft().ingameGUI.displayTitle(string, "", 1, 10, 8);
            }else{
                Minecraft.getMinecraft().ingameGUI.displayTitle(string, "", 1, 6, 4);
            }
            lastSec = string;
        }


//                   www.hypixel.net§r
//                   §r
//                   Starting in: 0:57§r
//                   §r
//                   B kokoniara Lv25§r
//                   §r
//                    The Catacombs F3§r
//                   Late Winter 3rd§r
//                   §r
//                   10/22/22 m65G 28266§r

    }

}
