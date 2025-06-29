package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq.step2;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.PathfindPrecalculationRequestSet;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq.WidgetPendingRequestPage;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.modapi.data.ResourceIdentifier;

import java.lang.ref.WeakReference;

public class WidgetStep2Uploading extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "progress")
    public final BindableAttribute<Widget> progress = new BindableAttribute<>(Widget.class);

    private PathfindPrecalculationRequestSet requestSet;
    private WidgetPendingRequestPage parent;

    public WidgetStep2Uploading(WidgetPendingRequestPage parent, PathfindPrecalculationRequestSet requestSet) {
        super(new ResourceIdentifier("dungeonsguide:gui/features/requestcalculation/pendingreq/step2/calculating.gui"));
        this.requestSet = requestSet;
        this.parent = parent;
        progress.setValue(requestSet.getProgressForGui());
        requestSet.setMaybeNotify2(new WeakReference<>(this));
    }

    public void notifyDone() {
        DungeonsGuide.getDungeonsGuide().runNextTick(() -> {
            parent.updateStep();
        });
    }
}
