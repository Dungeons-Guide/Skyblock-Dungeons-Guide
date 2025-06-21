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

package kr.syeyoung.dungeonsguide.launcher.gui.screen;

import kr.syeyoung.dungeonsguide.launcher.LetsEncrypt;
import kr.syeyoung.dungeonsguide.launcher.LoaderMeta;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.auth.AuthManager;
import kr.syeyoung.dungeonsguide.launcher.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.launcher.guiv2.Widget;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.DomElementRegistry;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.ParsedWidgetConverter;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.data.Parser;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.data.ParserElement;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.data.W3CBackedParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WidgetPrivacyPolicy extends AnnotatedImportOnlyWidget {
    public static final ExecutorService executor = Executors.newSingleThreadExecutor();
    @Bind(variableName = "policy")
    public final BindableAttribute<Widget> policy = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "policyVisibility")
    public final BindableAttribute<String> policyVisibility = new BindableAttribute<>(String.class, "loading");

    @Bind(variableName = "policyVersion")
    public final BindableAttribute<Integer> version = new BindableAttribute<>(Integer.class, 0);

    public WidgetPrivacyPolicy() {
        super(new ResourceLocation("dungeons_guide_loader:gui/privacyPolicy/privacyPolicy.gui"));
        reload0();
    }

    @On(functionName = "accept")
    public void accept() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        AuthManager.getInstance().acceptPrivacyPolicy(version.getValue());
        Minecraft.getMinecraft().displayGuiScreen(null);
    }
    @On(functionName = "deny")
    public void deny() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        Minecraft.getMinecraft().displayGuiScreen(null);
    }


    public void reload0() {
        policyVisibility.setValue("loading");
        executor.submit(() -> {
            try {
                HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(Main.POLICY).openConnection();
                urlConnection.setSSLSocketFactory(LetsEncrypt.LETS_ENCRYPT);
                urlConnection.setRequestProperty("User-Agent", "DungeonsGuideLoader/"+ LoaderMeta.LOADER_VERSION);
                urlConnection.setConnectTimeout(1000);
                urlConnection.setReadTimeout(3000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);

                try (W3CBackedParser parser = new W3CBackedParser(urlConnection.getInputStream())) {
                    ParserElement element = parser.getRootNode();
                    ParsedWidgetConverter converter = DomElementRegistry.obtainConverter(element.getNodeName());
                    Widget w = converter.convert(this, element);
                    policy.setValue(w);
                    policyVisibility.setValue("loaded");
                }
            } catch (Exception e) {
                e.printStackTrace();
                policyVisibility.setValue("failed");
            }
        });
    }
    @On(functionName = "reload")
    public void reload() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        reload0();
    }
}
