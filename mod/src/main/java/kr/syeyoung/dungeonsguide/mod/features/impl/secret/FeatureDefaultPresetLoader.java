package kr.syeyoung.dungeonsguide.mod.features.impl.secret;

import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.VersionInfo;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip.Notification;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip.WidgetNotificationAutoClose;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip.WidgetNotificationProgress;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculation;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculationRegistry;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.TSPCacheRegistry;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.PathfindPreset;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.PathfindPresetRegistry;
import org.apache.commons.io.FileUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FeatureDefaultPresetLoader extends SimpleFeature {

    public FeatureDefaultPresetLoader() {
        super("Pathfinding & Secrets", "Default Preset Loader", "When enabled, this feature downloads default preset with precalculations and applies it if not applied.", "secret.download", true);
    }

    @Override
    public void init() {
        if (PathfindPresetRegistry.getINSTANCE().getPreset("0d4644ea-a02a-4407-a7f3-f9cd33b27ee7") == null && isEnabled()) {
            String[] str = {
                    "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10"
            };
            String[] conv = new String[str.length];
            for (int i = 0; i < str.length; i++) {
                conv[i] = "https://presets.dungeons.guide/defaultpreset/default%20preset.zip."+str[i];
            }

            download(conv);
        } else if (PathfindPresetRegistry.getINSTANCE().getPreset("0d4644ea-a02a-4407-a7f3-f9cd33b27ee7") != null) {
            PathfindPresetRegistry.getINSTANCE().unregister(PathfindPresetRegistry.DEFAULT_PRESET);
            PathfindPresetRegistry.DEFAULT_PRESET = PathfindPresetRegistry.getINSTANCE().getPreset("0d4644ea-a02a-4407-a7f3-f9cd33b27ee7");
        }
    }

    private AtomicBoolean loading = new AtomicBoolean();
    public void download(String[] url) {
        if (loading.getAndSet(true)) return;

        final UUID calcuuid = UUID.randomUUID();
        WidgetNotificationProgress progressForTopRight = new WidgetNotificationProgress(calcuuid, "Default Pathfind Preset Download Progress");
        FeatureRegistry.NOTIFICATIONS.getRootWidget().updateNotification(calcuuid, progressForTopRight); // should be thread safe. shouuuuld be.

        new Thread(DungeonsGuide.THREAD_GROUP, () -> {
            try {
                File[] downloadTarget = new File[url.length];

                WidgetNotificationProgress.Progress progress2 = new WidgetNotificationProgress.Progress("Downloading Files (0/" + url.length + ")", new AtomicLong(0), new AtomicLong(url.length), true);
                progressForTopRight.addProgress(progress2);
                try {
                    for (int i = 0; i < url.length; i++) {
                        progress2.setMessage("Downloading Files ("+(i+1)+"/"+url.length+")");
                        progress2.getCurrent().incrementAndGet();

                        WidgetNotificationProgress.Progress progress = new WidgetNotificationProgress.Progress("Downloading", null, null, false);
                        progressForTopRight.addProgress(progress);
                        try {
                            downloadTarget[i] = new File(DungeonsGuide.getDungeonsGuide().getTempDir(), "dg-default-preset-download-" + System.currentTimeMillis() + ".zip." + i);
                            downloadTarget[i].deleteOnExit();
                            HttpsURLConnection connection = (HttpsURLConnection) new URL(url[i]).openConnection();
                            connection.setRequestMethod("GET");
                            connection.setRequestProperty("User-Agent", "DungeonsGuide/" + VersionInfo.VERSION);
                            connection.connect();

                            long contentLength = Long.parseLong(connection.getHeaderField("Content-Length"));
                            progressForTopRight.removeProgress(progress);
                            progress = new WidgetNotificationProgress.Progress("Downloading (" + FileUtils.byteCountToDisplaySize(contentLength) + ")", new AtomicLong(), new AtomicLong(contentLength), true);
                            progressForTopRight.addProgress(progress);
                            long startTime = System.currentTimeMillis();

                            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                                 FileOutputStream fileOutputStream = new FileOutputStream(downloadTarget[i])) {
                                byte dataBuffer[] = new byte[1024 * 1024];
                                int bytesRead;
                                while ((bytesRead = in.read(dataBuffer, 0, 1024 * 1024)) != -1) {
                                    fileOutputStream.write(dataBuffer, 0, bytesRead);

                                    progress.getCurrent().addAndGet(bytesRead);


                                    long elapsed = System.currentTimeMillis() - startTime;
                                    double speed = (progress.getCurrent().get() / 1024.0) / (elapsed / 1000.0 + 1) / 1024.0; // MB/s
                                    long remainingBytes = contentLength - progress.getCurrent().get();
                                    long etaMillis = (long) ((remainingBytes / 1024.0 / 1024.0) / (speed + 0.1) * 1000);

                                    progress.setMessage("Downloading (" + FileUtils.byteCountToDisplaySize(contentLength) + ") " + String.format("%.2f", speed) + "MB/s" + " ETA: " + (etaMillis / 1000) + " Seconds");
                                }
                            }
                        } finally {
                            progressForTopRight.removeProgress(progress);
                        }
                    }
                } finally {
                    progressForTopRight.removeProgress(progress2);
                }



                WidgetNotificationProgress.Progress combining = new WidgetNotificationProgress.Progress("Combining Files (0/" + url.length + ")", new AtomicLong(0), new AtomicLong(url.length), true);
                progressForTopRight.addProgress(combining);
                File combinedFile;
                try {
                    combinedFile = new File(DungeonsGuide.getDungeonsGuide().getTempDir(), "dg-default-preset-download-" + System.currentTimeMillis() + ".zip");
                    combinedFile.deleteOnExit();
                    try (FileOutputStream fos = new FileOutputStream(combinedFile);
                         FileChannel outChannel = fos.getChannel()) {

                        for (File part : downloadTarget) {
                            combining.setMessage("Combining Files ("+(combining.getCurrent().incrementAndGet())+"/"+url.length+")");

                            try (FileInputStream fis = new FileInputStream(part);
                                 FileChannel inChannel = fis.getChannel()) {
                                long size = inChannel.size();
                                long transferred = 0;
                                while (transferred < size) {
                                    transferred += inChannel.transferTo(transferred, size - transferred, outChannel);
                                }
                            }
                        }
                    }
                    for (File file : downloadTarget) {
                        Files.deleteIfExists(file.toPath());
                    }
                } finally {
                    progressForTopRight.removeProgress(combining);
                }

                WidgetNotificationProgress.Progress openingFile = new WidgetNotificationProgress.Progress("Opening File...", null, null, false);
                progressForTopRight.addProgress(openingFile);
                PathfindPreset preset;
                try (ZipFile zipFile = new ZipFile(combinedFile)) {

                    if (!"Dungeons Guide Preset Export".equals(zipFile.getComment())) {
                        throw new IllegalArgumentException("File is not valid pathfind preset export");
                    }

                    {
                        ZipEntry zipEntry = zipFile.getEntry("preset.json");
                        if (zipEntry == null) {
                            throw new IllegalArgumentException("File is not valid pathfind preset export");
                        }
                        File presetExtractionTarget = new File(new File(Main.getConfigDir(), "presets"), "default.json");
                        try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
                            preset = PathfindPreset.loadFromStream(inputStream);
                            preset.setEditable(false);
                            preset.setFile(presetExtractionTarget);
                        }
                        preset.markDirty();
                        preset.save();
                        PathfindPresetRegistry.getINSTANCE().register(preset);
                    }

                    List<String> targets = new ArrayList<>();
                    long totalSize = 0;

                    Enumeration<? extends ZipEntry> elements = zipFile.entries();
                    while (elements.hasMoreElements()) {
                        ZipEntry zipEntry = elements.nextElement();
                        if (zipEntry.getName().startsWith("precalculations/")) {
                            targets.add(zipEntry.getName());
                            totalSize += zipEntry.getSize();
                        }
                    }
                    progressForTopRight.removeProgress(openingFile);

                    WidgetNotificationProgress.Progress extracting = new WidgetNotificationProgress.Progress("Extracting Precalculations 0/"+targets.size(), new AtomicLong(0), new AtomicLong(targets.size()), true);
                    progressForTopRight.addProgress(extracting);

                    File importTarget = new File(new File(Main.getConfigDir(), "precalculations"), preset.getPresetId());
                    if (!importTarget.exists())
                        importTarget.mkdirs();

                    long usablespace = Files.getFileStore(importTarget.toPath()).getUsableSpace();
                    if (usablespace < totalSize) {
                        throw new IllegalStateException(FileUtils.byteCountToDisplaySize(totalSize) + " of storage required but only " + FileUtils.byteCountToDisplaySize(usablespace) + " available");
                    }

                    List<File> extractions = new ArrayList<>();

                    for (String s : targets) {
                        ZipEntry entry = zipFile.getEntry(s);
                        try (InputStream is = zipFile.getInputStream(entry)) {
                            String id = s.split("/")[1];
                            File extractTarget2 =  new File(importTarget, id);
                            if (PathfindPrecalculationRegistry.getINSTANCE().getById(id.split("\\.")[0]) == null) {
                                Files.copy(is, extractTarget2.toPath());
                                extractions.add(extractTarget2);
                            }
                            extracting.setMessage("Extracting Precalculations "+extracting.getCurrent().incrementAndGet()+"/"+extracting.getTotal().get());
                        }
                    }

                    progressForTopRight.removeProgress(extracting);

                    extracting = new WidgetNotificationProgress.Progress("Loading Precalculations 0/"+extractions.size(), new AtomicLong(0), new AtomicLong(targets.size()), true);
                    progressForTopRight.addProgress(extracting);
                    for (File extraction : extractions) {
                        extracting.setMessage("Loading Precalculations "+extracting.getCurrent().incrementAndGet()+"/"+extracting.getTotal().get());
                        try {
                            PathfindPrecalculationRegistry.getINSTANCE().register(new PathfindPrecalculation(extraction));
                        } catch (Exception e) {
                            e.printStackTrace();
                            UUID uid = UUID.randomUUID();
                            FeatureRegistry.NOTIFICATIONS.getRootWidget().updateNotification(uid, new WidgetNotificationAutoClose(uid, Notification.builder()
                                    .title("Error while loading precalculation")
                                    .description(e.getMessage()+"\n Cause: "+extraction.getName())
                                    .titleColor(0xFFFF0000).build(),5000));
                        }
                    }

                    progressForTopRight.removeProgress(extracting);
                }

                PathfindPresetRegistry.getINSTANCE().unregister(PathfindPresetRegistry.DEFAULT_PRESET);
                PathfindPresetRegistry.DEFAULT_PRESET = preset;
                TSPCacheRegistry.getINSTANCE().loadPreset(preset);

                UUID newuuid = UUID.randomUUID();
                FeatureRegistry.NOTIFICATIONS.getRootWidget().updateNotification(newuuid, new WidgetNotificationAutoClose(newuuid, Notification.builder()
                        .title("Successfully Imported Default Preset!")
                        .description("")
                        .titleColor(0xFF00FF00)
                        .build(), 5000));
            }catch (Throwable e) {
                e.printStackTrace();
                UUID newUID = UUID.randomUUID();
                FeatureRegistry.NOTIFICATIONS.getRootWidget().updateNotification(newUID, new WidgetNotificationAutoClose(newUID, Notification.builder()
                        .title("Task Error :: Loading Default Preset")
                        .description(e.getClass().getSimpleName()+": "+e.getMessage())
                        .titleColor(0xFFFF0000)
                        .build(), 5000));
            } finally {
                FeatureRegistry.NOTIFICATIONS.getRootWidget().removeNotification(calcuuid);
                loading.set(false);
            }
        }).start();
    }

    @Override
    public boolean shouldShowOnConfig() {
        return false;
    }
}
