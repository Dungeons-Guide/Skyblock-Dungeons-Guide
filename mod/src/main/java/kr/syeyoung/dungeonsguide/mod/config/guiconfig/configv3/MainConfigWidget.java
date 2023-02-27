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
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Navigator;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Text;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.SingleChildRenderer;
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

public class MainConfigWidget extends AnnotatedImportOnlyWidget implements Renderer {
    @Bind(variableName = "menu")
    public final BindableAttribute menu = new BindableAttribute<>(WidgetList.class);
    @Bind(variableName = "relocate")
    public final BindableAttribute<Widget> relocate = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "version")
    public final BindableAttribute<String> version = new BindableAttribute<>(String.class, VersionInfo.VERSION);

    @Bind(variableName = "sidebar")
    public final BindableAttribute<String> sidebar = new BindableAttribute<>(String.class, "hide");
    @Bind(variableName = "search")
    public final BindableAttribute<String> search = new BindableAttribute<>(String.class, "");

    @Bind(variableName = "mainpage")
    public final BindableAttribute<Widget> mainPage = new BindableAttribute<>(Widget.class, new MainPageWidget());

    private long doSearch = Long.MAX_VALUE;
    public MainConfigWidget() {
        super(new ResourceLocation("dungeonsguide:gui/config/normalconfig.gui"));
        menu.setValue(buildMenu());
        relocate.setValue(new GUIOpenItem("GUI Config", () -> new HUDLocationConfig(null)));

        search.addOnUpdate((old, neu) -> {
            doSearch = System.currentTimeMillis() + 500;
        });
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

    @On(functionName = "toggleSidebar")
    public void toggle() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        if ("show".equals(this.sidebar.getValue()))
            this.sidebar.setValue("hide");
        else
            this.sidebar.setValue("show");
    }

    @Override
    public void doRender(int absMouseX, int absMouseY, double relMouseX, double relMouseY, float partialTicks, RenderingContext context, DomElement buildContext) {
        if (doSearch < System.currentTimeMillis()) {
            doSearch = Long.MAX_VALUE;

            Navigator navigator = Navigator.getNavigator(getDomElement());
            if (search.getValue().isEmpty()) {
                if (navigator.getCurrent() instanceof SearchPageWidget)
                    navigator.goBack();
            } else {
                if (navigator.getCurrent() instanceof SearchPageWidget)
                    navigator.setPageWithoutPush(new SearchPageWidget(search.getValue()));
                else
                    navigator.openPage(new SearchPageWidget(search.getValue()));
            }
        }

        SingleChildRenderer.INSTANCE.doRender(absMouseX,absMouseY,relMouseX,relMouseY,partialTicks,context,buildContext);
    }
}
