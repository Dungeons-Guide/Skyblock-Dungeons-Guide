package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq;

import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.PathfindPrecalculationRequestSet;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.remotereq.WidgetRequestDetails;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Navigator;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import net.minecraft.util.ResourceLocation;

public class WidgetDone extends AnnotatedImportOnlyWidget {
    private PathfindPrecalculationRequestSet requestSet;
    private WidgetPendingRequestPage parent;

    public WidgetDone(WidgetPendingRequestPage parent, PathfindPrecalculationRequestSet requestSet) {
        super(new ResourceLocation("dungeonsguide:gui/features/requestcalculation/pendingreq/done.gui"));
        this.requestSet = requestSet;
        this.parent = parent;
    }

    @On(functionName = "viewProgress")
    public void goCheck() {
        Navigator navigator = Navigator.getNavigator(getDomElement());
        navigator.setPageWithoutPush(FeatureRegistry.SECRET_PATHFIND_REQUEST.getConfigureWidget());
        navigator.openPage(new WidgetRequestDetails(requestSet.getRequestId()));
    }
}
