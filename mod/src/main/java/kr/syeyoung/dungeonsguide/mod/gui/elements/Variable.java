package kr.syeyoung.dungeonsguide.mod.gui.elements;

import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.ExportedWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.ImportingWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Export;

import java.lang.invoke.MethodHandle;
import java.util.Collections;
import java.util.List;

public class Variable extends AnnotatedExportOnlyWidget {
    @Export(attributeName = "target")
    public final BindableAttribute<String> target = new BindableAttribute<>(String.class);
    @Export(attributeName = "value")
    public final BindableAttribute<String> value = new BindableAttribute<>(String.class);


    public Variable() {
        value.exportTo(target);
    }

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.emptyList();
    }
}
