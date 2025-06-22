package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.remotereq;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.auth.AuthManager;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.VersionInfo;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip.Notification;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip.WidgetNotificationAutoClose;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip.WidgetNotificationProgress;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.ApiFetcher;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.FeatureRequestCalculation;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculation;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculationRegistry;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.PathfindPreset;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.PathfindPresetRegistry;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.RoomPreset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class WidgetRequestDetails extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "progress")
    public final BindableAttribute<Widget> widgetBindableAttribute = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "error")
    public final BindableAttribute<String> err = new BindableAttribute<>(String.class, "");

    @Bind(variableName = "loading")
    public final BindableAttribute<String> loading = new BindableAttribute<>(String.class, "true");

    @Bind(variableName = "requestId")
    public final BindableAttribute<String> requestIdAtr = new BindableAttribute<>(String.class, "");
    @Bind(variableName = "requestName")
    public final BindableAttribute<String> requestName = new BindableAttribute<>(String.class, "");
    @Bind(variableName = "linkTo")
    public final BindableAttribute<String> linkTo = new BindableAttribute<>(String.class, "");

    @Bind(variableName = "complete")
    public final BindableAttribute<String> complete = new BindableAttribute<>(String.class, "false");


    private String requestId;
    private String downloadUrl;
    private RemoteCache cache;
    public WidgetRequestDetails(String requestId) {
        super(new ResourceLocation("dungeonsguide:gui/features/requestcalculation/remotereq/requestdetails.gui"));
        this.requestId = requestId;

        this.requestIdAtr.setValue(requestId);

        this.cache = FeatureRegistry.SECRET_PATHFIND_REQUEST.getRemoteCacheMap().get(requestId);

        if (cache != null) {
            requestName.setValue(cache.getName());
            PathfindPreset preset = PathfindPresetRegistry.getINSTANCE().getPreset(cache.getLinkedPreset());
            linkTo.setValue(preset == null ? "Unknown:: "+cache.getLinkedPreset() : preset.getPresetName());
        } else {
            requestName.setValue("Unknown");
            linkTo.setValue("N/A");
        }
        reload();
    }

    private void doReload() {
        loading.setValue("true");
        widgetBindableAttribute.setValue(null);
        downloadUrl = null;
        complete.setValue("false");
        this.err.setValue("");
        try {
            JsonObject jsonObject = ApiFetcher.getJsonWithAuth(FeatureRequestCalculation.DOMAIN+"/requests/"+requestId, AuthManager.getInstance().getWorkingTokenOrThrow());

            if (jsonObject.has("execution"))
                widgetBindableAttribute.setValue(new WidgetCalculationProcess(jsonObject.getAsJsonObject("execution"), jsonObject.getAsJsonObject("request")));

            JsonElement element = jsonObject.getAsJsonObject("request").get("downloadUrl");
            downloadUrl = element == null || element.isJsonNull() ? null : element.getAsString();

            loading.setValue("false");

            if (cache != null && downloadUrl != null) {
                cache.setWasInProgress(false);
            }
            if (downloadUrl != null) {
                complete.setValue("true");
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.err.setValue(e.getClass().getSimpleName()+": "+e.getMessage());
        }
    }

    @On(functionName = "reload")
    public void reload() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        ApiFetcher.ex.submit(this::doReload);
    }

    @On(functionName = "download")
    public void download() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

        if (downloadUrl != null) {
            try {
                Desktop.getDesktop().browse(new URI(downloadUrl));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }
    @On(functionName = "downloadAndAutoApply")
    public void downloadAndAutoApply() {
        final String downloadUrl = this.downloadUrl;
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

        if (cache != null) {
            cache.setCheckedAfterComplete(true);
        }


        final UUID calcuuid = UUID.randomUUID();
        WidgetNotificationProgress progressForTopRight = new WidgetNotificationProgress(calcuuid, "Pathfind Precalculation Download Progress");
        FeatureRegistry.NOTIFICATIONS.getRootWidget().updateNotification(calcuuid, progressForTopRight); // should be thread safe. shouuuuld be.

        new Thread(DungeonsGuide.THREAD_GROUP, () -> {
            try {

                WidgetNotificationProgress.Progress progress = new WidgetNotificationProgress.Progress("Downloading", null, null, false);
                progressForTopRight.addProgress(progress);
                File downloadTarget;
                try {
                    downloadTarget = new File(DungeonsGuide.getDungeonsGuide().getTempDir(), "dg-pfprecalc-download-"+System.currentTimeMillis()+".zip");
                    downloadTarget.deleteOnExit();
                    HttpsURLConnection connection = (HttpsURLConnection) new URL(downloadUrl).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", "DungeonsGuide/" + VersionInfo.VERSION);
                    connection.connect();

                    long contentLength = Long.parseLong(connection.getHeaderField("Content-Length"));
                    progressForTopRight.removeProgress(progress);
                    progress = new WidgetNotificationProgress.Progress("Downloading ("+FileUtils.byteCountToDisplaySize(contentLength)+")", new AtomicLong(), new AtomicLong(contentLength), true);
                    progressForTopRight.addProgress(progress);

                    try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                         FileOutputStream fileOutputStream = new FileOutputStream(downloadTarget)) {
                        byte dataBuffer[] = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                            fileOutputStream.write(dataBuffer, 0, bytesRead);

                            progress.getCurrent().addAndGet(bytesRead);
                        }
                    }
                } finally {
                    progressForTopRight.removeProgress(progress);
                }

                progress = new WidgetNotificationProgress.Progress("Extracting", new AtomicLong(), new AtomicLong(), false);
                progressForTopRight.addProgress(progress);
                List<File> toLoad = new ArrayList<>();
                try {
                    File targetDir = new File(Main.getConfigDir(), "precalculations/"+requestId);
                    targetDir.mkdirs();

                    try (ZipFile zipFile = new ZipFile(downloadTarget)) {
                        progressForTopRight.removeProgress(progress);
                        int size = Collections.list(zipFile.getEntries()).size();

                        progress = new WidgetNotificationProgress.Progress("Extracting (0/"+size+")", new AtomicLong(0), new AtomicLong(size), true);
                        progressForTopRight.addProgress(progress);

                        Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
                        while (entries.hasMoreElements()) {
                            ZipArchiveEntry zipEntry = entries.nextElement();
                            try (InputStream in = zipFile.getInputStream(zipEntry)) {
                                String[] name = zipEntry.getName().split("/");
                                File target = new File(targetDir, name[name.length - 1]);
                                Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                toLoad.add(target);
                            }
                            long current = progress.getCurrent().incrementAndGet();
                            progress.setMessage("Extracting ("+current+"/"+size+")");
                        }
                    }

                } finally {
                    progressForTopRight.removeProgress(progress);
                }
                progress = new WidgetNotificationProgress.Progress("Loading (0/"+toLoad.size()+")", new AtomicLong(), new AtomicLong(toLoad.size()), true);
                progressForTopRight.addProgress(progress);

                List<PathfindPrecalculation> precalculations = new ArrayList<>();
                int errors = 0;
                try {
                    for (File precalculation : toLoad) {
//                        if (!preset.getPresets().containsKey(precalculation.getRoomUID()))
//                            preset.getPresets().put(precalculation.getRoomUID(), new RoomPreset(preset, precalculation.getRoomUID()));
                        // wtf really
                        try {
                            PathfindPrecalculation precalculation1 = new PathfindPrecalculation(precalculation);
                            PathfindPrecalculationRegistry.getINSTANCE().register(precalculation1);
                            precalculations.add(precalculation1);
                        } catch (Exception e) {
                            e.printStackTrace();
                            errors++;
                        }

                        long current = progress.getCurrent().incrementAndGet();

                        progress.setMessage("Loading ("+current+"/"+toLoad.size()+") "+errors+" errors");
                    }
                    Thread.sleep(1000);
                } finally {
                    progressForTopRight.removeProgress(progress);
                }

                if (cache == null) {
                    FeatureRegistry.NOTIFICATIONS.getRootWidget().updateNotification(calcuuid, new WidgetNotificationAutoClose(calcuuid, Notification.builder()
                            .title("Task Partially Complete")
                            .description("Successfully loaded "+(toLoad.size()-errors)+"/"+toLoad.size()+" precalculations but was unable to link to requested preset. Please manually link to preset")
                            .titleColor(0xFFFFFF00)
                            .build(), 5000));
                    Thread.sleep(5000);
                    return;
                }

                PathfindPreset preset = PathfindPresetRegistry.getINSTANCE().getPreset(cache.getLinkedPreset());
                if (preset == null) {
                    FeatureRegistry.NOTIFICATIONS.getRootWidget().updateNotification(calcuuid, new WidgetNotificationAutoClose(calcuuid, Notification.builder()
                            .title("Task Partially Complete")
                            .description("Successfully loaded "+(toLoad.size()-errors)+"/"+toLoad.size()+"precalculations but was unable to link to requested preset. Please manually link to preset")
                            .titleColor(0xFFFFFF00)
                            .build(), 5000));
                    Thread.sleep(5000);
                    return;
                }
                progress = new WidgetNotificationProgress.Progress("Linking to preset (0/"+precalculations.size()+")", new AtomicLong(), new AtomicLong(toLoad.size()), true);
                progressForTopRight.addProgress(progress);
                try {
                    for (PathfindPrecalculation precalculation : precalculations) {
                        if (!preset.getPresets().containsKey(precalculation.getRoomUID()))
                            preset.getPresets().put(precalculation.getRoomUID(), new RoomPreset(preset, precalculation.getRoomUID()));
                        // wtf really
                        RoomPreset roomPreset = preset.getPresets().get(precalculation.getRoomUID());
                        roomPreset.addPrecalculation(precalculation.getId());


                        long current = progress.getCurrent().incrementAndGet();

                        progress.setMessage("Linking to preset (" + current + "/" + toLoad.size() + ")");
                    }
                } finally {
                    progressForTopRight.removeProgress(progress);
                }

                PathfindPresetRegistry.getINSTANCE().saveAll();

                FeatureRegistry.NOTIFICATIONS.getRootWidget().updateNotification(calcuuid, new WidgetNotificationAutoClose(calcuuid, Notification.builder()
                        .title("Task Successful!")
                        .description("Successfully loaded "+(toLoad.size()-errors)+"/"+toLoad.size()+" precalculations and linked to origin preset!")
                        .titleColor(0xFF00FF00)
                        .build(), 5000));
                Thread.sleep(5000);
            }catch (Exception e) {
                e.printStackTrace();
                UUID newUID = UUID.randomUUID();
                FeatureRegistry.NOTIFICATIONS.getRootWidget().updateNotification(newUID, new WidgetNotificationAutoClose(newUID, Notification.builder()
                        .title("Task Error :: Loading Precalculations")
                        .description(e.getClass().getSimpleName()+": "+e.getMessage())
                        .titleColor(0xFFFF0000)
                        .build(), 5000));
            } finally {
                FeatureRegistry.NOTIFICATIONS.getRootWidget().removeNotification(calcuuid);
            }


        }).start();
    }
}
