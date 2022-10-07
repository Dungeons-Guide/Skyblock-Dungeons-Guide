package kr.syeyoung.dungeonsguide;

import com.google.common.base.Throwables;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static kr.syeyoung.dungeonsguide.Main.SERVER_URL;

public class YoMamaOutdated {

    Logger logger = LogManager.getLogger("YoMamaOutdated");

    boolean isUsingOutdatedDg = false;
    String outdatedMessage;
    public YoMamaOutdated() {
        this.check();
    }

    void check(){
        try {
            HttpURLConnection httpsURLConnection = (HttpsURLConnection) new URL(SERVER_URL + "/outdated").openConnection();
            httpsURLConnection.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
            httpsURLConnection.setRequestProperty("Content-Type", "application/json");
            httpsURLConnection.setRequestMethod("GET");
            httpsURLConnection.setDoInput(true);
            httpsURLConnection.setDoOutput(true);

            httpsURLConnection.connect();

            int code = httpsURLConnection.getResponseCode();

            if(code == 200){
                isUsingOutdatedDg = true;
                outdatedMessage = IOUtils.toString(httpsURLConnection.getInputStream());
            }

            httpsURLConnection.disconnect();

        } catch (IOException e) {
            logger.error("Outdated check failed with message: {}", String.valueOf(Throwables.getRootCause(e)));
        }
    }

    private boolean showedError = true;

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if(!showedError && isUsingOutdatedDg){
            showedError = true;
            event.gui = new GuiLoadingError(null, outdatedMessage, event.gui);
        }
    }

}
