package kr.syeyoung.dungeonsguide.dungeon;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonSpecificDataProvider;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonSpecificDataProviderRegistry;
import kr.syeyoung.dungeonsguide.dungeon.events.*;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.events.BossroomEnterEvent;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.FeatureDungeonMap;
import kr.syeyoung.dungeonsguide.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.roomprocessor.bossfight.BossfightProcessor;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;

import java.awt.*;
import java.util.*;
import java.util.List;

public class DungeonContext {
    @Getter
    private final World world;
    @Getter
    private final MapProcessor mapProcessor;

    @Getter
    @Setter
    private BlockPos dungeonMin;

    @Getter
    private final Map<Point, DungeonRoom> roomMapper = new HashMap<Point, DungeonRoom>();
    @Getter
    private final List<DungeonRoom> dungeonRoomList = new ArrayList<DungeonRoom>();

    @Getter
    private final List<RoomProcessor> globalRoomProcessors = new ArrayList<RoomProcessor>();

    @Getter
    private final Map<String, Integer> deaths = new HashMap<String, Integer>();
    @Getter
    private final List<String[]> milestoneReached = new ArrayList<String[]>();
    @Getter
    @Setter
    private long BossRoomEnterSeconds = -1;
    @Getter
    @Setter
    private long init = -1;
    @Getter
    @Setter
    private BlockPos bossroomSpawnPos = null;

    @Getter
    @Setter
    private boolean trapRoomGen = false;

    @Getter
    private boolean gotMimic = false;

    private int latestSecretCnt = 0;
    private int latestTotalSecret = 0;
    private int latestCrypts = 0;

    public void setGotMimic(boolean gotMimic) {
        this.gotMimic = gotMimic;
        createEvent(new DungeonNodataEvent("MIMIC_KILLED"));
    }

    @Getter
    @Setter
    private BossfightProcessor bossfightProcessor;

    @Getter
    private final Set<String> players = new HashSet<String>();

    @Getter
    private final List<DungeonEvent> events = new ArrayList<DungeonEvent>();

    public DungeonContext(World world) {
        this.world = world;
        createEvent(new DungeonNodataEvent("DUNGEON_CONTEXT_CREATION"));
        mapProcessor = new MapProcessor(this);
        DungeonSpecificDataProvider doorFinder = DungeonSpecificDataProviderRegistry.getDoorFinder(e.getDungeonsGuide().getSkyblockStatus().getDungeonName());
        if (doorFinder != null)
            trapRoomGen = doorFinder.isTrapSpawn(e.getDungeonsGuide().getSkyblockStatus().getDungeonName());
        else mapProcessor.setBugged(true);
        init = System.currentTimeMillis();
    }

    public void createEvent(DungeonEventData eventData) {
        events.add(new DungeonEvent(eventData));
    }


    private final Rectangle roomBoundary = new Rectangle(0,0,128,128);

    public void tick() {
        mapProcessor.tick();

        if (mapProcessor.isInitialized() && BossRoomEnterSeconds == -1 && !roomBoundary.contains(mapProcessor.worldPointToMapPoint(Minecraft.getMinecraft().thePlayer.getPositionVector()))) {
            BossRoomEnterSeconds = FeatureRegistry.DUNGEON_SBTIME.getTimeElapsed() / 1000;
            bossroomSpawnPos = Minecraft.getMinecraft().thePlayer.getPosition();
            MinecraftForge.EVENT_BUS.post(new BossroomEnterEvent());
            createEvent(new DungeonNodataEvent("BOSSROOM_ENTER"));
            DungeonSpecificDataProvider doorFinder = DungeonSpecificDataProviderRegistry.getDoorFinder(e.getDungeonsGuide().getSkyblockStatus().getDungeonName());
            if (doorFinder != null) {
                bossfightProcessor = doorFinder.createBossfightProcessor(world, e.getDungeonsGuide().getSkyblockStatus().getDungeonName());
            } else {
                e.sendDebugChat(new ChatComponentText("Error:: Null Data Providier"));
            }
        }
        List<NetworkPlayerInfo> list = FeatureDungeonMap.field_175252_a.sortedCopy(Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap());
        try {
            for (int i = 1; i < 20; i++) {
                NetworkPlayerInfo networkPlayerInfo = list.get(i);
                String name = networkPlayerInfo.getDisplayName() != null ? networkPlayerInfo.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfo.getPlayerTeam(), networkPlayerInfo.getGameProfile().getName());
                if (name.trim().equals("§r") || name.startsWith("§r ")) continue;
                players.add(TextUtils.stripColor(name).trim().split(" ")[0]);
            }
        } catch (Exception e) {}

        if (latestSecretCnt != FeatureRegistry.DUNGEON_SECRETS.getSecretsFound()) {
            int newSecretCnt = FeatureRegistry.DUNGEON_SECRETS.getSecretsFound();
            createEvent(new DungeonSecretCountChangeEvent(latestSecretCnt, newSecretCnt, latestTotalSecret, FeatureRegistry.DUNGEON_SECRETS.sureOfTotalSecrets()));
            latestSecretCnt = newSecretCnt;
        }
        if (latestTotalSecret != FeatureRegistry.DUNGEON_SECRETS.getTotalSecretsInt()) {
            latestTotalSecret = FeatureRegistry.DUNGEON_SECRETS.getTotalSecretsInt();
            createEvent(new DungeonSecretCountChangeEvent(latestSecretCnt, latestSecretCnt, latestTotalSecret, FeatureRegistry.DUNGEON_SECRETS.sureOfTotalSecrets()));
        }
        if (latestCrypts != FeatureRegistry.DUNGEON_TOMBS.getTombsFound()) {
            int newlatestCrypts = FeatureRegistry.DUNGEON_TOMBS.getTombsFound();
            createEvent(new DungeonCryptBrokenEvent(latestCrypts, newlatestCrypts));
            this.latestCrypts = newlatestCrypts;
        }
    }

    public void onChat(ClientChatReceivedEvent event) {
        IChatComponent component = event.message;
        String formatted = component.getFormattedText();
        if (formatted.contains("$DG-Comm")) {
            event.setCanceled(true);
            String data = component.getFormattedText().substring(component.getFormattedText().indexOf("$DG-Comm"));
            String actual = TextUtils.stripColor(data);
            String coords = actual.split(" ")[1];
            String secrets = actual.split(" ")[2];
            int x = Integer.parseInt(coords.split("/")[0]);
            int z = Integer.parseInt(coords.split("/")[1]);
            int secrets2 = Integer.parseInt(secrets);
            Point roomPt = mapProcessor.worldPointToRoomPoint(new BlockPos(x,70,z));
            e.sendDebugChat(new ChatComponentText("Message from Other dungeons guide :: "+roomPt.x+" / " + roomPt.y + " total secrets "+secrets2));
            DungeonRoom dr = roomMapper.get(roomPt);
            if (dr != null) {
                dr.setTotalSecrets(secrets2);
            }
        } else if (formatted.contains("$DG-Mimic")) {
            setGotMimic(true);
        } else if (formatted.startsWith("§r§c§lPUZZLE FAIL! ") && formatted.endsWith(" §r§4Y§r§ci§r§6k§r§ee§r§as§r§2!§r")) {
            createEvent(new DungeonPuzzleFailureEvent(TextUtils.stripColor(formatted.split(" ")[2]), formatted));
        } else if (formatted.contains("§6> §e§lEXTRA STATS §6<")) {
            createEvent(new DungeonNodataEvent("DUNGEON_END"));
        }
    }
}
