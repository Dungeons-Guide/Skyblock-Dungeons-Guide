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
import kr.syeyoung.dungeonsguide.mod.events.impl.DiscordUserInvitedEvent;
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

import java.lang.management.ManagementFactory;
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

    public void respond(String userId, PartyJoinRequest.Reply accept) {
        if (accept == PartyJoinRequest.Reply.ACCEPT) {
            ipcClient.send(new JSONObject()
                    .put("cmd", "SEND_ACTIVITY_JOIN_INVITE")
                    .put("args", new JSONObject().put("user_id", userId)), null);
        } else if (accept == PartyJoinRequest.Reply.DENY || accept == PartyJoinRequest.Reply.IGNORE) {
            ipcClient.send(new JSONObject()
                    .put("cmd", "CLOSE_ACTIVITY_JOIN_REQUEST")
                    .put("args", new JSONObject().put("user_id", userId)), null);
        }
    }

    public void accept(RequestHandle handle) {
        // ACCEPT_ACTIVITY_INVITE
        ipcClient.send(new JSONObject()
                .put("cmd", "ACCEPT_ACTIVITY_INVITE")
                .put("args", new JSONObject()
                        .put("type", 1)
                        .put("user_id", handle.getUserId())
                        .put("session_id", handle.getSessionId())
                        .put("channel_id", handle.getChannelId())
                        .put("message_id", handle.getMessageId())), null);

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
        } else if (type.equals("ACTIVITY_JOIN_REQUEST")) {
            JSONObject data = packet.getJson().getJSONObject("data");
            try {
                User user =  new User(data.getJSONObject("user")
                        .getString("username"),
                        data.getJSONObject("user")
                                .getString("discriminator"),
                        Long.parseUnsignedLong(data.getJSONObject("user")
                                .getString("id")),
                        data.getJSONObject("user")
                                .getString("avatar"));
                MinecraftForge.EVENT_BUS.post(new DiscordUserJoinRequestEvent(user));
                System.out.println("Received Join Request from "+user.getId()+" - "+user.getName());
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else if (type.equals("ACTIVITY_INVITE")) {
            // some1 invite me. RequestHandle
            //{"cmd":"DISPATCH","data":{"user":{"id":"310702108997320705","username":"nea89","discriminator":"0998","avatar":"abf6e48fa230ef0339059bf07ff01a9f","avatar_decoration":null,"bot":false,"flags":4325376,"premium_type":0},"activity":{"application_id":"816298079732498473","assets":{"large_image":"816487567804989461","large_text":"mort"},"created_at":"1673783244721","details":"blah","flags":194,"id":"35ed478de33b7940","name":"Skyblock Dungeons Guide","party":{"id":"asdasdadjfilkdjflksjldfjlsd","size":[1,50]},"session_id":"559d9370e1334d8607bb83ca85d1f1e3","state":"blah","supported_platforms":["desktop","android","ios"],"timestamps":{},"type":0},"type":1,"channel_id":"1017804419330494508","message_id":"1064149363808555108"},"evt":"ACTIVITY_INVITE","nonce":null}
            JSONObject data = packet.getJson().getJSONObject("data");
            try {

                if (!data.getJSONObject("activity").getString("application_id").equals("816298079732498473"))
                    return;
                MinecraftForge.EVENT_BUS.post(new DiscordUserInvitedEvent(
                        new User(data.getJSONObject("user")
                                .getString("username"),
                                data.getJSONObject("user")
                                        .getString("discriminator"),
                                Long.parseUnsignedLong(data.getJSONObject("user")
                                        .getString("id")),
                                data.getJSONObject("user")
                                        .getString("avatar")),
                        new RequestHandle(
                                data.getJSONObject("user").getString("id"),
                                data.getJSONObject("activity").getString("session_id"),
                                data.getString("channel_id"),
                                data.getString("message_id")
                        )));
            } catch (Throwable t) {
                t.printStackTrace();
            } // requesthandle2
            System.out.println("Received Invite  from ???");
        }
    }

    public void sendInvite(String id, String content) {
        ipcClient.send(new JSONObject()
                .put("cmd", "ACTIVITY_INVITE_USER")
                .put("args", new JSONObject()
                        .put("type", 1)
                        .put("user_id", id)
                        .put("content", content)
                        .put("pid",getPID())
                ), null);
    }

    private static int getPID()
    {
        String pr = ManagementFactory.getRuntimeMXBean().getName();
        return Integer.parseInt(pr.substring(0,pr.indexOf('@')));
    }

    public void init() {
        executorService.submit(this::setup);
    }

    public void cleanup() {
        ipcClient.close();
        ipcClient = null;
    }

}
