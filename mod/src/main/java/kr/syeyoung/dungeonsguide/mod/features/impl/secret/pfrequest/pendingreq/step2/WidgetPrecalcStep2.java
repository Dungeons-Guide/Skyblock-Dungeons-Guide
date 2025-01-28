package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq.step2;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.auth.AuthManager;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.VersionInfo;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip.WidgetNotificationProgress;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.FeatureRequestCalculation;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.PathfindPrecalculationRequestSet;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq.WidgetPendingRequestPage;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

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
        super(new ResourceLocation("dungeonsguide:gui/features/requestcalculation/pendingreq/step2/step2.gui"));

        this.parent = parent;
        this.requestSet = requestSet;

        receipt.setValue(new WidgetReceipt(this, requestSet.getCredits()));
    }

    public void setPurchaseReq(long calc) {
        purchase.setValue(calc > 0 ? "true" : "false");
    }

    @On(functionName = "purchase")
    public void purchaseCredits() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
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
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

        this.requestSet.createRequest();
        this.parent.updateStep();
    }
}
