package kr.syeyoung.dungeonsguide.features.impl.etc;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.chat.ChatProcessResult;
import kr.syeyoung.dungeonsguide.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.utils.ScoreBoardUtils;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static kr.syeyoung.dungeonsguide.chat.ChatProcessResult.NONE;
import static kr.syeyoung.dungeonsguide.chat.ChatProcessResult.REMOVE_CHAT;

public class FeatureEpicCountdown extends SimpleFeature {

    static volatile long updatedAt;
    static volatile int secondsLeft;
    private static boolean cleanChat;

    int actualSecondsLeft;

    public FeatureEpicCountdown() {
        super("Misc", "Epic Dungeon Start Countdown", "Shows a cool dungeon start instead of the chat messages", "etc.dungeoncountdown", true);
        addParameter("cleanchat", new FeatureParameter<>("cleanchat", "Clean Dungeon Chat", "name", true, "boolean", nval -> cleanChat = nval));

        ChatProcessor.INSTANCE.subscribe(FeatureEpicCountdown::processChat);
        MinecraftForge.EVENT_BUS.register(this);
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
            if(txt.startsWith("§r§a[Berserk] §r§f")){
                return REMOVE_CHAT;
            }
        }


        if (txt.startsWith("§r§aDungeon starts in")) {
            String striped = TextUtils.stripColor(txt);
            System.out.println("DUNGEON START MESSAGE STRIPED: " + striped);

            String secondsStr = striped.replace("Dungeon starts in ", "");
            secondsStr = secondsStr.replace(" seconds.", "");
            secondsStr = secondsStr.replace(" second.", "");

            System.out.println("DUNGEON NUMBER ALONE: " + secondsStr);

            secondsLeft = Integer.parseInt(secondsStr);
            updatedAt = System.currentTimeMillis();

            System.out.println("Seconds Left to open a dungeon: " + secondsLeft + " Updated at: " + updatedAt);

            return REMOVE_CHAT;
        }

        return NONE;

    }

    String lastSec = "GO!!!";

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e){
        if(e.phase != TickEvent.Phase.START || !isEnabled() || !DungeonsGuide.getDungeonsGuide().getSkyblockStatus().isOnDungeon()) return;

        System.out.println("checking for starting text");
        AtomicBoolean foundtext = new AtomicBoolean(false);
        ScoreBoardUtils.forEachLineClean(line -> {
            if(line.contains("Starting in:")){
                foundtext.set(true);
                String time = line.replace("Starting in: ", "").replace("§r", "").replace("0:", "");
                if(!time.isEmpty()){
                    secondsLeft = Integer.parseInt(time);
                    updatedAt = System.currentTimeMillis();
                    System.out.println("Seconds Left to open a dungeon: " + secondsLeft + " Updated at: " + updatedAt);
                }
            }
        });
        System.out.println(foundtext.get() ? "Found starting text" : "didnt find statring text");


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

//                [02:28:15]   www.hypixel.net§r
//                [02:28:15]   §r
//                [02:28:15]   Starting in: 0:03§r
//                [02:28:16]   §r
//                [02:28:16]   B kokoniara Lv25§r
//                [02:28:16]   §r
//                [02:28:16]    The Catacombs F3§r
//                [02:28:16]   Late Winter 3rd§r
//                [02:28:16]   §r
//                [02:28:16]   10/22/22 m65G 28266§r

    }


    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post postRender) {
        if (!isEnabled()) return;
        if (!(postRender.type == RenderGameOverlayEvent.ElementType.EXPERIENCE || postRender.type == RenderGameOverlayEvent.ElementType.JUMPBAR))
            return;

        long timepassed = System.currentTimeMillis() - updatedAt;

        long secs = timepassed / 1000;

        int actualSecondspassed = (int) secs;
        actualSecondsLeft = secondsLeft - actualSecondspassed;
        if (actualSecondsLeft <= 0) {
            if(!Objects.equals(lastSec, "GO!!!")){
                lastSec = "GO!!!";
                Minecraft.getMinecraft().ingameGUI.displayTitle(lastSec, "", 5, 750, 50);
            }
            return;
        }

        String string = String.valueOf(actualSecondsLeft);

        if(!Objects.equals(string, lastSec)){
            Minecraft.getMinecraft().ingameGUI.displayTitle(string, "", 5, 950, 50);
            lastSec = string;
        }

    }


}
