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

import kr.syeyoung.dungeonsguide.mod.VersionInfo;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.location2.HUDLocationConfig;
import kr.syeyoung.dungeonsguide.mod.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Navigator;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Text;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainConfigWidget extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "menu")
    public final BindableAttribute menu = new BindableAttribute<>(WidgetList.class);
    @Bind(variableName = "relocate")
    public final BindableAttribute<Widget> relocate = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "version")
    public final BindableAttribute<String> version = new BindableAttribute<>(String.class, VersionInfo.VERSION);

    @Bind(variableName = "mainpage")
    public final BindableAttribute<Widget> mainPage = new BindableAttribute<>(Widget.class, new MainPageWidget());
    public MainConfigWidget() {
        super(new ResourceLocation("dungeonsguide:gui/config/normalconfig.gui"));
        menu.setValue(buildMenu());
        relocate.setValue(new GUIOpenItem("GUI Config", () -> new HUDLocationConfig(null)));
    }

    public List<Widget> buildMenu() {
        return FeatureRegistry.getFeaturesByCategory().keySet()
                .stream().map(a -> a.split("\\.")[0])
                .collect(Collectors.toSet()).stream().map(
                        a -> new MenuItem(a, () -> new CategoryPageWidget(a))
                ).collect(Collectors.toList());
    }

    @On(functionName = "back")
    public void back() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        Navigator.getNavigator(getDomElement()).goBack();
    }
}
