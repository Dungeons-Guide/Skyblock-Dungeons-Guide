package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.preset;

import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.PathfindPreset;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.PathfindPresetRegistry;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.WidgetAbilitySettings;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.abilitysettings.WidgetCreateAbilitySettings;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.abilitysettings.modal.WidgetModalChooseAbilitySettings;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.Modal;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.ModalAsk;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.ModalConfirm;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

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
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

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
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

        PathfindPreset preset1 = preset.clone();
        PathfindPresetRegistry.getINSTANCE().register(preset1);

        parent.notifyNew(preset1);
    }


    @On(functionName = "delete")
    public void delete() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

        ModalConfirm modalMessage = new ModalConfirm("Deleting can not be reverted");
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
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

        parent.getPresetList().apply(preset);
    }


    @On(functionName = "editAbilitySettings")
    public void editAbilitySettings() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));


        PopupMgr.getPopupMgr(getDomElement()).openPopup(new Modal(400, 300, "Choose New Default Algorithm Setting", new WidgetModalChooseAbilitySettings(), true), (a) -> {
            if (a != null) {
                this.algorithmSettingBindableAttribute.setValue((AlgorithmSetting) a);
                preset.setAlgorithmSetting((AlgorithmSetting) a);
                this.parent.recalc();
            }
        });
    }

    @On(functionName = "requestMissing")
    public void requestMissing() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

    }


}
