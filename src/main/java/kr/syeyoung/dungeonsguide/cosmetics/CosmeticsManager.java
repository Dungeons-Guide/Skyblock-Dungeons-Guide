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

import com.google.gson.JsonPrimitive;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.events.PlayerListItemPacketEvent;
import kr.syeyoung.dungeonsguide.events.StompConnectedEvent;
import kr.syeyoung.dungeonsguide.stomp.*;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CosmeticsManager implements StompMessageHandler {
    @Getter
    private Map<UUID, CosmeticData> cosmeticDataMap = new HashMap<>();
    @Getter
    private Map<UUID, ActiveCosmetic> activeCosmeticMap = new HashMap<>();
    @Getter
    private Map<String, List<ActiveCosmetic>> activeCosmeticByType = new HashMap<>();
    @Getter
    private Map<UUID, List<ActiveCosmetic>> activeCosmeticByPlayer = new HashMap<>();
    @Getter
    private Map<String, List<ActiveCosmetic>> activeCosmeticByPlayerNameLowerCase = new HashMap<>();
    @Getter
    private Set<String> perms = new HashSet<>();

    public void requestActiveCosmetics() {
        DungeonsGuide.getDungeonsGuide().getStompConnection().send(new StompPayload()
                .method(StompHeader.SEND)
                .header("destination", "/app/cosmetic.activelist")
        );
    }
    public void requestCosmeticsList() {
        DungeonsGuide.getDungeonsGuide().getStompConnection().send(new StompPayload()
                .method(StompHeader.SEND)
                .header("destination", "/app/cosmetic.list")
        );
    }
    public void requestPerms() {
        DungeonsGuide.getDungeonsGuide().getStompConnection().send(new StompPayload()
                .method(StompHeader.SEND)
                .header("destination", "/app/user.perms")
        );
    }
    public void setCosmetic(CosmeticData cosmetic) {
        if (!perms.contains(cosmetic.getReqPerm())) return;
        DungeonsGuide.getDungeonsGuide().getStompConnection().send(new StompPayload()
                .method(StompHeader.SEND)
                .header("destination", "/app/cosmetic.set")
                .payload(cosmetic.getId().toString())
        );
    }
    public void removeCosmetic(ActiveCosmetic activeCosmetic) {
        DungeonsGuide.getDungeonsGuide().getStompConnection().send(new StompPayload()
                .method(StompHeader.SEND)
                .header("destination", "/app/cosmetic.remove")
                .payload(activeCosmetic.getActivityUID().toString())
        );
    }

    @Override
    public void handle(StompInterface stompInterface, StompPayload stompPayload) {
        String destination = stompPayload.headers().get("destination");
        if (destination.equals("/topic/cosmetic.set")) {
            JSONObject jsonObject = new JSONObject(stompPayload.payload());
            ActiveCosmetic activeCosmetic = new ActiveCosmetic();
            activeCosmetic.setActivityUID(UUID.fromString(jsonObject.getString("activityUID")));
            activeCosmetic.setPlayerUID(UUID.fromString(jsonObject.getString("playerUID")));
            if (jsonObject.isNull("cosmeticUID")) {
                ActiveCosmetic activeCosmetic1 = activeCosmeticMap.remove(activeCosmetic.getActivityUID());

                List<ActiveCosmetic> activeCosmetics = activeCosmeticByPlayer.computeIfAbsent(activeCosmetic.getPlayerUID(), a-> new ArrayList<>());
                activeCosmetics.remove(activeCosmetic1);

                activeCosmetics = activeCosmeticByPlayerNameLowerCase.computeIfAbsent(activeCosmetic.getUsername().toLowerCase(), a-> new ArrayList<>());
                activeCosmetics.remove(activeCosmetic1);

                CosmeticData cosmeticData = cosmeticDataMap.get(activeCosmetic.getCosmeticData());
                if (cosmeticData != null) {
                    List<ActiveCosmetic> cosmeticsByTypeList = activeCosmeticByType.computeIfAbsent(cosmeticData.getCosmeticType(), a-> new ArrayList<>());
                    cosmeticsByTypeList.remove(activeCosmetic1);
                }
            } else {
                activeCosmetic.setCosmeticData(UUID.fromString(jsonObject.getString("cosmeticUID")));
                activeCosmetic.setUsername(jsonObject.getString("username"));

                ActiveCosmetic previousThing = activeCosmeticMap.get(activeCosmetic.getActivityUID());
                activeCosmeticMap.put(activeCosmetic.getActivityUID(), activeCosmetic);

                CosmeticData cosmeticData = cosmeticDataMap.get(activeCosmetic.getCosmeticData());
                if (cosmeticData != null) {
                    List<ActiveCosmetic> cosmeticsByTypeList = activeCosmeticByType.computeIfAbsent(cosmeticData.getCosmeticType(), a-> new ArrayList<>());
                    cosmeticsByTypeList.add(activeCosmetic);
                    cosmeticsByTypeList.remove(previousThing);
                }
                List<ActiveCosmetic> activeCosmetics = activeCosmeticByPlayer.computeIfAbsent(activeCosmetic.getPlayerUID(), a-> new ArrayList<>());
                activeCosmetics.add(activeCosmetic);
                activeCosmetics.remove(previousThing);

                activeCosmetics = activeCosmeticByPlayerNameLowerCase.computeIfAbsent(activeCosmetic.getUsername().toLowerCase(), a-> new ArrayList<>());
                activeCosmetics.add(activeCosmetic);
                activeCosmetics.remove(previousThing);
            }


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
                List<ActiveCosmetic> cosmeticsByTypeList = activeCosmeticByType.computeIfAbsent(cosmeticData.getCosmeticType(), a-> new ArrayList<>());
                cosmeticsByTypeList.add(value);
            }
            List<ActiveCosmetic> activeCosmetics = activeCosmeticByPlayer.computeIfAbsent(value.getPlayerUID(), a-> new ArrayList<>());
            activeCosmetics.add(value);
            activeCosmetics = activeCosmeticByPlayerName.computeIfAbsent(value.getUsername().toLowerCase(), a-> new ArrayList<>());
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

    // §9Party §8> §a[VIP§6+§a] syeyoung§f: ty
    // §2Guild > §a[VIP§6+§a] syeyoung §3[Vet]§f
    // §dTo §r§a[VIP§r§6+§r§a] SlashSlayer§r§7: §r§7what§r
    // §dFrom §r§a[VIP§r§6+§r§a] SlashSlayer§r§7: §r§7?§r
    // §7Rock_Bird§7§r§7: SELLING 30 DIAMOD BLOCK /p me§r
    // §b[MVP§c+§b] Probutnoobgamer§f: quitting skyblock! highe
    // §r§bCo-op > §a[VIP§6+§a] syeyoung§f: §rwhat§r

    public static String substitute(String str) {
        str = str.replace("{HYPIXEL_RANKED_NAME}", "§.(?:\\[[a-zA-Z\\+§0-9]+\\] )?{MC_NAME}");
        str = str.replace("{HYPIXEL_RANKED_NAME_PAT}", "(§.(?:\\[[a-zA-Z\\+§0-9]+\\] )?)({MC_NAME})");
        str = str.replace("{MC_NAME}", "[a-zA-Z0-9_]+");
        str = str.replace("{ANY_COLOR}", "(?:§[a-zA-Z0-9])+");
        return str;
    }

    private static final Pattern PARTY_MSG = Pattern.compile(substitute("§r§9Party §8> {HYPIXEL_RANKED_NAME_PAT}({ANY_COLOR}): (.+)"));
    private static final Pattern GUILD_MSG = Pattern.compile(substitute("§r§2Guild > {HYPIXEL_RANKED_NAME_PAT} ({ANY_COLOR}\\[.+\\]{ANY_COLOR}): (.+)"));
    private static final Pattern CHAT_MSG = Pattern.compile(substitute("(?:§r)?{HYPIXEL_RANKED_NAME_PAT}({ANY_COLOR}): (.+)"));
    private static final Pattern COOP_MSG = Pattern.compile(substitute("§r§bCo-op > {HYPIXEL_RANKED_NAME_PAT}({ANY_COLOR}): (.+)"));
    private static final Pattern DM_TO = Pattern.compile(substitute("§dTo §r{HYPIXEL_RANKED_NAME_PAT}§r§7: (.+)"));
    private static final Pattern DM_FROM = Pattern.compile(substitute("§dFrom §r{HYPIXEL_RANKED_NAME_PAT}§r§7: (.+)"));


    @SubscribeEvent(receiveCanceled = false, priority = EventPriority.HIGHEST)
    public void onChat(ClientChatReceivedEvent clientChatReceivedEvent) {
        Matcher m;
        String msg = clientChatReceivedEvent.message.getFormattedText();
        boolean match = false;
        String preRank = "";
        String rank = "";
        String last = "";
        String nickname = "";
        if ((m = PARTY_MSG.matcher(msg)).matches()) {
            match = true;
            nickname = m.group(2);
            preRank = "§r§9Party §8> ";
            rank = m.group(1);
            last = m.group(2)+m.group(3)+": "+m.group(4);
        } else if ((m = GUILD_MSG.matcher(msg)).matches()) {
            match = true;
            nickname = m.group(2);
            preRank = "§r§2Guild > ";
            rank = m.group(1);
            last = m.group(2)+" "+m.group(3)+": "+m.group(4);
        } else if ((m = CHAT_MSG.matcher(msg)).matches()) {
            match = true;
            nickname = m.group(2);
            preRank = "";
            rank = m.group(1);
            last = m.group(2)+m.group(3)+": "+m.group(4);
        } else if ((m = COOP_MSG.matcher(msg)).matches()) {
            match = true;
            nickname = m.group(2);
            preRank = "§r§bCo-op > ";
            rank = m.group(1);
            last = m.group(2)+m.group(3)+": "+m.group(4);
        } else if ((m = DM_TO.matcher(msg)).matches()) {
            match = true;
            nickname = m.group(2);
            preRank = "§dTo §r";
            rank = m.group(1);
            last = m.group(2)+"§r§7: "+m.group(3);
        } else if ((m = DM_FROM.matcher(msg)).matches()) {
            match = true;
            nickname = m.group(2);
            preRank = "§dFrom §r";
            rank = m.group(1);
            last = m.group(2)+"§r§7: "+m.group(3);
        }

        if (!match) return;

        List<ActiveCosmetic> activeCosmetics = activeCosmeticByPlayerNameLowerCase.get(nickname.toLowerCase());
        if (activeCosmetics != null) {
            CosmeticData prefix = null, color =null;
            for (ActiveCosmetic activeCosmetic : activeCosmetics) {
                CosmeticData cosmeticData = cosmeticDataMap.get(activeCosmetic.getCosmeticData());
                if (cosmeticData == null) continue;
                if (cosmeticData.getCosmeticType().equals("prefix")) prefix = cosmeticData;
                if (cosmeticData.getCosmeticType().equals("color")) color = cosmeticData;
            }

            if (prefix != null) {
                preRank += prefix.getData().replace("&", "§")+" ";
            }
            if (color != null) {
                last = color.getData().replace("&", "§") + last;
            }
        }

        clientChatReceivedEvent.message = new ChatComponentText(preRank + rank + last);
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
}
