package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq.step1;

import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.auth.AuthManager;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.ApiFetcher;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.FeatureRequestCalculation;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.PathfindPrecalculationRequestSet;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq.WidgetPendingRequestPage;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.Modal;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.ModalMessage;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;

public class WidgetPrecalcStep1 extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "step2calc")
    public final BindableAttribute<Widget> step2calc = new BindableAttribute<>(Widget.class);

    private PathfindPrecalculationRequestSet requestSet;
    private WidgetPendingRequestPage parent;

    public WidgetPrecalcStep1(WidgetPendingRequestPage parent, PathfindPrecalculationRequestSet requestSet) {
        super(new ResourceLocation("dungeonsguide:gui/features/requestcalculation/pendingreq/step1/step1.gui"));
        this.requestSet = requestSet;
        this.parent = parent;
        step2calc.setValue(new WidgetStep2Calc(requestSet));
    }

    @On(functionName = "generateZip")
    public void generate() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);

        try {
            long requiredBytes = requestSet.getCredits() * 2622037L;

            long totalSize = 1024 * 1024 * 1024 + requiredBytes; // 1 GB leeway.
            // usually it results in 52x reduction for zip so....
            totalSize = (long) (totalSize * 1.04); // assuming 25x reduction for zip.

            long usablespace = Files.getFileStore(Main.getConfigDir().toPath()).getUsableSpace();
            if (usablespace < totalSize) {
                throw new IllegalStateException(FileUtils.byteCountToDisplaySize(totalSize) + " of storage required but only " + FileUtils.byteCountToDisplaySize(usablespace) + " available");
            }

        } catch (Exception e) {
            e.printStackTrace();
            String message = e.getMessage();
            PopupMgr.getPopupMgr(getDomElement()).openPopup(new Modal(300, 200, "An Error occured while generating zip", new ModalMessage(message), true), null);
            return;
        }

        this.requestSet.generateZip();
        this.parent.updateStep();
    }

    public static class WidgetStep2Calc extends AnnotatedImportOnlyWidget {
        @Bind(variableName = "remoteEstimate")
        public final BindableAttribute<Widget> remoteEstimate = new BindableAttribute<>(Widget.class);
        @Bind(variableName = "localEstimate")
        public final BindableAttribute<Widget> localEstimate = new BindableAttribute<>(Widget.class);

        private PathfindPrecalculationRequestSet requestSet;
        public WidgetStep2Calc(PathfindPrecalculationRequestSet requestSet) {
            super(new ResourceLocation("dungeonsguide:gui/features/requestcalculation/pendingreq/step1/step2calc.gui"));
            this.requestSet = requestSet;

            remoteEstimate.setValue(new WidgetStep2CalcRemote(requestSet));
            localEstimate.setValue(new WidgetStep2CalcLocal(requestSet));
        }

        public static class WidgetStep2CalcLocal extends AnnotatedImportOnlyWidget {
            @Bind(variableName = "reqCalcUnit")
            public final BindableAttribute<String> reqCalcUnit = new BindableAttribute<>(String.class);
            @Bind(variableName = "estimate")
            public final BindableAttribute<String> estimate = new BindableAttribute<>(String.class);

            private PathfindPrecalculationRequestSet requestSet;

            public WidgetStep2CalcLocal(PathfindPrecalculationRequestSet requestSet) {
                super(new ResourceLocation("dungeonsguide:gui/features/requestcalculation/pendingreq/step1/local.gui"));
                this.requestSet = requestSet;

                this.reqCalcUnit.setValue(requestSet.getCredits()+"");
                this.estimate.setValue(String.format("%.2f", (requestSet.getCredits() * 15) / 60 / 60.0 )+ " h with 16 threads at 3.2GHz");
            }
        }

        public static class WidgetStep2CalcRemote extends AnnotatedImportOnlyWidget {
            @Bind(variableName = "reqCredit")
            public final BindableAttribute<String> reqCalcUnit = new BindableAttribute<>(String.class);
            @Bind(variableName = "currCredit")
            public final BindableAttribute<String> currCredit = new BindableAttribute<>(String.class);
            @Bind(variableName = "purchaseCredit")
            public final BindableAttribute<String> purchaseCredit = new BindableAttribute<>(String.class);
            @Bind(variableName = "estimatedPrice")
            public final BindableAttribute<String> estimatedPrice = new BindableAttribute<>(String.class);
            @Bind(variableName = "estimatedTime")
            public final BindableAttribute<String> estimatedTime = new BindableAttribute<>(String.class);
            @Bind(variableName = "err")
            public final BindableAttribute<String> err = new BindableAttribute<>(String.class);
            private PathfindPrecalculationRequestSet requestSet;

            public WidgetStep2CalcRemote(PathfindPrecalculationRequestSet requestSet) {
                super(new ResourceLocation("dungeonsguide:gui/features/requestcalculation/pendingreq/step1/remote.gui"));
                this.requestSet = requestSet;

                this.reqCalcUnit.setValue(requestSet.getCredits()+"");
                this.currCredit.setValue("Loading...");
                this.purchaseCredit.setValue("Loading...");

                this.estimatedPrice.setValue("Loading...");
                // 9501 in 30 minute
                int seconds = (int) (requestSet.getCredits() / 9090.0 * 2400);
                int minutes = seconds / 60 + 2;
                this.estimatedTime.setValue(minutes+"m ");

                ApiFetcher.ex.submit(this::doReload);
            }


            @On(functionName = "reload")
            public void reload() {
                ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);

                this.err.setValue("");
                this.currCredit.setValue("Loading...");
                this.purchaseCredit.setValue("Loading...");

                this.estimatedPrice.setValue("Loading...");

                ApiFetcher.ex.submit(this::doReload);
            }


            private void doReload() {
                try {
                    JsonObject jsonObject = ApiFetcher.getJsonWithAuth(FeatureRequestCalculation.DOMAIN+"/info", AuthManager.getInstance().getWorkingTokenOrThrow());
                    int tokens = jsonObject.get("credit").getAsInt();

                    this.currCredit.setValue(tokens+"");
                    long purchase = Math.max(0, requestSet.getCredits() - tokens);
                    this.purchaseCredit.setValue(purchase+"");

                    int units = (int) Math.ceil(purchase / 10000.0);

                    this.estimatedPrice.setValue("$"+(units * 2)); // TODO: fetch pricing info too

                } catch (IOException e) {
                    this.err.setValue(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}
