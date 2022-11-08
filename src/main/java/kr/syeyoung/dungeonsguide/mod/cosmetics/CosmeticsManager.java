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

import kr.syeyoung.dungeonsguide.mod.cosmetics.replacers.chat.ChatReplacer;
import kr.syeyoung.dungeonsguide.mod.cosmetics.data.ActiveCosmetic;
import kr.syeyoung.dungeonsguide.mod.cosmetics.data.CosmeticData;
import kr.syeyoung.dungeonsguide.mod.cosmetics.replacers.playername.PlayerNameReplacer;
import kr.syeyoung.dungeonsguide.mod.cosmetics.replacers.tab.TabReplacer;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerListItemPacketEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.StompConnectedEvent;
import kr.syeyoung.dungeonsguide.mod.stomp.*;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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


    private final ChatReplacer chatReplacer;
    private final TabReplacer tabReplacer;
    private final PlayerNameReplacer playerNameReplacer;

    public CosmeticsManager() {
        this.playerNameReplacer = new PlayerNameReplacer(this);

        this.tabReplacer = new TabReplacer(this);

        this.chatReplacer = new ChatReplacer(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent clientChatReceivedEvent) {
        chatReplacer.consumeEvent(clientChatReceivedEvent);
    }

    @SubscribeEvent
    public void onTabList(PlayerListItemPacketEvent packetPlayerListItem) {
        tabReplacer.consumeEvent(packetPlayerListItem);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void nameFormat(PlayerEvent.NameFormat nameFormat) {
        playerNameReplacer.consumeEvent(nameFormat);
    }

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

}
