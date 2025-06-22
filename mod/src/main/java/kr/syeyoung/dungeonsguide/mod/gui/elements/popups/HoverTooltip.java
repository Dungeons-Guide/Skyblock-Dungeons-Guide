package kr.syeyoung.dungeonsguide.mod.gui.elements.popups;

import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Export;

import java.util.Collections;
import java.util.List;

public class HoverTooltip extends AnnotatedExportOnlyWidget {

    @Export(attributeName = "_")
    public final BindableAttribute<Widget> child = new BindableAttribute<>(Widget.class);
    @Export(attributeName = "_tooltip")
    public final BindableAttribute<Widget> actualTooltip = new BindableAttribute<>(Widget.class);

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.singletonList(child.getValue());
    }


    private MouseTooltip tooltip = null;

    @Override
    public void mouseEntered(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        if (this.tooltip == null) {
            PopupMgr.getPopupMgr(getDomElement())
                    .openPopup(this.tooltip = new MouseTooltip(actualTooltip.getValue()), (a) -> {
                        this.tooltip = null;
                    });
        }
    }

    @Override
    public void mouseExited(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        if (this.tooltip != null) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(this.tooltip, null);
            this.tooltip = null;
        }
    }

    @Override
    public void onUnmount() {
        if (this.tooltip != null) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(this.tooltip, null);
            this.tooltip = null;
        }
        super.onUnmount();
    }
}
