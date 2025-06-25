package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.preset;

import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip.WidgetNotificationProgress;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.PathfindPrecalculationRequestSet;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq.WidgetPendingRequestPage;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.AdditionalInfoCaculatedDungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.WidgetAbilitySettings;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.abilitysettings.modal.WidgetModalChooseAbilitySettings;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Navigator;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.*;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculation;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculationRegistry;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.PathfindPreset;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.PathfindPresetRegistry;
import kr.syeyoung.dungeonsguide.mod.pathfinding.world.PathfindRequest;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WidgetPresetMetadata  extends AnnotatedImportOnlyWidget {
    private PathfindPreset preset;
    private WidgetViewPreset parent;

    @Bind(variableName = "presetName")
    public final BindableAttribute<String> presetName = new BindableAttribute<>(String.class);

    @Bind(variableName = "id")
    public final BindableAttribute<String> id = new BindableAttribute<>(String.class);

    @Bind(variableName = "generatedAt")
    public final BindableAttribute<String> generatedAt = new BindableAttribute<>(String.class);

    @Bind(variableName = "origin")
    public final BindableAttribute<String> origin = new BindableAttribute<>(String.class);

    @Bind(variableName = "filename")
    public final BindableAttribute<String> filename = new BindableAttribute<>(String.class);

    @Bind(variableName = "abilitySettings")
    public final BindableAttribute<Widget> abilitySettings = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "editable")
    public final BindableAttribute<String> editable = new BindableAttribute<>(String.class);

    private BindableAttribute<AlgorithmSetting> algorithmSettingBindableAttribute = new BindableAttribute<>(AlgorithmSetting.class);

    public WidgetPresetMetadata(PathfindPreset preset, WidgetViewPreset widgetViewPreset) {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/presetview/metadata.gui"));
        this.preset = preset;
        this.parent = widgetViewPreset;

        this.presetName.setValue(preset.getPresetName());
        this.id.setValue(preset.getPresetId());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL);
        this.generatedAt.setValue(dateTimeFormatter.format(preset.getGeneratedAt().atZone(ZoneId.systemDefault())));
        this.origin.setValue(preset.getOrigin());
        if (preset.getFile() != null)
            this.filename.setValue(Main.getConfigDir().toPath().relativize(preset.getFile().toPath()).toString());
        else
            this.filename.setValue("no file");
        algorithmSettingBindableAttribute.setValue(preset.getAlgorithmSetting());
        this.abilitySettings.setValue(new WidgetAbilitySettings(algorithmSettingBindableAttribute));
        this.editable.setValue(preset.isEditable() ? "true" : "false");
    }


    @On(functionName = "changeName")
    public void changeName() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);

        ModalAsk modalMessage = new ModalAsk("Please enter the new preset name in below box", "Enter new name here", preset.getPresetName());
        PopupMgr.getPopupMgr(getDomElement()).openPopup(new Modal(300, 200, "Choose new name for preset", modalMessage, true), (a) -> {
            if (a == null) return;
            preset.setPresetName((String)a);
            parent.notifyNameUpdate(preset);
            WidgetPresetMetadata.this.presetName.setValue(preset.getPresetName());
        });
    }

    @On(functionName = "clone")
    public void clonePreset() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);

        PathfindPreset preset1 = preset.clone();
        PathfindPresetRegistry.getINSTANCE().register(preset1);

        parent.notifyNew(preset1);
    }


    @On(functionName = "delete")
    public void delete() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);

        if (preset == PathfindPresetRegistry.DEFAULT_PRESET) {
            ModalMessage modalMessage = new ModalMessage("Default preset can not be deleted");
            PopupMgr.getPopupMgr(getDomElement()).openPopup(new Modal(300, 200, "Error", modalMessage, true), null);
            return;
        }

        ModalConfirm modalMessage = new ModalConfirm("You're trying to delete: "+preset.getPresetName()+"\n\nDeleting can not be reverted");
        PopupMgr.getPopupMgr(getDomElement()).openPopup(new Modal(300, 200, "Are you sure?", modalMessage, true), (a) -> {
            if (a == null) return;
            if (a == Boolean.TRUE) {
                PathfindPresetRegistry.getINSTANCE().unregister(preset);
                parent.notifyDelete(preset);

                try {
                    if (preset.getFile() != null)
                        Files.delete(preset.getFile().toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                    FeatureCollectDiagnostics.queueSendLogAsync(e);
                }
            }
        });
    }

    @On(functionName = "apply")
    public void apply() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);

        parent.getPresetList().apply(preset);
    }


    @On(functionName = "editAbilitySettings")
    public void editAbilitySettings() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);


        PopupMgr.getPopupMgr(getDomElement()).openPopup(new Modal(400, 300, "Choose New Default Algorithm Setting", new WidgetModalChooseAbilitySettings(), true), (a) -> {
            if (a != null) {
                this.algorithmSettingBindableAttribute.setValue((AlgorithmSetting) a);
                preset.setAlgorithmSetting((AlgorithmSetting) a);
                this.parent.recalc();
            }
        });
    }

    @On(functionName = "export")
    public void export() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);



        PopupMgr.getPopupMgr(getDomElement()).openPopup(new Modal(200, 150, "Export Options", new WidgetModalExportSettings(preset), true), null);
    }


    @On(functionName = "unlinkUnused")
    public void unlinkUnused() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        ModalConfirm modalMessage = new ModalConfirm("This will unlink all UNUSED precalculations in this PRESET.\nThis operation can not be undone");
        PopupMgr.getPopupMgr(getDomElement()).openPopup(new Modal(300, 200, "Are you sure?", modalMessage, true), (a) -> {
            if (a == null) return;
            if (a == Boolean.TRUE) {
                UUID uid = UUID.randomUUID();
                WidgetNotificationProgress progress = new WidgetNotificationProgress(
                        uid, "Unlinking"
                );
                FeatureRegistry.NOTIFICATIONS.getRootWidget().updateNotification(uid, progress);
                progress.addProgress(new WidgetNotificationProgress.Progress("Unlinking...", null, null, false));

                WidgetViewPreset.calculator.submit(() -> {
                    try {
                        List<AdditionalInfoCaculatedDungeonRoomInfo> additionalInfoCaculatedDungeonRoomInfoList = new ArrayList<>();
                        for (DungeonRoomInfo dungeonRoomInfo : DungeonRoomInfoRegistry.getRegistered()) {
                            additionalInfoCaculatedDungeonRoomInfoList.add(new AdditionalInfoCaculatedDungeonRoomInfo(dungeonRoomInfo, preset));
                        }

                        for (AdditionalInfoCaculatedDungeonRoomInfo roomInfo : additionalInfoCaculatedDungeonRoomInfoList) {
                            for (PathfindPrecalculation precalculation : roomInfo.getUnused()) {
                                roomInfo.getRoomPreset().removePrecalculation(precalculation.getId());
                            }
                        }
                    } finally {
                        FeatureRegistry.NOTIFICATIONS.getRootWidget().removeNotification(uid);
                        DungeonsGuide.getDungeonsGuide().runNextTick(() -> {
                            parent.recalc();
                        });
                    }
                });
            }
        });
    }

    @On(functionName = "autolink")
    public void autolink() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);

        UUID uid = UUID.randomUUID();
        WidgetNotificationProgress progress = new WidgetNotificationProgress(
                uid, "Autolinking"
        );
        FeatureRegistry.NOTIFICATIONS.getRootWidget().updateNotification(uid, progress);
        progress.addProgress(new WidgetNotificationProgress.Progress("Autolinking...", null, null, false));

        WidgetViewPreset.calculator.submit(() -> {
            try {
                List<AdditionalInfoCaculatedDungeonRoomInfo> additionalInfoCaculatedDungeonRoomInfoList = new ArrayList<>();
                for (DungeonRoomInfo dungeonRoomInfo : DungeonRoomInfoRegistry.getRegistered()) {
                    additionalInfoCaculatedDungeonRoomInfoList.add(new AdditionalInfoCaculatedDungeonRoomInfo(dungeonRoomInfo, preset));
                }

                for (AdditionalInfoCaculatedDungeonRoomInfo roomInfo : additionalInfoCaculatedDungeonRoomInfoList) {

                    for (PathfindRequest request : roomInfo.getMissing()) {
                        List<PathfindPrecalculation> precalcs = PathfindPrecalculationRegistry.getINSTANCE().getsByHash(request.getHash());
                        for (PathfindPrecalculation precalc : precalcs) {
                            if (!precalc.getAlgorithmSetting().equals(roomInfo.getRoomPreset().getEffectiveAlgorithmSetting(roomInfo.getDungeonRoomInfo())))
                                continue;

                            roomInfo.getRoomPreset().addPrecalculation(precalc.getId());
                            break;
                        }
                    }
                }
            } finally {
                FeatureRegistry.NOTIFICATIONS.getRootWidget().removeNotification(uid);
                DungeonsGuide.getDungeonsGuide().runNextTick(() -> {
                    parent.recalc();
                });
            }
        });
    }

    @On(functionName = "requestMissing")
    public void requestMissing() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);

        UUID uid = UUID.randomUUID();
        WidgetNotificationProgress progress = new WidgetNotificationProgress(
                uid, "Generating Precalculation Request Set..."
        );
        FeatureRegistry.NOTIFICATIONS.getRootWidget().updateNotification(uid, progress);
        progress.addProgress(new WidgetNotificationProgress.Progress("Generating Precalculation Request Set...", null, null, false));


        WidgetViewPreset.calculator.submit(() -> {
            try {
                List<AdditionalInfoCaculatedDungeonRoomInfo> additionalInfoCaculatedDungeonRoomInfoList = new ArrayList<>();
                for (DungeonRoomInfo dungeonRoomInfo : DungeonRoomInfoRegistry.getRegistered()) {
                    additionalInfoCaculatedDungeonRoomInfoList.add(new AdditionalInfoCaculatedDungeonRoomInfo(dungeonRoomInfo, preset));
                }
                List<PathfindRequest> requests = new ArrayList<>();
                for (AdditionalInfoCaculatedDungeonRoomInfo roomInfo : additionalInfoCaculatedDungeonRoomInfoList) {
                    for (PathfindRequest request : roomInfo.getMissing()) {
                        requests.add(request);
                    }
                }

                PathfindPrecalculationRequestSet requestSet = new PathfindPrecalculationRequestSet(preset, requests);

                FeatureRegistry.SECRET_PATHFIND_REQUEST.addPathfindPrecalculationRequestSet(requestSet);
                // open gui.

                Navigator navigator = Navigator.getNavigator(getDomElement());
                navigator.setPageWithoutPush(FeatureRegistry.SECRET_PATHFIND_REQUEST.getConfigureWidget());
                navigator.openPage(new WidgetPendingRequestPage(requestSet));
            } finally {
                FeatureRegistry.NOTIFICATIONS.getRootWidget().removeNotification(uid);
            }
        });

    }


}
