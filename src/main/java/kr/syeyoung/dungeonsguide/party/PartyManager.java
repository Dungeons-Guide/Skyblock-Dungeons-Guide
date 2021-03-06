package kr.syeyoung.dungeonsguide.party;

import kr.syeyoung.dungeonsguide.RichPresenceManager;
import kr.syeyoung.dungeonsguide.commands.CommandReparty;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.events.SkyblockJoinedEvent;
import kr.syeyoung.dungeonsguide.events.StompConnectedEvent;
import kr.syeyoung.dungeonsguide.stomp.StompInterface;
import kr.syeyoung.dungeonsguide.stomp.StompMessageHandler;
import kr.syeyoung.dungeonsguide.stomp.StompPayload;
import kr.syeyoung.dungeonsguide.stomp.StompSubscription;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import scala.util.parsing.json.JSON;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PartyManager implements StompMessageHandler {
    public static final PartyManager INSTANCE = new PartyManager();

    @Getter
    private String partyID = null;
    @Getter
    private String askToJoinSecret = null;

    private SecureRandom random = new SecureRandom();
    private static final String validChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";

    @Getter
    private boolean allowAskToJoin = false;
    @Getter
    private boolean canInvite = false;

    public void toggleAllowAskToJoin() {
        if (canInvite) allowAskToJoin = !allowAskToJoin;
        if (allowAskToJoin) {
            generateNewAskToJoinSecret();
        }
    }

    public void setPartyID(String partyID) {
        if (this.partyID != null && partyID == null) {
            JSONObject object = new JSONObject();
            object.put("members", new JSONArray());
            StompInterface stompInterface = e.getDungeonsGuide().getStompConnection();
            stompInterface.send(new StompPayload().payload(object.toString()).header("destination", "/app/party.join"));
        }
        this.partyID = partyID;
        this.askToJoinSecret = null;

        if (partyID != null && !partyID.equals(this.partyID)) {
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/p invite -");
        } else {
            canInvite = true;
            allowAskToJoin = false;
        }

        if (allowAskToJoin) {
            generateNewAskToJoinSecret();
        } else {
            RichPresenceManager.INSTANCE.updatePresence();
        }
    }

    public void generateNewAskToJoinSecret() {
        if (partyID == null) {
            JSONObject object = new JSONObject();
            object.put("members", new JSONArray());
            StompInterface stompInterface = e.getDungeonsGuide().getStompConnection();
            stompInterface.send(new StompPayload().payload(object.toString()).header("destination", "/app/party.join"));
        }

        StringBuilder secretBuilder = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            secretBuilder.append(validChars.charAt(random.nextInt(validChars.length())));
        }
        this.askToJoinSecret = secretBuilder.toString();

        StompInterface stompInterface = e.getDungeonsGuide().getStompConnection();
        stompInterface.send(new StompPayload().payload(new JSONObject().put("secret", askToJoinSecret).toString()).header("destination", "/app/party.setjoinsecret"));
        RichPresenceManager.INSTANCE.updatePresence();
    }

    private boolean partyJoin = false;
    private Set<String> members = new HashSet<>();
    private Map<String, Long> recentlyJoined = new HashMap<>();
    @SubscribeEvent
    public void onMessage(ClientChatReceivedEvent chatReceivedEvent) {
        if (chatReceivedEvent.type == 2) return;

        String str = chatReceivedEvent.message.getFormattedText();

        if (str.startsWith("§eYou have joined ")) {
            setPartyID(null);
            members.clear();
            String strs[] = TextUtils.stripColor(str).split(" ");
            for (String s : strs) {
                if (s.endsWith("'s")) {
                    members.add(s.substring(0, s.indexOf("'s")));
                    partyJoin = true;
                    break;
                }
            }
        } else if (str.startsWith("§eYou'll be partying with: ")) {
            String[] players = TextUtils.stripColor(str.substring(27)).split(" ");
            for (String player : players) {
                if (player.startsWith("[")) continue;
                members.add(player);
            }
        } else if (str.equals("§9§m-----------------------------§r")) {
            if (partyJoin) {
                partyJoin = false;
                // REQ PARTY JOIN

                JSONArray jsonArray = new JSONArray();
                for (String member : members) {
                    jsonArray.put(member);
                }
                JSONObject object = new JSONObject();
                object.put("members", jsonArray);
                StompInterface stompInterface = e.getDungeonsGuide().getStompConnection();
                stompInterface.send(new StompPayload().payload(object.toString()).header("destination", "/app/party.join"));
            }
        } else if (str.endsWith("§ejoined the party.§r")) {
            String asd = null;
            for (String s : TextUtils.stripColor(str).split(" ")) {
                if (s.startsWith("[")) continue;
                asd = s;
            }
            if (asd != null)
                recentlyJoined.put(asd, System.currentTimeMillis());
        } else if ((str.equals("§eYou left the party.§r")
        || str.equals("§cThe party was disbanded because all invites expired and the party was empty§r"))
        ){
            setPartyID(null);
        } else if (str.startsWith("§6Party Members ")) {
            partyJoin = true;
            members.clear();
        } else if (str.startsWith("§cYou are not currently in a party.§r")) {
            members.clear();
            setPartyID(null);
        } else if (str.startsWith("§eParty ") && str.contains(":")) {
            String playerNames = TextUtils.stripColor(str.split(":")[1]);
            for (String s : playerNames.split(" ")) {
                if (s.isEmpty()) continue;
                if (s.equals("●")) continue;
                if (s.startsWith("[")) continue;
                members.add(s);
            }
        } else if (str.equals("§cYou are not allowed to invite players.§r")) {
            canInvite = false;
            allowAskToJoin = false;
            askToJoinSecret = "";
            RichPresenceManager.INSTANCE.updatePresence();
        } else if (str.equals("§cCouldn't find a player with that name!§r")) {
            canInvite = true;
        } else if (str.endsWith("§aenabled All Invite§r")) {
            canInvite = true;
        } else if (str.endsWith("§cdisabled All Invite§r")) {
            canInvite = false;
            allowAskToJoin = false;
            askToJoinSecret = "";
            RichPresenceManager.INSTANCE.updatePresence();
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/p invite -");
        }
    }

    @SubscribeEvent
    public void onSBJoin(SkyblockJoinedEvent skyblockJoinedEvent) {
        Minecraft.getMinecraft().thePlayer.sendChatMessage("/pl");
    }

    @Override
    public void handle(StompInterface stompInterface, StompPayload stompPayload) {
        JSONObject object = new JSONObject(stompPayload.payload());
        if ("/queue/party.check".equals(stompPayload.headers().get("destination"))) {
            String playerName = object.getString("player");
            String token = object.getString("token");
            Long loong = recentlyJoined.get(playerName);
            if (loong == null) {
                stompInterface.send(new StompPayload().payload(new JSONObject().put("status", "failure").put("token", token).toString()).header("destination", "/app/party.check.resp"));
            } else if (loong > System.currentTimeMillis() - 2000){
                stompInterface.send(new StompPayload().payload(new JSONObject().put("status", "success").put("token", token).toString()).header("destination", "/app/party.check.resp"));
            }
        } else if ("/queue/party.join".equals(stompPayload.headers().get("destination"))) {
            String playerName = object.getString("player");
            String secret = object.getString("secret");
            if (secret.equals(askToJoinSecret) && partyID != null) {
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/p invite "+playerName);
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
    }
}
