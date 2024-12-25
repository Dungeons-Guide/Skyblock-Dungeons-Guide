package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.preset;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.*;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip.Notification;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip.WidgetNotificationAutoClose;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip.WidgetNotificationProgress;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.Modal;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.ModalMessage;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import org.apache.commons.io.FileUtils;
import scala.xml.Atom;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class WidgetModalExportSettings extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "includePrecalculation")
    public final BindableAttribute<Boolean> includePrecalc = new BindableAttribute<Boolean>(Boolean.class, false);


    private PathfindPreset preset;
    public WidgetModalExportSettings(PathfindPreset preset) {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/presetview/modal_export_settings.gui"));

        this.preset = preset;

        includePrecalc.addOnUpdate((old, neu) -> {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        });
    }



    @On(functionName = "confirm")
    public void confirm() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));


        File target = new File(Main.getConfigDir(), "presetExports");
        if (!target.exists())
            target.mkdirs();

        if (includePrecalc.getValue()) {
            try {
                PathfindResultRegistry registry = PathfindResultRegistry.getINSTANCE();
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
            File targetDir = new File(Main.getConfigDir(), "presetExports");


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

            List<Tuple<String, File>> files = new ArrayList<>();
            PathfindResultRegistry registry = PathfindResultRegistry.getINSTANCE();
            for (RoomPreset value : preset.getPresets().values()) {
                for (String calcid : value.getPrecalculations()) {
                    PathfindPrecalculation precalc = registry.getById(calcid);
                    files.add(new Tuple(precalc.getId(), new File(precalc.getFile())));
                }
            }


            long st = System.currentTimeMillis();

            WidgetNotificationProgress.Progress progress1 = new WidgetNotificationProgress.Progress("Writing Preset Export 0/"+(files.size() + 1), new AtomicInteger(0), new AtomicInteger(files.size()+1), true);
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
                    for (Tuple<String, File> srcFile : files) {
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
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

        PopupMgr.getPopupMgr(getDomElement()).closePopup(null);
    }
}
