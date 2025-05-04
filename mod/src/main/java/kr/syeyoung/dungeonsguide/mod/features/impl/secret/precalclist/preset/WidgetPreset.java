package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.preset;

import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.PathfindPreset;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.WidgetPrecalcList;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

public class WidgetPreset extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "backgroundColor")
    public final BindableAttribute<Integer> backgroundColor = new BindableAttribute<>(Integer.class);


    @Bind(variableName = "buttonTxt")
    public final BindableAttribute<String> buttonTxt = new BindableAttribute<>(String.class);

    @Bind(variableName = "name")
    public final BindableAttribute<String> name = new BindableAttribute<>(String.class);

    private WidgetPrecalcList presetList;
    @Getter
    private PathfindPreset preset;

    public WidgetPreset(PathfindPreset preset, WidgetPrecalcList presetList) {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/preset.gui"));
        this.name.setValue(preset.getPresetName());
        this.backgroundColor.setValue(preset.getPresetId().equals(FeatureRegistry.SECRET_PRECALC_LIST.getSelectedPresetId()) ? 0xFF005756 : 0xFF505050);
        this.buttonTxt.setValue(preset.isEditable() ? "Edit/View" : "View");
        this.presetList = presetList;
        this.preset = preset;
    }

    public void setSelected(boolean selected) {
        this.backgroundColor.setValue(selected ? 0xff065702 : preset.getPresetId().equals(FeatureRegistry.SECRET_PRECALC_LIST.getSelectedPresetId()) ? 0xFF005756 : 0xFF505050); // #507750
    }

    @On(functionName = "edit")
    public void edit() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

        this.presetList.edit(this);
    }

    public void update() {
        this.buttonTxt.setValue(preset.isEditable() ? "Edit/View" : "View");
        this.name.setValue(preset.getPresetName());
    }
}
