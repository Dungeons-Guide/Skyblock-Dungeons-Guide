package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq.step1;

import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.PathfindPrecalculationRequestSet;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq.WidgetPendingRequestPage;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.lang.ref.WeakReference;

public class WidgetPrecalcStep1Calculating extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "step2calc")
    public final BindableAttribute<Widget> step2calc = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "progress")
    public final BindableAttribute<Widget> progress = new BindableAttribute<>(Widget.class);

    private PathfindPrecalculationRequestSet requestSet;
    private WidgetPendingRequestPage parent;

    public WidgetPrecalcStep1Calculating(WidgetPendingRequestPage parent, PathfindPrecalculationRequestSet requestSet) {
        super(new ResourceLocation("dungeonsguide:gui/features/requestcalculation/pendingreq/step1/calculating.gui"));
        this.requestSet = requestSet;
        this.parent = parent;
        step2calc.setValue(new WidgetPrecalcStep1.WidgetStep2Calc(requestSet));

        progress.setValue(requestSet.getProgressForGui());
        requestSet.setMaybeNotify(new WeakReference<>(this));
    }

    public void notifyDone() {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            parent.updateStep();
        });
    }
}
