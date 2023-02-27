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
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.GuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.GlobalHUDScale;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
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
                        FeatureRegistry.getCategoryDescription().getOrDefault(a, "idk")))
                .collect(Collectors.toList());
    }

    @On(functionName = "guiconfig")
    public void guiConfig() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        Minecraft.getMinecraft().displayGuiScreen(new GuiScreenAdapter(new GlobalHUDScale(new HUDLocationConfig(null)), Minecraft.getMinecraft().currentScreen));
    }
    @On(functionName = "discord")
    public void discord() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        try {
            Desktop.getDesktop().browse(new URI("https://discord.gg/VuxayCWGE8"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @On(functionName = "github")
    public void github() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/Dungeons-Guide/Skyblock-Dungeons-Guide/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
    @On(functionName = "store")
    public void store() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        try {
            Desktop.getDesktop().browse(new URI("https://store.dungeons.guide/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
