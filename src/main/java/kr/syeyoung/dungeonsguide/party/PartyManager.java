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

package kr.syeyoung.dungeonsguide.party;

import kr.syeyoung.dungeonsguide.rpc.RichPresenceManager;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.events.HypixelJoinedEvent;
import kr.syeyoung.dungeonsguide.events.StompConnectedEvent;
import kr.syeyoung.dungeonsguide.stomp.StompInterface;
import kr.syeyoung.dungeonsguide.stomp.StompMessageHandler;
import kr.syeyoung.dungeonsguide.stomp.StompPayload;
import kr.syeyoung.dungeonsguide.stomp.StompSubscription;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Tuple;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.simple.SimpleLogger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class PartyManager implements StompMessageHandler {
    public static final PartyManager INSTANCE = new PartyManager();

    @Getter
    private String partyID = "GENERATE_PARTYID_PLEASE_POG_THIS_IS_INVALID_ID_THAT_SHOULD_BE_REGENERATED";
    @Getter
    private String askToJoinSecret = null;

    private final SecureRandom random = new SecureRandom();
    private static final String validChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";

    @Getter
    private boolean allowAskToJoin = false;
    @Getter
    private boolean canInvite = false;
    private int invitedDash  =0;

    @Getter
    private final Queue<Consumer<Set<String>>> runOnMembersReceived = new LinkedList<>();


    @Getter
    @Setter
    private int maxParty = 5;

    private static final Logger logger = LogManager.getLogger();


    private PartyManager() {
        Logger l = LogManager.getLogger(GuiNewChat.class);
        if (l instanceof SimpleLogger) {
            ((SimpleLogger) l).setLevel(Level.OFF);
        } else if (l instanceof org.apache.logging.log4j.core.Logger) {
            ((org.apache.logging.log4j.core.Logger) l).setLevel(Level.OFF);
        }
    }

    public void toggleAllowAskToJoin() {
        if (canInvite) allowAskToJoin = !allowAskToJoin;
        if (allowAskToJoin) {
            generateNewAskToJoinSecret();
        }
    }

    public int getMemberCount() {
        return Math.max(1, members.size());
    }

    public void setPartyID(String partyID) {
        if (this.partyID != null && partyID == null) {
            JSONObject object = new JSONObject();
            object.put("members", new JSONArray());
            StompInterface stompInterface = DungeonsGuide.getDungeonsGuide().getStompConnection();
            stompInterface.send(new StompPayload().payload(object.toString()).header("destination", "/app/party.join"));
        }

        if (partyID != null && !partyID.equals(this.partyID)) {
            sendChat.add(new Tuple<>("/p invite -", () -> {invitedDash = 1;}));
        } else {
            canInvite = true;
            allowAskToJoin = false;
        }
        this.partyID = partyID;
        this.askToJoinSecret = null;

        if (allowAskToJoin && RichPresenceManager.INSTANCE.getLastSetupCode() != -9999) {
            generateNewAskToJoinSecret();
        }
    }

    public void generateNewAskToJoinSecret() {
        if (partyID == null) {
            JSONObject object = new JSONObject();
            object.put("members", new JSONArray());
            StompInterface stompInterface = DungeonsGuide.getDungeonsGuide().getStompConnection();
            stompInterface.send(new StompPayload().payload(object.toString()).header("destination", "/app/party.join"));
        }

        StringBuilder secretBuilder = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            secretBuilder.append(validChars.charAt(random.nextInt(validChars.length())));
        }
        this.askToJoinSecret = secretBuilder.toString();

        StompInterface stompInterface = DungeonsGuide.getDungeonsGuide().getStompConnection();
        stompInterface.send(new StompPayload().payload(new JSONObject().put("secret", askToJoinSecret).toString()).header("destination", "/app/party.setjoinsecret"));
    }

    private int partyJoin =0;
    private final Set<String> members = new HashSet<>();
    private final Map<String, Long> recentlyJoined = new HashMap<>();
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMessage(ClientChatReceivedEvent chatReceivedEvent) {
        if (chatReceivedEvent.type == 2) return;

        String str = chatReceivedEvent.message.getFormattedText();
        logger.log(Level.INFO, "[CHAT] "+str);

        try {

            if (str.startsWith("§eYou have joined ")) {
                members.clear();
                String[] strs = TextUtils.stripColor(str).split(" ");
                for (String s : strs) {
                    if (s.endsWith("'s")) {
                        members.add(s.substring(0, s.indexOf("'s")));
                        break;
                    }
                }
                members.add(Minecraft.getMinecraft().getSession().getUsername());
                partyJoin = 100;
            } else if (str.startsWith("§eYou'll be partying with: ")) {
                String[] players = TextUtils.stripColor(str.substring(27)).split(" ");
                for (String player : players) {
                    if (player.startsWith("[")) continue;
                    members.add(player);
                }
            } else if (str.equals("§9§m-----------------------------§r")) {
                if ((checkPlayer > 0 || partyJoin > 0) && partyJoin != 100) {
                    chatReceivedEvent.setCanceled(true);
                }
                if (partyJoin == 2 || partyJoin == 100) {
                    partyJoin = 0;
                    // REQ PARTY JOIN
                    Consumer<Set<String>> r;
                    while ((r = runOnMembersReceived.poll()) != null){
                        r.accept(members);
                    }

                    JSONArray jsonArray = new JSONArray();
                    for (String member : members) {
                        jsonArray.put(member);
                    }
                    JSONObject object = new JSONObject();
                    object.put("members", jsonArray);
                    StompInterface stompInterface = DungeonsGuide.getDungeonsGuide().getStompConnection();
                    stompInterface.send(new StompPayload().payload(object.toString()).header("destination", "/app/party.join"));
                }
                if (checkPlayer == 3) {
                    checkPlayer = 0;
                    String playerName = theObject.getString("player");
                    String token = theObject.getString("token");
                    if (!members.contains(playerName)) {
                        DungeonsGuide.getDungeonsGuide().getStompConnection().send(new StompPayload().payload(new JSONObject().put("status", "failure").put("token", token).toString()).header("destination", "/app/party.check.resp"));
                    } else {
                        DungeonsGuide.getDungeonsGuide().getStompConnection().send(new StompPayload().payload(new JSONObject().put("status", "success").put("token", token).toString()).header("destination", "/app/party.check.resp"));
                    }
                }
                if (invitedDash == 1 || invitedDash == 3) {
                    chatReceivedEvent.setCanceled(true);
                    invitedDash++;
                }
                if (invitedDash == 4) invitedDash = 0;
            } else if (str.endsWith("§ejoined the party.§r")) {
                String asd = null;
                for (String s : TextUtils.stripColor(str).split(" ")) {
                    if (s.startsWith("[")) continue;
                    asd = s;
                    break;
                }
                if (asd != null)
                    members.add(asd);
            } else if (str.contains("§r§ejoined the dungeon group! (§r§b")) {
                String username = TextUtils.stripColor(str).split(" ")[3];
                if (username.equalsIgnoreCase(Minecraft.getMinecraft().getSession().getUsername())) {
                    sendChat.add(new Tuple<>("/pl", () -> {partyJoin = 1;}));
                    partyJoin = 1;
                } else {
                    members.add(username);
                }
            } else if (str.endsWith("§ehas been removed from the party.§r")
                    || str.endsWith("§ehas left the party.§r")) {
                String asd = null;
                for (String s : TextUtils.stripColor(str).split(" ")) {
                    if (s.startsWith("[")) continue;
                    asd = s;
                    break;
                }
                if (asd != null)
                    members.remove(asd);
            } else if ((str.equals("§eYou left the party.§r")
                    || str.startsWith("§cThe party was disbanded")
                    || str.endsWith("§ehas disbanded the party!§r"))
            ) {
                members.clear();
                setPartyID(null);
            } else if (str.startsWith("§6Party Members ")) {
                if (checkPlayer > 0 || partyJoin > 0) {
                    chatReceivedEvent.setCanceled(true);
                }
                if (partyJoin == 1) partyJoin = 2;
                if (checkPlayer == 2) checkPlayer = 3;
                members.clear();
            } else if (str.startsWith("§cYou are not currently in a party.§r")) {
                members.clear();
                if (partyJoin > 0) {
                    partyJoin = 2;
                    chatReceivedEvent.setCanceled(true);
                }
                if (invitedDash > 0) invitedDash = 3;
                if (invitedDash > 0) chatReceivedEvent.setCanceled(true);
                setPartyID(null);
            } else if (TextUtils.stripColor(str).trim().isEmpty()) {
                if ((checkPlayer > 0 || partyJoin > 0) && partyJoin != 100) {
                    chatReceivedEvent.setCanceled(true);
                }
            } else if (str.startsWith("§cYou are not in a party")) {
                members.clear();
                if (partyJoin > 0) {
                    partyJoin = 2;
                    chatReceivedEvent.setCanceled(true);
                }
                if (invitedDash > 0) invitedDash = 3;
                if (invitedDash > 0) chatReceivedEvent.setCanceled(true);
                setPartyID(null);
            } else if (str.startsWith("§eParty ") && str.contains(":")) {
                if (checkPlayer > 0 || partyJoin > 0) {
                    chatReceivedEvent.setCanceled(true);
                }
                String playerNames = TextUtils.stripColor(str.split(":")[1]);
                for (String s : playerNames.split(" ")) {
                    if (s.isEmpty()) continue;
                    if (s.equals("●")) continue;
                    if (s.startsWith("[")) continue;
                    members.add(s);
                }
            } else if (str.equals("§cYou are not allowed to invite players.§r")) {
                if (invitedDash > 0) invitedDash = 3;
                if (invitedDash > 0) chatReceivedEvent.setCanceled(true);
                canInvite = false;
                allowAskToJoin = false;
                askToJoinSecret = "";
            } else if (str.equals("§cCouldn't find a player with that name!§r")) {
                canInvite = true;
                if (invitedDash > 0) invitedDash = 3;
                if (invitedDash > 0) chatReceivedEvent.setCanceled(true);
            } else if (str.equals("§cYou cannot invite that player since they're not online.")) {
                if (invitedDash > 0) invitedDash = 3;
                if (invitedDash > 0) chatReceivedEvent.setCanceled(true);
                canInvite = true;
            } else if (str.endsWith("§aenabled All Invite§r")) {
                canInvite = true;
            } else if (str.endsWith("§cdisabled All Invite§r")) {
                canInvite = false;
                allowAskToJoin = false;
                askToJoinSecret = "";
                sendChat.add(new Tuple<>("/p invite -", () -> {invitedDash = 1;}));
            } else if (str.endsWith("§r§eto Party Moderator§r")) {
                // §b[MVP§r§f+§r§b] apotato321§r§e has promoted §r§a[VIP§r§6+§r§a] syeyoung §r§eto Party Moderator§r
                String[] thetext = TextUtils.stripColor(str).split(" ");
                int seenThings = 0;
                for (String s : thetext) {
                    if (s.equals("has") && seenThings == 0) seenThings = 1;
                    else if (s.equals("promoted") && seenThings == 1) seenThings = 2;
                    else if (s.equals("[")) continue;
                    else if (seenThings == 2) {
                        if (s.equals(Minecraft.getMinecraft().getSession().getUsername())) {
                            canInvite = true;
                        } else {
                            sendChat.add(new Tuple<>("/p invite -", () -> {invitedDash = 1;}));
                            break;
                        }
                    } else {
                        seenThings = 0;
                    }
                }
            } else if (str.startsWith("§eThe party was transferred to ")) {
                //§eThe party was transferred to §r§b[MVP§r§f+§r§b] apotato321 §r§eby §r§a[VIP§r§6+§r§a] syeyoung§r
                String[] thetext = TextUtils.stripColor(str.substring(31)).split(" ");
                String asd = null;
                for (String s : thetext) {
                    if (s.startsWith("[")) continue;
                    asd = s;
                    break;
                }
                if (asd != null && Minecraft.getMinecraft().getSession().getUsername().equalsIgnoreCase(asd)) {
                    canInvite = true;
                } else {
                    sendChat.add(new Tuple<>("/p invite -", () -> {invitedDash = 1;}));
                }
            } else if (str.endsWith("§eto Party Leader§r")) {
                // §a[VIP§r§6+§r§a] syeyoung§r§e has promoted §r§b[MVP§r§f+§r§b] apotato321 §r§eto Party Leader§r
                String[] thetext = TextUtils.stripColor(str).split(" ");
                int seenThings = 0;
                for (String s : thetext) {
                    if (s.equals("has") && seenThings == 0) seenThings = 1;
                    else if (s.equals("promoted") && seenThings == 1) seenThings = 2;
                    else if (s.equals("[")) continue;
                    else if (seenThings == 2) {
                        if (s.equals(Minecraft.getMinecraft().getSession().getUsername())) {
                            canInvite = true;
                        } else {
                            sendChat.add(new Tuple<>("/p invite -", () -> {invitedDash = 1;}));
                            break;
                        }
                    } else {
                        seenThings = 0;
                    }
                }
            } else if (str.endsWith("§r§eto Party Member§r")) {
                String[] thetext = TextUtils.stripColor(str).split(" ");
                int seenThings = 0;
                for (String s : thetext) {
                    if (s.equals("has") && seenThings == 0) seenThings = 1;
                    else if (s.equals("demoted") && seenThings == 1) seenThings = 2;
                    else if (s.equals("[")) continue;
                    else if (seenThings == 2) {
                        if (s.equals(Minecraft.getMinecraft().getSession().getUsername())) {
                            sendChat.add(new Tuple<>("/p invite -", () -> {invitedDash = 1;}));
                            canInvite = false;
                            break;
                        }
                    } else {
                        seenThings = 0;
                    }
                }
            }
        } catch (Exception ex) {ex.printStackTrace();
        DungeonsGuide.sendDebugChat(new ChatComponentText("ERRORRR!! on chat "+ ex));}
    }
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent clientTickEvent) {
        try {
            if (clientTickEvent.phase == TickEvent.Phase.START && Minecraft.getMinecraft().thePlayer != null && minimumNext < System.currentTimeMillis()) {
                if (checkPlayer == 1) {
                    checkPlayer = -1;
                    sendChat.add(new Tuple<>("/pl", () -> {
                        checkPlayer = 2;
                    }));
                }
                if (!sendChat.isEmpty()) {
                    Tuple<String, Runnable> tuple = sendChat.poll();
                    Minecraft.getMinecraft().thePlayer.sendChatMessage(tuple.getFirst());
                    if (tuple.getSecond() != null)
                        tuple.getSecond().run();
                    minimumNext = System.currentTimeMillis() + 200;
                    DungeonsGuide.sendDebugChat(new ChatComponentText("Sending " + tuple.getFirst() + " Secretly"));
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onHypixelJoin(HypixelJoinedEvent skyblockJoinedEvent) {
        minimumNext = System.currentTimeMillis() + 1000;
        sendChat.add(new Tuple<>("/pl", () -> {partyJoin = 1;}));
    }

    public void requestPartyRetrieval() {
        sendChat.add(new Tuple<>("/pl", () -> {partyJoin = 1;}));
    }

    private int checkPlayer = 0;
    private JSONObject theObject;
    private long minimumNext = 0;

    public static Queue<Tuple<String, Runnable>> sendChat = new ConcurrentLinkedQueue<>();

    @Override
    public void handle(StompInterface stompInterface, StompPayload stompPayload) {
        JSONObject object = new JSONObject(stompPayload.payload());
        if ("/user/queue/party.check".equals(stompPayload.headers().get("destination"))) {
            checkPlayer = 1;
            theObject = object;
        } else if ("/user/queue/party.join".equals(stompPayload.headers().get("destination"))) {
            String playerName = object.getString("player");
            String secret = object.getString("secret");
            if (secret.equals(askToJoinSecret) && partyID != null) {
                sendChat .add(new Tuple<>("/p invite "+playerName, null));
            }
        } else if ("/user/queue/party.broadcast".equals(stompPayload.headers().get("destination"))) {
            try {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: Message Broadcasted from player:: \n" + new JSONObject(stompPayload.payload()).getString("payload")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String str = object.getString("status");
            if ("success".equals(str)) {
                setPartyID(object.getString("partyId"));
            } else {
                setPartyID(null);
            }
        }
    }

    @SubscribeEvent
    public void stompConnect(StompConnectedEvent stompConnectedEvent) {
        stompConnectedEvent.getStompInterface().subscribe(StompSubscription.builder()
        .stompMessageHandler(this).ackMode(StompSubscription.AckMode.AUTO).destination("/user/queue/party.resp").build());
        stompConnectedEvent.getStompInterface().subscribe(StompSubscription.builder()
                .stompMessageHandler(this).ackMode(StompSubscription.AckMode.AUTO).destination("/user/queue/party.check").build());
        stompConnectedEvent.getStompInterface().subscribe(StompSubscription.builder()
                .stompMessageHandler(this).ackMode(StompSubscription.AckMode.AUTO).destination("/user/queue/party.join").build());
        stompConnectedEvent.getStompInterface().subscribe(StompSubscription.builder()
                .stompMessageHandler(this).ackMode(StompSubscription.AckMode.AUTO).destination("/user/queue/party.broadcast").build());
    }
}
