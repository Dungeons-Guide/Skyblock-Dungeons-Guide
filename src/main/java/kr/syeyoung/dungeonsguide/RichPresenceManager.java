package kr.syeyoung.dungeonsguide;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.User;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;
import kr.syeyoung.dungeonsguide.events.SkyblockJoinedEvent;
import kr.syeyoung.dungeonsguide.events.SkyblockLeftEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.json.JSONObject;

import java.time.OffsetDateTime;

public class RichPresenceManager implements IPCListener {
    public static RichPresenceManager INSTANCE = new RichPresenceManager();

    private IPCClient ipcClient;
    public void setup() throws NoDiscordClientException {
        ipcClient = new IPCClient(816298079732498473L);
        ipcClient.setListener(this);
        ipcClient.connect();
    }

    @Override
    public void onReady(IPCClient client) {
        updatePresence();
    }

    @Override
    public void onActivityJoinRequest(IPCClient client, String secret, User user) {
        System.out.println(user.getAsMention()+" wanna join");
    }

    @Override
    public void onPacketReceived(IPCClient client, Packet packet) {
        System.out.println("Packet recv from rpc "+packet);
    }

    @Override
    public void onDisconnect(IPCClient client, Throwable t) {
        t.printStackTrace();
        try {
            setup();
        } catch (NoDiscordClientException e) {
            e.printStackTrace();
        }
    }

    private SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();


    public void updatePresence() {
        if (!skyblockStatus.isOnHypixel()) {
            ipcClient.sendRichPresence(null);
        } else {
            ipcClient.sendRichPresence(new RichPresence.Builder()
            .setJoinSecret(""));
        }
    }

    private String JOIN_SECRET;


    @SubscribeEvent
    public void joinSkyblock(SkyblockJoinedEvent skyblockJoinedEvent) {
        updatePresence();
    }
    @SubscribeEvent
    public void leaveSkyblock(SkyblockLeftEvent skyblockLeftEvent) {
        updatePresence();
    }
}
