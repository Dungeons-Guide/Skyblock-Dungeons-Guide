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
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Navigator;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.SingleChildRenderer;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.gui.xml.data.WidgetList;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;

import java.util.List;
import java.util.stream.Collectors;

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
        super(new ResourceIdentifier("dungeonsguide:gui/config/normalconfig.gui"));
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

    @Override
    public void onMount() {
        super.onMount();
        getDomElement().getContext().CONTEXT.put("mainconfig", this);
    }

    @On(functionName = "back")
    public void back() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        Navigator.getNavigator(getDomElement()).goBack();
    }

    @On(functionName = "toggleSidebar")
    public void toggle() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        if ("show".equals(this.sidebar.getValue()))
            this.sidebar.setValue("hide");
        else
            this.sidebar.setValue("show");
    }

    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
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

        SingleChildRenderer.INSTANCE.doRender(partialTicks,context,buildContext);
    }
}
