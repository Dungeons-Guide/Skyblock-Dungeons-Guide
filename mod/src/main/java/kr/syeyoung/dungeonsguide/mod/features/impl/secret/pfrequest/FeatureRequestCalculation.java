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
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip.WidgetNotification;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip.WidgetNotificationProgress;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayType;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayWidget;
import kr.syeyoung.dungeonsguide.mod.overlay.WholeScreenPositioner;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.lwjgl.opengl.Display;

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

public class FeatureRequestCalculation extends SimpleFeature {
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


    private UUID calcuuid = UUID.randomUUID();
    private UUID calcuuid2 = UUID.randomUUID();


    public void requestCalc() {
        if (calculating.getAndSet(true)) return;
        new Thread(DungeonsGuide.THREAD_GROUP, () -> {
            WidgetNotificationProgress progress = new WidgetNotificationProgress(calcuuid, "Pathfind Request Generation Progress");
            FeatureRegistry.NOTIFICATIONS.getRootWidget().updateNotification(calcuuid, progress); // should be thread safe. shouuuuld be.

            try {


                int est = 0;
                Set<PathfindRequest> requests = new HashSet<>();
                WidgetNotificationProgress.Progress progress1 = new WidgetNotificationProgress.Progress("Generating headers...", null, null, false);
                progress.addProgress(progress1);
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
                                requests.add(new PathfindRequest(FeatureRegistry.SECRET_PATHFIND_SETTINGS.getAlgorithmSetting(), dungeonRoomInfo, open, offsetVec3s));
                            }
                        }


                        System.out.println(toPfTo.size() + " pfs for " + (1 << openMech.size()) + " states " + toPfTo.size() * (1 << openMech.size()) + " Pf for " + dungeonRoomInfo.getName());
                        ChatTransmitter.getReceiveQueue().clear();

                        est += toPfTo.size() * (1 << openMech.size()) * dungeonRoom.getUnitPoints().size();
                        fakeContext.cleanup();
                    }
                } finally {
                    progress.removeProgress(progress1);
                }
                if (requests.size() == 0) {
                    ChatTransmitter.addToQueue("§eDungeons Guide §7:: §eTotal" + requests.size() + " requests");
                    ChatTransmitter.addToQueue("§eDungeons Guide §7:: §eEstimated PF " + est + " on unit room");
                    return;
                }


                int totalRoomAndState = requests.stream().map(a -> new ImmutablePair(a.getDungeonRoomInfo().getUuid(),a.getOpenMech().stream().sorted(String::compareTo).collect(Collectors.joining(",")))).collect(Collectors.toSet()).size();
                WidgetNotificationProgress.Progress roomProgress = new WidgetNotificationProgress.Progress ("Room&States 0/"+totalRoomAndState, new AtomicInteger(), new AtomicInteger(totalRoomAndState), true);
                WidgetNotificationProgress.Progress requestProgress = new WidgetNotificationProgress.Progress ("Requests 0/"+requests.size(), new AtomicInteger(), new AtomicInteger(requests.size()), true);

                progress.addProgress(roomProgress);
                progress.addProgress(requestProgress);

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
                                int currentReq = requestProgress.getCurrent().incrementAndGet();
                                requestProgress.setMessage("Requests " + currentReq + "/" + requestProgress.getTotal().get());
                                files.add(f);
                            } catch (Exception e) {
                                System.out.println("Error while " + id.toString() + ".pfreq / " + request.getId());
                                e.printStackTrace();
                            }
                        }
                        int currentRooms = roomProgress.getCurrent().incrementAndGet();
                        roomProgress.setMessage("Room&States " + currentRooms + "/" + roomProgress.getTotal().get());
                        System.out.println("ROOM: " + begin.getDungeonRoomInfo().getName() + " took " + (System.currentTimeMillis() - start2) + "ms to complete");
                    });
                } finally {
                    progress.removeProgress(roomProgress);
                    progress.removeProgress(requestProgress);
                }

                WidgetNotificationProgress.Progress zip = new WidgetNotificationProgress.Progress ("Zipping... 0/"+files.size()+1, new AtomicInteger(0), new AtomicInteger(files.size()+1), true);
                progress.addProgress(zip);

                try {
                    File target = new File(Main.getConfigDir(), "pfreq-"+System.currentTimeMillis() + ".zip");
                    {
                        System.out.println("Writing to " + target);
                        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §eWriting pathfind request zip file to " + target.getAbsolutePath());
                        final FileOutputStream fos = new FileOutputStream(target);
                        ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(fos));

                        for (File srcFile : files) {
                            FileInputStream fis = new FileInputStream(srcFile);
                            ZipEntry zipEntry = new ZipEntry(srcFile.getName());
                            zipOut.putNextEntry(zipEntry);

                            Files.copy(srcFile.toPath(), zipOut);

                            fis.close();
                            int cnt = zip.getCurrent().incrementAndGet();
                            zip.setMessage("Zipping... "+cnt+"/"+zip.getTotal().get());
                        }
                        zipOut.close();
                        fos.close();
                    }
                    ChatTransmitter.addToQueue("§eDungeons Guide §7:: §eSuccessfully wrote pathfind request zip file to "+target.getAbsolutePath());
                } finally {
                    progress.removeProgress(zip);
                }
                WidgetNotificationProgress.Progress complete = new WidgetNotificationProgress.Progress("Complete!", new AtomicInteger(1), new AtomicInteger(1), true);
                progress.addProgress(complete);
                try {
                    Thread.sleep(5000);
                } finally {
                    progress.removeProgress(complete);
                }

            } catch (Exception e) {
                System.out.println("An error occured while generating pfreqs");
                e.printStackTrace();
            } finally {
                this.calculating.set(false);

                FeatureRegistry.NOTIFICATIONS.getRootWidget().removeNotification(calcuuid);
            }
        }).start();
    }


    public void uploadToService(WidgetRequestCalculation widgetRequestCalculation) {
        new Thread(DungeonsGuide.THREAD_GROUP, () -> {
            WidgetNotificationProgress progress = new WidgetNotificationProgress(calcuuid, "Pathfind Request Uploading Progress");
            FeatureRegistry.NOTIFICATIONS.getRootWidget().updateNotification(calcuuid2, progress); // should be thread safe. shouuuuld be.


            try {


                Frame parent = new Frame();
                FileDialog dialog = new FileDialog(parent, "Choose a Pathfind Request ZIP", FileDialog.LOAD);
                dialog.setDirectory(Main.getConfigDir().getAbsolutePath());

                dialog.setFilenameFilter((dir, name) -> name.endsWith(".zip")); //osx
                dialog.setFile("*.zip"); // windows

                dialog.setVisible(true);

                File[] chosen = dialog.getFiles();

                dialog.dispose();
                parent.dispose();

                if (chosen.length == 0) {
                    widgetRequestCalculation.reload();
                    return;
                }
                File f = chosen[0];

                String uploadUrl;
                WidgetNotificationProgress.Progress p1 = new WidgetNotificationProgress.Progress ("Getting upload url...", null, null, false);
                try {
                    progress.addProgress(p1);
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
                    progress.removeProgress(p1);
                }
                p1 = new WidgetNotificationProgress.Progress ("Uploading..." , new AtomicInteger(), new AtomicInteger((int) f.length()), true);
                try {
                    progress.addProgress(p1);
                    HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL(uploadUrl).openConnection();
                    httpsURLConnection.setDoOutput(true);
                    httpsURLConnection.setRequestProperty("Content-Length", f.length()+"");
                    httpsURLConnection.setRequestProperty("Content-Type", "application/zip");
                    httpsURLConnection.setFixedLengthStreamingMode(f.length());
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
                    progress.removeProgress(p1);
                }
                p1 = new WidgetNotificationProgress.Progress ("Requesting Calculation", null, null, false);
                try {
                    progress.addProgress(p1);
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
                    progress.removeProgress(p1);
                }
                p1 = new WidgetNotificationProgress.Progress ("Requested calculation! Track status in config", new AtomicInteger(1), new AtomicInteger(1), true);
                progress.addProgress(p1);
                try {
                    Thread.sleep(5000);
                } finally {
                    progress.removeProgress(p1);
                }

            } catch (Exception e) {
                ChatTransmitter.addToQueue("An error occured while doing stuff: contact dg support");
                System.out.println("An error occured while requesting pfreqs");
                e.printStackTrace();
            } finally {
                FeatureRegistry.NOTIFICATIONS.getRootWidget().removeNotification(calcuuid2);
            }
        }).start();
    }

    public boolean calculating() {
        return calculating.get();
    }
}
