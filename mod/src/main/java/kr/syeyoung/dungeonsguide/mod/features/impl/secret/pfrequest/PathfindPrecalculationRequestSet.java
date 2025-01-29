package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.auth.AuthManager;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.VersionInfo;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.mocking.DRIWorld;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.world.PathfindRequest;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.pathfindcache.PathfindPreset;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip.WidgetNotificationProgress;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq.step1.WidgetPrecalcStep1Calculating;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq.step2.WidgetStep2Uploading;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.remotereq.RemoteCache;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
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
    @Setter
    private volatile WeakReference<WidgetStep2Uploading> maybeNotify2 ;



    private String uploadUrl;
    private String requestId;


    public static enum Status {
        PENDING, GENERATING_ZIP, WAITING_FOR_USER, CREATING_UPLOADING_REQUEST, DONE
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
            DungeonRoomInfo dri = pathfindRequest.getDungeonRoomInfo();
            int bitCount = dri.getWidth() * dri.getLength() / 1024;
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

    public void createRequest() {
        if (status != Status.WAITING_FOR_USER) throw new IllegalStateException("State is not waiting for user");
        this.status = Status.CREATING_UPLOADING_REQUEST;

        final UUID calcuuid = UUID.randomUUID();
        progressForTopRight = new WidgetNotificationProgress(calcuuid, "Pathfind Request Progress");
        progressForGui = new WidgetNotificationProgress(calcuuid, "Pathfind Request Progress");

        new Thread(DungeonsGuide.THREAD_GROUP, () -> {
            FeatureRegistry.NOTIFICATIONS.getRootWidget().updateNotification(calcuuid, progressForTopRight); // should be thread safe. shouuuuld be.

            try {
                WidgetNotificationProgress.Progress progress = new WidgetNotificationProgress.Progress("Creating Request", null, null, false);
                progressForTopRight.addProgress(progress);
                progressForGui.addProgress(progress);
                try {
                    HttpsURLConnection connection = (HttpsURLConnection) new URL(FeatureRequestCalculation.DOMAIN + "/requests").openConnection();
                    connection.setRequestProperty("User-Agent", "DungeonsGuide/" + VersionInfo.VERSION);
                    connection.setRequestMethod("POST");
                    connection.addRequestProperty("Authorization", "Bearer " + AuthManager.getInstance().getWorkingTokenOrThrow());
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    JSONObject request = new JSONObject()
                            .put("credit", getCredits())
                            .put("contentSize", Files.size(zipFile.toPath()));
                    connection.getOutputStream().write(request.toString().getBytes());
                    connection.getOutputStream().flush();

                    InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                    String servers = IOUtils.toString(inputStreamReader);
                    JsonObject key = new Gson().fromJson(servers, JsonObject.class);
                    uploadUrl = key.get("uploadUrl").getAsString();
                    requestId = key.get("request").getAsJsonObject().get("requestId").getAsString();
                } finally {
                    progressForTopRight.removeProgress(progress);
                    progressForGui.removeProgress(progress);
                }

                WidgetNotificationProgress.Progress progress1 = new WidgetNotificationProgress.Progress("Uploading... ("+ FileUtils.byteCountToDisplaySize(Files.size(zipFile.toPath()))+")", new AtomicLong(), new AtomicLong(Files.size(zipFile.toPath())), true);
                progressForTopRight.addProgress(progress1);
                progressForGui.addProgress(progress1);
                try {
                    HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL(uploadUrl).openConnection();
                    httpsURLConnection.setDoOutput(true);
                    httpsURLConnection.setRequestProperty("User-Agent", "DungeonsGuide/" + VersionInfo.VERSION);
                    httpsURLConnection.setRequestProperty("Content-Length", zipFile.length()+"");
                    httpsURLConnection.setRequestProperty("Content-Type", "application/zip");
                    httpsURLConnection.setFixedLengthStreamingMode(zipFile.length());
                    httpsURLConnection.setRequestMethod("PUT");
                    FileInputStream fileInputStream = new FileInputStream(zipFile);
                    byte buf[] = new byte[1024 *1024];
                    int len = 0;
                    long total = 0;
                    while((len = fileInputStream.read(buf)) != -1) {
                        httpsURLConnection.getOutputStream().write(buf, 0, len);
                        total += len;
                        progress1.getCurrent().set((int) total);
                    }
                    System.out.println(httpsURLConnection.getResponseCode());
                    System.out.println(httpsURLConnection.getResponseMessage());
                    if (httpsURLConnection.getResponseCode() != 200) {
                        throw new RuntimeException("Status code "+httpsURLConnection.getResponseCode());
                    }
                } finally {
                    progressForTopRight.removeProgress(progress1);
                    progressForGui.removeProgress(progress1);
                }
                this.status = Status.DONE;
                FeatureRegistry.SECRET_PATHFIND_REQUEST.getRemoteCacheMap().put(requestId, new RemoteCache(requestId, name, linkedPreset, true, false));
                if (maybeNotify2 != null) {
                    WidgetStep2Uploading calculating = maybeNotify2.get();
                    if (calculating != null) calculating.notifyDone();
                }

                WidgetNotificationProgress.Progress progress2 = new WidgetNotificationProgress.Progress ("Requested calculation! Track status in config", new AtomicLong(1), new AtomicLong(1), true);
                progressForTopRight.addProgress(progress2);
                progressForGui.addProgress(progress2);
                try {
                    Thread.sleep(5000);
                } finally {
                    progressForTopRight.removeProgress(progress2);
                    progressForGui.removeProgress(progress2);
                }
            } catch (Exception e) {
                System.out.println("An error occured while requesting pathfind");
                e.printStackTrace();

                this.status = Status.WAITING_FOR_USER;
                uploadUrl = null;
                requestId = null;
                if (maybeNotify2 != null) {
                    WidgetStep2Uploading calculating = maybeNotify2.get();
                    if (calculating != null) calculating.notifyDone();
                }
            } finally {
                FeatureRegistry.NOTIFICATIONS.getRootWidget().removeNotification(calcuuid);
            }
        }).start();

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

                ChatTransmitter.addToQueue("§eDungeons Guide §7:: §eTotal " + requests.size() + " requests");



                int totalRoomAndState = requests.stream().map(a -> new ImmutablePair(a.getDungeonRoomInfo().getUuid(),a.getOpenMech().stream().sorted(String::compareTo).collect(Collectors.joining(",")))).collect(Collectors.toSet()).size();
                WidgetNotificationProgress.Progress roomProgress = new WidgetNotificationProgress.Progress ("Room&States 0/"+totalRoomAndState, new AtomicLong(), new AtomicLong(totalRoomAndState), true);
                WidgetNotificationProgress.Progress requestProgress = new WidgetNotificationProgress.Progress ("Requests 0/"+requests.size(), new AtomicLong(), new AtomicLong(requests.size()), true);

                progressForTopRight.addProgress(roomProgress);
                progressForGui.addProgress(roomProgress);
                progressForTopRight.addProgress(requestProgress);
                progressForGui.addProgress(requestProgress);

                File outdir;
                List<File> files;
                try {
                    Path p = Files.createTempDirectory("dg-pfrequest-gen");
                    outdir = p.toFile();
                    System.out.println("Writing to " + p);
                    files = requests.stream().collect(Collectors.groupingBy(a ->
                            new ImmutablePair<>(a.getDungeonRoomInfo().getUuid(), a.getOpenMech().stream().sorted(String::compareTo).collect(Collectors.joining(",")))
                    )).entrySet().parallelStream().flatMap(stuff -> {
                        PathfindRequest begin = stuff.getValue().get(0);

                        DRIWorld driWorld = new DRIWorld(begin.getDungeonRoomInfo(), new ArrayList<>(begin.getOpenMech()));

                        List<File> intermediate = new ArrayList<>();
                        long start2 = System.currentTimeMillis();
                        for (PathfindRequest request : stuff.getValue()) {
                            UUID id = UUID.randomUUID();
                            try {
                                long start = System.currentTimeMillis();
                                System.out.println("Writing " + id.toString() + ".pfreq  / " + request.getId());
                                File f = new File(outdir, id.toString() + ".pfreq");
                                f.deleteOnExit();
                                DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
                                request.write(driWorld, dataOutputStream);
                                dataOutputStream.flush();
                                dataOutputStream.close();
                                System.out.println("It took " + (System.currentTimeMillis() - start) + "ms : " + request.getId());
                                long currentReq = requestProgress.getCurrent().incrementAndGet();
                                requestProgress.setMessage("Requests " + currentReq + "/" + requestProgress.getTotal().get());
                                intermediate.add(f);
                            } catch (Exception e) {
                                System.out.println("Error while " + id.toString() + ".pfreq / " + request.getId());
                                e.printStackTrace();
                                throw new RuntimeException("Error while "+id.toString()+".pfreq / "+request.getId(), e);
                            }
                        }
                        long currentRooms = roomProgress.getCurrent().incrementAndGet();
                        roomProgress.setMessage("Room&States " + currentRooms + "/" + roomProgress.getTotal().get());
                        System.out.println("ROOM: " + begin.getDungeonRoomInfo().getName() + " took " + (System.currentTimeMillis() - start2) + "ms to complete");
                        return intermediate.stream();
                    }).collect(Collectors.toList());
                } finally {
                    progressForTopRight.removeProgress(roomProgress);
                    progressForGui.removeProgress(roomProgress);
                    progressForTopRight.removeProgress(requestProgress);
                    progressForGui.removeProgress(requestProgress);
                }

                WidgetNotificationProgress.Progress zip = new WidgetNotificationProgress.Progress ("Zipping... 0/"+files.size(), new AtomicLong(0), new AtomicLong(files.size()), true);
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
                            long cnt = zip.getCurrent().incrementAndGet();
                            zip.setMessage("Zipping... "+cnt+"/"+zip.getTotal().get());

                            try {
                                Files.deleteIfExists(srcFile.toPath());
                            } catch (Exception e) {
                                System.out.println("Error while deleting "+srcFile+" but I don't care.");
                                e.printStackTrace();
                            }
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

                WidgetNotificationProgress.Progress complete = new WidgetNotificationProgress.Progress("Complete!", new AtomicLong(1), new AtomicLong(1), true);
                progressForTopRight.addProgress(complete);
                try {
                    Thread.sleep(5000);
                } finally {
                    progressForTopRight.removeProgress(complete);
                }
            } catch (Exception e) {
                System.out.println("An error occured while generating pfreqs");
                e.printStackTrace();
                if (maybeNotify != null) {
                    WidgetPrecalcStep1Calculating calculating = maybeNotify.get();
                    if (calculating != null) calculating.notifyDone();
                }
                this.status = Status.PENDING;
            } finally {
                FeatureRegistry.NOTIFICATIONS.getRootWidget().removeNotification(calcuuid);
            }
        }).start();
    }
}
