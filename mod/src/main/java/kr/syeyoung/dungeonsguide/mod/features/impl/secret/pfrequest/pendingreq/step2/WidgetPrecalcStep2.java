package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq.step2;

import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.PathfindPrecalculationRequestSet;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq.WidgetPendingRequestPage;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import net.minecraft.util.ResourceLocation;

public class WidgetPrecalcStep2 extends AnnotatedImportOnlyWidget {

    private WidgetPendingRequestPage parent;
    private PathfindPrecalculationRequestSet requestSet;
    public WidgetPrecalcStep2(WidgetPendingRequestPage parent, PathfindPrecalculationRequestSet requestSet) {
        super(new ResourceLocation("dungeonsguide:gui/features/requestcalculation/pendingreq/step2/step2.gui"));

        this.parent = parent;
        this.requestSet = requestSet;
    }
}
