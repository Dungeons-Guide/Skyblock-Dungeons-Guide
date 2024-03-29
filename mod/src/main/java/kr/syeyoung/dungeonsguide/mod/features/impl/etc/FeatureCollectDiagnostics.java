/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.features.impl.etc;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.launcher.LetsEncrypt;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.auth.AuthManager;
import kr.syeyoung.dungeonsguide.launcher.gui.screen.GuiDisplayer;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.VersionInfo;
import kr.syeyoung.dungeonsguide.mod.WidgetUpdateLog;
import kr.syeyoung.dungeonsguide.mod.config.types.TCBoolean;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.GuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Scaler;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FeatureCollectDiagnostics extends SimpleFeature {
    private final Map<String, Long> lastSent = new HashMap<>();
    public FeatureCollectDiagnostics() {
        super("Misc", "Collect Error Logs", "Enable to allow sending mod errors to developers server\n\nThis option sends Stacktraces to developers server\n\nDisable to opt out of it", "misc.diagnostics_logcollection", false);
        addParameter("prompted", new FeatureParameter<Boolean>("prompted", "Was this prompted?", "Did this feature prompt for user apporval yet?", false, TCBoolean.INSTANCE));
    }

    public class WidgetUserApproval extends AnnotatedImportOnlyWidget {
        public WidgetUserApproval() {
            super(new ResourceLocation("dungeonsguide:gui/collect_diagnostic_approval.gui"));
        }

        @On(functionName = "approve")
        public void onApprove() {
            FeatureCollectDiagnostics.this.<Boolean>getParameter("prompted").setValue(true);
            FeatureCollectDiagnostics.this.setEnabled(true);
            Minecraft.getMinecraft().displayGuiScreen(null);
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        }

        @On(functionName = "deny")
        public void onDeny() {
            FeatureCollectDiagnostics.this.<Boolean>getParameter("prompted").setValue(true);
            FeatureCollectDiagnostics.this.setEnabled(false);
            Minecraft.getMinecraft().displayGuiScreen(null);
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        }
    }

    @Override
    public void loadConfig(JsonObject jsonObject) {
        super.loadConfig(jsonObject);
        if (!this.<Boolean>getParameter("prompted").getValue()) {
            Scaler scaler = new Scaler();
            scaler.scale.setValue((double) new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor());
            scaler.child.setValue(new WidgetUserApproval());
            GuiDisplayer.INSTANCE.displayGui(new GuiScreenAdapter(scaler, null, false));
        }
    }

    public static final Executor executorService = Executors
            .newSingleThreadExecutor(new ThreadFactoryBuilder()
                    .setThreadFactory(DungeonsGuide.THREAD_FACTORY)
                    .setNameFormat("DG-Error-Reporter-%d").build());

    public static void queueSendLogAsync(Throwable t) {
        executorService.execute(() -> {
            try {
                FeatureRegistry.COLLECT_ERRORS.sendLogActually(t);
            } catch (Exception ignored) {ignored.printStackTrace();}
        });
    }

    private void sendLogActually(Throwable t) throws IOException {
        if (!isEnabled()) return;
        String token = AuthManager.getInstance().getWorkingTokenOrThrow(); // this require privacy policy.

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        t.printStackTrace(pw);

        String trace = sw.toString();

        if (lastSent.getOrDefault(trace, 0L) + 60*1000 > System.currentTimeMillis()) { // don't send same thing for 1m
            return;
        }
        lastSent.put(trace, System.currentTimeMillis());

        HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(Main.DOMAIN+"/logging/stacktrace").openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        urlConnection.setSSLSocketFactory(LetsEncrypt.LETS_ENCRYPT);
        urlConnection.setRequestProperty("User-Agent", "DungeonsGuide/"+ VersionInfo.VERSION);
        urlConnection.setConnectTimeout(10000);
        urlConnection.setReadTimeout(10000);
        urlConnection.setRequestProperty("Authorization", "Bearer "+token);
        urlConnection.getOutputStream().write(trace.getBytes(StandardCharsets.UTF_8));
        int code = urlConnection.getResponseCode(); // make sure to send req actually
    }


}
