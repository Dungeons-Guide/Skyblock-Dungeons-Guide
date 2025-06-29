package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.modal;

import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.details.WidgetPathfindResultDetails;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculation;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;

public class WidgetPrecalcOption extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "precalculation")
    public final BindableAttribute<Widget> widgetBindableAttribute = new BindableAttribute<>(Widget.class);

    PathfindPrecalculation precalculation;
    public WidgetPrecalcOption(PathfindPrecalculation precalculation) {
        super(new ResourceIdentifier("dungeonsguide:gui/features/precalclist/choose_precalculation_dummy.gui"));

        this.precalculation = precalculation;
        this.widgetBindableAttribute.setValue(new WidgetPathfindResultDetails(
                precalculation,
                null
        ));
    }

    @On(functionName = "link")
    public void link() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        PopupMgr.getPopupMgr(getDomElement()).closePopup(precalculation);
    }
}
