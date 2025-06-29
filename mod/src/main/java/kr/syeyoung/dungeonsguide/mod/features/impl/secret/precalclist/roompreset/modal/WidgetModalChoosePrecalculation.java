package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.modal;

import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.gui.xml.data.WidgetList;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculation;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;

import java.util.ArrayList;
import java.util.List;

public class WidgetModalChoosePrecalculation extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "list")
    public final BindableAttribute<List<Widget>> list = new BindableAttribute(WidgetList.class);
    public WidgetModalChoosePrecalculation(List<PathfindPrecalculation> options) {
        super(new ResourceIdentifier("dungeonsguide:gui/features/precalclist/modal_choose_precalculation.gui"));

        List<Widget> optionsList = new ArrayList<>();
        for (PathfindPrecalculation option : options) {
            optionsList.add(new WidgetPrecalcOption(option));
        }
        this.list.setValue(optionsList);
    }

    @On(functionName = "cancel")
    public void cancel() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        PopupMgr.getPopupMgr(getDomElement()).closePopup(null);
    }
}
