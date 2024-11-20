package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist;

import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.PathfindPreset;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.PathfindPresetRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.preset.WidgetPreset;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.preset.WidgetViewPreset;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Column;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/precalclist.gui"));
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
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

        PathfindPreset pathfindPreset = new PathfindPreset();
        PathfindPresetRegistry.getINSTANCE().register(pathfindPreset);
        addPreset(pathfindPreset);
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

    public void update(PathfindPreset preset) {
        for (WidgetPreset widgetPreset : widgetPresetList) {
            if (widgetPreset.getPreset() == preset) {
                widgetPreset.update();
            }
        }
    }
}
