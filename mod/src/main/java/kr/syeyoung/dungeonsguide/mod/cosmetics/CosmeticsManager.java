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

package kr.syeyoung.dungeonsguide.mod.cosmetics;


import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.cosmetics.chatdetectors.*;
import kr.syeyoung.dungeonsguide.mod.cosmetics.surgical.ReplacementContext;
import kr.syeyoung.dungeonsguide.mod.cosmetics.surgical.SurgicalReplacer;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGPlayerJoinEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGPlayerQuitEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerListItemPacketEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.StompConnectedEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.player.PlayerManager;
import kr.syeyoung.dungeonsguide.mod.stomp.StompHeader;
import kr.syeyoung.dungeonsguide.mod.stomp.StompManager;
import kr.syeyoung.dungeonsguide.mod.stomp.StompPayload;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.entity.UEntityPlayer;
import kr.syeyoung.modapi.event.ListenerPriority;
import kr.syeyoung.modapi.event.SubscribeEvent;
import kr.syeyoung.modapi.event.events.ChatReceivedEvent;
import kr.syeyoung.modapi.event.events.PlayerNameFormatEvent;
import kr.syeyoung.modapi.event.events.TabNameFormatEvent;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class CosmeticsManager {
    @Getter
    private Map<UUID, CosmeticData> cosmeticDataMap = new ConcurrentHashMap<>();
    @Getter
    private Map<UUID, ActiveCosmetic> activeCosmeticMap = new ConcurrentHashMap<>();
    @Getter
    private Map<String, List<ActiveCosmetic>> activeCosmeticByType = new ConcurrentHashMap<>();
    @Getter
    private Map<UUID, List<ActiveCosmetic>> activeCosmeticByPlayer = new ConcurrentHashMap<>();
    @Getter
    private Map<String, List<ActiveCosmetic>> activeCosmeticByPlayerNameLowerCase = new ConcurrentHashMap<>();
    @Getter
    private Set<String> perms = new CopyOnWriteArraySet<>();

    @Getter
    private Map<String, UUID> nameIdCache = new HashMap<>();

    public void requestActiveCosmetics() {
        StompManager.getInstance().send(new StompPayload()
                .method(StompHeader.SEND)
                .destination("/app/cosmetic.activelist")
        );
    }
    public void requestCosmeticsList() {
        StompManager.getInstance().send(new StompPayload()
                .method(StompHeader.SEND)
                .destination("/app/cosmetic.list")
        );
    }
    public void requestPerms() {
        StompManager.getInstance().send(new StompPayload()
                .method(StompHeader.SEND)
                .destination("/app/user.perms")
        );
    }
    public void setCosmetic(CosmeticData cosmetic) {
        if (!perms.contains(cosmetic.getReqPerm())) return;
        StompManager.getInstance().send(new StompPayload()
                .method(StompHeader.SEND)
                .destination("/app/cosmetic.set")
                .payload(cosmetic.getId().toString())
        );
    }
    public void removeCosmetic(ActiveCosmetic activeCosmetic) {
        StompManager.getInstance().send(new StompPayload()
                .method(StompHeader.SEND)
                .destination("/app/cosmetic.remove")
                .payload(activeCosmetic.getActivityUID().toString())
        );
    }

    private void rebuildCaches() {
        activeCosmeticByType = new HashMap<>();
        activeCosmeticByPlayer = new HashMap<>();
        Map<String, List<ActiveCosmetic>> activeCosmeticByPlayerName = new HashMap<>();
        for (ActiveCosmetic value : activeCosmeticMap.values()) {
            CosmeticData cosmeticData = cosmeticDataMap.get(value.getCosmeticData());
            if (cosmeticData != null) {
                List<ActiveCosmetic> cosmeticsByTypeList = activeCosmeticByType.computeIfAbsent(cosmeticData.getCosmeticType(), a-> new CopyOnWriteArrayList<>());
                cosmeticsByTypeList.add(value);
            }
            List<ActiveCosmetic> activeCosmetics = activeCosmeticByPlayer.computeIfAbsent(value.getPlayerUID(), a-> new CopyOnWriteArrayList<>());
            activeCosmetics.add(value);
            activeCosmetics = activeCosmeticByPlayerName.computeIfAbsent(value.getUsername().toLowerCase(), a-> new CopyOnWriteArrayList<>());
            activeCosmetics.add(value);
        }

        this.activeCosmeticByPlayerNameLowerCase = activeCosmeticByPlayerName;
    }

    @SubscribeEvent
    public void stompConnect(StompConnectedEvent e) {

        e.getStompInterface().subscribe("/topic/cosmetic.set", (stompClient, payload) -> {
            JSONObject jsonObject = new JSONObject(payload);
            ActiveCosmetic activeCosmetic = new ActiveCosmetic();
            activeCosmetic.setActivityUID(UUID.fromString(jsonObject.getString("activityUID")));
            activeCosmetic.setPlayerUID(UUID.fromString(jsonObject.getString("playerUID")));
            if (jsonObject.isNull("cosmeticUID")) {
                ActiveCosmetic activeCosmetic1 = activeCosmeticMap.remove(activeCosmetic.getActivityUID());

                List<ActiveCosmetic> activeCosmetics = activeCosmeticByPlayer.computeIfAbsent(activeCosmetic.getPlayerUID(), a-> new CopyOnWriteArrayList<>());
                activeCosmetics.remove(activeCosmetic1);

                activeCosmetics = activeCosmeticByPlayerNameLowerCase.computeIfAbsent(activeCosmetic1.getUsername().toLowerCase(), a-> new CopyOnWriteArrayList<>());
                activeCosmetics.remove(activeCosmetic1);

                CosmeticData cosmeticData = cosmeticDataMap.get(activeCosmetic.getCosmeticData());
                if (cosmeticData != null) {
                    List<ActiveCosmetic> cosmeticsByTypeList = activeCosmeticByType.computeIfAbsent(cosmeticData.getCosmeticType(), a-> new CopyOnWriteArrayList<>());
                    cosmeticsByTypeList.remove(activeCosmetic1);
                }
            } else {
                activeCosmetic.setCosmeticData(UUID.fromString(jsonObject.getString("cosmeticUID")));
                activeCosmetic.setUsername(jsonObject.getString("username"));

                ActiveCosmetic previousThing = activeCosmeticMap.get(activeCosmetic.getActivityUID());
                activeCosmeticMap.put(activeCosmetic.getActivityUID(), activeCosmetic);

                CosmeticData cosmeticData = cosmeticDataMap.get(activeCosmetic.getCosmeticData());
                if (cosmeticData != null) {
                    List<ActiveCosmetic> cosmeticsByTypeList = activeCosmeticByType.computeIfAbsent(cosmeticData.getCosmeticType(), a-> new CopyOnWriteArrayList<>());
                    cosmeticsByTypeList.add(activeCosmetic);
                    cosmeticsByTypeList.remove(previousThing);
                }
                List<ActiveCosmetic> activeCosmetics = activeCosmeticByPlayer.computeIfAbsent(activeCosmetic.getPlayerUID(), a-> new CopyOnWriteArrayList<>());
                activeCosmetics.add(activeCosmetic);
                activeCosmetics.remove(previousThing);

                activeCosmetics = activeCosmeticByPlayerNameLowerCase.computeIfAbsent(activeCosmetic.getUsername().toLowerCase(), a-> new CopyOnWriteArrayList<>());
                activeCosmetics.add(activeCosmetic);
                activeCosmetics.remove(previousThing);
            }

            refresh(activeCosmetic.getPlayerUID());
        });


        e.getStompInterface().subscribe("/user/queue/reply/user.perms", (stompClient ,payload) -> {
            JSONArray object = new JSONArray(payload);
            Set<String> cache = new HashSet<>();
            for (Object o : object) {
                cache.add((String) o);
            }
            this.perms = cache;
        });



        e.getStompInterface().subscribe("/user/queue/reply/cosmetic.activelist", (stompClient, payload) -> {
            activeCosmeticMap = new HashMap<>();
            System.out.println(payload);
            JSONArray object = new JSONArray(payload);
            for (Object o : object) {
                JSONObject jsonObject = (JSONObject) o;
                ActiveCosmetic cosmeticData = new ActiveCosmetic();
                cosmeticData.setActivityUID(UUID.fromString(jsonObject.getString("activityUID")));
                cosmeticData.setPlayerUID(UUID.fromString(jsonObject.getString("playerUID")));
                cosmeticData.setCosmeticData(UUID.fromString(jsonObject.getString("cosmeticUID")));
                cosmeticData.setUsername(jsonObject.getString("username"));

                activeCosmeticMap.put(cosmeticData.getActivityUID(), cosmeticData);
                try {
                    if (ModAPI.getAPI().getWorld() != null) {
                        UEntityPlayer entityPlayer = ModAPI.getAPI().getWorld().getPlayerEntityByUuid(cosmeticData.getPlayerUID());
                        if (entityPlayer != null) entityPlayer.refreshDisplayName();
                    }
                } catch (Exception exception) {
                    FeatureCollectDiagnostics.queueSendLogAsync(exception);exception.printStackTrace();}
            }
            rebuildCaches();
        });



        e.getStompInterface().subscribe("/user/queue/reply/cosmetic.list", (stompClient ,payload) -> {
            JSONArray object = new JSONArray(payload);
            Map<UUID, CosmeticData> newCosmeticList = new HashMap<>();
            for (Object o : object) {
                JSONObject jsonObject = (JSONObject) o;
                CosmeticData cosmeticData = new CosmeticData();
                cosmeticData.setCosmeticType(jsonObject.getString("cosmeticType"));
                cosmeticData.setReqPerm(jsonObject.getString("reqPerm"));
                cosmeticData.setData(jsonObject.getString("data"));
                cosmeticData.setId(UUID.fromString(jsonObject.getString("id")));

                newCosmeticList.put(cosmeticData.getId(), cosmeticData);
            }

            cosmeticDataMap = newCosmeticList;
            rebuildCaches();
        });


        requestCosmeticsList();
        requestActiveCosmetics();
        requestPerms();
    }
    @Getter @Setter
    private static List<IChatDetector> iChatDetectors = new ArrayList<>();
    static {
        iChatDetectors.add(new ChatDetectorProbablyUniversal());
        iChatDetectors.add(new ChatDetectorFriendList());
        iChatDetectors.add(new ChatDetectorGuildPartyList());
        iChatDetectors.add(new ChatDetectorPartyMessages());
        iChatDetectors.add(new ChatDetectorJoinLeave());
    }

    private final ThreadLocal<Stack<List<ReplacementContext>>> contextThreadLocal = new ThreadLocal<>();
    @SubscribeEvent(priority = ListenerPriority.FIRST, receiveCanceled = true)
    public void onChatDetect(ChatReceivedEvent clientChatReceivedEvent) {
        try {
            List<ReplacementContext> total = new ArrayList<>();
            for (IChatDetector iChatReplacer : iChatDetectors) {
                List<ReplacementContext> replacementContext = iChatReplacer.getReplacementContext(clientChatReceivedEvent.chat);
                if (replacementContext != null) {
                    total.addAll(replacementContext);
                }
            }
            if (contextThreadLocal.get() == null)
                contextThreadLocal.set(new Stack<>());
            contextThreadLocal.get().push(total);
        } catch (Exception t) {
            System.out.println(clientChatReceivedEvent.chat);
            FeatureCollectDiagnostics.queueSendLogAsync(t);
            t.printStackTrace();
        }
    }

    @SubscribeEvent(priority = ListenerPriority.LAST, receiveCanceled = true)
    public void onChat(ChatReceivedEvent clientChatReceivedEvent) {
        try {
            Stack<List<ReplacementContext>> threadCtx = contextThreadLocal.get();
            if (threadCtx == null) return;
            if (clientChatReceivedEvent.isCanceled()) {
                threadCtx.pop();
                return;
            }

            List<ReplacementContext> replacementContexts = threadCtx.pop();

            Component replaceTarget = clientChatReceivedEvent.chat;

//            LinkedList<Component> chatComponents = SurgicalReplacer.linearifyMoveColorCharToStyle(clientChatReceivedEvent.chat);
            for (ReplacementContext replacementContext : replacementContexts) {
                if (replacementContext.getUsername().isEmpty()) continue;
                List<ActiveCosmetic> activeCosmetics = getActiveCosmeticByPlayerNameLowerCase()
                        .get(replacementContext.getUsername().toLowerCase());
                String color=null, rawPrefix=null, rawPrefixColor=null;
                if (activeCosmetics != null) {
                    for (ActiveCosmetic activeCosmetic : activeCosmetics) {
                        CosmeticData cosmeticData = getCosmeticDataMap().get(activeCosmetic.getCosmeticData());
                        if (cosmeticData != null && cosmeticData.getCosmeticType().equals("ncolor")) {
                            color = cosmeticData.getData().replace("&", "§");
                        } else if (cosmeticData != null && cosmeticData.getCosmeticType().equals("nprefix")) {
                            rawPrefix = cosmeticData.getData().replace("&", "§");
                        } else if (cosmeticData != null && cosmeticData.getCosmeticType().equals("bracket_color")) {
                            rawPrefixColor = cosmeticData.getData().replace("&", "§");
                        }
                    }
                }

                if (color == null && rawPrefix == null) continue;

                String prefix = null;
                if (rawPrefix != null) {
                    prefix = rawPrefix.substring(1);
                    char control = rawPrefix.charAt(0);
                    if (control != 'T' && control != 'Y') {
                        if (rawPrefixColor != null)
                            prefix = rawPrefixColor+"["+prefix+"§r"+rawPrefixColor+"]";
                    }
                }

//                replaceTarget.ren
                StringBuilder sb = new StringBuilder();
                for (Component component : replaceTarget.iterable(ComponentIteratorType.DEPTH_FIRST)) {
                    if (component instanceof TextComponent) {
                        sb.append(((TextComponent) component).content());
                    }
                }

                List<Integer> allIdxes = new ArrayList<>();
                int lastIdx = -1;
                String theThing = sb.toString();
                do {
                    lastIdx = theThing.indexOf(replacementContext.getUsername(), lastIdx+1);
                    if (lastIdx != -1)
                        allIdxes.add(lastIdx);
                } while (lastIdx != -1);

                int idx = allIdxes.stream()
                        .min(Comparator.comparingInt(a -> Math.abs(a - replacementContext.getNearIdx()))).orElse(-1);

                if (idx == -1) {
                    replaceTarget = Component.text(
                            prefix
                    ).append(replaceTarget);
                    continue; // since we can't find target, continue.
                }

                int stIdx = theThing.lastIndexOf('\n', idx) + 1;
                String beforeUsername = theThing.substring(theThing.lastIndexOf('\n', idx) + 1, idx);
                int startingSearch = beforeUsername.length();
                while (true) {
                    startingSearch = beforeUsername.lastIndexOf(' ', startingSearch-1);
                    if (startingSearch == -1) break;
                    if (startingSearch-1 >= 0) {
                        char c = beforeUsername.charAt(startingSearch-1);
                        int next = beforeUsername.lastIndexOf(' ', startingSearch-1);
                        if (c == ']' && beforeUsername.charAt(next+1) == '[') continue;
                        startingSearch ++;
                        break;
                    }
                }

                if (startingSearch == -1) startingSearch = 0;
                startingSearch += stIdx;

                if (color != null)
                    replaceTarget = SurgicalReplacer.inject(idx, replacementContext.getUsername().length(),
                            replaceTarget,
                            Component.text(
                                    color+replacementContext.getUsername()
                            ));

                if (prefix != null)
                    replaceTarget = SurgicalReplacer.inject(startingSearch, 0,
                            replaceTarget,
                            Component.text(
                                    prefix+" "
                            ));
            }
            clientChatReceivedEvent.chat = replaceTarget;
        } catch (Exception t) {
            FeatureCollectDiagnostics.queueSendLogAsync(t);
            System.out.println(clientChatReceivedEvent.chat);
            t.printStackTrace();
        }
    }


    @SubscribeEvent
    public void onTabList(PlayerListItemPacketEvent packetPlayerListItem) {
        S38PacketPlayerListItem asd = packetPlayerListItem.getPacketPlayerListItem();
        if (asd.getAction() == S38PacketPlayerListItem.Action.ADD_PLAYER) {
            List<UUID> pingTarget = new ArrayList<>();
            for (S38PacketPlayerListItem.AddPlayerData entry : asd.getEntries()) {
                if (entry.getProfile().getId().version() == 4 && entry.getProfile().getName() != null) {
                    PlayerManager.INSTANCE.subscribeTo(entry.getProfile().getId());
                    pingTarget.add(entry.getProfile().getId());

                    nameIdCache.put(entry.getProfile().getName(), entry.getProfile().getId());
                }
            }
            PlayerManager.INSTANCE.ping(pingTarget);
        } else if (asd.getAction() == S38PacketPlayerListItem.Action.REMOVE_PLAYER) {
            for (S38PacketPlayerListItem.AddPlayerData entry : asd.getEntries()) {
                if (entry.getProfile().getId().version() == 4) {
                    PlayerManager.INSTANCE.unsubscribe(entry.getProfile().getId());
                    nameIdCache.remove(entry.getProfile().getName(), entry.getProfile().getId());
                }
            }
        }
    }

    private void refresh(UUID uuid) {
        try {
            if (ModAPI.getAPI().getWorld() != null) {
                UEntityPlayer entityPlayer =ModAPI.getAPI().getWorld().getPlayerEntityByUuid(uuid);
                if (entityPlayer != null) entityPlayer.refreshDisplayName();
            }
        } catch (Exception exception) {
            FeatureCollectDiagnostics.queueSendLogAsync(exception);
            exception.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onUpdate(DGPlayerJoinEvent event) {
        refresh(event.getUuid());
    }
    @SubscribeEvent
    public void onUpdate(DGPlayerQuitEvent event) {
        refresh(event.getUuid());
    }


    @SubscribeEvent
    public void onTabFormat(TabNameFormatEvent formatEvent) {
        String rawPlayerString = formatEvent.getDisplayName();

        String actualName = null;
        List<ActiveCosmetic> activeCosmetics;
        for (String s : rawPlayerString.split(" ")) {
            String strippped = TextUtils.stripColor(s);
            if (strippped.startsWith("[")) continue;
            actualName = strippped;
            break;
        }

        if (actualName == null) return;

        UUID uuid = DungeonsGuide.getDungeonsGuide().getCosmeticsManager().getNameIdCache().get(actualName);
        boolean dg = FeatureRegistry.DG_INDICATOR.isEnabled() && PlayerManager.INSTANCE.getOnlineStatus().getOrDefault(uuid, false);


        activeCosmetics = DungeonsGuide.getDungeonsGuide().getCosmeticsManager().getActiveCosmeticByPlayerNameLowerCase().get(actualName.toLowerCase());
        if (activeCosmetics == null && dg) {
            formatEvent.setDisplayName("\ued00" + rawPlayerString);
            return;
        }
        else if (activeCosmetics == null) return;

        CosmeticData color=null;
        for (ActiveCosmetic activeCosmetic : activeCosmetics) {
            CosmeticData cosmeticData = DungeonsGuide.getDungeonsGuide().getCosmeticsManager().getCosmeticDataMap().get(activeCosmetic.getCosmeticData());
            if (cosmeticData == null) continue;
            if (cosmeticData.getCosmeticType().equals("ncolor")) color = cosmeticData;
        }

//        FontRenderer
        if (color != null) { // ᨠ
            String coloredName = color.getData() + actualName;
            if (dg) {
                formatEvent.setDisplayName( "\ued00" + rawPlayerString.replace(actualName, coloredName));
            } else {
                formatEvent.setDisplayName(coloredName);
            }
        } else {
            if (dg) {
                formatEvent.setDisplayName( "\ued00" + rawPlayerString);
            }
        }
    }

    @SubscribeEvent(priority = ListenerPriority.LAST)
    public void nameFormat(PlayerNameFormatEvent nameFormat) {
        List<ActiveCosmetic> activeCosmetics = activeCosmeticByPlayer.get(nameFormat.getPlayer().getUUID());
        boolean dg =
                FeatureRegistry.DG_INDICATOR.isEnabled() &&
                PlayerManager.INSTANCE.getOnlineStatus().getOrDefault(nameFormat.getPlayer().getUUID(), false);


        if (dg)
            nameFormat.getPrefix().add(Component.text("\ued01")); // dg char.


        if (activeCosmetics == null) return;
        String color=null, rawPrefix=null, rawPrefixColor=null;
            for (ActiveCosmetic activeCosmetic : activeCosmetics) {
                CosmeticData cosmeticData = getCosmeticDataMap().get(activeCosmetic.getCosmeticData());
                if (cosmeticData != null && cosmeticData.getCosmeticType().equals("ncolor")) {
                    color = cosmeticData.getData().replace("&", "§");
                } else if (cosmeticData != null && cosmeticData.getCosmeticType().equals("nprefix")) {
                    rawPrefix = cosmeticData.getData().replace("&", "§");
                } else if (cosmeticData != null && cosmeticData.getCosmeticType().equals("bracket_color")) {
                    rawPrefixColor = cosmeticData.getData().replace("&", "§");
                }
            }

        String prefix = null;
        if (rawPrefix != null) {
            prefix = rawPrefix.substring(1);
            char control = rawPrefix.charAt(0);
            if (control != 'T' && control != 'Y') {
                if (rawPrefixColor != null)
                    prefix = rawPrefixColor+"["+prefix+"§r"+rawPrefixColor+"]";
            }
        }

        if (color != null)
            nameFormat.setDisplayName(color+nameFormat.username);

        nameFormat.getPrefix().add(Component.text(prefix+" "));
    }
}
