/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide;

import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.events.SkyblockJoinedEvent;
import kr.syeyoung.dungeonsguide.events.SkyblockLeftEvent;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.party.PartyInviteViewer;
import kr.syeyoung.dungeonsguide.party.PartyJoinRequest;
import kr.syeyoung.dungeonsguide.party.PartyManager;
import kr.syeyoung.dungeonsguide.stomp.StompHeader;
import kr.syeyoung.dungeonsguide.stomp.StompPayload;
import net.arikia.dev.drpc.*;
import net.arikia.dev.drpc.callbacks.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.json.JSONObject;

public class RichPresenceManager implements Runnable {
    public static RichPresenceManager INSTANCE = new RichPresenceManager();
    private Thread t = new Thread(this);


    public RichPresenceManager() {
        t.start();
        Runtime.getRuntime().addShutdownHook(new Thread(DiscordRPC::discordShutdown));
    }
    public void setup() {
        DiscordRPC.discordInitialize("816298079732498473", new DiscordEventHandlers.Builder()
                .setReadyEventHandler(new ReadyCallback() {
                    @Override
                    public void apply(DiscordUser user) {
                        updatePresence();
                    }
                }).setJoinRequestEventHandler(request -> {
                    PartyJoinRequest partyJoinRequest = new PartyJoinRequest();
                    partyJoinRequest.setDiscordUser(request);
                    partyJoinRequest.setExpire(System.currentTimeMillis() + 30000);

                    PartyInviteViewer.INSTANCE.joinRequests.add(partyJoinRequest);
                }).setJoinGameEventHandler(joinSecret -> {
                    DungeonsGuide.getDungeonsGuide().getStompConnection().send(new StompPayload().method(StompHeader.SEND)
                            .header("destination", "/app/party.askedtojoin")
                            .payload(new JSONObject().put("token", joinSecret).toString()));
                }).setErroredEventHandler((errorCode, message) -> {
                    System.out.println("ERROR! "+errorCode+ " - "+message);
                    t.interrupt();
                    (t = new Thread(this)).start();
                }).setDisconnectedEventHandler((errorCode, message) -> {
                    System.out.println("ERROR! "+errorCode+ " - "+message);
                    t.interrupt();
                    (t = new Thread(this)).start();
                }).build(), true);
    }

    private final SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();


    public void updatePresence() {
        if (!skyblockStatus.isOnHypixel() || !FeatureRegistry.ADVANCED_RICHPRESENCE.isEnabled() || (!skyblockStatus.isOnSkyblock() && FeatureRegistry.ADVANCED_RICHPRESENCE.<Boolean>getParameter("disablenotskyblock").getValue())) {
            DiscordRPC.discordClearPresence();
        } else {
            String name = skyblockStatus.getDungeonName();
            if (name.trim().equals("Your Island")) name = "Private Island";
            DiscordRichPresence.Builder richPresenceBuilder = new DiscordRichPresence.Builder(name);
            richPresenceBuilder.setBigImage("mort", "mort")
                    .setParty(PartyManager.INSTANCE.getPartyID(), PartyManager.INSTANCE.getMemberCount(), PartyManager.INSTANCE.getMaxParty());

            if (skyblockStatus.getContext() != null) {
                DungeonContext dungeonContext = skyblockStatus.getContext();
                long init = dungeonContext.getInit();
                richPresenceBuilder.setStartTimestamps(init);

                if (dungeonContext.getBossfightProcessor() != null) {
                    richPresenceBuilder.setDetails("Fighting "+dungeonContext.getBossfightProcessor().getBossName()+": "+dungeonContext.getBossfightProcessor().getCurrentPhase());
                } else {
                    richPresenceBuilder.setDetails("Clearing rooms");
                }
            }
            if (PartyManager.INSTANCE.isAllowAskToJoin())
                richPresenceBuilder.setSecrets(PartyManager.INSTANCE.getAskToJoinSecret(), null);
            richPresenceBuilder.setDetails("Dungeons Guide");
            DiscordRPC.discordUpdatePresence(richPresenceBuilder.build());
        }
    }


    @SubscribeEvent
    public void joinSkyblock(SkyblockJoinedEvent skyblockJoinedEvent) {
        updatePresence();
    }
    @SubscribeEvent
    public void leaveSkyblock(SkyblockLeftEvent skyblockLeftEvent) {
        updatePresence();
    }

    private String lastLoc = "";

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent clientTickEvent) {
    }
    @Override
    public void run() {

        try {
            Thread.sleep(300L);
            setup();
            while(!Thread.interrupted()) {
                    DiscordRPC.discordRunCallbacks();
                    if (skyblockStatus.isOnSkyblock() && !lastLoc.equalsIgnoreCase(skyblockStatus.getDungeonName())) {
                        lastLoc = skyblockStatus.getDungeonName()+"";
                    }
                    updatePresence();
                    Thread.sleep(300L);
            }
        } catch (Exception e) {e.printStackTrace();}
    }
}
