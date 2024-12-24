package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.abilitysettings;

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Passthrough;
import net.minecraft.util.ResourceLocation;

import java.text.Bidi;

@Passthrough(bindName = "click", exportName = "click", type = Runnable.class)
@Passthrough(bindName = "disabled", exportName = "disabled", type = Boolean.class)
@Passthrough(bindName = "text", exportName = "text", type = String.class)
public class WidgetEfficiencyButton extends AnnotatedWidget {
    @Export(attributeName = "selected")
    public final BindableAttribute<Boolean> selected = new BindableAttribute<>(Boolean.class);

    @Bind(variableName = "backgroundColor")
    public final BindableAttribute<Integer> backgroundColor = new BindableAttribute<Integer>(Integer.class, 0xFF777777);
    public WidgetEfficiencyButton() {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/abilityedit/efficiencyButton.gui"));

        selected.addOnUpdate((old, neu) -> {
            backgroundColor.setValue(neu ? 0xFFDDDD00 : 0xFF777777);
        });
    }
}
