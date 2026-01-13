package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.preset;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip.Notification;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip.WidgetNotificationAutoClose;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip.WidgetNotificationProgress;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.Modal;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.ModalMessage;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculation;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculationRegistry;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.PathfindPreset;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.RoomPreset;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.Pair;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class WidgetModalExportSettings extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "includePrecalculation")
    public final BindableAttribute<Boolean> includePrecalc = new BindableAttribute<Boolean>(Boolean.class, false);


    private PathfindPreset preset;
    public WidgetModalExportSettings(PathfindPreset preset) {
        super(new ResourceIdentifier("dungeonsguide:gui/features/precalclist/presetview/modal_export_settings.gui"));

        this.preset = preset;

        includePrecalc.addOnUpdate((old, neu) -> {
            ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        });
    }



    @On(functionName = "confirm")
    public void confirm() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);


        File target = new File(DungeonsGuide.getDungeonsGuide().getConfigDir(), "presetExports");
        if (!target.exists())
            target.mkdirs();

        if (includePrecalc.getValue()) {
            try {
                PathfindPrecalculationRegistry registry = PathfindPrecalculationRegistry.getINSTANCE();
                long totalSize = 4 * 1024; // 4 kb leeway.
                for (RoomPreset value : preset.getPresets().values()) {
                    for (String calcid : value.getPrecalculations()) {
                        PathfindPrecalculation precalc = registry.getById(calcid);
                        totalSize += Files.size(Paths.get(precalc.getFile()));
                    }
                }

                long usablespace = Files.getFileStore(target.toPath()).getUsableSpace();
                if (usablespace < totalSize) {
                    throw new IllegalStateException(FileUtils.byteCountToDisplaySize(totalSize) + " of storage required but only " + FileUtils.byteCountToDisplaySize(usablespace) + " available");
                }

            } catch (Exception e) {
                e.printStackTrace();
                String message = e.getMessage();
                PopupMgr.getPopupMgr(getDomElement()).openPopup(new Modal(300, 200, "An Error occured while exporting", new ModalMessage(message), true), (a) -> {
                    PopupMgr.getPopupMgr(getDomElement()).closePopup(null);
                });
                return;
            }
        }

        PopupMgr.getPopupMgr(getDomElement()).closePopup(null);

        new Thread(DungeonsGuide.THREAD_GROUP, this::export).start();
    }


    private void export() {
        // actually calculate required space.


        UUID uid = UUID.randomUUID();
        boolean includePrecalc = this.includePrecalc.getValue();
        try {
            File targetDir = new File(DungeonsGuide.getDungeonsGuide().getConfigDir(), "presetExports");


            Frame parent = new Frame();
            FileDialog dialog = new FileDialog(parent, "Select export target", FileDialog.SAVE);
            dialog.setDirectory(targetDir.getAbsolutePath());
            dialog.setFile(preset.getPresetId() + ".zip");
            dialog.setVisible(true);

            File[] chosen = dialog.getFiles();
            if (chosen.length == 0) return;

            File target = chosen[0];

            parent.dispose();
            dialog.dispose();


            WidgetNotificationProgress progress = new WidgetNotificationProgress(uid, "Exporting Preset");
            FeatureRegistry.NOTIFICATIONS.getRootWidget().updateNotification(uid, progress);

            List<Pair<String, File>> files = new ArrayList<>();
            PathfindPrecalculationRegistry registry = PathfindPrecalculationRegistry.getINSTANCE();
            for (RoomPreset value : preset.getPresets().values()) {
                for (String calcid : value.getPrecalculations()) {
                    PathfindPrecalculation precalc = registry.getById(calcid);
                    files.add(new Pair(precalc.getId(), new File(precalc.getFile())));
                }
            }


            long st = System.currentTimeMillis();

            WidgetNotificationProgress.Progress progress1 = new WidgetNotificationProgress.Progress("Writing Preset Export 0/"+(files.size() + 1), new AtomicLong(0), new AtomicLong(files.size()+1), true);
            progress.addProgress(progress1);
            try (FileOutputStream fos = new FileOutputStream(target); ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(fos));) {
                zipOut.setComment("Dungeons Guide Preset Export");
                zipOut.setLevel(Deflater.NO_COMPRESSION);
                {
                    ZipEntry zipEntry = new ZipEntry("preset.json");
                    zipOut.putNextEntry(zipEntry);


                    JsonObject object = preset.saveToJson();

                    JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(zipOut));
                    new Gson().toJson(object, jsonWriter);
                    jsonWriter.flush();

                    progress1.setMessage("Writing Preset Export "+(progress1.getCurrent().incrementAndGet())+"/"+progress1.getTotal().get());
                }


                if (includePrecalc) {
                    for (Pair<String, File> srcFile : files) {
                        FileInputStream fis = new FileInputStream(srcFile.getSecond());
                        ZipEntry zipEntry = new ZipEntry("precalculations/"+srcFile.getFirst()+".pfres");
                        zipOut.putNextEntry(zipEntry);

                        Files.copy(srcFile.getSecond().toPath(), zipOut);

                        fis.close();

                        progress1.setMessage("Writing Preset Export "+(progress1.getCurrent().incrementAndGet())+"/"+progress1.getTotal().get());
                    }
                }
            }


            FeatureRegistry.NOTIFICATIONS.getRootWidget().updateNotification(uid, new WidgetNotificationAutoClose(uid,
                    Notification.builder()
                            .title("Export Complete!")
                            .description("Exported to "+target.getAbsolutePath())
                            .titleColor(0xFF00FF00).build(), 10000));
        } catch (Exception e) {
            e.printStackTrace();


            FeatureRegistry.NOTIFICATIONS.getRootWidget().updateNotification(uid, new WidgetNotificationAutoClose(uid,
                    Notification.builder()
                            .title("An error occured while exporting")
                            .description(e.getMessage())
                            .titleColor(0xFFFF0000).build(), 30000));
        }

    }

    @On(functionName = "cancel")
    public void cancel() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);

        PopupMgr.getPopupMgr(getDomElement()).closePopup(null);
    }
}
