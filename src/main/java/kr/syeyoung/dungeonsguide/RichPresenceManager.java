package kr.syeyoung.dungeonsguide;

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

import java.io.*;
import java.net.URL;
import java.time.Instant;

public class RichPresenceManager implements Runnable {
    public static RichPresenceManager INSTANCE = new RichPresenceManager();

    public RichPresenceManager() {
        setup();
        new Thread(this).start();
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
                    e.getDungeonsGuide().getStompConnection().send(new StompPayload().method(StompHeader.SEND)
                            .header("destination", "/app/party.askedtojoin")
                            .payload(new JSONObject().put("token", joinSecret).toString()));
                }).setErroredEventHandler((errorCode, message) -> {
                    System.out.println("ERROR! "+errorCode+ " - "+message);
                    setup();
                }).setDisconnectedEventHandler((errorCode, message) -> {
                    System.out.println("ERROR! "+errorCode+ " - "+message);
                    setup();
                }).build(), true);
    }

    private SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();


    public void updatePresence() {
        if (!skyblockStatus.isOnHypixel() || !FeatureRegistry.ADVANCED_RICHPRESENCE.isEnabled()) {
            DiscordRPC.discordClearPresence();
        } else {
            DiscordRichPresence.Builder richPresenceBuilder = new DiscordRichPresence.Builder(skyblockStatus.getDungeonName());
            richPresenceBuilder.setBigImage("mort", "mort")
                    .setParty(PartyManager.INSTANCE.getPartyID(), PartyManager.INSTANCE.getMemberCount(), 5);

            if (skyblockStatus.getContext() != null) {
                long init = skyblockStatus.getContext().getInit();
                richPresenceBuilder.setStartTimestamps(init);
            } else {
                if (PartyManager.INSTANCE.isAllowAskToJoin())
                    richPresenceBuilder.setSecrets(PartyManager.INSTANCE.getAskToJoinSecret(), null);
            }
            richPresenceBuilder.setDetails("Dungeons Guide RichPresence Test");
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
        while(true) {
            try {
                DiscordRPC.discordRunCallbacks();
                if (skyblockStatus.isOnSkyblock() && !lastLoc.equalsIgnoreCase(skyblockStatus.getDungeonName())) {
                    lastLoc = skyblockStatus.getDungeonName()+"";
                }
                updatePresence();
                Thread.sleep(300L);
            } catch (Exception e) {e.printStackTrace();}
        }
    }
}
