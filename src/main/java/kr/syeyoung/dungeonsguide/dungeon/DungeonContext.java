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

package kr.syeyoung.dungeonsguide.dungeon;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonSpecificDataProvider;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonSpecificDataProviderRegistry;
import kr.syeyoung.dungeonsguide.dungeon.events.DungeonEvent;
import kr.syeyoung.dungeonsguide.dungeon.events.DungeonEventData;
import kr.syeyoung.dungeonsguide.dungeon.events.impl.DungeonCryptBrokenEvent;
import kr.syeyoung.dungeonsguide.dungeon.events.impl.DungeonNodataEvent;
import kr.syeyoung.dungeonsguide.dungeon.events.impl.DungeonPuzzleFailureEvent;
import kr.syeyoung.dungeonsguide.dungeon.events.impl.DungeonSecretCountChangeEvent;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bossfight.BossfightProcessor;
import kr.syeyoung.dungeonsguide.events.impl.BossroomEnterEvent;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.utils.TabListUtil;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;

import java.awt.*;
import java.util.List;
import java.util.*;

public class DungeonContext {
    @Getter
    @Setter
    public int percentage;
    @Getter
    private final World world;
    @Getter
    private final MapProcessor mapProcessor;

    @Getter
    @Setter
    private BlockPos dungeonMin;

    @Getter
    private final Map<Point, DungeonRoom> roomMapper = new HashMap<>();
    @Getter
    private final List<DungeonRoom> dungeonRoomList = new ArrayList<>();

    @Getter
    private final List<RoomProcessor> globalRoomProcessors = new ArrayList<>();

    @Getter
    private final Map<String, Integer> deaths = new HashMap<>();
    @Getter
    private final List<String[]> milestoneReached = new ArrayList<>();
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

    @Getter
    private int maxSpeed = 600;
    @Getter
    private double secretPercentage = 1.0;

    public void setGotMimic(boolean gotMimic) {
        this.gotMimic = gotMimic;
        createEvent(new DungeonNodataEvent("MIMIC_KILLED"));
    }

    @Getter
    @Setter
    private BossfightProcessor bossfightProcessor;

    @Getter
    private final Set<String> players = new HashSet<>();

    @Getter
    private final List<DungeonEvent> events = new ArrayList<>();

    public DungeonContext(World world) {
        this.world = world;
        createEvent(new DungeonNodataEvent("DUNGEON_CONTEXT_CREATION"));
        mapProcessor = new MapProcessor(this);
        DungeonSpecificDataProvider doorFinder = DungeonSpecificDataProviderRegistry.getDoorFinder(DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getDungeonName());
        if (doorFinder != null) {
            trapRoomGen = doorFinder.isTrapSpawn(DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getDungeonName());

            secretPercentage = doorFinder.secretPercentage(DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getDungeonName());
            maxSpeed = doorFinder.speedSecond(DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getDungeonName());
        } else {
            mapProcessor.setBugged(true);
        }
        init = System.currentTimeMillis();
    }

    public void createEvent(DungeonEventData eventData) {
//        events.add(new DungeonEvent(eventData));
    }


    private final Rectangle roomBoundary = new Rectangle(-10, -10, 138, 138);

    public void tick() {


        if (mapProcessor.isInitialized() && BossRoomEnterSeconds == -1 && !roomBoundary.contains(mapProcessor.worldPointToMapPoint(Minecraft.getMinecraft().thePlayer.getPositionVector()))) {
            BossRoomEnterSeconds = FeatureRegistry.DUNGEON_SBTIME.getTimeElapsed() / 1000;
            bossroomSpawnPos = Minecraft.getMinecraft().thePlayer.getPosition();
            MinecraftForge.EVENT_BUS.post(new BossroomEnterEvent());
            createEvent(new DungeonNodataEvent("BOSSROOM_ENTER"));
            DungeonSpecificDataProvider doorFinder = DungeonSpecificDataProviderRegistry.getDoorFinder(DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getDungeonName());
            if (doorFinder != null) {
                bossfightProcessor = doorFinder.createBossfightProcessor(world, DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getDungeonName());
            } else {
                DungeonsGuide.sendDebugChat(new ChatComponentText("Error:: Null Data Providier"));
            }
        }

        players.clear();
        players.addAll(TabListUtil.getPlayersInDungeon());


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

    @Getter
    private boolean ended = false;
    @Getter
    private boolean defeated = false;

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
            Point roomPt = mapProcessor.worldPointToRoomPoint(new BlockPos(x, 70, z));
            DungeonsGuide.sendDebugChat(new ChatComponentText("Message from Other dungeons guide :: " + roomPt.x + " / " + roomPt.y + " total secrets " + secrets2));
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
            ended = true;
        } else if (formatted.contains("§r§c☠ §r§eDefeated ")) {
            defeated = true;
        }
    }
}
