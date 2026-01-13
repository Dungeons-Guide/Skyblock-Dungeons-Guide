package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip.Notification;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip.WidgetNotificationAutoClose;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip.WidgetNotificationProgress;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.preset.WidgetPreset;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.preset.WidgetViewPreset;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Column;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.gui.xml.data.WidgetList;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculation;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculationRegistry;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.PathfindPreset;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.PathfindPresetRegistry;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class WidgetPrecalcList extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "presetsApi")
    public final BindableAttribute<Column> presetApi = new BindableAttribute<>(Column.class);
    @Bind(variableName = "presets")
    public final BindableAttribute<List<Widget>> widgetList = new BindableAttribute(WidgetList.class);

    @Bind(variableName = "presetVisibility")
    public final BindableAttribute<String> presetVisibility = new BindableAttribute<>(String.class, "false");
    @Bind(variableName = "viewPreset")
    public final BindableAttribute<Widget> viewPreset = new BindableAttribute<>(Widget.class);


    public WidgetPrecalcList() {
        super(new ResourceIdentifier("dungeonsguide:gui/features/precalclist/precalclist.gui"));
        loadPresets();
    }

    private List<WidgetPreset> widgetPresetList = new ArrayList<>();

    private void loadPresets() {
        widgetPresetList.clear();
        for (PathfindPreset loadedPreset : PathfindPresetRegistry.getINSTANCE().getLoadedPresets()) {
            WidgetPreset preset = new WidgetPreset(loadedPreset, this);
            widgetPresetList.add(preset);
        }
        widgetList.setValue(new ArrayList<>(widgetPresetList));
    }

    @On(functionName = "create")
    public void createNew() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);

        PathfindPreset pathfindPreset = new PathfindPreset();
        PathfindPresetRegistry.getINSTANCE().register(pathfindPreset);
        addPreset(pathfindPreset);
    }

    @On(functionName = "import")
    public void importFile() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);

        new Thread(DungeonsGuide.THREAD_GROUP, this::_importFile).start();
    }

    @On(functionName = "docs")
    public void docs() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        try {
            Desktop.getDesktop().browse(new URI("https://docs.dungeons.guide/docs/pathfinding/presets/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }


    private void _importFile() {

        UUID uuid = UUID.randomUUID();


        try {
            Frame parent = new Frame();
            FileDialog dialog = new FileDialog(parent, "Select Import Target", FileDialog.LOAD);
            dialog.setFilenameFilter((dir, name) -> name.endsWith(".zip"));
            dialog.setFile("*.zip");
            dialog.setVisible(true);

            File[] chosen = dialog.getFiles();
            if (chosen.length == 0) return;

            File target = chosen[0];

            parent.dispose();
            dialog.dispose();


            WidgetNotificationProgress progress = new WidgetNotificationProgress(uuid, "Importing Preset :: "+target.getName());
            FeatureRegistry.NOTIFICATIONS.getRootWidget().updateNotification(uuid, progress);

            WidgetNotificationProgress.Progress openingFile = new WidgetNotificationProgress.Progress("Opening File...", null, null, false);
            progress.addProgress(openingFile);
            PathfindPreset preset;
            try (ZipFile zipFile = new ZipFile(target)) {

                if (!"Dungeons Guide Preset Export".equals(zipFile.getComment())) {
                    throw new IllegalArgumentException("File is not valid pathfind preset export");
                }

                {
                    UUID random = UUID.randomUUID();
                    ZipEntry zipEntry = zipFile.getEntry("preset.json");
                    if (zipEntry == null) {
                        throw new IllegalArgumentException("File is not valid pathfind preset export");
                    }
                    File presetExtractionTarget = new File(new File(DungeonsGuide.getDungeonsGuide().getConfigDir(), "presets"), random+".json");
                    try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
                        preset = PathfindPreset.loadFromStream(inputStream);
                        preset.setEditable(false);
                        preset.setPresetId(random.toString());
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
                progress.removeProgress(openingFile);

                WidgetNotificationProgress.Progress extracting = new WidgetNotificationProgress.Progress("Extracting Precalculations 0/"+targets.size(), new AtomicLong(0), new AtomicLong(targets.size()), true);
                progress.addProgress(extracting);

                File importTarget = new File(new File(DungeonsGuide.getDungeonsGuide().getConfigDir(), "precalculations"), preset.getPresetId());
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
                        File extractTarget =  new File(importTarget, id);
                        if (PathfindPrecalculationRegistry.getINSTANCE().getById(id.split("\\.")[0]) == null) {
                            Files.copy(is, extractTarget.toPath());
                            extractions.add(extractTarget);
                        }
                        extracting.setMessage("Extracting Precalculations "+extracting.getCurrent().incrementAndGet()+"/"+extracting.getTotal().get());
                    }
                }

                progress.removeProgress(extracting);

                extracting = new WidgetNotificationProgress.Progress("Loading Precalculations 0/"+extractions.size(), new AtomicLong(0), new AtomicLong(targets.size()), true);
                progress.addProgress(extracting);
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

                progress.removeProgress(extracting);
            }

            FeatureRegistry.NOTIFICATIONS.getRootWidget().updateNotification(uuid, new WidgetNotificationAutoClose(uuid, Notification.builder()
                    .title("Successfully Imported Preset!")
                    .description("File: "+target.getAbsolutePath())
                    .titleColor(0xFF00FF00)
                    .build(), 5000));

            DungeonsGuide.getDungeonsGuide().runNextTick(() -> {
                addPreset(preset);
            });
        } catch (Exception e) {
            e.printStackTrace();
            FeatureRegistry.NOTIFICATIONS.getRootWidget().updateNotification(uuid, new WidgetNotificationAutoClose(uuid, Notification.builder()
                    .title("Error while importing preset")
                    .description(e.getMessage())
                    .titleColor(0xFFFF0000).build(),30000));
        }
    }

    public void addPreset(PathfindPreset preset) {
        WidgetPreset widgetPreset = new WidgetPreset(preset, this);
        presetApi.getValue().addWidget(widgetPreset);
        widgetPresetList.add(widgetPreset);

        edit(widgetPreset);
    }

    public void edit(WidgetPreset widgetPreset) {
        for (WidgetPreset preset : widgetPresetList) {
            preset.setSelected(false);
        }
        if (widgetPreset != null) {
            widgetPreset.setSelected(true);
            presetVisibility.setValue("true");
            viewPreset.setValue(new WidgetViewPreset(widgetPreset.getPreset(), this));
        } else {
            presetVisibility.setValue("false");
            viewPreset.setValue(null);
        }
    }

    public void notifyDelete(PathfindPreset preset) {
        Iterator<WidgetPreset> presetIterator = widgetPresetList.iterator();
        while (presetIterator.hasNext()) {
            WidgetPreset preset1 = presetIterator.next();
            if (preset1.getPreset() == preset) {
                presetIterator.remove();
                presetApi.getValue().removeWidget(preset1);
                break;
            }
        }
        edit(null);
    }

    public void apply(PathfindPreset preset) {

        FeatureRegistry.SECRET_PRECALC_LIST.setSelectedPreset(preset);

        for (WidgetPreset widgetPreset : widgetPresetList) {
            widgetPreset.setSelected(widgetPreset.getPreset() == preset);
        }
    }

    public void update(PathfindPreset preset) {
        for (WidgetPreset widgetPreset : widgetPresetList) {
            if (widgetPreset.getPreset() == preset) {
                widgetPreset.update();
            }
        }
    }
}
