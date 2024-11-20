package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.modal;

import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.PathfindPrecalculation;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class WidgetModalChoosePrecalculation extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "list")
    public final BindableAttribute<List<Widget>> list = new BindableAttribute(WidgetList.class);
    public WidgetModalChoosePrecalculation(List<PathfindPrecalculation> options) {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/modal_choose_precalculation.gui"));

        List<Widget> optionsList = new ArrayList<>();
        for (PathfindPrecalculation option : options) {
            optionsList.add(new WidgetPrecalcOption(option));
        }
        this.list.setValue(optionsList);
    }

    @On(functionName = "cancel")
    public void cancel() {
        PopupMgr.getPopupMgr(getDomElement()).closePopup(null);
    }
}
