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


import kr.syeyoung.dungeonsguide.mod.cosmetics.chatdetectors.*;
import kr.syeyoung.dungeonsguide.mod.cosmetics.surgical.ReplacementContext;
import kr.syeyoung.dungeonsguide.mod.cosmetics.surgical.SurgicalReplacer;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerListItemPacketEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.StompConnectedEvent;
import kr.syeyoung.dungeonsguide.mod.stomp.StompHeader;
import kr.syeyoung.dungeonsguide.mod.stomp.StompManager;
import kr.syeyoung.dungeonsguide.mod.stomp.StompPayload;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
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

            try {
                if (Minecraft.getMinecraft().theWorld != null) {
                    EntityPlayer entityPlayer = Minecraft.getMinecraft().theWorld.getPlayerEntityByUUID(activeCosmetic.getPlayerUID());
                    if (entityPlayer != null) entityPlayer.refreshDisplayName();
                }
            } catch (Exception exception) {exception.printStackTrace();}
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
                    if (Minecraft.getMinecraft().theWorld != null) {
                        EntityPlayer entityPlayer = Minecraft.getMinecraft().theWorld.getPlayerEntityByUUID(cosmeticData.getPlayerUID());
                        if (entityPlayer != null) entityPlayer.refreshDisplayName();
                    }
                } catch (Exception exception) {exception.printStackTrace();}
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

    private final ThreadLocal<List<ReplacementContext>> contextThreadLocal = new ThreadLocal<>();
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatDetect(ClientChatReceivedEvent clientChatReceivedEvent) {
        try {
            if (clientChatReceivedEvent.type == 2) return;
            List<ReplacementContext> total = new ArrayList<>();
            for (IChatDetector iChatReplacer : iChatDetectors) {
                List<ReplacementContext> replacementContext = iChatReplacer.getReplacementContext(clientChatReceivedEvent.message);
                if (replacementContext != null) {
                    total.addAll(replacementContext);
                }
            }
            contextThreadLocal.set(total);
        } catch (Throwable t) {
            System.out.println(clientChatReceivedEvent.message);
            t.printStackTrace();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent clientChatReceivedEvent) {
        try {
            if (clientChatReceivedEvent.type == 2) return;
            if (clientChatReceivedEvent.isCanceled()) {
                contextThreadLocal.set(null);
                return;
            }

            List<ReplacementContext> replacementContexts = contextThreadLocal.get();
            contextThreadLocal.set(null);

            LinkedList<IChatComponent> chatComponents = SurgicalReplacer.linearifyMoveColorCharToStyle(clientChatReceivedEvent.message);
            for (ReplacementContext replacementContext : replacementContexts) {
                if (replacementContext.getUsername().isEmpty()) continue;
                List<ActiveCosmetic> activeCosmetics = getActiveCosmeticByPlayerNameLowerCase()
                        .get(replacementContext.getUsername().toLowerCase());
                String color=null, prefix="[coolprefix]";
                if (activeCosmetics != null) {
                    for (ActiveCosmetic activeCosmetic : activeCosmetics) {
                        CosmeticData cosmeticData = getCosmeticDataMap().get(activeCosmetic.getCosmeticData());
                        if (cosmeticData != null && cosmeticData.getCosmeticType().equals("color")) {
                            color = cosmeticData.getData().replace("&", "§");
                        } else if (cosmeticData != null && cosmeticData.getCosmeticType().equals("prefix")) {
                            prefix = cosmeticData.getData().replace("&", "§");
                        }
                    }
                }

                if (color == null && prefix == null) continue;


                StringBuilder sb = new StringBuilder();
                for (IChatComponent chatComponent : chatComponents) {
                    String str = chatComponent.getUnformattedText();
                    sb.append(str);
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

                System.out.println("Was expecting to find " +replacementContext.getUsername());
                if (idx == -1) {
                    System.out.println("WTF?");

                    List<IChatComponent> components = SurgicalReplacer.inject(chatComponents,
                            SurgicalReplacer.linearifyMoveColorCharToStyle(
                                    new ChatComponentText(prefix+" ")
                                            .setChatStyle(SurgicalReplacer.getChatStyleAt(chatComponents, 0))
                            ),
                            0, 0);
                    clientChatReceivedEvent.message = SurgicalReplacer.combine(components);
                    // since we couldn't find username anywhere, just do this.
                    return;
                }

                int stIdx = theThing.lastIndexOf('\n', idx) + 1;
                String beforeUsername = theThing.substring(theThing.lastIndexOf('\n', idx) + 1, idx);
                int startingSearch = beforeUsername.length();
                while (true) {
                    startingSearch = beforeUsername.lastIndexOf(' ', startingSearch-1);
                    if (startingSearch == -1) break;
                    if (startingSearch-1 >= 0) {
                        char c = beforeUsername.charAt(startingSearch-1);
                        if (c == ' ') continue;
                        if (c == ']') continue;
                        startingSearch ++;
                        break;
                    }
                }

                if (startingSearch == -1) startingSearch = 0;
                startingSearch += stIdx;

                if (color != null)
                    chatComponents = SurgicalReplacer.inject(chatComponents,
                            SurgicalReplacer.linearifyMoveColorCharToStyle(
                                    new ChatComponentText(color+replacementContext.getUsername())
                                            .setChatStyle(SurgicalReplacer.getChatStyleAt(chatComponents, idx))
                            ),
                            idx, replacementContext.getUsername().length());

                if (prefix != null)
                    chatComponents = SurgicalReplacer.inject(chatComponents,
                            SurgicalReplacer.linearifyMoveColorCharToStyle(
                                    new ChatComponentText(prefix+" ")
                                            .setChatStyle(SurgicalReplacer.getChatStyleAt(chatComponents, 0))
                            ),
                            startingSearch, 0);
            }
            clientChatReceivedEvent.message = SurgicalReplacer.combine(chatComponents);
        } catch (Throwable t) {
            System.out.println(clientChatReceivedEvent.message);
            t.printStackTrace();
        }
    }


    @SubscribeEvent
    public void onTabList(PlayerListItemPacketEvent packetPlayerListItem) {
        S38PacketPlayerListItem asd = packetPlayerListItem.getPacketPlayerListItem();
        if (asd.getAction() == S38PacketPlayerListItem.Action.ADD_PLAYER) {
            if (Minecraft.getMinecraft().getNetHandler() == null) return;

            Map<UUID, NetworkPlayerInfo> playerInfoMap = ReflectionHelper.getPrivateValue(NetHandlerPlayClient.class, Minecraft.getMinecraft().getNetHandler(), "playerInfoMap", "field_147310_i","i");
            for (S38PacketPlayerListItem.AddPlayerData entry : asd.getEntries()) {
                playerInfoMap.remove(entry.getProfile().getId());
                playerInfoMap.put(entry.getProfile().getId(), new CustomNetworkPlayerInfo(entry));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void nameFormat(PlayerEvent.NameFormat nameFormat) {
        List<ActiveCosmetic> activeCosmetics = activeCosmeticByPlayer.get(nameFormat.entityPlayer.getGameProfile().getId());
        if (activeCosmetics == null) return;
        CosmeticData color=null;
        CosmeticData prefix=null;
        for (ActiveCosmetic activeCosmetic : activeCosmetics) {
            CosmeticData cosmeticData = cosmeticDataMap.get(activeCosmetic.getCosmeticData());
            if (cosmeticData !=null && cosmeticData.getCosmeticType().equals("color")) {
                color = cosmeticData;
            } else if (cosmeticData != null && cosmeticData.getCosmeticType().equals("prefix")) {
                prefix = cosmeticData;
            }
        }


        if (color != null)
            nameFormat.displayname = color.getData().replace("&","§")+nameFormat.username;

        if (prefix != null)
            nameFormat.displayname = prefix.getData().replace("&","§")+" "+nameFormat.displayname;

    }
}
