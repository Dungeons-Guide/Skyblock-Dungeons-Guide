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

package kr.syeyoung.dungeonsguide.cosmetics;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.cosmetics.chatreplacers.*;
import kr.syeyoung.dungeonsguide.events.PlayerListItemPacketEvent;
import kr.syeyoung.dungeonsguide.events.StompConnectedEvent;
import kr.syeyoung.dungeonsguide.stomp.*;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
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

public class CosmeticsManager implements StompMessageHandler {
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
        DungeonsGuide.getDungeonsGuide().getStompConnection().send(new StompPayload()
                .method(StompHeader.SEND)
                .destination("/app/cosmetic.activelist")
        );
    }
    public void requestCosmeticsList() {
        DungeonsGuide.getDungeonsGuide().getStompConnection().send(new StompPayload()
                .method(StompHeader.SEND)
                .destination("/app/cosmetic.list")
        );
    }
    public void requestPerms() {
        DungeonsGuide.getDungeonsGuide().getStompConnection().send(new StompPayload()
                .method(StompHeader.SEND)
                .destination("/app/user.perms")
        );
    }
    public void setCosmetic(CosmeticData cosmetic) {
        if (!perms.contains(cosmetic.getReqPerm())) return;
        DungeonsGuide.getDungeonsGuide().getStompConnection().send(new StompPayload()
                .method(StompHeader.SEND)
                .destination("/app/cosmetic.set")
                .payload(cosmetic.getId().toString())
        );
    }
    public void removeCosmetic(ActiveCosmetic activeCosmetic) {
        DungeonsGuide.getDungeonsGuide().getStompConnection().send(new StompPayload()
                .method(StompHeader.SEND)
                .destination("/app/cosmetic.remove")
                .payload(activeCosmetic.getActivityUID().toString())
        );
    }

    @Override
    public void handle(StompClient stompInterface, StompPayload stompPayload) {
        String destination = stompPayload.headers().get("destination");
        if (destination.equals("/topic/cosmetic.set")) {
            JSONObject jsonObject = new JSONObject(stompPayload.payload());
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
            } catch (Exception e) {e.printStackTrace();}

        } else if (destination.equals("/user/queue/reply/user.perms")) {
            JSONArray object = new JSONArray(stompPayload.payload());
            Set<String> cache = new HashSet<>();
            for (Object o : object) {
                cache.add((String) o);
            }
            this.perms = cache;
        } else if (destination.equals("/user/queue/reply/cosmetic.activelist")) {
            Map<UUID, ActiveCosmetic> activeCosmeticMap = new HashMap<>();
            JSONArray object = new JSONArray(stompPayload.payload());
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
                } catch (Exception e) {e.printStackTrace();}
            }
            this.activeCosmeticMap = activeCosmeticMap;
            rebuildCaches();
        } else if (destination.equals("/user/queue/reply/cosmetic.list")) {
            JSONArray object = new JSONArray(stompPayload.payload());
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
        }
    }

    private void rebuildCaches() {
        Map<String, List<ActiveCosmetic>> activeCosmeticByType = new HashMap<>();
        Map<UUID, List<ActiveCosmetic>> activeCosmeticByPlayer = new HashMap<>();
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
        this.activeCosmeticByPlayer = activeCosmeticByPlayer;
        this.activeCosmeticByType = activeCosmeticByType;
    }

    @SubscribeEvent
    public void stompConnect(StompConnectedEvent stompConnectedEvent) {
        stompConnectedEvent.getStompInterface().subscribe(StompSubscription.builder()
                .stompMessageHandler(this).ackMode(StompSubscription.AckMode.AUTO).destination("/topic/cosmetic.set").build());
        stompConnectedEvent.getStompInterface().subscribe(StompSubscription.builder()
                .stompMessageHandler(this).ackMode(StompSubscription.AckMode.AUTO).destination("/user/queue/reply/user.perms").build());
        stompConnectedEvent.getStompInterface().subscribe(StompSubscription.builder()
                .stompMessageHandler(this).ackMode(StompSubscription.AckMode.AUTO).destination("/user/queue/reply/cosmetic.activelist").build());
        stompConnectedEvent.getStompInterface().subscribe(StompSubscription.builder()
                .stompMessageHandler(this).ackMode(StompSubscription.AckMode.AUTO).destination("/user/queue/reply/cosmetic.list").build());

        requestCosmeticsList();
        requestActiveCosmetics();
        requestPerms();
    }
    @Getter @Setter
    private static List<IChatReplacer> iChatReplacers = new ArrayList<>();
    static {
        iChatReplacers.add(new ChatReplacerViewProfile());
        iChatReplacers.add(new ChatReplacerPV());
        iChatReplacers.add(new ChatReplacerSocialOptions());
        iChatReplacers.add(new ChatReplacerCoop());
        iChatReplacers.add(new ChatReplacerMessage());
        iChatReplacers.add(new ChatReplacerChatByMe());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChat(ClientChatReceivedEvent clientChatReceivedEvent) {
        try {
            if (clientChatReceivedEvent.type == 2) return;
            for (IChatReplacer iChatReplacer : iChatReplacers) {
                if (iChatReplacer.isAcceptable(clientChatReceivedEvent)) {
                    iChatReplacer.translate(clientChatReceivedEvent, this);
                    return;
                }
            }
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
        CosmeticData color=null, prefix=null;
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
