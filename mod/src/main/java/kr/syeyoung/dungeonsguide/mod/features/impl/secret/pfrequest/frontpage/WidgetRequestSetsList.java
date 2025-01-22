package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.frontpage;

import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.PathfindPrecalculationRequestSet;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Column;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class WidgetRequestSetsList extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "requests")
    public final BindableAttribute<List<Widget>> widgetPrecalcReqList = new BindableAttribute(WidgetList.class);
    @Bind(variableName = "requestsApi")
    public final BindableAttribute<Column> columnApi = new BindableAttribute<>(Column.class);

    public WidgetRequestSetsList() {
        super(new ResourceLocation("dungeonsguide:gui/features/requestcalculation/frontpage/precalculationrequestsets.gui"));

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
