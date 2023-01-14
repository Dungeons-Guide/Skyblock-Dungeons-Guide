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

package kr.syeyoung.dungeonsguide.mod.discord.rpc;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.Callback;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.User;
import com.jagrosh.discordipc.entities.pipe.PipeStatus;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.events.impl.DiscordUserJoinRequestEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DiscordUserUpdateEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.discord.inviteViewer.PartyJoinRequest;
import kr.syeyoung.dungeonsguide.mod.party.PartyContext;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import lombok.Getter;
import net.minecraftforge.common.MinecraftForge;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiscordIntegrationManager implements IPCListener {
    public static DiscordIntegrationManager INSTANCE = new DiscordIntegrationManager();
    private IPCClient ipcClient;
    private ExecutorService executorService = Executors.newSingleThreadExecutor(DungeonsGuide.THREAD_FACTORY);

    private DiscordIntegrationManager(){
        ipcClient = new IPCClient(816298079732498473L, DungeonsGuide.THREAD_FACTORY);
    }

    @Getter
    private Map<Long, JDiscordRelation> relationMap = new HashMap<>();

    public boolean isLoaded() {
        return ipcClient.getStatus() == PipeStatus.CONNECTED;
    }
    public synchronized void setup() {
        try {
            try {
                if (ipcClient == null)
                    ipcClient = new IPCClient(816298079732498473L, DungeonsGuide.THREAD_FACTORY);
                ipcClient.connect();
            } catch (Throwable t) {
                t.printStackTrace();

                executorService.submit(() -> {
                    try {
                        Thread.sleep(5000L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    setup();
                });
                return;
            }
            ipcClient.subscribe("ACTIVITY_JOIN");
            ipcClient.subscribe("ACTIVITY_SPECTATE");
            ipcClient.subscribe("ACTIVITY_JOIN_REQUEST");
            ipcClient.subscribe("ACTIVITY_INVITE");
            ipcClient.subscribe("RELATIONSHIP_UPDATE");
            ipcClient.send(new JSONObject().put("cmd", "GET_RELATIONSHIPS"), new Callback(this::onRelationshipLoad));

            ipcClient.setListener(this);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    private final SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

    public void respond(RequestHandle2 handle, PartyJoinRequest.Reply accept) {
//        if (activityManager == null) return;
//        activityManager.SendRequestReply.sendRequestReply(activityManager, userID, reply, Pointer.NULL, (callbackData, result) -> {
//            System.out.println("Discord Returned "+result+" For Replying "+reply+" To "+userID.longValue()+"L");
//        });
    }

    public void accept(RequestHandle handle) {
        // ACCEPT_ACTIVITY_INVITE

    }
    public void updatePresence() {
        if (!skyblockStatus.isOnHypixel() || !FeatureRegistry.DISCORD_RICHPRESENCE.isEnabled() || (!skyblockStatus.isOnSkyblock() && FeatureRegistry.DISCORD_RICHPRESENCE.<Boolean>getParameter("disablenotskyblock").getValue())) {
            ipcClient.sendRichPresence(null);
        } else {
            String name = DungeonContext.getDungeonName() == null ? "" : DungeonContext.getDungeonName();
            if (!skyblockStatus.isOnSkyblock()) name ="Somewhere on Hypixel";
            if (name.trim().equals("Your Island")) name = "Private Island";

            RichPresence.Builder presence = new RichPresence.Builder();

            presence.setLargeImage("mort", "mort");
            presence.setState(name);
            presence.setParty(
                    Optional.ofNullable( PartyManager.INSTANCE.getPartyContext()).map(PartyContext::getPartyID).orElse(""),
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
                presence.setStartTimestamp(Instant.MIN.atOffset(ZoneOffset.UTC));
                presence.setDetails("Dungeons Guide");
            }
            if (PartyManager.INSTANCE.getAskToJoinSecret() != null) {
                presence.setJoinSecret(PartyManager.INSTANCE.getAskToJoinSecret());
            }
            ipcClient.sendRichPresence(presence.build());
        }
    }
    @Override
    public void onActivityJoin(IPCClient client, String secret) {
        PartyManager.INSTANCE.joinWithToken(secret);
        System.out.println("Trying to join with token "+secret);
    }

    @Override
    public void onActivityJoinRequest(IPCClient client, String secret, User user) {
        try {
            MinecraftForge.EVENT_BUS.post(new DiscordUserJoinRequestEvent(user, new RequestHandle2(), null, false));
        } catch (Throwable t) {
            t.printStackTrace();
        } // requesthandle2
        System.out.println("Received Join Request from "+user.getId()+" - "+user.getName());
    }

    @Override
    public void onClose(IPCClient client, JSONObject json) {
        executorService.submit(() -> {
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            setup();
        });
    }

    @Override
    public void onDisconnect(IPCClient client, Throwable t) {
        executorService.submit(() -> {
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            setup();
        });
    }

    private JDiscordRelation parse(JSONObject data) {
        JDiscordRelation relation = new JDiscordRelation();
        JDiscordRelation.DiscordRelationType relationType = JDiscordRelation.DiscordRelationType.values()[data.getInt("type")];
        JSONObject userJson = data.getJSONObject("user");
        User user = new User(userJson.getString("username"), userJson.getString("discriminator"),
                Long.parseUnsignedLong(userJson.getString("id")), userJson.getString("avatar"));
        JDiscordRelation.Status status = JDiscordRelation.Status.fromString(data.getString("status"));

        relation.setRelationType(relationType);
        relation.setDiscordUser(user);
        relation.setStatus(status);

        if (data.has("activity") && !data.isNull("activity")) {
            JSONObject activity = data.getJSONObject("activity");
            String appId = activity.getString("application_id");
            relation.setApplicationId(appId);
        }
        return relation;
    }



    private void onRelationshipLoad(Packet object) {
        try {
            JSONArray relationship = object.getJson().getJSONObject("data").getJSONArray("relationships");
            for (Object o : relationship) {
                JSONObject obj = (JSONObject) o;
                JDiscordRelation relation = parse(obj);
                relationMap.put(relation.getDiscordUser().getIdLong(), relation);
            }
        } catch (Throwable e) {e.printStackTrace();}
    }
    @Override
    public void onPacketReceived(IPCClient client, Packet packet) {
        String type = packet.getJson().getString("evt");
        System.out.println(packet);
        if (type.equals("RELATIONSHIP_UPDATE")) {
            JSONObject data = packet.getJson().getJSONObject("data");
            JDiscordRelation relation = parse(data);
            JDiscordRelation old = relationMap.put(relation.getDiscordUser().getIdLong(), relation);
            MinecraftForge.EVENT_BUS.post(new DiscordUserUpdateEvent(old, relation));
        } else if (type.equals("ACTIVITY_INVITE")) {
            // some1 invite me. RequestHandle
            try {
                MinecraftForge.EVENT_BUS.post(new DiscordUserJoinRequestEvent(new User("","",0,""), null, new RequestHandle(), true));
            } catch (Throwable t) {
                t.printStackTrace();
            } // requesthandle2
            System.out.println("Received Invite  from ???");
        }
    }

    public void sendInvite(long id, String content) {
//        ipcClient.send();
        // idk how
    }

    public void init() {
        executorService.submit(this::setup);
    }

    public void cleanup() {
        ipcClient.close();
        ipcClient = null;
    }
}
