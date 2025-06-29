package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq.step2;

import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.PathfindPrecalculationRequestSet;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq.WidgetPendingRequestPage;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class WidgetPrecalcStep2 extends AnnotatedImportOnlyWidget {


    private WidgetPendingRequestPage parent;
    private PathfindPrecalculationRequestSet requestSet;

    @Bind(variableName = "receipt")
    public final BindableAttribute<Widget> receipt = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "purchase")
    public final BindableAttribute<String> purchase = new BindableAttribute<>(String.class, "loading");

    @Bind(variableName = "err")
    public final BindableAttribute<String> err = new BindableAttribute<>(String.class, "");

    public WidgetPrecalcStep2(WidgetPendingRequestPage parent, PathfindPrecalculationRequestSet requestSet) {
        super(new ResourceIdentifier("dungeonsguide:gui/features/requestcalculation/pendingreq/step2/step2.gui"));

        this.parent = parent;
        this.requestSet = requestSet;

        receipt.setValue(new WidgetReceipt(this, requestSet.getCredits()));
    }

    public void setPurchaseReq(long calc) {
        purchase.setValue(calc > 0 ? "true" : "false");
    }

    @On(functionName = "purchase")
    public void purchaseCredits() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        try {
            Desktop.getDesktop().browse(new URL("https://store.dungeons.guide/category/pathfinding").toURI());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @On(functionName = "request")
    public void actuallyRequest() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);

        this.requestSet.createRequest();
        this.parent.updateStep();
    }

    @On(functionName = "openGuide")
    public void openGuide() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);

        try {
            Desktop.getDesktop().browse(new URI("https://docs.dungeons.guide/docs/pathfinding/precalculation/introduction"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @On(functionName = "openDir")
    public void openDir() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);

        File f = this.requestSet.getZipFile();
        try {
            Desktop.getDesktop().browse(f.getParentFile().toURI());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
