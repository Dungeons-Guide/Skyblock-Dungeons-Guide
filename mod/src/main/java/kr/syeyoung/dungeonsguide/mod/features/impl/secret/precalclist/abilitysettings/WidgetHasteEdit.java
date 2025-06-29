package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.abilitysettings;

import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.data.WidgetList;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class WidgetHasteEdit extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "hastes")
    public final BindableAttribute<List<Widget>> hasteButtons = new BindableAttribute(WidgetList.class);

    private final BindableAttribute<Boolean>[] hasteSelected = new BindableAttribute[5];

    private final BindableAttribute<Integer> currentHaste = new BindableAttribute<>(Integer.class);
    public WidgetHasteEdit(BindableAttribute<Integer> hasteBindableAttribute) {
        super(new ResourceIdentifier("dungeonsguide:gui/features/precalclist/abilityedit/hasteedit.gui"));
        BindableAttribute<Boolean> disabled = new BindableAttribute<>(Boolean.class, false);

        currentHaste.exportTo(hasteBindableAttribute);

        List<Widget> efficiencies = new ArrayList<>();
        for (int i = 0; i < hasteSelected.length; i++) {
            WidgetEfficiencyButton hasteButton = new WidgetEfficiencyButton();
            hasteSelected[i] = new BindableAttribute<>(Boolean.class, hasteBindableAttribute.getValue() == i);
            hasteButton.selected.exportTo(hasteSelected[i]);
            hasteButton.<Boolean>getExportedAttribute("disabled").exportTo(disabled);
            int finalI = i;
            hasteButton.<Runnable>getExportedAttribute("click").exportTo(new BindableAttribute<>(Runnable.class, () -> {
                updateHaste(finalI);
            }));
            hasteButton.<String>getExportedAttribute("text").exportTo(new BindableAttribute<>(String.class, i == 0 ? "X" : String.valueOf(i)));
            efficiencies.add(hasteButton);
        }
        this.hasteButtons.setValue(efficiencies);

    }

    private void updateHaste(int i) {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);

        for (BindableAttribute<Boolean> booleanBindableAttribute : hasteSelected) {
            booleanBindableAttribute.setValue(false);
        }
        hasteSelected[i].setValue(true);

        currentHaste.setValue(i);
    }



}
