package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.abilitysettings;

import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Export;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Passthrough;
import kr.syeyoung.modapi.data.ResourceIdentifier;

@Passthrough(exportName = "disabled", bindName = "disabled", type = Boolean.class)
@Passthrough(exportName = "click", bindName = "click", type = Runnable.class)
public class WidgetIconButton extends AnnotatedWidget {
    @Bind(variableName = "x")
    public final BindableAttribute<Integer> x = new BindableAttribute<>(Integer.class);
    @Bind(variableName = "y")
    public final BindableAttribute<Integer> y = new BindableAttribute<>(Integer.class);
    @Bind(variableName = "hoverX")
    public final BindableAttribute<Integer> hoverX = new BindableAttribute<>(Integer.class);
    @Bind(variableName = "hoverY")
    public final BindableAttribute<Integer> hoverY = new BindableAttribute<>(Integer.class);
    @Bind(variableName = "clickX")
    public final BindableAttribute<Integer> clickX = new BindableAttribute<>(Integer.class);
    @Bind(variableName = "clickY")
    public final BindableAttribute<Integer> clickY = new BindableAttribute<>(Integer.class);
    @Bind(variableName = "disableX")
    public final BindableAttribute<Integer> disableX = new BindableAttribute<>(Integer.class);
    @Bind(variableName = "disableY")
    public final BindableAttribute<Integer> disableY = new BindableAttribute<>(Integer.class);

    @Export(attributeName = "selected")
    public final BindableAttribute<Boolean> selected = new BindableAttribute<>(Boolean.class, false);
    @Export(attributeName = "iconIdx")
    public final BindableAttribute<Integer> iconIdx = new BindableAttribute<>(Integer.class, 0);
    @Export(attributeName = "iconOffset")
    public final BindableAttribute<Integer> iconOffset = new BindableAttribute<>(Integer.class, 0);

    public WidgetIconButton() {
        super(new ResourceIdentifier("dungeonsguide:gui/features/precalclist/abilityedit/iconButton.gui"));
        iconIdx.addOnUpdate((a,b) -> onUpdate());
        selected.addOnUpdate((a,b) -> onUpdate());
        iconOffset.addOnUpdate((a,b) -> onUpdate());
        onUpdate();
    }

    private void onUpdate() {
        int iconIdx = this.iconIdx.getValue() + iconOffset.getValue();
        boolean selected = this.selected.getValue();

        if (selected) {
            x.setValue((iconIdx % 8) * 32);
            y.setValue((iconIdx / 8) * 32);
        } else {
            x.setValue((iconIdx % 8) * 32 + 256);
            y.setValue((iconIdx / 8) * 32);
        }
        hoverX.setValue((iconIdx % 8) * 32);
        hoverY.setValue((iconIdx / 8) * 32 + 256);

        clickX.setValue((iconIdx % 8) * 32 + 256);
        clickY.setValue((iconIdx / 8) * 32 + 256);


        disableX.setValue((iconIdx % 8) * 32 + 256);
        disableY.setValue((iconIdx / 8) * 32);
    }
}
