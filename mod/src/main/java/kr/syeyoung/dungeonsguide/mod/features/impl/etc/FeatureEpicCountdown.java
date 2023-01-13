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
import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessResult;
import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.utils.ScoreBoardUtils;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import kr.syeyoung.dungeonsguide.mod.utils.TitleRender;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import java.util.Map;
import java.util.Objects;

import static kr.syeyoung.dungeonsguide.mod.chat.ChatProcessResult.NONE;
import static kr.syeyoung.dungeonsguide.mod.chat.ChatProcessResult.REMOVE_CHAT;

/**
 * CREDITS FOR THE COUNTDOWN SOUNDTRACK: <a href="https://www.youtube.com/watch?v=acCqrA-JxAw">...</a>
 */
public class FeatureEpicCountdown extends SimpleFeature {

    static volatile long updatedAt;
    static volatile int secondsLeft;
    private static boolean cleanChat;
    private boolean sfxenabled;

    int actualSecondsLeft;

    public FeatureEpicCountdown() {
        super("Dungeon.HUDs", "Epic Dungeon Start Countdown", "Shows a cool dungeon start instead of the chat messages", "etc.dungeoncountdown", true);
        addParameter("cleanchat", new FeatureParameter<>("cleanchat", "Clean Dungeon Chat", "^^^", true, "boolean", nval -> cleanChat = nval));
        addParameter("sounds", new FeatureParameter<>("sounds", "Countdown SFX", "^^^", true, "boolean", nval -> sfxenabled = nval));

        lastSec = GO_TEXT;

        ChatProcessor.INSTANCE.subscribe(FeatureEpicCountdown::processChat);
    }

    public static ChatProcessResult processChat(String txt, Map<String, Object> context) {
        if(cleanChat){
            if(txt.startsWith("§e[NPC] §bMort§f: §rTalk to me to change your class and ready up.§r")){
                return REMOVE_CHAT;
            }
            if(txt.startsWith("§r§aYour active Potion Effects have been paused and stored.")){
                return REMOVE_CHAT;
            }
            if(txt.startsWith("§e[NPC] §bMort§f: §rGood luck.§r")){
                return REMOVE_CHAT;
            }
            if(txt.startsWith("§e[NPC] §bMort§f: §rYou should find it useful if you get lost.§r")){
                return REMOVE_CHAT;
            }
            if(TextUtils.stripColor(txt).contains("[NPC] Mort: Here, I found this map")){
                return REMOVE_CHAT;
            }
            if(txt.startsWith("§r§a[Berserk] §r§f")){
                return REMOVE_CHAT;
            }
        }


        if (txt.startsWith("§r§aDungeon starts in")) {
            String striped = TextUtils.stripColor(txt);

            String secondsStr = striped.replace("Dungeon starts in ", "");
            secondsStr = secondsStr.replace(" seconds.", "");
            secondsStr = secondsStr.replace(" second.", "");

            secondsLeft = Integer.parseInt(secondsStr);
            updatedAt = System.currentTimeMillis();

            return REMOVE_CHAT;
        }

        return NONE;

    }

    static final String GO_TEXT = "GO!!!";
    String lastSec;

    @DGEventHandler
    public void onTick(DGTickEvent event){
        if(!isEnabled() || !DungeonsGuide.getDungeonsGuide().getSkyblockStatus().isOnDungeon()) return;


        ScoreBoardUtils.forEachLineClean(line -> {
            if(line.contains("Starting in:")){
                String time = line.replace("Starting in: ", "").replace("§r", "").replace("0:", "");
                if(!time.isEmpty()){
                    secondsLeft = Integer.parseInt(time);
                    updatedAt = System.currentTimeMillis();
                }
            }
        });


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


    @DGEventHandler
    public void onRender(RenderGameOverlayEvent.Post postRender) {
        if (!isEnabled()) return;
        if (!(postRender.type == RenderGameOverlayEvent.ElementType.EXPERIENCE || postRender.type == RenderGameOverlayEvent.ElementType.JUMPBAR))
            return;

        long timepassed = System.currentTimeMillis() - updatedAt;

        long secs = timepassed / 1000;

        int actualSecondspassed = (int) secs;
        actualSecondsLeft = secondsLeft - actualSecondspassed;
        if (actualSecondsLeft <= 0) {
            if(!Objects.equals(lastSec, GO_TEXT)){
                lastSec = GO_TEXT;
                TitleRender.displayTitle(lastSec, "", 2, 25, 15);
            }
            return;
        }

        String string = "§c" + actualSecondsLeft;

        if(!Objects.equals(string, lastSec)){
            if(actualSecondsLeft == 3   && sfxenabled){
                Minecraft.getMinecraft().thePlayer.playSound("skyblock_dungeons_guide:readysetgo", 1F, 1F);
            }
            if(actualSecondsLeft > 5){
                TitleRender.displayTitle(string, "", 1, 10, 8);
            }else{
                TitleRender.displayTitle(string, "", 1, 6, 4);
            }
            lastSec = string;
        }

    }


}
