package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.frontpage;

import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.PathfindPrecalculationRequestSet;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq.WidgetPendingRequestPage;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Navigator;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;

public class WidgetRequestSet extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "backgroundColor")
    public final BindableAttribute<Integer> backgroundColor = new BindableAttribute<>(Integer.class);

    @Bind(variableName = "name")
    public final BindableAttribute<String> name = new BindableAttribute<>(String.class);

    private WidgetRequestSetsList parent;
    private PathfindPrecalculationRequestSet requestSet;

    public WidgetRequestSet(WidgetRequestSetsList parent, PathfindPrecalculationRequestSet requestSet) {
        super(new ResourceIdentifier("dungeonsguide:gui/features/requestcalculation/frontpage/precalcrequestset.gui"));

        backgroundColor.setValue(requestSet.getStatus() == PathfindPrecalculationRequestSet.Status.PENDING ? 0xFF505050 :
                requestSet.getStatus() == PathfindPrecalculationRequestSet.Status.GENERATING_ZIP || requestSet.getStatus() == PathfindPrecalculationRequestSet.Status.CREATING_UPLOADING_REQUEST ? 0xFF575600 :
                        requestSet.getStatus() == PathfindPrecalculationRequestSet.Status.WAITING_FOR_USER ? 0xff065702 : 0xFF111111);

        name.setValue(requestSet.getName());

        this.parent = parent;
        this.requestSet = requestSet;
    }

    @Override
    public void onMount() {

        backgroundColor.setValue(requestSet.getStatus() == PathfindPrecalculationRequestSet.Status.PENDING ? 0xFF505050 :
                requestSet.getStatus() == PathfindPrecalculationRequestSet.Status.GENERATING_ZIP || requestSet.getStatus() == PathfindPrecalculationRequestSet.Status.CREATING_UPLOADING_REQUEST ? 0xFF575600 :
                        requestSet.getStatus() == PathfindPrecalculationRequestSet.Status.WAITING_FOR_USER ? 0xff065702 : 0xFF111111);

    }


    @On(functionName = "view")
    public void view() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);

        // open new gui or smth

        requestSet.setSeen(true);
        backgroundColor.setValue(requestSet.getStatus() == PathfindPrecalculationRequestSet.Status.PENDING ? 0xFF505050 :
                requestSet.getStatus() == PathfindPrecalculationRequestSet.Status.GENERATING_ZIP || requestSet.getStatus() == PathfindPrecalculationRequestSet.Status.CREATING_UPLOADING_REQUEST ? 0xFF575600 :
                requestSet.getStatus() == PathfindPrecalculationRequestSet.Status.WAITING_FOR_USER ? 0xff065702 : 0xFF111111);

        Navigator.getNavigator(getDomElement()).openPage(new WidgetPendingRequestPage(requestSet));
    }
}
