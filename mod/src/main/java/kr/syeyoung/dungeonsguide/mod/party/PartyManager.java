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

package kr.syeyoung.dungeonsguide.mod.party;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessResult;
import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.mod.chat.ChatSubscriber;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.events.impl.HypixelJoinedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.StompConnectedEvent;
import kr.syeyoung.dungeonsguide.mod.features.impl.advanced.FeatureTestPeople;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.stomp.StompHeader;
import kr.syeyoung.dungeonsguide.mod.stomp.StompManager;
import kr.syeyoung.dungeonsguide.mod.stomp.StompPayload;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PartyManager {
    public static final PartyManager INSTANCE = new PartyManager();
    @Getter
    private PartyContext partyContext;

    public PartyContext getPartyContext(boolean createIfNeeded) {
        PartyContext pc =  partyContext == null && createIfNeeded ? partyContext = new PartyContext() : partyContext;
        if (createIfNeeded)
            pc.addRawMember(Minecraft.getMinecraft().getSession().getUsername());
        return pc;
    }

    @Getter
    @Setter
    private int maxParty = 5;
    @Getter
    private String askToJoinSecret = null;

    private static final SecureRandom random = new SecureRandom();
    private static final String validChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";


    private Set<Consumer<PartyContext>> partyBuiltCallback = new HashSet<>();


    private final MessageMatcher NOT_IN_PARTY;
    private final MessageMatcher PARTY_CHANNEL;
    private final MessageMatcher TRANSFER_LEFT;
    private final MessageMatcher ALL_INVITE_ON;
    private final MessageMatcher ALL_INVITE_OFF;
    private final MessageMatcher PARTY_JOIN;
    private final MessageMatcher PARTY_LEAVE;
    private final MessageMatcher INVITED;
    private final MessageMatcher INVITE_PERM;
    private final MessageMatcher TRANSFER;
    private final MessageMatcher PROMOTE_LEADER;
    private final MessageMatcher PROMOTE_MODERATOR;
    private final MessageMatcher MEMBER;
    private final MessageMatcher ACCEPT_INVITE_LEADER;
    private final MessageMatcher ACCEPT_INVITE_MEMBERS;

    private MessageMatcher createMatcher(JSONObject object, String key) {
        return new MessageMatcher(object.getJSONArray(key).toList().stream()
                .filter(a -> a instanceof String)
                .map(a -> (String) a)
                .collect(Collectors.toList()));
    }

    private String processName(String name) {
        String username = null;
        for (String s : TextUtils.stripColor(name).split(" ")) {
            if (s.startsWith("[")) continue;
            username = s;
            break;
        }
        return username;
    }

    public PartyManager() {
        try {
            JSONObject jsonObject = new JSONObject(IOUtils.toString(Objects.requireNonNull(DungeonsGuide.class.getResourceAsStream("/party_languages.json")), StandardCharsets.UTF_8));
            NOT_IN_PARTY = createMatcher(jsonObject, "not_in_party");
            PARTY_CHANNEL = createMatcher(jsonObject, "party_channel");
            ALL_INVITE_ON = createMatcher(jsonObject, "all_invite_on");
            ALL_INVITE_OFF = createMatcher(jsonObject, "all_invite_off");
            PARTY_JOIN = createMatcher(jsonObject, "party_join");
            PARTY_LEAVE = createMatcher(jsonObject, "party_leave");
            INVITED = createMatcher(jsonObject, "invited");
            INVITE_PERM = createMatcher(jsonObject, "invite_perm");
            TRANSFER = createMatcher(jsonObject, "transfer");
            TRANSFER_LEFT = createMatcher(jsonObject, "transfer_left");
            PROMOTE_LEADER = createMatcher(jsonObject, "promote_leader");
            PROMOTE_MODERATOR = createMatcher(jsonObject, "promote_moderator");
            MEMBER = createMatcher(jsonObject, "member");
            ACCEPT_INVITE_LEADER = createMatcher(jsonObject, "accept_invite_leader");
            ACCEPT_INVITE_MEMBERS = createMatcher(jsonObject, "accept_invite_members");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        ChatProcessor cp = ChatProcessor.INSTANCE;
        // Not in Party
        cp.subscribe(new ChatSubscriber() {
            @Override
            public ChatProcessResult process(String str, Map<String, Object> a) {
                if (NOT_IN_PARTY.match(str, null)) {
                    PartyManager.this.leaveParty();

                    for (Consumer<PartyContext> partyContextConsumer : partyBuiltCallback) {
                        try {
                            partyContextConsumer.accept(null);
                        } catch (Exception e) {
                            FeatureCollectDiagnostics.queueSendLogAsync(e);
                            e.printStackTrace();
                        }
                    }
                    partyBuiltCallback.clear();
                    a.put("type", "notinparty");
                }
                return ChatProcessResult.NONE;
            }
        });
        // All invite
        cp.subscribe(new ChatSubscriber() {
            @Override
            public ChatProcessResult process(String str, Map<String, Object> a) {
                if (ALL_INVITE_ON.match(str, null)) {
                    PartyManager.this.getPartyContext(true).setAllInvite(true);
                    a.put("type", "allinvite_on");
                } else if (ALL_INVITE_OFF.match(str, null)) {
                    PartyManager.this.getPartyContext(true).setAllInvite(false);
                    a.put("type", "allinvite_off");
                    PartyManager.this.potentialInvitenessChange();
                }
                return ChatProcessResult.NONE;
            }
        });
        // Member building
        cp.subscribe(new ChatSubscriber() {
            boolean memberExpected;
            PartyContext partyContext = new PartyContext();
            @Override
            public ChatProcessResult process(String txt, Map<String, Object> context) {
                if (txt.startsWith("§6Party Members ")) {
                    memberExpected = true;
                    partyContext = new PartyContext();
                    partyContext.setPartyModerator(new TreeSet<>(String.CASE_INSENSITIVE_ORDER));
                    partyContext.setPartyMember(new TreeSet<>(String.CASE_INSENSITIVE_ORDER));
                    context.put("type", "member_start");
                } else if (txt.startsWith("§eParty ") && txt.contains(":")){
                    String role = txt.split(":")[0];
                    String playerNames = TextUtils.stripColor(txt.split(":")[1]);
                    for (String s : playerNames.split(" ")) {
                        if (s.isEmpty()) continue;
                        if (s.equals("●")) continue;
                        if (s.startsWith("[")) continue;
                        partyContext.addRawMember(s);
                        if (role.contains("Moder")) partyContext.addPartyModerator(s);
                        if (role.contains("Member")) partyContext.addPartyMember(s);
                        if (role.contains("Leader")) partyContext.setPartyOwner(s);
                    }
                    if (role.contains("Moder")) {
                        partyContext.setModeratorComplete(true);
                        context.put("type", "member_moder");
                    }
                    if (role.contains("Member")) {
                        partyContext.setMemberComplete(true);
                        context.put("type", "member_member");
                    }
                    if (role.contains("Leader")) {
                        context.put("type", "member_leader");
                    }
                } else if (txt.startsWith("§9§m---------------------------")) {
                    if (memberExpected) {
                        PartyContext old = getPartyContext(true);
                        old.setPartyOwner(partyContext.getPartyOwner());
                        old.setPartyModerator(partyContext.getPartyModerator());
                        old.setPartyMember(partyContext.getPartyMember());
                        old.setPartyRawMembers(new TreeSet<>(String.CASE_INSENSITIVE_ORDER));
                        old.getPartyRawMembers().addAll(old.getPartyMember());
                        old.getPartyRawMembers().addAll(old.getPartyModerator());
                        old.getPartyRawMembers().add(old.getPartyOwner());
                        old.setModeratorComplete(true); old.setMemberComplete(true);
                        old.setRawMemberComplete(true);
                        old.setPartyExistHypixel(true);

                        memberExpected = false;
                        context.put("type", "member_end");

                        for (Consumer<PartyContext> partyContextConsumer : partyBuiltCallback) {
                            try {
                                partyContextConsumer.accept(partyContext);
                            } catch (Exception e) {
                                FeatureCollectDiagnostics.queueSendLogAsync(e);
                                e.printStackTrace();
                            }
                        }
                        partyBuiltCallback.clear();

                        if (old.getPartyID() == null) {
                            joinedParty();
                        }
                        potentialInvitenessChange();
                    }
                }
                return ChatProcessResult.NONE;
            }
        });
        // Player party join / leave
        cp.subscribe(new ChatSubscriber() {
            Map<String, String> matches = new HashMap<>();
            @Override
            public ChatProcessResult process(String str, Map<String, Object> a) {
                if (PARTY_JOIN.match(str, matches)) {
                    String username = processName(matches.get("1"));
                    if (username != null) {
                        PartyManager.this.getPartyContext(true).addPartyMember(username);
                    }
                    a.put("type", "party_join");
                } else if (PARTY_LEAVE.match(str, matches)) {
                    String username = processName(matches.getOrDefault("1", matches.get("2")));
                    if (username != null && partyContext != null) {
                        PartyManager.this.getPartyContext().removeFromParty(username);
                    }
                    a.put("type", "party_leave");
                } else if (INVITED.match(str, matches)) {
                    String inviter = processName(matches.get("0"));
                    if (inviter != null && partyContext != null) {
                        if (PartyManager.this.getPartyContext().hasMember(inviter)) {
                            PartyManager.this.getPartyContext().setAllInvite(true);
                        }
                    }
                    PartyManager.this.getPartyContext(true).setPartyExistHypixel(true);
                    a.put("type", "party_invite_exist");
                } else if (INVITE_PERM.match(str, null)) {
                    a.put("type", "party_invite_noexist");
                    String username = Minecraft.getMinecraft().getSession().getUsername();
                    if (partyContext != null && PartyManager.this.getPartyContext().hasMember(username)) {
                        PartyManager.this.getPartyContext().setAllInvite(true);
                    }
                }
                return ChatProcessResult.NONE;
            }
        });
        // Promotion
        cp.subscribe(new ChatSubscriber() {
            Map<String, String> matches = new HashMap<>();
            @Override
            public ChatProcessResult process(String str, Map<String, Object> a) {
                if (TRANSFER.match(str, matches)) {
                    String newLeader = processName(matches.get("0"));
                    String oldLeader = processName(matches.get("1"));

                    if (oldLeader != null && newLeader != null) {
                        PartyManager.this.getPartyContext(true).setPartyOwner(newLeader);
                        PartyManager.this.getPartyContext(true).addPartyModerator(oldLeader);
                    }
                    a.put("type", "party_transfer");
                    PartyManager.this.potentialInvitenessChange();
                } else if (TRANSFER_LEFT.match(str, matches)) {
                    String newLeader = processName(matches.get("0"));
                    String oldLeader = processName(matches.get("1"));

                    if (oldLeader != null && newLeader != null) {
                        PartyManager.this.getPartyContext(true).setPartyOwner(newLeader);
                        PartyManager.this.getPartyContext(true).removeFromParty(oldLeader);
                    }
                    a.put("type", "party_transfer");
                    PartyManager.this.potentialInvitenessChange();
                } else if (PROMOTE_LEADER.match(str, matches)) {
                    String oldLeader = processName(matches.get("0"));
                    String newLeader = processName(matches.get("1"));

                    if (oldLeader != null && newLeader != null) {
                        PartyManager.this.getPartyContext(true).setPartyOwner(newLeader);
                        PartyManager.this.getPartyContext(true).addPartyModerator(oldLeader);
                    }
                    a.put("type", "party_transfer");
                    PartyManager.this.potentialInvitenessChange();
                } else if (PROMOTE_MODERATOR.match(str, matches)) {
                    String oldLeader = processName(matches.get("0"));
                    String newModerator = processName(matches.get("1"));

                    if (oldLeader != null && newModerator != null) {
                        PartyManager.this.getPartyContext(true).setPartyOwner(oldLeader);
                        PartyManager.this.getPartyContext(true).addPartyModerator(newModerator);
                    }
                    a.put("type", "party_promotion");
                    PartyManager.this.potentialInvitenessChange();
                } else if (MEMBER.match(str, matches)) {
                    String oldLeader = processName(matches.get("1"));
                    String newMember = processName(matches.get("0"));

                    if (oldLeader != null && newMember != null) {
                        PartyManager.this.getPartyContext(true).setPartyOwner(oldLeader);
                        PartyManager.this.getPartyContext(true).addPartyMember(newMember);
                    }
                    a.put("type", "party_demotion");
                    PartyManager.this.potentialInvitenessChange();
                }
                return ChatProcessResult.NONE;
            }
        });
        // Player Join
        cp.subscribe(new ChatSubscriber() {
            boolean joined;
            Map<String, String> matches = new HashMap<>();
            @Override
            public ChatProcessResult process(String str, Map<String, Object> context) {
                if (ACCEPT_INVITE_LEADER.match(str, matches)) {
                    String leader = processName(matches.get("1"));

                    leader = leader.substring(0, leader.length()-2); // remove 's
                    partyContext = new PartyContext();
                    getPartyContext().setPartyOwner(leader);
                    getPartyContext().addPartyMember(Minecraft.getMinecraft().getSession().getUsername());
                    context.put("type", "party_selfjoin_leader");
                    joined=  true;
                } else if (ACCEPT_INVITE_MEMBERS.match(str, matches)) {
                    String[] players = TextUtils.stripColor(matches.get("2")).split(" ");
                    for (String player : players) {
                        if (player.startsWith("[")) continue;
                        getPartyContext().addRawMember(player);
                    }
                    context.put("type", "party_selfjoin_players");
                } else if (str.startsWith("§9§m---------------------------") && joined) {
                    joined = false;
                    getPartyContext().setRawMemberComplete(true);
                    joinedParty();
                    potentialInvitenessChange();
                }
                return ChatProcessResult.NONE;
        }});
        // Player Join Dungon
        cp.subscribe(new ChatSubscriber() {
            @Override
            public ChatProcessResult process(String str, Map<String, Object> a) {
                if (str.contains("§r§ejoined the dungeon group! (§r§b")) {
                    String username = TextUtils.stripColor(str).split(" ")[3];
                    if (username.equalsIgnoreCase(Minecraft.getMinecraft().getSession().getUsername())) {
                        partyContext = new PartyContext();
                        PartyManager.this.requestPartyList((str2) -> {
                            PartyManager.this.potentialInvitenessChange();
                        });
                    } else {
                        PartyManager.this.getPartyContext(true).setMemberComplete(false);
                        PartyManager.this.requestPartyList((str2) -> {
                        });
                    }
                }
                return ChatProcessResult.NONE;
            }
        });
    }

    public void toggleAllowAskToJoin() {
        if (canInvite()) {
            if (askToJoinSecret != null) askToJoinSecret = null;
            else {
                updateAskToJoin();
            }
        }
    }

    public void updateAskToJoin() {
        StringBuilder secretBuilder = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            secretBuilder.append(validChars.charAt(random.nextInt(validChars.length())));
        }
        askToJoinSecret = secretBuilder.toString();

        StompManager.getInstance().send(new StompPayload().payload(new JSONObject().put("secret", askToJoinSecret).toString()).destination("/app/party.setjoinsecret"));
    }

    public static ChatSubscriber dashShredder() {
        return (str, a) -> (int)a.get("removed") == 0 && str.startsWith("§9§m---------------------------") ? ChatProcessResult.REMOVE_LISTENER_AND_CHAT : ChatProcessResult.NONE;
    }

    public static ChatSubscriber typeShredder(boolean end, String... types) {
        return (str, a) -> (int)a.get("removed") == 0 &&Arrays.stream(types).anyMatch(s -> s.equals(a.getOrDefault("type", null))) ? (end ? ChatProcessResult.REMOVE_LISTENER_AND_CHAT : ChatProcessResult.REMOVE_CHAT) : ChatProcessResult.NONE;
    }

    public static ChatSubscriber combinedShredder(ChatSubscriber... chatSubscribers) {
        return (str, a) -> {
            boolean removeChat = false;
            boolean removeListener = false;
            for (ChatSubscriber chatSubscriber : chatSubscribers) {
                ChatProcessResult chatProcessResult = chatSubscriber.process(str, a);
                if (chatProcessResult.isRemoveChat()) removeChat = true;
                if (chatProcessResult.isRemoveListener()) removeListener = true;
            }
            return (removeChat && removeListener) ? ChatProcessResult.REMOVE_LISTENER_AND_CHAT : (removeChat ? ChatProcessResult.REMOVE_CHAT : (removeListener ? ChatProcessResult.REMOVE_LISTENER : ChatProcessResult.NONE));
        };
    }

    @SubscribeEvent
    public void onHypixelJoin(HypixelJoinedEvent skyblockJoinedEvent) {
        partyContext = null;
        requestPartyList((a) -> {
            if (a == null) return;
            if (isLeader() || isModerator()) return;
            if (a.getAllInvite() != null) return;
            requestAllInvite();
        });
    }

    private void leaveParty() {


        if (partyContext != null) {
            getPartyContext().setPartyExistHypixel(false);
            if (getPartyContext().isSelfSolo()) return;
            if (getPartyContext().getPartyID() != null) {
                JSONObject object = new JSONObject();
                object.put("partyid", getPartyContext().getPartyID());
                if (StompManager.getInstance().isStompConnected())
                    StompManager.getInstance().send(new StompPayload().payload(object.toString()).destination( "/app/party.leave"));
            }
        }

        partyContext = new PartyContext();
        playerInvAntiSpam.clear();

        getPartyContext().setPartyExistHypixel(false);
        getPartyContext().setPartyOwner(Minecraft.getMinecraft().getSession().getUsername());
        getPartyContext().setPartyModerator(new TreeSet<>(String.CASE_INSENSITIVE_ORDER)); getPartyContext().setMemberComplete(true);
        getPartyContext().setPartyMember(new TreeSet<>(String.CASE_INSENSITIVE_ORDER)); getPartyContext().setModeratorComplete(true);
        getPartyContext().setAllInvite(false);
        joinedParty();

    }
    private void joinedParty() {
        // TODO: Figure out what to do if stomp faield
        if (StompManager.getInstance().isStompConnected()) {
            JSONArray jsonArray = new JSONArray();
            for (String member : getPartyContext().getPartyRawMembers()) {
                jsonArray.put(member);
            }
            JSONObject object = new JSONObject();
            object.put("members", jsonArray);
            StompManager.getInstance().send(new StompPayload().payload(object.toString()).destination("/app/party.join"));

            getPartyContext().setPartyID("!@#!@#!@#..........FETCHING..........$!@$!@$!@$"+UUID.randomUUID().toString());
        } else {
            getPartyContext().setPartyID("!@#!@#!@#..........UNABLE TO FETCH..........$!@$!@$!@$"+UUID.randomUUID().toString());
        }
    }

    public boolean isLeader() {
        return partyContext != null && getPartyContext().hasLeader(Minecraft.getMinecraft().getSession().getUsername()); // "getUsername"
    }
    public boolean isModerator() {
        return partyContext != null && getPartyContext().hasModerator(Minecraft.getMinecraft().getSession().getUsername());
    }
    public boolean canInvite() {
        return isLeader() || isModerator() || (partyContext != null && getPartyContext().getAllInvite() != null && getPartyContext().getAllInvite());
    }

    private boolean requested = false;
    public void requestPartyList(Consumer<PartyContext> onPartyCallback) {
        if (requested) {
            partyBuiltCallback.add(onPartyCallback);
            return;
        }
        requested = true;

        ChatProcessor.INSTANCE.addToChatQueue("/pl", () -> {
            ChatProcessor.INSTANCE.subscribe(dashShredder());
            ChatProcessor.INSTANCE.subscribe(combinedShredder(typeShredder(true, "member_end"), dashShredder(), typeShredder(false,"notinparty", "member_start", "member_moder", "member_leader", "member_member")));
        }, true);
        partyBuiltCallback.add(onPartyCallback);
        partyBuiltCallback.add(pc -> requested=false);
    }

    public void requestAllInvite() {
        if (isLeader() || isModerator()) return;
        if (partyContext != null && getPartyContext().getAllInvite() != null) return;

        ChatProcessor.INSTANCE.addToChatQueue("/p invite -", () -> {
            ChatProcessor.INSTANCE.subscribe(dashShredder());
            ChatProcessor.INSTANCE.subscribe(typeShredder(true, "notinparty", "allinvite_off", "party_invite_noexist"));
            ChatProcessor.INSTANCE.subscribe(dashShredder());
        }, true);
    }

    private Map<String, Long> playerInvAntiSpam = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @SubscribeEvent
    public void stompConnect(StompConnectedEvent event) {

        event.getStompInterface().subscribe("/user/queue/party.resp", (stompClient ,payload) -> {
            JSONObject object = new JSONObject(payload);

            String str = object.getString("status");
            if ("success".equals(str) && partyContext != null) {
                getPartyContext().setPartyID(object.getString("partyId"));
                if (askToJoinSecret != null) {
                    updateAskToJoin();
                }
            } else if (partyContext != null){
                getPartyContext().setPartyID(null);
            }
        });

        event.getStompInterface().subscribe("/user/queue/party.check", (stompClient ,payload) -> {
            JSONObject object = new JSONObject(payload);
            String playerName = object.getString("player");
            String token = object.getString("token");
            if (partyContext == null) {
                requestPartyList((pc) -> {
                    boolean contains = pc != null && pc.getPartyRawMembers().contains(playerName);
                    if (!contains) {
                        StompManager.getInstance().send(new StompPayload().payload(new JSONObject().put("status", "failure").put("token", token).toString()).destination("/app/party.check.resp"));
                    } else {
                        StompManager.getInstance().send(new StompPayload().payload(new JSONObject().put("status", "success").put("token", token).toString()).destination("/app/party.check.resp"));
                    }
                });
            } else {
                if (getPartyContext().getPartyRawMembers().contains(playerName)) {
                    StompManager.getInstance().send(new StompPayload().payload(new JSONObject().put("status", "success").put("token", token).toString()).destination("/app/party.check.resp"));
                } else if (getPartyContext().isMemberComplete() && getPartyContext().isModeratorComplete() && getPartyContext().getPartyOwner() != null) {
                    StompManager.getInstance().send(new StompPayload().payload(new JSONObject().put("status", "failure").put("token", token).toString()).destination("/app/party.check.resp"));
                } else {
                    requestPartyList((pc) -> {
                        boolean contains = pc != null && pc.getPartyRawMembers().contains(playerName);
                        if (!contains) {
                            StompManager.getInstance().send(new StompPayload().payload(new JSONObject().put("status", "failure").put("token", token).toString()).destination("/app/party.check.resp"));
                        } else {
                            StompManager.getInstance().send(new StompPayload().payload(new JSONObject().put("status", "success").put("token", token).toString()).destination("/app/party.check.resp"));
                        }
                    });
                }
            }
        });
        event.getStompInterface().subscribe("/user/queue/party.broadcast", (stompClient ,payload) -> {
            String broadCastPlayload = new JSONObject(payload).getString("payload");
            System.out.println("Received broadcast");
            if(broadCastPlayload.startsWith("C:")) {
                FeatureTestPeople.handlePartyBroadCast(broadCastPlayload);
            }else {
                try {
                    ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: Message Broadcasted from player:: \n" + new JSONObject(payload).getString("payload")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        event.getStompInterface().subscribe("/user/queue/party.join", (stompClient ,payload) -> {
            JSONObject object = new JSONObject(payload);
            String playerName = object.getString("player");
            String secret = object.getString("secret");
            if (secret.equals(askToJoinSecret) && partyContext != null && getPartyContext().getPartyRawMembers().size() < maxParty && playerInvAntiSpam.getOrDefault(playerName, 0L)  < System.currentTimeMillis() - 5000) {
                playerInvAntiSpam.put(playerName, System.currentTimeMillis());
                ChatProcessor.INSTANCE.addToChatQueue("/p invite "+playerName,() -> {}, true);
            }
        });
        event.getStompInterface().subscribe("/user/queue/party.askedtojoin.resp", (stompClient ,payload) -> {
            JSONObject object = new JSONObject(payload);
            String invFrom = object.getString("username");
            String token2 = object.getString("token");
            if (!token2.equals(lastToken)) return;
            lastToken = null;
            ChatProcessor.INSTANCE.addToChatQueue("/p accept "+invFrom, () -> {}, true);
            long end = System.currentTimeMillis() + 3000;
            ChatProcessor.INSTANCE.subscribe((str, a) -> {
                if (!str.contains("§r§ehas invited you to join their party!")) return System.currentTimeMillis() > end ? ChatProcessResult.REMOVE_LISTENER : ChatProcessResult.NONE;
                String[] messageSplit = TextUtils.stripColor(str).split(" ");
                String inviter = null;
                for (String s : messageSplit) {
                    if (s.startsWith("[")) continue;
                    if (s.startsWith("-")) continue;;
                    inviter = s;
                    break;
                }
                if (invFrom.equalsIgnoreCase(inviter)) {
                    ChatProcessor.INSTANCE.addToChatQueue("/p accept "+invFrom, () -> {}, true);
                }
                return ChatProcessResult.NONE;
            });
        });


    }

    private String lastToken;
    public void joinWithToken(String secret) {
        lastToken = secret;
        if (partyContext != null && getPartyContext().isPartyExistHypixel())
            ChatProcessor.INSTANCE.addToChatQueue("/p leave", () -> {}, true);
        StompManager.getInstance().send(new StompPayload().method(StompHeader.SEND)
                .destination("/app/party.askedtojoin")
                .payload(new JSONObject().put("token", secret).toString()));
    }

    private void potentialInvitenessChange() {
        if (askToJoinSecret != null && !canInvite()) askToJoinSecret = null;
    }
}
