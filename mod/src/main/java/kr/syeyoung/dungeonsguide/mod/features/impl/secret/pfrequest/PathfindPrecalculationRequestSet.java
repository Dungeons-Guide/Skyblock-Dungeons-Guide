package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest;

import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.mocking.DRIWorld;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindRequest;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.PathfindPreset;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip.WidgetNotificationProgress;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq.WidgetPrecalcStep1Calculating;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.*;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Getter
public class PathfindPrecalculationRequestSet {
    private String id;
    private String name;
    private List<PathfindRequest> requestList;
    private Instant createdAt;

    private String linkedPreset;

    @Setter
    private boolean seen = false;


    @Setter
    private volatile Status status;

    private File zipFile;
    private volatile WidgetNotificationProgress progressForTopRight;
    private volatile WidgetNotificationProgress progressForGui;
    @Setter
    private volatile WeakReference<WidgetPrecalcStep1Calculating> maybeNotify ;



    public static enum Status {
        PENDING, GENERATING_ZIP, WAITING_FOR_USER
    }

    public PathfindPrecalculationRequestSet(PathfindPreset preset, List<PathfindRequest> requestList) {
        this.id = UUID.randomUUID().toString();
        this.name = "Precalculation for "+preset.getPresetName();
        this.requestList = new ArrayList<>(requestList);
        this.createdAt = Instant.now();
        this.linkedPreset = preset.getPresetId();
        this.status = Status.PENDING;

        calculateCredits();

        System.out.println(credits);
    }


    private long credits = -1;

    private void calculateCredits() {
        long credits = 0;
        for (PathfindRequest pathfindRequest : requestList) {
            int bitCount = Integer.bitCount(pathfindRequest.getDungeonRoomInfo().getShape() & 0xffff);
            credits += bitCount;
        }

        this.credits = credits;
    }

    public void removePFRequest(PathfindRequest request) {
        this.requestList.remove(request);
        calculateCredits();
    }

    public void addPFRequest(PathfindRequest request) {
        this.requestList.add(request);
        calculateCredits();
    }

    public void removeDupe() {
        Map<String, PathfindRequest> ahh = new HashMap<>();
        List<PathfindRequest> toRemove = new ArrayList<>();
        for (PathfindRequest pathfindRequest : requestList) {
            PathfindRequest what = ahh.put(pathfindRequest.getId(), pathfindRequest);
            toRemove.add(what);
        }
        requestList.removeAll(toRemove);

        calculateCredits();
    }



    public void generateZip() {
        if (status != Status.PENDING) throw new IllegalStateException("State is not pending");
        this.status = Status.GENERATING_ZIP;

        final UUID calcuuid = UUID.randomUUID();
        progressForTopRight = new WidgetNotificationProgress(calcuuid, "Pathfind Request Generation Progress");
        progressForGui = new WidgetNotificationProgress(calcuuid, "Pathfind Request Generation Progress");

        new Thread(DungeonsGuide.THREAD_GROUP, () -> {
            FeatureRegistry.NOTIFICATIONS.getRootWidget().updateNotification(calcuuid, progressForTopRight); // should be thread safe. shouuuuld be.

            try {
                int est = 0;
                Set<PathfindRequest> requests = new HashSet<>(requestList);

                ChatTransmitter.addToQueue("§eDungeons Guide §7:: §eTotal" + requests.size() + " requests");
                ChatTransmitter.addToQueue("§eDungeons Guide §7:: §eEstimated PF " + est + " on unit room");



                int totalRoomAndState = requests.stream().map(a -> new ImmutablePair(a.getDungeonRoomInfo().getUuid(),a.getOpenMech().stream().sorted(String::compareTo).collect(Collectors.joining(",")))).collect(Collectors.toSet()).size();
                WidgetNotificationProgress.Progress roomProgress = new WidgetNotificationProgress.Progress ("Room&States 0/"+totalRoomAndState, new AtomicInteger(), new AtomicInteger(totalRoomAndState), true);
                WidgetNotificationProgress.Progress requestProgress = new WidgetNotificationProgress.Progress ("Requests 0/"+requests.size(), new AtomicInteger(), new AtomicInteger(requests.size()), true);

                progressForTopRight.addProgress(roomProgress);
                progressForGui.addProgress(roomProgress);
                progressForTopRight.addProgress(requestProgress);
                progressForGui.addProgress(requestProgress);

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
                    progressForTopRight.removeProgress(roomProgress);
                    progressForGui.removeProgress(roomProgress);
                    progressForTopRight.removeProgress(requestProgress);
                    progressForGui.removeProgress(requestProgress);
                }

                WidgetNotificationProgress.Progress zip = new WidgetNotificationProgress.Progress ("Zipping... 0/"+files.size()+1, new AtomicInteger(0), new AtomicInteger(files.size()+1), true);
                progressForTopRight.addProgress(zip);
                progressForGui.addProgress(zip);

                try {
                    zipFile = new File(Main.getConfigDir(), "pfreq-"+System.currentTimeMillis() + ".zip");
                    {
                        System.out.println("Writing to " + zipFile);
                        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §eWriting pathfind request zip file to " + zipFile.getAbsolutePath());
                        final FileOutputStream fos = new FileOutputStream(zipFile);
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
                    ChatTransmitter.addToQueue("§eDungeons Guide §7:: §eSuccessfully wrote pathfind request zip file to "+zipFile.getAbsolutePath());
                } finally {
                    progressForTopRight.removeProgress(zip);
                    progressForGui.removeProgress(zip);
                }
                this.status = Status.WAITING_FOR_USER;
                if (maybeNotify != null) {
                    WidgetPrecalcStep1Calculating calculating = maybeNotify.get();
                    if (calculating != null) calculating.notifyDone();
                }


                WidgetNotificationProgress.Progress complete = new WidgetNotificationProgress.Progress("Complete!", new AtomicInteger(1), new AtomicInteger(1), true);
                progressForTopRight.addProgress(complete);
                try {
                    Thread.sleep(5000);
                } finally {
                    progressForTopRight.removeProgress(complete);
                }
            } catch (Exception e) {
                System.out.println("An error occured while generating pfreqs");
                e.printStackTrace();
                this.status = Status.PENDING;
                if (maybeNotify != null) {
                    WidgetPrecalcStep1Calculating calculating = maybeNotify.get();
                    if (calculating != null) calculating.notifyDone();
                }
            } finally {

                FeatureRegistry.NOTIFICATIONS.getRootWidget().removeNotification(calcuuid);
            }
        }).start();
    }
}
