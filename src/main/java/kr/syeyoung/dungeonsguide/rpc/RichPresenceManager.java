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

package kr.syeyoung.dungeonsguide.rpc;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.events.DiscordUserJoinRequestEvent;
import kr.syeyoung.dungeonsguide.events.DiscordUserUpdateEvent;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.gamesdk.GameSDK;
import kr.syeyoung.dungeonsguide.gamesdk.jna.NativeGameSDK;
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.*;
import kr.syeyoung.dungeonsguide.gamesdk.jna.enumuration.*;
import kr.syeyoung.dungeonsguide.gamesdk.jna.interfacestruct.*;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.*;
import kr.syeyoung.dungeonsguide.party.PartyManager;
import kr.syeyoung.dungeonsguide.stomp.StompHeader;
import kr.syeyoung.dungeonsguide.stomp.StompPayload;
import lombok.Getter;
import net.minecraftforge.common.MinecraftForge;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RichPresenceManager implements Runnable {
    public static RichPresenceManager INSTANCE = new RichPresenceManager();
    private Thread t = new Thread(this);

    public RichPresenceManager() {
        t.start();
    }
    @Getter
    private NativeGameSDK nativeGameSDK;
    @Getter
    private IDiscordCore iDiscordCore;
    private IDiscordActivityManager activityManager;
    private IDiscordRelationshipManager relationshipManager;
    private IDiscordActivityEvents.ByReference callbacks;
    private IDiscordRelationshipEvents.ByReference relation_callbacks;

    @Getter
    private Map<Long, JDiscordRelation> relationMap = new HashMap<>();

    @Getter
    private int lastSetupCode = -99999;
    public int setup() {
        if (iDiscordCore != null) {
            iDiscordCore.Destroy.destroy(iDiscordCore);
            activityManager = null; callbacks = null; relation_callbacks = null; relationMap.clear();
        }

        nativeGameSDK = GameSDK.getNativeGameSDK();
        if (nativeGameSDK == null) return -9999;
        DiscordCreateParams discordCreateParams = new DiscordCreateParams();
        discordCreateParams.flags = new UInt64(EDiscordCreateFlags.DiscordCreateFlags_NoRequireDiscord.getValue());
        discordCreateParams.client_id = new DiscordClientID(816298079732498473L);

        callbacks = new IDiscordActivityEvents.ByReference();
        callbacks.OnActivityInvite = (eventData, type, user, activity) -> {
            try {
                MinecraftForge.EVENT_BUS.post(new DiscordUserJoinRequestEvent(user, true));
            } catch (Throwable t) {
                t.printStackTrace();
            }
            System.out.println("Received Join Request from "+user.id.longValue()+" ("+GameSDK.readString(user.username)+")");
        };
        callbacks.OnActivityJoin = (eventData, secret) -> {
            DungeonsGuide.getDungeonsGuide().getStompConnection().send(new StompPayload().method(StompHeader.SEND)
                .header("destination", "/app/party.askedtojoin")
                .payload(new JSONObject().put("token", secret).toString()));
            System.out.println("Trying to join with token "+secret);
        };
        callbacks.OnActivityJoinRequest = (eventData, user) -> {
            try {
                MinecraftForge.EVENT_BUS.post(new DiscordUserJoinRequestEvent(user, false));
            } catch (Throwable t) {
                t.printStackTrace();
            }
                System.out.println("Received Join Request from "+user.id.longValue()+" - "+GameSDK.readString(user.username));
        };
        callbacks.OnActivitySpectate = (eventData, secret) -> {
        };
        callbacks.write();

        relation_callbacks = new IDiscordRelationshipEvents.ByReference();
        relation_callbacks.OnRefresh = (p) -> {
            IDiscordRelationshipManager iDiscordRelationshipManager = iDiscordCore.GetRelationshipManager.getRelationshipManager(iDiscordCore);
            iDiscordRelationshipManager.Filter.filter(iDiscordRelationshipManager, Pointer.NULL, (d, relation) -> true);
            IntByReference intByReference = new IntByReference();
            iDiscordRelationshipManager.Count.count(iDiscordRelationshipManager, intByReference);
            int count = intByReference.getValue();
            for (int i = 0; i < count; i++) {
                DiscordRelationship discordRelationship = new DiscordRelationship();
                iDiscordRelationshipManager.GetAt.getAt(iDiscordRelationshipManager, new UInt32(i), discordRelationship);

                JDiscordRelation jDiscordRelation = JDiscordRelation.fromJNA(discordRelationship);
                relationMap.put(jDiscordRelation.getDiscordUser().getId(), jDiscordRelation);
            }
        };
        relation_callbacks.OnRelationshipUpdate = (p, rel) -> {
            JDiscordRelation jDiscordRelation = JDiscordRelation.fromJNA(rel);
            JDiscordRelation prev = relationMap.put(jDiscordRelation.getDiscordUser().getId(), jDiscordRelation);
            try {
                MinecraftForge.EVENT_BUS.post(new DiscordUserUpdateEvent(prev, jDiscordRelation));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        };
        relation_callbacks.write();

        discordCreateParams.activity_events = callbacks;
        discordCreateParams.relationship_events = relation_callbacks; // 96

        PointerByReference pointerByReference = new PointerByReference();
        discordCreateParams.write();

        EDiscordResult eDiscordResult = nativeGameSDK.DiscordCreate(new DiscordVersion(NativeGameSDK.DISCORD_VERSION), discordCreateParams, pointerByReference);
        if (eDiscordResult != EDiscordResult.DiscordResult_Ok) return eDiscordResult.getValue();
        if (pointerByReference.getValue() == Pointer.NULL) return -9998;
        iDiscordCore = new IDiscordCore(pointerByReference.getValue());

        iDiscordCore.SetLogHook.setLogHook(iDiscordCore, EDiscordLogLevel.DiscordLogLevel_Debug, Pointer.NULL, new IDiscordCore.LogHook() {
            @Override
            public void hook(Pointer hookData, EDiscordLogLevel level, String message) {
                System.out.println(message+" - "+level+" - "+hookData);
            }
        });

        activityManager = iDiscordCore.GetActivityManager.getActivityManager(iDiscordCore);
        relationshipManager = iDiscordCore.GetRelationshipManager.getRelationshipManager(iDiscordCore);

        return eDiscordResult.getValue();
    }

    private final SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

    public void respond(DiscordSnowflake userID, EDiscordActivityJoinRequestReply reply) {
        activityManager.SendRequestReply.sendRequestReply(activityManager, userID, reply, Pointer.NULL, (callbackData, result) -> {
            System.out.println("Discord Returned "+result+" For Replying "+reply+" To "+userID.longValue()+"L");
        });
    }

    public void accept(DiscordSnowflake userID) {
        activityManager.AcceptInvite.acceptInvite(activityManager, userID, Pointer.NULL, (callbackData, result) -> {
            System.out.println("Discord Returned "+result+" For Accepting invite from "+userID.longValue()+"L");
        });
    }
    public void updatePresence() {
        if (!skyblockStatus.isOnHypixel() || !FeatureRegistry.DISCORD_RICHPRESENCE.isEnabled() || (!skyblockStatus.isOnSkyblock() && FeatureRegistry.DISCORD_RICHPRESENCE.<Boolean>getParameter("disablenotskyblock").getValue())) {
            activityManager.ClearActivity.clearActivity(activityManager, Pointer.NULL, new NativeGameSDK.DiscordCallback() {
                @Override
                public void callback(Pointer callbackData, EDiscordResult result) {
                }
            });
        } else {
            String name = skyblockStatus.getDungeonName() == null ? "" : skyblockStatus.getDungeonName();
            if (!skyblockStatus.isOnSkyblock()) name ="Somewhere on Hypixel";
            if (name.trim().equals("Your Island")) name = "Private Island";

            DiscordActivity latestDiscordActivity = new DiscordActivity();
            latestDiscordActivity.assets = new DiscordActivityAssets();
            latestDiscordActivity.secrets = new DiscordActivitySecrets();
            latestDiscordActivity.party = new DiscordActivityParty();
            latestDiscordActivity.party.discordActivityParty = new DiscordPartySize();
            latestDiscordActivity.timestamps = new DiscordActivityTimestamps();
            GameSDK.writeString(latestDiscordActivity.assets.large_image, "mort");
            GameSDK.writeString(latestDiscordActivity.assets.large_text, "mort");
            GameSDK.writeString(latestDiscordActivity.state, name);

                GameSDK.writeString(latestDiscordActivity.party.id, PartyManager.INSTANCE.getPartyID() == null ? "" : PartyManager.INSTANCE.getPartyID());
                latestDiscordActivity.party.discordActivityParty.current_size = new Int32(PartyManager.INSTANCE.getMemberCount());
                latestDiscordActivity.party.discordActivityParty.max_size = new Int32(PartyManager.INSTANCE.getMaxParty());

            if (skyblockStatus.getContext() != null) {
                DungeonContext dungeonContext = skyblockStatus.getContext();
                long init = dungeonContext.getInit();
                latestDiscordActivity.timestamps.start = new DiscordTimestamp(init);

                if (dungeonContext.getBossfightProcessor() != null) {
                    GameSDK.writeString(latestDiscordActivity.details, "Fighting "+dungeonContext.getBossfightProcessor().getBossName()+": "+dungeonContext.getBossfightProcessor().getCurrentPhase());
                } else {
                    GameSDK.writeString(latestDiscordActivity.details, "Clearing Rooms");
                }
            } else {
                latestDiscordActivity.timestamps.start = new DiscordTimestamp(0);
                GameSDK.writeString(latestDiscordActivity.details, "Dungeons Guide");
            }
            if (PartyManager.INSTANCE.isAllowAskToJoin()) {
                GameSDK.writeString(latestDiscordActivity.secrets.join, PartyManager.INSTANCE.getAskToJoinSecret());
            } else {
                GameSDK.writeString(latestDiscordActivity.secrets.join, "");
            }
            activityManager.UpdateActivity.updateActivity(activityManager, latestDiscordActivity, Pointer.NULL, new NativeGameSDK.DiscordCallback() {
                @Override
                public void callback(Pointer callbackData, EDiscordResult result) {
                }
            });
        }
    }

    @Override
    public void run() {
        boolean setup = true;
        int counter = 0;
        while(!Thread.interrupted()) {
            try {
                if (iDiscordCore == null || setup) {
                    lastSetupCode = setup();
                    System.out.println("Discord returned "+lastSetupCode+" for setup "+EDiscordResult.fromValue(lastSetupCode));
                    setup = lastSetupCode != EDiscordResult.DiscordResult_Ok.getValue();
                    counter = 0;
                } else {
                    EDiscordResult eDiscordResult = iDiscordCore.RunCallbacks.runCallbacks(iDiscordCore);
                    if (eDiscordResult == EDiscordResult.DiscordResult_NotRunning) {
                        setup = true;
                    } else {
                        if (counter == 0)
                            updatePresence();
                        if (++counter == 15) counter = 0;
                    }
                }
                Thread.sleep(16L);
            } catch (Exception e) {e.printStackTrace();}
        }
    }
}
