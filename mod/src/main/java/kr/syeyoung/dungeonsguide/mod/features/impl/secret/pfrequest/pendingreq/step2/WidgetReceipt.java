package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq.step2;

import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.ApiFetcher;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.FeatureRequestCalculation;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Column;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;

import java.util.ArrayList;
import java.util.List;

public class WidgetReceipt extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "reqCredits")
    public final BindableAttribute<String> requiredCredits = new BindableAttribute<>(String.class);
    @Bind(variableName = "purchaseCredits")
    public final BindableAttribute<String> purchaseCredits = new BindableAttribute<>(String.class);
    @Bind(variableName = "receiptApi")
    public final BindableAttribute<Column> receiptApi = new BindableAttribute<>(Column.class);
    @Bind(variableName = "err")
    public final BindableAttribute<String> err = new BindableAttribute<>(String.class);


    private long reqCredits;
    private WidgetPrecalcStep2 step2;
    public WidgetReceipt(WidgetPrecalcStep2 step2, long requiredCredits) {
        super(new ResourceIdentifier("dungeonsguide:gui/features/requestcalculation/pendingreq/step2/receipt.gui"));
        this.requiredCredits.setValue(requiredCredits+"");
        this.reqCredits = requiredCredits;
        this.purchaseCredits.setValue("Loading...");
        this.step2 = step2;

        ApiFetcher.ex.submit(this::doReload);
    }

    @On(functionName = "reload")
    public void reload() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);

        this.err.setValue("");
        this.receiptApi.getValue().removeAllWidget();
        this.purchaseCredits.setValue("Loading...");

        ApiFetcher.ex.submit(this::doReload);
    }

    public void doReload() {
        try {
            JsonObject jsonObject = ApiFetcher.getJsonWithAuth(FeatureRequestCalculation.DOMAIN + "/info", DungeonsGuide.getDungeonsGuide().getAuthManager().getWorkingTokenOrThrow());

            long calc = reqCredits;


            List<Widget> toAdd = new ArrayList<>();
            {
                long balance = jsonObject.get("credit").getAsLong();
                toAdd.add(new WidgetReceiptItem("Current Balance", balance + ""));
                calc -= balance;
            }

            if (calc < 0) calc = 0;

            this.purchaseCredits.setValue(calc + "");
            if (this.receiptApi.getValue() != null) {
                this.receiptApi.getValue().removeAllWidget();
                for (Widget widget : toAdd) {
                    this.receiptApi.getValue().addWidget(widget);
                }
            }

            step2.setPurchaseReq(calc);
        } catch (Exception e) {
            this.err.setValue(e.getMessage());
            e.printStackTrace();
        }
    }




    public static class WidgetReceiptItem extends AnnotatedImportOnlyWidget {
        @Bind(variableName = "item")
        public final BindableAttribute<String> item = new BindableAttribute<>(String.class);
        @Bind(variableName = "credit")
        public final BindableAttribute<String> credit = new BindableAttribute<>(String.class);

        public WidgetReceiptItem(String item, String credit) {
            super(new ResourceIdentifier("dungeonsguide:gui/features/requestcalculation/pendingreq/step2/receiptitem.gui"));
            this.item.setValue(item);
            this.credit.setValue(credit);
        }
    }
}
