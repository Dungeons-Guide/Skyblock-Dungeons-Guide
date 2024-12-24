package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset;

import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindRequest;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.PathfindPrecalculation;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.PathfindResultRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.AdditionalInfoCaculatedDungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.WidgetAbilitySettings;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.abilitysettings.modal.WidgetModalChooseAbilitySettings;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.preset.WidgetViewPreset;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.Modal;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.ModalConfirm;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

public class WidgetPresetRoomDetailsMetadata extends AnnotatedImportOnlyWidget {
    private AdditionalInfoCaculatedDungeonRoomInfo roomInfo;

    private WidgetPresetRoomDetails details;

    @Bind(variableName = "roomName")
    public final BindableAttribute<String> roomName = new BindableAttribute<>(String.class);
    @Bind(variableName = "roomUID")
    public final BindableAttribute<String> roomUID = new BindableAttribute<>(String.class);
    @Bind(variableName = "secretCount")
    public final BindableAttribute<String> secretCount = new BindableAttribute<>(String.class);
    @Bind(variableName = "roomShape")
    public final BindableAttribute<String> roomShape = new BindableAttribute<>(String.class);
    @Bind(variableName = "roomType")
    public final BindableAttribute<String> roomType = new BindableAttribute<>(String.class);
    @Bind(variableName = "roomStateCount")
    public final BindableAttribute<String> roomStateCount = new BindableAttribute<>(String.class);
    @Bind(variableName = "roomStates")
    public final BindableAttribute<String> roomStates = new BindableAttribute<>(String.class);

    @Bind(variableName = "algorithmSetting")
    public final BindableAttribute<Widget> algorithmSetting = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "editable")
    public final BindableAttribute<String> editable = new BindableAttribute<>(String.class);


    private final BindableAttribute<AlgorithmSetting> algorithmSettingBindableAttribute = new BindableAttribute<>(AlgorithmSetting.class);

    public WidgetPresetRoomDetailsMetadata(AdditionalInfoCaculatedDungeonRoomInfo roomInfo, WidgetPresetRoomDetails parent) {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/roompresetview/metadata.gui"));
        this.roomInfo = roomInfo;
        this.details = parent;

        this.roomName.setValue(roomInfo.getDungeonRoomInfo().getName());
        this.roomUID.setValue(roomInfo.getDungeonRoomInfo().getUuid().toString());
        this.secretCount.setValue(roomInfo.getDungeonRoomInfo().getTotalSecrets()+"");
        this.roomShape.setValue(roomInfo.getRoomShape());
        this.roomType.setValue(roomInfo.getRoomType());
        this.roomStateCount.setValue(roomInfo.getStateInfos().size()+"");
        this.roomStates.setValue(
                roomInfo.getStateInfos().stream()
                        .map(AdditionalInfoCaculatedDungeonRoomInfo.RoomStateInfo::getStateIdentifier)
                        .map(a -> a.isEmpty() ? "(empty)" : a).collect(Collectors.joining(" | ")));

        this.editable.setValue(roomInfo.getRoomPreset().getParent().isEditable() ? "true" : "false");

        algorithmSettingBindableAttribute.setValue(roomInfo.getRoomPreset().getAlgorithmSettingOverride());
        this.algorithmSetting.setValue(new WidgetAbilitySettings(algorithmSettingBindableAttribute));
    }

    @On(functionName = "unlinkUnused")
    public void unlinkUnused() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        ModalConfirm modalMessage = new ModalConfirm("This will unlink all UNUSED precalculations in this room.\nThis operation can not be undone");
        PopupMgr.getPopupMgr(getDomElement()).openPopup(new Modal(300, 200, "Are you sure?", modalMessage, true), (a) -> {
            if (a == null) return;
            if (a == Boolean.TRUE) {
                for (PathfindPrecalculation precalculation : roomInfo.getUnused()) {
                    roomInfo.getRoomPreset().removePrecalculation(precalculation.getId());
                }
                details.setDetailsWidget(null);

                WidgetViewPreset.calculator.submit(() -> {
                    roomInfo.rematchWithRoomPreset();
                    Minecraft.getMinecraft().addScheduledTask(() -> {
                        details.refresh();
                    });
                });
            }
        });
    }
    @On(functionName = "unlinkAll")
    public void unlinkAll() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        ModalConfirm modalMessage = new ModalConfirm("This will unlink *ALL* precalculations in this room.\nThis operation can not be undone\n\nConsider making clone of this preset before continuing");
        PopupMgr.getPopupMgr(getDomElement()).openPopup(new Modal(300, 200, "Are you sure?", modalMessage, true), (a) -> {
            if (a == null) return;
            if (a == Boolean.TRUE) {
                WidgetViewPreset.calculator.submit(() -> {
                    details.setDetailsWidget(null);
                    Set<String> stuff = new HashSet<>(roomInfo.getRoomPreset().getPrecalculations());

                    for (String stuff2 : stuff) {
                        roomInfo.getRoomPreset().removePrecalculation(stuff2);
                    }

                    roomInfo.rematchWithRoomPreset();
                    Minecraft.getMinecraft().addScheduledTask(() -> {
                        details.refresh();
                    });
                });
            }
        });
    }
    @On(functionName = "unlinkUnknown")
    public void unlinkUnknown() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        ModalConfirm modalMessage = new ModalConfirm("This will unlink all UNKNOWN (included in preset but nowhere to be found) precalculations in this room.\nThis operation can not be undone\n\nConsider making clone of this preset before continuing");
        PopupMgr.getPopupMgr(getDomElement()).openPopup(new Modal(300, 200, "Are you sure?", modalMessage, true), (a) -> {
            if (a == null) return;
            if (a == Boolean.TRUE) {
                WidgetViewPreset.calculator.submit(() -> {
                    details.setDetailsWidget(null);
                    Set<String> stuff = new HashSet<>(roomInfo.getMissingPrecalculation());

                    for (String stuff2 : stuff) {
                        roomInfo.getRoomPreset().removePrecalculation(stuff2);
                    }

                    roomInfo.rematchWithRoomPreset();
                    Minecraft.getMinecraft().addScheduledTask(() -> {
                        details.refresh();
                    });
                });
            }
        });
    }
    @On(functionName = "autolink")
    public void autolink() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        WidgetViewPreset.calculator.submit(() -> {
            for (PathfindRequest request : roomInfo.getMissing()) {
                List<PathfindPrecalculation> precalcs = PathfindResultRegistry.getINSTANCE().getsByHash(request.getHash());
                for (PathfindPrecalculation precalc : precalcs) {
                    if (!precalc.getAlgorithmSetting().equals(roomInfo.getRoomPreset().getAlgorithmSetting()))
                        continue;

                    roomInfo.getRoomPreset().addPrecalculation(precalc.getId());
                    break;
                }
            }

            roomInfo.rematchWithRoomPreset();
            Minecraft.getMinecraft().addScheduledTask(() -> {
                details.refresh();
            });
        });
    }

    @On(functionName = "removeOverrideAbilitySettings")
    public void removeOverride() {
        algorithmSettingBindableAttribute.setValue(null);
        this.roomInfo.getRoomPreset().setAlgorithmSettingOverride(null);

        WidgetViewPreset.calculator.submit(() -> {
            roomInfo.rematchWithRoomPreset();
            Minecraft.getMinecraft().addScheduledTask(() -> {
                details.refresh();
            });
        });
    }

    @On(functionName = "editOverrideAbilitySettings")
    public void editOverride() {

        PopupMgr.getPopupMgr(getDomElement()).openPopup(new Modal(400, 300, "Choose New Algorithm Setting Override", new WidgetModalChooseAbilitySettings(), true), (a) -> {
            if (a != null) {
                this.algorithmSettingBindableAttribute.setValue((AlgorithmSetting) a);
                this.roomInfo.getRoomPreset().setAlgorithmSettingOverride((AlgorithmSetting) a);


                WidgetViewPreset.calculator.submit(() -> {
                    roomInfo.rematchWithRoomPreset();
                    Minecraft.getMinecraft().addScheduledTask(() -> {
                        details.refresh();
                    });
                });
            }
        });
    }
}
