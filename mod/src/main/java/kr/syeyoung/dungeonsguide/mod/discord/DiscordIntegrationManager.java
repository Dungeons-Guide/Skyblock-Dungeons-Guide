/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.discord;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.Callback;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.pipe.PipeStatus;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.events.impl.DiscordUserInvitedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DiscordUserJoinRequestEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DiscordUserUpdateEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.discord.inviteViewer.Reply;
import kr.syeyoung.dungeonsguide.mod.party.PartyContext;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DiscordIntegrationManager implements IPCListener {
    public static DiscordIntegrationManager INSTANCE = new DiscordIntegrationManager();
    private IPCClient ipcClient;
    private final Thread t = new Thread(DungeonsGuide.THREAD_GROUP, this::run);
    private Logger logger = LogManager.getLogger("DG-DiscordIntegrationManager");

    private DiscordIntegrationManager(){
        ipcClient = new IPCClient(816298079732498473L, DungeonsGuide.THREAD_FACTORY);
        t.start();
    }

    @Getter
    private final Map<Long, JDiscordRelation> relationMap = new HashMap<>();

    public boolean isLoaded() {
        return ipcClient.getStatus() == PipeStatus.CONNECTED;
    }

    public void sendInvite(String id, String content) {
        JSONObject payload = new JSONObject()
                .put("cmd", "ACTIVITY_INVITE_USER")
                .put("args", new JSONObject()
                                .put("type", 1)
                                .put("user_id", id)
                                .put("content", content)
                                .put("pid",getPID()));
        ipcClient.send(payload, new Callback(success ->{}, fail -> {
                    logger.log(Level.WARN, "Discord failed send Invite for "+fail+"\n Sent payload: "+payload);
        }));
    }


    public void respondToJoinRequest(String userId, Reply accept) {
        JSONObject payload = null;
        if (accept == Reply.ACCEPT) {
            payload = new JSONObject()
                    .put("cmd", "SEND_ACTIVITY_JOIN_INVITE")
                    .put("args", new JSONObject().put("user_id", userId));
        } else  {
            payload = new JSONObject()
                    .put("cmd", "CLOSE_ACTIVITY_JOIN_REQUEST")
                    .put("args", new JSONObject().put("user_id", userId));
        }
        JSONObject finalPayload = payload;
        ipcClient.send(payload, new Callback(success ->{}, fail -> {
            logger.log(Level.WARN, "Discord failed respond to join request for "+fail+"\n Sent payload: "+ finalPayload);
        }));
    }

    public void acceptInvite(InviteHandle handle) {
        JSONObject payload = new JSONObject()
                .put("cmd", "ACCEPT_ACTIVITY_INVITE")
                .put("args", new JSONObject()
                        .put("type", 1)
                        .put("user_id", handle.getUserId())
                        .put("session_id", handle.getSessionId())
                        .put("channel_id", handle.getChannelId())
                        .put("message_id", handle.getMessageId()));
        ipcClient.send(payload, new Callback(success ->{}, fail -> {
            logger.log(Level.WARN, "Discord failed accept invite for "+fail+"\n Sent payload: "+ payload);
        }));
    }



    private void setup() {
        try {
            ipcClient.connect();
            ipcClient.subscribe("ACTIVITY_JOIN", this::onActivityJoin);
            ipcClient.subscribe("ACTIVITY_JOIN_REQUEST", this::onActivityJoinRequest);
            ipcClient.subscribe("ACTIVITY_INVITE", this::onActivityInvite);
            ipcClient.subscribe("RELATIONSHIP_UPDATE", this::onRelationshipUpdate);
            ipcClient.send(new JSONObject().put("cmd", "GET_RELATIONSHIPS"), new Callback(this::onRelationshipLoad));
            ipcClient.setListener(this);
            System.out.println("Connecting");
        } catch (NoDiscordClientException ignored) {
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void onRelationshipUpdate(Packet packet){
        JSONObject data = packet.getJson().getJSONObject("data");
        JDiscordRelation relation = JDiscordRelation.parse(data);
        JDiscordRelation old = relationMap.put(relation.getDiscordUser().getIdLong(), relation);
        Minecraft.getMinecraft().addScheduledTask(() -> {
            MinecraftForge.EVENT_BUS.post(new DiscordUserUpdateEvent(old, relation));
        });
    }
    private void onActivityJoinRequest(Packet packet) {
        JSONObject data = packet.getJson().getJSONObject("data");
        User user = new User(data.getJSONObject("user")
                .getString("username"),
                data.getJSONObject("user")
                        .getString("discriminator"),
                Long.parseUnsignedLong(data.getJSONObject("user")
                        .getString("id")),
                data.getJSONObject("user")
                        .getString("avatar"));
        Minecraft.getMinecraft().addScheduledTask(() -> {
            MinecraftForge.EVENT_BUS.post(new DiscordUserJoinRequestEvent(user));
        });
    }
    private void onActivityInvite(Packet packet) {
        JSONObject data = packet.getJson().getJSONObject("data");
        if (!data.getJSONObject("activity").getString("application_id").equals("816298079732498473"))
            return;
        Minecraft.getMinecraft().addScheduledTask(() -> {
            MinecraftForge.EVENT_BUS.post(new DiscordUserInvitedEvent(
                    new User(data.getJSONObject("user")
                            .getString("username"),
                            data.getJSONObject("user")
                                    .getString("discriminator"),
                            Long.parseUnsignedLong(data.getJSONObject("user")
                                    .getString("id")),
                            data.getJSONObject("user")
                                    .getString("avatar")),
                    new InviteHandle(
                            data.getJSONObject("user").getString("id"),
                            data.getJSONObject("activity").getString("session_id"),
                            data.getString("channel_id"),
                            data.getString("message_id")
                    )));
        });
    }

    private void onRelationshipLoad(Packet object) {
        try {
            JSONArray relationship = object.getJson().getJSONObject("data").getJSONArray("relationships");
            for (Object o : relationship) {
                JSONObject obj = (JSONObject) o;
                JDiscordRelation relation = JDiscordRelation.parse(obj);
                relationMap.put(relation.getDiscordUser().getIdLong(), relation);
            }
        } catch (Throwable e) {e.printStackTrace();}
    }

    private final SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

    private void sendRichPresence(RichPresence presence) {
        ipcClient.send(new JSONObject()
                .put("cmd","SET_ACTIVITY")
                .put("args", new JSONObject()
                .put("pid",getPID())
                .put("activity",presence == null ? null : presence.toJson())), new Callback(success ->{}, fail -> {System.out.println(fail);}));
    }
    private void updatePresence() {
        if (!skyblockStatus.isOnHypixel() || !FeatureRegistry.DISCORD_RICHPRESENCE.isEnabled() || (!skyblockStatus.isOnSkyblock() && FeatureRegistry.DISCORD_RICHPRESENCE.<Boolean>getParameter("disablenotskyblock").getValue())) {
            sendRichPresence(null);
        } else {
            String name = SkyblockStatus.locationName == null ? "" : SkyblockStatus.locationName;
            if (!skyblockStatus.isOnSkyblock()) name ="Somewhere on Hypixel";
            if (name.trim().equals("Your Island")) name = "Private Island";

            RichPresence.Builder presence = new RichPresence.Builder();

            presence.setLargeImage("mort", "mort");
            presence.setState(name);
            presence.setParty(
                    Optional.ofNullable( PartyManager.INSTANCE.getPartyContext()).map(PartyContext::getPartyID).orElse(null),
                    Optional.ofNullable(PartyManager.INSTANCE.getPartyContext()).map(PartyContext::getPartyRawMembers).map(Set::size).orElse(1),
                    PartyManager.INSTANCE.getMaxParty()
            );

            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            if (context != null) {
                long init = context.getInit();
                presence.setStartTimestamp(Instant.ofEpochMilli(init).atZone(ZoneId.systemDefault()).toOffsetDateTime());

                if (context.getBossfightProcessor() != null) {
                    presence.setDetails("Fighting "+context.getBossfightProcessor().getBossName()+": "+context.getBossfightProcessor().getCurrentPhase());
                } else {
                    presence.setDetails("Clearing Rooms");
                }
            } else {
                presence.setStartTimestamp(null);
                presence.setDetails("Dungeons Guide");
            }
            if (PartyManager.INSTANCE.getAskToJoinSecret() != null) {
                presence.setJoinSecret(PartyManager.INSTANCE.getAskToJoinSecret());
            }
            presence.setInstance(false);
            sendRichPresence(presence.build());
        }
    }
    private void run() {
        while(!t.isInterrupted()) {
            try{
                if (!isLoaded()) setup();
                else {
                    updatePresence();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            try {
                Thread.sleep(32);
            } catch (InterruptedException e) {
                break;
            }
        }
    }



    private long next = 0;
    public void onActivityJoin(Packet packet) {
        String secret = packet.getJson().getJSONObject("data").getString("secret");
        if (System.currentTimeMillis() < next) return;
        next = System.currentTimeMillis() + 500;
        PartyManager.INSTANCE.joinWithToken(secret);
        logger.log(Level.DEBUG, "Trying to join with token: "+secret);
    }

    @Override
    public void onClose(IPCClient client, JSONObject json) {
        logger.log(Level.DEBUG, "IPC Client closed with: "+json);
    }

    @Override
    public void onDisconnect(IPCClient client, Throwable t) {
        logger.log(Level.DEBUG, "IPC Client disconnected for: ", t);
    }



    public void cleanup() {
        ipcClient.close();
        this.t.interrupt();
        ipcClient = null;
    }

    private static int getPID()
    {
        String pr = ManagementFactory.getRuntimeMXBean().getName();
        return Integer.parseInt(pr.substring(0,pr.indexOf('@')));
    }
}
