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

package kr.syeyoung.dungeonsguide.mod.dungeon;


import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder.DungeonSpecificDataProvider;
import kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder.DungeonSpecificDataProviderRegistry;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.DungeonEventRecorder;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.impl.DungeonNodataEvent;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.impl.DungeonPuzzleFailureEvent;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonMapConstantRetriever;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonMapLayout;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonRoomScaffoldParser;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.MapPlayerProcessor;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.PathfinderExecutor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.BossfightProcessor;
import kr.syeyoung.dungeonsguide.mod.events.impl.BossroomEnterEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.MapUpdateEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.utils.MapUtils;
import kr.syeyoung.dungeonsguide.mod.utils.TabListUtil;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class DungeonContext {
    @Getter @Setter
    private String dungeonName;
    @Getter
    private final World world;
    @Getter
    private final MapPlayerProcessor mapPlayerMarkerProcessor;
    @Getter
    private DungeonRoomScaffoldParser scaffoldParser;
    @Getter
    private DungeonEventRecorder recorder = new DungeonEventRecorder();

    @Getter
    private List<PathfinderExecutor> executors = new CopyOnWriteArrayList<>();


    @Getter
    private final List<RoomProcessor> globalRoomProcessors = new ArrayList<>();

    // bunch of statistics
    @Getter @Setter
    private long bossRoomEnterSeconds = -1;
    @Getter @Setter
    private long init = -1;
    @Getter @Setter
    private BlockPos bossroomSpawnPos = null;
    @Getter
    private boolean gotMimic = false;


    // general info
    @Getter @Setter
    private boolean trapRoomGen = false;

    @Getter private int maxSpeed = 600;
    @Getter private double secretPercentage = 1.0;

    @Getter @Setter
    public int percentage;

    private PathfinderExecutorExecutor executor = new PathfinderExecutorExecutor(this);


    public void setGotMimic(boolean gotMimic) {
        this.gotMimic = gotMimic;
        recorder.createEvent(new DungeonNodataEvent("MIMIC_KILLED"));
    }

    @Getter
    @Setter
    private BossfightProcessor bossfightProcessor;

    @Getter
    private final Set<String> players = new HashSet<>();


    private Vector2d doorOffset;
    private BlockPos door;

    public DungeonContext(String dungeonName, World world) {
        this.dungeonName = dungeonName;
        this.world = world;
        recorder.createEvent(new DungeonNodataEvent("DUNGEON_CONTEXT_CREATION"));
        mapPlayerMarkerProcessor = new MapPlayerProcessor(this);
        DungeonSpecificDataProvider doorFinder = DungeonSpecificDataProviderRegistry.getDoorFinder(getDungeonName());
        if (doorFinder != null) {
            trapRoomGen = doorFinder.isTrapSpawn(getDungeonName());
            secretPercentage = doorFinder.secretPercentage(getDungeonName());
            maxSpeed = doorFinder.speedSecond(getDungeonName());
        } else {
            throw new IllegalStateException("No door finder found");
        }

        doorOffset = doorFinder.findDoorOffset(world, getDungeonName());
        door = doorFinder.findDoor(world, getDungeonName());

        if (doorOffset == null || door == null) throw new IllegalStateException("?");


        init = System.currentTimeMillis();

        executor.start();
    }



    private final Rectangle roomBoundary = new Rectangle(-10, -10, 138, 138);

    public void tick() {
        if (scaffoldParser != null && bossRoomEnterSeconds == -1 && !roomBoundary.contains(scaffoldParser.getDungeonMapLayout().worldPointToMapPoint(Minecraft.getMinecraft().thePlayer.getPositionVector()))) {
            bossRoomEnterSeconds = FeatureRegistry.DUNGEON_SBTIME.getTimeElapsed() / 1000;
            bossroomSpawnPos = Minecraft.getMinecraft().thePlayer.getPosition();
            MinecraftForge.EVENT_BUS.post(new BossroomEnterEvent());
            recorder.createEvent(new DungeonNodataEvent("BOSSROOM_ENTER"));
            DungeonSpecificDataProvider doorFinder = DungeonSpecificDataProviderRegistry.getDoorFinder(getDungeonName());
            if (doorFinder != null) {
                bossfightProcessor = doorFinder.createBossfightProcessor(world, getDungeonName());
            } else {
                ChatTransmitter.sendDebugChat(new ChatComponentText("Error:: Null Data Providier"));
            }
        }

        if (scaffoldParser != null) {
            for (DungeonRoom dungeonRoom : scaffoldParser.getDungeonRoomList()) {
                if (!dungeonRoom.isMatched()) {
                    dungeonRoom.tryRematch();
                }
            }
        }

        players.clear();
        players.addAll(TabListUtil.getPlayersInDungeon());
    }


    private boolean processed = false;
    private void processFinishedMap(byte[] mapData) {
        if (MapUtils.getMapColorAt(mapData, 0, 0) == 0) {
            return;
        }
        if (processed) {
            return;
        }
        processed = true;

        MapUtils.clearMap();
        MapUtils.record(mapData, 0, 0, Color.GREEN);


        FeatureRegistry.ETC_COLLECT_SCORE.collectDungeonRunData(mapData, this);
    }
    private int mapId = -1;
    public void onMapUpdate(MapUpdateEvent mapUpdateEvent) {
        if (mapUpdateEvent.getMapId() > 10000) return; // tictactoe
        if (mapId == -1) {
            mapId = mapUpdateEvent.getMapId();
        }
        if (mapId != mapUpdateEvent.getMapId()) return;

        if (isEnded()) {
            processFinishedMap(mapUpdateEvent.getMapData().colors);
        }
        if (getScaffoldParser() == null) {
            DungeonMapLayout layout = DungeonMapConstantRetriever.beginParsingMap(mapUpdateEvent.getMapData().colors, door, doorOffset);
            if (layout != null)
                scaffoldParser = new DungeonRoomScaffoldParser(
                        layout,
                        this
                );
        } else {
            getScaffoldParser().processMap(mapUpdateEvent.getMapData());
        }
    }

    public void cleanup() {
        executor.interrupt();
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
            Point roomPt = scaffoldParser.getDungeonMapLayout().worldPointToRoomPoint(new BlockPos(x, 70, z));
            ChatTransmitter.sendDebugChat(new ChatComponentText("Message from Other dungeons guide :: " + roomPt.x + " / " + roomPt.y + " total secrets " + secrets2));
            DungeonRoom dr = scaffoldParser.getRoomMap().get(roomPt);
            if (dr != null) {
                dr.setTotalSecrets(secrets2);
            }
        } else if (formatted.contains("$DG-Mimic")) {
            setGotMimic(true);
        } else if (formatted.startsWith("§r§c§lPUZZLE FAIL! ") && formatted.endsWith(" §r§4Y§r§ci§r§6k§r§ee§r§as§r§2!§r")) {
            recorder.createEvent(new DungeonPuzzleFailureEvent(TextUtils.stripColor(formatted.split(" ")[2]), formatted));
        } else if (formatted.contains("§6> §e§lEXTRA STATS §6<")) {
            recorder.createEvent(new DungeonNodataEvent("DUNGEON_END"));
            ended = true;
        } else if (formatted.contains("§r§c☠ §r§eDefeated ")) {
            defeated = true;
        }
    }
}
