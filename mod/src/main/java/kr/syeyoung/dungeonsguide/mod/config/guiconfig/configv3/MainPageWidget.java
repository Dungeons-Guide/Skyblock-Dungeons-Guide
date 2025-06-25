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

package kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3;

import kr.syeyoung.dungeonsguide.mod.config.guiconfig.location2.HUDLocationConfig;
import kr.syeyoung.dungeonsguide.mod.config.onboarding.OnboardingPage;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.GuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.GlobalHUDScale;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.Modal;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.ModalConfirm;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.gui.xml.data.WidgetList;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

public class MainPageWidget extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "categories")
    public final BindableAttribute categories = new BindableAttribute<>(WidgetList.class);
    public MainPageWidget() {
        super(new ResourceLocation("dungeonsguide:gui/config/mainpage.gui"));
        categories.setValue(buildCategory());
    }


    private List<Widget> buildCategory() {
        return FeatureRegistry.getFeaturesByCategory().keySet().stream().map(a -> a.split("\\.")[0])
                .collect(Collectors.toSet()).stream()
                .map( a -> new CategoryItem(() -> new CategoryPageWidget(a), a,
                        FeatureRegistry.getCategoryDescription().getOrDefault(a, "idk")).triggerSidemenu())
                .collect(Collectors.toList());
    }

    @On(functionName = "guiconfig")
    public void guiConfig() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        Minecraft.getMinecraft().displayGuiScreen(new GuiScreenAdapter(new GlobalHUDScale(new HUDLocationConfig(null)), Minecraft.getMinecraft().currentScreen));
    }
    @On(functionName = "discord")
    public void discord() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        try {
            Desktop.getDesktop().browse(new URI("https://dungeons.guide/discord"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @On(functionName = "github")
    public void github() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/Dungeons-Guide/Skyblock-Dungeons-Guide/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
    @On(functionName = "store")
    public void store() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        try {
            Desktop.getDesktop().browse(new URI("https://store.dungeons.guide/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
    @On(functionName = "modrinth")
    public void modrinth() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        try {
            Desktop.getDesktop().browse(new URI("https://modrinth.com/mod/dungeons-guide"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
    @On(functionName = "docs")
    public void docs() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        try {
            Desktop.getDesktop().browse(new URI("https://docs.dungeons.guide/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @On(functionName = "setupwizard")
    public void setupwizard() {
        ModalConfirm modalMessage = new ModalConfirm("Triggering Setup Wizard can reset some of your configuration.");
        PopupMgr.getPopupMgr(getDomElement()).openPopup(new Modal(300, 200, "Are you sure?", modalMessage, true), (a) -> {
            if (a == null) return;
            if (a == Boolean.TRUE) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiScreenAdapter(new GlobalHUDScale(new OnboardingPage("pages/front.gui")),
                        GuiScreenAdapter.getAdapter(getDomElement()), true));
            }
        });
    }
}
