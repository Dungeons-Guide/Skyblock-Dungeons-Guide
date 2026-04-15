package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.frontpage;

import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.ApiFetcher;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.FeatureRequestCalculation;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class WidgetPathfindCredits extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "credits")
    public final BindableAttribute<String> credits = new BindableAttribute<>(String.class, "Loading...");
    @Bind(variableName = "error")
    public final BindableAttribute<String> err = new BindableAttribute<>(String.class, "");


    public WidgetPathfindCredits() {
        super(new ResourceIdentifier("dungeonsguide:gui/features/requestcalculation/frontpage/pathfindcredits.gui"));

        reload();
    }

    private void doReload() {
        try {
            JsonObject jsonObject = ApiFetcher.getJsonWithAuth(FeatureRequestCalculation.DOMAIN+"/info", DungeonsGuide.getDungeonsGuide().getAuthManager().getWorkingTokenOrThrow());
            credits.setValue(jsonObject.get("credit").getAsInt()+" credits");
        } catch (IOException e) {
            this.err.setValue(e.getMessage());
            e.printStackTrace();
        }
    }

    @On(functionName = "reload")
    public void reload() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        credits.setValue("Loading...");
        ApiFetcher.ex.submit(this::doReload);
    }


    @On(functionName = "purchaseCredits")
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


}
