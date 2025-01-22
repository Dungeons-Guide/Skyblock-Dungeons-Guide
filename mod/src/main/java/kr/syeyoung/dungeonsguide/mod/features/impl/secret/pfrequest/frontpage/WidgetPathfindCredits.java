package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.frontpage;

import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.launcher.auth.AuthManager;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.ApiFetcher;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

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
        super(new ResourceLocation("dungeonsguide:gui/features/requestcalculation/frontpage/pathfindcredits.gui"));

        reload();
    }

    private void doReload() {
        try {
            JsonObject jsonObject = ApiFetcher.getJsonWithAuth("https://pathfind.dungeons.guide/info", AuthManager.getInstance().getWorkingTokenOrThrow());
            credits.setValue(jsonObject.get("token").getAsInt()+" tokens");
        } catch (IOException e) {
            this.err.setValue(e.getMessage());
            e.printStackTrace();
        }
    }

    @On(functionName = "reload")
    public void reload() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        credits.setValue("Loading...");
        ApiFetcher.ex.submit(this::doReload);
    }


    @On(functionName = "purchaseCredits")
    public void purchaseCredits() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        try {
            Desktop.getDesktop().browse(new URL("https://pathfind.dungeons.guide/purchase?uuid="+Minecraft.getMinecraft().getSession().getProfile().getId()).toURI());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


}
