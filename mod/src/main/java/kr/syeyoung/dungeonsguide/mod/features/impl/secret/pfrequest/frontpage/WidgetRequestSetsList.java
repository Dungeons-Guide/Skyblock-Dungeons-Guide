package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.frontpage;

import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.PathfindPrecalculationRequestSet;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Column;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.data.WidgetList;
import kr.syeyoung.modapi.data.ResourceIdentifier;

import java.util.ArrayList;
import java.util.List;

public class WidgetRequestSetsList extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "requests")
    public final BindableAttribute<List<Widget>> widgetPrecalcReqList = new BindableAttribute(WidgetList.class);
    @Bind(variableName = "requestsApi")
    public final BindableAttribute<Column> columnApi = new BindableAttribute<>(Column.class);

    public WidgetRequestSetsList() {
        super(new ResourceIdentifier("dungeonsguide:gui/features/requestcalculation/frontpage/precalculationrequestsets.gui"));

        loadReqSets();
    }

    private void loadReqSets() {
        List<Widget> um = new ArrayList<>();
        for (PathfindPrecalculationRequestSet pfReqSet : FeatureRegistry.SECRET_PATHFIND_REQUEST.getPathfindPrecalculationRequestSets()) {
            WidgetRequestSet reqSet = new WidgetRequestSet(this, pfReqSet);
            um.add(reqSet);
        }
        widgetPrecalcReqList.setValue(new ArrayList<>(um));
    }

}
