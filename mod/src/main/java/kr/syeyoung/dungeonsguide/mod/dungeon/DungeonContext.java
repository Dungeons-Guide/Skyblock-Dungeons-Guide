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
import kr.syeyoung.dungeonsguide.mod.dungeon.dataprovider.DungeonSpecificDataProvider;
import kr.syeyoung.dungeonsguide.mod.dungeon.dataprovider.DungeonSpecificDataProviderRegistry;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.DungeonEventRecorder;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.impl.DungeonNodataEvent;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.impl.DungeonPercentageChangeEvent;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.impl.DungeonPuzzleFailureEvent;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonMapConstantRetriever;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonMapLayout;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonRoomScaffoldParser;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.MapPlayerProcessor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.BossfightProcessor;
import kr.syeyoung.dungeonsguide.mod.events.impl.BossroomEnterEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGChatReceivedEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.PathfindPreset;
import kr.syeyoung.dungeonsguide.mod.utils.MapUtils;
import kr.syeyoung.dungeonsguide.mod.utils.TabListUtil;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.event.events.MapUpdateEvent;
import kr.syeyoung.modapi.world.UMapData;
import kr.syeyoung.modapi.world.UWorld;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.World;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DungeonContext {
    @Getter @Setter
    private String dungeonName;
    @Getter
    private final World world;
    @Getter
    private final UWorld uworld;

    @Getter
    private final MapPlayerProcessor mapPlayerMarkerProcessor;
    @Getter @Setter
    private DungeonRoomScaffoldParser scaffoldParser;
    @Getter
    private final DungeonEventRecorder recorder = new DungeonEventRecorder();

    @Getter
    private final PathfindPreset preset;

    @Getter
    private final List<RoomProcessor> globalRoomProcessors = new ArrayList<>();

    // bunch of statistics
    @Getter @Setter
    private long bossRoomEnterSeconds = -1;
    @Getter @Setter
    private long init = -1;
    @Getter @Setter
    private VectorI3D bossroomSpawnPos = null;
    @Getter
    private boolean gotMimic = false;

    // observation => sensor fusion => new state

    // general info
    @Getter
    private final boolean trapRoomGen;

    @Getter private final int maxSpeed;
    @Getter private final double secretPercentage;

    @Getter
    public int percentage;

    public void setPercentage(int percentage) {
        this.percentage = percentage;
        recorder.createEvent(new DungeonPercentageChangeEvent(percentage));
    }

    @Getter
    private final PathfinderExecutorExecutor executor = new PathfinderExecutorExecutor(this);


    public void setGotMimic(boolean gotMimic) {
        this.gotMimic = gotMimic;
        recorder.createEvent(new DungeonNodataEvent("MIMIC_KILLED"));
    }

    @Getter
    @Setter
    private BossfightProcessor bossfightProcessor;

    @Getter
    private final Set<String> players = new HashSet<>();


    private final Vector2d doorOffset;
    private final VectorI3D door;

    public DungeonContext(String dungeonName,  UWorld uworld) {
        this(dungeonName, uworld, FeatureRegistry.SECRET_PRECALC_LIST.getSelectedPreset());
    }
    public DungeonContext(String dungeonName,UWorld uworld, PathfindPreset preset) {
        this.dungeonName = dungeonName;
        this.uworld = uworld;
        this.preset = preset;
        this.world = null;

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

        doorOffset = doorFinder.findDoorOffset(uworld, getDungeonName());
        door = doorFinder.findDoor(uworld, getDungeonName());

        if (doorOffset == null || door == null) throw new IllegalStateException("?");


        init = System.currentTimeMillis();

        executor.start();
    }



    private final Rectangle roomBoundary = new Rectangle(-10, -10, 138, 138);

    public void tick() {
        if (scaffoldParser != null && bossRoomEnterSeconds == -1 && !roomBoundary.contains(scaffoldParser.getDungeonMapLayout().worldPointToMapPoint(ModAPI.getAPI().getPlayer().getPositionVector()))) {
            bossRoomEnterSeconds = FeatureRegistry.DUNGEON_SBTIME.getTimeElapsed() / 1000;
            bossroomSpawnPos =  ModAPI.getAPI().getPlayer().getPosition();
            ModAPI.getAPI().getEventBus().fireEvent(new BossroomEnterEvent());
            recorder.createEvent(new DungeonNodataEvent("BOSSROOM_ENTER"));
            DungeonSpecificDataProvider doorFinder = DungeonSpecificDataProviderRegistry.getDoorFinder(getDungeonName());
            if (doorFinder != null) {
                bossfightProcessor = doorFinder.createBossfightProcessor(uworld, getDungeonName());
            } else {
                ChatTransmitter.sendDebugChat("Error:: Null Data Providier");
            }
        }

        if (scaffoldParser != null) {
            for (DungeonRoom dungeonRoom : scaffoldParser.getDungeonRoomList()) {
                if (!dungeonRoom.isMatched()) {
                    dungeonRoom.tryRematch();
                }
            }
            executor.setRoomIn(scaffoldParser.getRoomMap().get(getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(ModAPI.getAPI().getPlayer().getPositionVector())));
        }


        players.clear();
        players.addAll(TabListUtil.getPlayersInDungeon());
    }


    private boolean processed = false;
    private void processFinishedMap(UMapData mapData) {
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
        if (mapId == -1 && mapUpdateEvent.getMapData().get(0,0) == 0) { // dungeon map top left is ALWAYS 0.
            mapId = mapUpdateEvent.getMapId();
        }
        if (mapId != mapUpdateEvent.getMapId()) return;

        if (isEnded()) {
            processFinishedMap(mapUpdateEvent.getMapData());
        }
        if (getScaffoldParser() == null) {
            DungeonMapLayout layout = DungeonMapConstantRetriever.beginParsingMap(mapUpdateEvent.getMapData(), door, doorOffset);
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

    public void onChat(DGChatReceivedEvent event) {
        String formatted = event.getFormattedText();
        if (formatted.contains("$DG-Comm")) {
            event.setCanceled(true);
            String data = formatted.substring(formatted.indexOf("$DG-Comm"));
            String actual = TextUtils.stripColor(data);
            String coords = actual.split(" ")[1];
            String secrets = actual.split(" ")[2];
            int x = Integer.parseInt(coords.split("/")[0]);
            int z = Integer.parseInt(coords.split("/")[1]);
            int secrets2 = Integer.parseInt(secrets);
            Point roomPt = scaffoldParser.getDungeonMapLayout().worldPointToRoomPoint(new VectorI3D(x, 70, z));
            ChatTransmitter.sendDebugChat("Message from Other dungeons guide :: " + roomPt.x + " / " + roomPt.y + " total secrets " + secrets2);
            DungeonRoom dr = scaffoldParser.getRoomMap().get(roomPt);
            if (dr != null) {
                dr.setTotalSecrets(secrets2);
            }
        } else if (formatted.contains("$DG-Mimic")) {
            setGotMimic(true);
        } else if (TextUtils.startsWith(formatted, "§c§lPUZZLE FAIL! ") && TextUtils.startsWith(formatted, " §4Y§ci§6k§ee§as§2!")) {
            recorder.createEvent(new DungeonPuzzleFailureEvent(TextUtils.stripColor(formatted.split(" ")[2]), formatted));
        } else if (TextUtils.contains(formatted, "§6> §e§lEXTRA STATS §6<")) {
            recorder.createEvent(new DungeonNodataEvent("DUNGEON_END"));
            ended = true;
        } else if (TextUtils.contains(formatted, "§c☠ §eDefeated ")) {
            defeated = true;
        }
    }
}
