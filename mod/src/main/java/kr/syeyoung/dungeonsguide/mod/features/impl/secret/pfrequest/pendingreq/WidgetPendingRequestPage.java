package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq;

import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.PathfindPrecalculationRequestSet;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq.step1.WidgetPrecalcStep1;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq.step1.WidgetPrecalcStep1Calculating;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq.step2.WidgetPrecalcStep2;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq.step2.WidgetStep2Uploading;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import net.minecraft.util.ResourceLocation;

public class WidgetPendingRequestPage extends AnnotatedImportOnlyWidget {
    private PathfindPrecalculationRequestSet requestSet;

    @Bind(variableName = "precalcReqSetPreview")
    public final BindableAttribute<Widget> precalcReqSetPreview = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "metadata")
    public final BindableAttribute<Widget> metadata = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "currentStep")
    public final BindableAttribute<Widget> currentStep = new BindableAttribute<>(Widget.class);


    public WidgetPendingRequestPage(PathfindPrecalculationRequestSet requestSet) {
        super(new ResourceLocation("dungeonsguide:gui/features/requestcalculation/pendingreq/page.gui"));

        this.requestSet = requestSet;

        this.precalcReqSetPreview.setValue(new WidgetPrecalcReqList(requestSet));
        this.metadata.setValue(new WidgetPrecalcReqSetMetadata(requestSet));

        updateStep();
    }

    public void updateStep() {
        switch (requestSet.getStatus()) {
            case PENDING:
                this.currentStep.setValue(new WidgetPrecalcStep1(this, requestSet));
                return;
            case GENERATING_ZIP:
                this.currentStep.setValue(new WidgetPrecalcStep1Calculating(this, requestSet));
                return;
            case WAITING_FOR_USER:
                this.currentStep.setValue(new WidgetPrecalcStep2(this, requestSet));
                return;
            case CREATING_UPLOADING_REQUEST:
                this.currentStep.setValue(new WidgetStep2Uploading(this, requestSet));
                return;
            case DONE:
                this.currentStep.setValue(new WidgetDone(this, requestSet));
                return;
        }
    }
}
