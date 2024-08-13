/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonRedstoneKey;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonRoomDoor2;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.ISecret;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.auth.AuthManager;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.VersionInfo;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.AbstractAction;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAG;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGNode;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonMapLayout;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonRoomScaffoldParser;
import kr.syeyoung.dungeonsguide.mod.dungeon.mocking.DRIWorld;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindRequest;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.features.AbstractGuiFeature;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayType;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayWidget;
import kr.syeyoung.dungeonsguide.mod.overlay.WholeScreenPositioner;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.BlockPos;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FeatureRequestCalculation extends AbstractGuiFeature {
    public FeatureRequestCalculation() {
        super("Pathfinding & Secrets", "Request path calculation", "- View which precalculations are missing\n- Request pre-calculation (Requires purchase on dg)", "secret.requestcalculation");
        setEnabled(true);
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

    @Override
    public void setupConfigureWidget(List<Widget> widgets) {
        super.setupConfigureWidget(widgets);
        widgets.add(new WidgetRequestCalculation());
        widgets.add(new WidgetCheckMissing());
    }


    private AtomicBoolean calculating = new AtomicBoolean();


    private WidgetProgress progress;

    @Override
    public OverlayWidget instantiateWidget() {
        return new OverlayWidget(
                progress = new WidgetProgress(),
                OverlayType.OVER_ANY,
                new WholeScreenPositioner(),
                getClass().getSimpleName()
        );
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @DGEventHandler(triggerOutOfSkyblock = true)
    public void onTick(DGTickEvent tickEvent) {
        if (progressUpdate) {
            if (progress != null) {
                progress.update(progresses);
                progressUpdate = false;
            }
        }
    }




    @AllArgsConstructor @Getter
    public static class Progress {
        private volatile String message;
        private AtomicInteger current;
        private AtomicInteger total;
        private final boolean bar;
    }
    private List<Progress> progresses = new CopyOnWriteArrayList<>();
    private volatile boolean progressUpdate = false;

    private void addProgress(Progress progress) {
        this.progresses.add(progress);
        this.progressUpdate = true;
    }

    private void removeProgress(Progress progress) {
        this.progresses.remove(progress);
        this.progressUpdate = true;
    }

    public void requestCalc() {
        if (calculating.getAndSet(true)) return;
        new Thread(DungeonsGuide.THREAD_GROUP, () -> {
            try {
                int est = 0;
                Set<PathfindRequest> requests = new HashSet<>();
                Progress progress1 = new Progress("Generating headers...", null, null, false);
                addProgress(progress1);
                try {
                    for (DungeonRoomInfo dungeonRoomInfo : DungeonRoomInfoRegistry.getRegistered()) {
                        DRIWorld driWorld = new DRIWorld(dungeonRoomInfo);
                        DungeonContext fakeContext = new DungeonContext("TEST DG", driWorld);
                        DungeonMapLayout dungeonMapLayout = new DungeonMapLayout(
                                new Dimension(16, 16),
                                5,
                                new Point(0, 0),
                                new BlockPos(0, 70, 0)
                        );
                        fakeContext.setScaffoldParser(new DungeonRoomScaffoldParser(dungeonMapLayout, fakeContext));
                        DungeonRoom dungeonRoom = new DungeonRoom(fakeContext);

                        ActionDAGBuilder builder = new ActionDAGBuilder(dungeonRoom);
                        for (Map.Entry<String, DungeonMechanic> value : dungeonRoom.getMechanics().entrySet()) {

                            if (value.getValue() instanceof ISecret) {
                                try {
                                    builder.requires(new ActionChangeState(value.getKey(), "found"));
                                } catch (PathfindImpossibleException e) {
                                    ChatTransmitter.addToQueue("Dungeons Guide :: Pathfind to " + value.getKey() + ":found failed due to " + e.getMessage());
                                    e.printStackTrace();
                                    continue;
                                }
                            } else if (value.getValue() instanceof DungeonRedstoneKey) {
                                try {
                                    builder.requires(new ActionChangeState(value.getKey(), "obtained-self"));
                                } catch (PathfindImpossibleException e) {
                                    ChatTransmitter.addToQueue("Dungeons Guide :: Pathfind to " + value.getKey() + ":found failed due to " + e.getMessage());
                                    e.printStackTrace();
                                    continue;
                                }
                            } else if (value.getValue() instanceof DungeonRoomDoor2) {
                                try {
                                    builder.requires(new ActionChangeState(value.getKey(), "navigate"));
                                } catch (PathfindImpossibleException e) {
                                    ChatTransmitter.addToQueue("Dungeons Guide :: Pathfind to door: " + value.getKey() + ":navigate failed due to " + e.getMessage());
                                    e.printStackTrace();
                                    continue;
                                }
                            }
                        }
                        ActionDAG dag = builder.build();
                        List<List<OffsetVec3>> toPfTo = new ArrayList<>();
                        Set<String> openMech = new HashSet<>();
                        for (ActionDAGNode allNode : dag.getAllNodes()) {
                            if (allNode.getAction() instanceof AtomicAction) {
                                for (AbstractAction action : ((AtomicAction) allNode.getAction()).getActions()) {
                                    if (action instanceof ActionMove) {
                                        toPfTo.add(
                                                ((ActionMove) action).getTargets().stream().flatMap(a -> a.getOffsetPointSet().stream())
                                                        .collect(Collectors.toList())
                                        );
                                    } else if (action instanceof ActionMoveSpot) {
                                        toPfTo.add(
                                                ((ActionMoveSpot) action).getTargets().stream().flatMap(a -> a.getOffsetPointSet().stream())
                                                        .collect(Collectors.toList())
                                        );
                                    } else if (action instanceof ActionMoveNearestAir) {
                                        OffsetPoint offsetPoint = ((ActionMoveNearestAir) action).getTarget();
                                        toPfTo.add(
                                                Collections.singletonList(new OffsetVec3(offsetPoint.getX(), offsetPoint.getY(), offsetPoint.getZ()))
                                        );
                                    } else if (action instanceof ActionChangeState) {
                                        if (((ActionChangeState) action).getState().equalsIgnoreCase("open")) {
                                            if (!((ActionChangeState) action).getMechanicName().startsWith("superboom") &&
                                                    !((ActionChangeState) action).getMechanicName().startsWith("crypt") &&
                                                    !((ActionChangeState) action).getMechanicName().startsWith("prince"))
                                                openMech.add(((ActionChangeState) action).getMechanicName());
                                        }
                                    }
                                }
                            } else if (allNode.getAction() instanceof ActionMove) {
                                toPfTo.add(
                                        ((ActionMove) allNode.getAction()).getTargets().stream().flatMap(a -> a.getOffsetPointSet().stream())
                                                .collect(Collectors.toList())
                                );
                            } else if (allNode.getAction() instanceof ActionMoveSpot) {
                                toPfTo.add(
                                        ((ActionMoveSpot) allNode.getAction()).getTargets().stream().flatMap(a -> a.getOffsetPointSet().stream())
                                                .collect(Collectors.toList())
                                );
                            } else if (allNode.getAction() instanceof ActionMoveNearestAir) {
                                OffsetPoint offsetPoint = ((ActionMoveNearestAir) allNode.getAction()).getTarget();
                                toPfTo.add(
                                        Collections.singletonList(new OffsetVec3(offsetPoint.getX(), offsetPoint.getY(), offsetPoint.getZ()))
                                );
                            } else if (allNode.getAction() instanceof ActionChangeState) {
                                if (((ActionChangeState) allNode.getAction()).getState().equalsIgnoreCase("open")
                                        && ((ActionChangeState) allNode.getAction()).getMechanicName().startsWith("door")) {
                                    openMech.add(((ActionChangeState) allNode.getAction()).getMechanicName());
                                }
                            }
                        }

                        List<String> openMechList = new ArrayList<>(openMech);

                        for (List<OffsetVec3> offsetVec3s : toPfTo) {
                            for (int i = 0; i < (1 << openMech.size()); i++) {
                                Set<String> open = new HashSet<>();
                                for (int i1 = 0; i1 < openMechList.size(); i1++) {
                                    if (((i >> i1) & 0x1) > 0) {
                                        open.add(openMechList.get(i1));
                                    }
                                }
                                requests.add(new PathfindRequest(FeatureRegistry.SECRET_PATHFIND_SETTINGS.getAlgorithmSettings(), dungeonRoomInfo, open, offsetVec3s));
                            }
                        }


                        System.out.println(toPfTo.size() + " pfs for " + (1 << openMech.size()) + " states " + toPfTo.size() * (1 << openMech.size()) + " Pf for " + dungeonRoomInfo.getName());
                        ChatTransmitter.getReceiveQueue().clear();

                        est += toPfTo.size() * (1 << openMech.size()) * dungeonRoom.getUnitPoints().size();
                        fakeContext.cleanup();
                    }
                } finally {
                    removeProgress(progress1);
                }
                if (requests.size() == 0) {
                    ChatTransmitter.addToQueue("§eDungeons Guide §7:: §eTotal" + requests.size() + " requests");
                    ChatTransmitter.addToQueue("§eDungeons Guide §7:: §eEstimated PF " + est + " on unit room");
                    return;
                }


                int totalRoomAndState = requests.stream().map(a -> new ImmutablePair(a.getDungeonRoomInfo().getUuid(),a.getOpenMech().stream().sorted(String::compareTo).collect(Collectors.joining(",")))).collect(Collectors.toSet()).size();
                Progress roomProgress = new Progress("Room&States 0/"+totalRoomAndState, new AtomicInteger(), new AtomicInteger(totalRoomAndState), true);
                Progress requestProgress = new Progress("Requests 0/"+requests.size(), new AtomicInteger(), new AtomicInteger(requests.size()), true);

                addProgress(roomProgress);
                addProgress(requestProgress);

                List<File> files = new ArrayList<>();
                File outdir;
                try {
                    Path p = Files.createTempDirectory("dg-pfrequest-gen");
                    outdir = p.toFile();
                    System.out.println("Writing to " + p);
                    requests.stream().collect(Collectors.groupingBy(a ->
                            new ImmutablePair<>(a.getDungeonRoomInfo().getUuid(), a.getOpenMech().stream().sorted(String::compareTo).collect(Collectors.joining(",")))
                    )).entrySet().parallelStream().forEach(stuff -> {
                        PathfindRequest begin = stuff.getValue().get(0);

                        DRIWorld driWorld = new DRIWorld(begin.getDungeonRoomInfo(), new ArrayList<>(begin.getOpenMech()));

                        long start2 = System.currentTimeMillis();
                        for (PathfindRequest request : stuff.getValue()) {
                            UUID id = UUID.randomUUID();
                            try {
                                long start = System.currentTimeMillis();
                                System.out.println("Writing " + id.toString() + ".pfreq  / " + request.getId());
                                File f = new File(outdir, id.toString() + ".pfreq");
                                DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
                                request.write(driWorld, dataOutputStream);
                                dataOutputStream.flush();
                                dataOutputStream.close();
                                System.out.println("It took " + (System.currentTimeMillis() - start) + "ms : " + request.getId());
                                int currentReq = requestProgress.current.incrementAndGet();
                                requestProgress.message = "Requests " + currentReq + "/" + requestProgress.total.get();
                                files.add(f);
                            } catch (Exception e) {
                                System.out.println("Error while " + id.toString() + ".pfreq / " + request.getId());
                                e.printStackTrace();
                            }
                        }
                        int currentRooms = roomProgress.current.incrementAndGet();
                        roomProgress.message = "Room&States " + currentRooms + "/" + roomProgress.total.get();
                        System.out.println("ROOM: " + begin.getDungeonRoomInfo().getName() + " took " + (System.currentTimeMillis() - start2) + "ms to complete");
                    });
                } finally {
                    removeProgress(roomProgress);
                    removeProgress(requestProgress);
                }

                Progress zip = new Progress("Zipping... 0/"+files.size()+1, new AtomicInteger(0), new AtomicInteger(files.size()+1), true);
                addProgress(zip);

                try {
                    File target = new File(Main.getConfigDir(), "pfreq-"+System.currentTimeMillis() + ".zip");
                    {
                        System.out.println("Writing to " + target);
                        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §eWriting pathfind request zip file to " + target.getAbsolutePath());
                        final FileOutputStream fos = new FileOutputStream(target);
                        ZipOutputStream zipOut = new ZipOutputStream(fos);

                        for (File srcFile : files) {
                            FileInputStream fis = new FileInputStream(srcFile);
                            ZipEntry zipEntry = new ZipEntry(srcFile.getName());
                            zipOut.putNextEntry(zipEntry);

                            byte[] bytes = new byte[1024 * 1024];
                            int length;
                            while ((length = fis.read(bytes)) >= 0) {
                                zipOut.write(bytes, 0, length);
                            }
                            fis.close();
                            int cnt = zip.current.incrementAndGet();
                            zip.message = "Zipping... "+cnt+"/"+zip.total.get();
                        }
                        zipOut.close();
                        fos.close();
                    }
                    ChatTransmitter.addToQueue("§eDungeons Guide §7:: §eSuccessfully wrote pathfind request zip file to "+target.getAbsolutePath());
                } finally {
                    removeProgress(zip);
                }
                FeatureRequestCalculation.Progress complete = new FeatureRequestCalculation.Progress("Complete!", new AtomicInteger(1), new AtomicInteger(1), true);
                FeatureRegistry.SECRET_PATHFIND_REQUEST.addProgress(complete);
                try {
                    Thread.sleep(5000);
                } finally {
                    FeatureRegistry.SECRET_PATHFIND_REQUEST.removeProgress(complete);
                }

            } catch (Exception e) {
                System.out.println("An error occured while generating pfreqs");
                e.printStackTrace();
            } finally {
                this.calculating.set(false);
            }
        }).start();
    }


    public void uploadToService(WidgetRequestCalculation widgetRequestCalculation) {
        new Thread(DungeonsGuide.THREAD_GROUP, () -> {
            try {
                final JFileChooser[] fc = new JFileChooser[1];
                final int[] returnVal = new int[1];
                EventQueue.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        fc[0] = new JFileChooser(Main.getConfigDir());
                        returnVal[0] = fc[0].showOpenDialog(null);
                    }
                });

                if (returnVal[0] != JFileChooser.APPROVE_OPTION) {
                    widgetRequestCalculation.reload();
                    return;
                }
                File f = fc[0].getSelectedFile();

                String uploadUrl;
                Progress p1 = new Progress("Getting upload url...", null, null, false);
                try {
                    addProgress(p1);
                    HttpsURLConnection connection = (HttpsURLConnection) new URL("https://pathfind.dungeons.guide/upload").openConnection();
                    connection.setRequestProperty("User-Agent", "DungeonsGuide/"+ VersionInfo.VERSION);
                    connection.setRequestMethod("POST");
                    connection.addRequestProperty("Authorization", "Bearer "+AuthManager.getInstance().getWorkingTokenOrThrow());
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);
                    InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                    String servers = IOUtils.toString(inputStreamReader);
                    JsonObject key = new Gson().fromJson(servers, JsonObject.class);
                    uploadUrl = key.get("url").getAsString();
                } finally {
                    removeProgress(p1);
                }
                p1 = new Progress("Uploading..." , new AtomicInteger(), new AtomicInteger((int) f.length()), true);
                try {
                    addProgress(p1);
                    HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL(uploadUrl).openConnection();
                    httpsURLConnection.setDoOutput(true);
                    httpsURLConnection.setRequestProperty("Content-Length", f.length()+"");
                    httpsURLConnection.setRequestProperty("Content-Type", "application/zip");
                    httpsURLConnection.setRequestMethod("PUT");
                    FileInputStream fileInputStream = new FileInputStream(f);
                    byte buf[] = new byte[1024 *1024];
                    int len = 0;
                    long total = 0;
                    while((len = fileInputStream.read(buf)) != -1) {
                        httpsURLConnection.getOutputStream().write(buf, 0, len);
                        total += len;
                        p1.getCurrent().set((int) total);
                    }
                    System.out.println(httpsURLConnection.getResponseCode());
                    System.out.println(httpsURLConnection.getResponseMessage());
                    if (httpsURLConnection.getResponseCode() != 200) {
                        throw new RuntimeException("Status code "+httpsURLConnection.getResponseCode());
                    }
                } finally {
                    removeProgress(p1);
                }
                p1 = new Progress("Requesting Calculation", null, null, false);
                try {
                    addProgress(p1);
                    HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL("https://pathfind.dungeons.guide/process").openConnection();
                    httpsURLConnection.setRequestMethod("POST");
                    httpsURLConnection.setRequestProperty("User-Agent", "DungeonsGuide/"+ VersionInfo.VERSION);
                    httpsURLConnection.addRequestProperty("Authorization", "Bearer "+AuthManager.getInstance().getWorkingTokenOrThrow());
                    System.out.println(httpsURLConnection.getResponseCode());
                    System.out.println(httpsURLConnection.getResponseMessage());
                    if (httpsURLConnection.getResponseCode() != 200) {
                        throw new RuntimeException("Status code "+httpsURLConnection.getResponseCode());
                    }
                } finally {
                    removeProgress(p1);
                }
                p1 = new Progress("Requested calculation! Track status in config", new AtomicInteger(1), new AtomicInteger(1), true);
                addProgress(p1);
                try {
                    Thread.sleep(5000);
                } finally {
                    removeProgress(p1);
                }

            } catch (Exception e) {
                ChatTransmitter.addToQueue("An error occured while doing stuff: contact dg support");
                System.out.println("An error occured while requesting pfreqs");
                e.printStackTrace();
            }
        }).start();
    }

    public boolean calculating() {
        return calculating.get();
    }
}
