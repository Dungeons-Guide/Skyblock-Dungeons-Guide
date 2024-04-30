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

import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Text;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CategoryPageWidget extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "items")
    public final BindableAttribute items = new BindableAttribute<>(WidgetList.class);
    @Bind(variableName = "categories")
    public final BindableAttribute categories = new BindableAttribute<>(WidgetList.class);
    @Bind(variableName = "categoryShow")
    public final BindableAttribute<String > showCategory = new BindableAttribute<>(String.class);
    private String category;

    public CategoryPageWidget(String category) {
        super(new ResourceLocation("dungeonsguide:gui/config/categorypage.gui"));
        items.setValue(buildMenu(category));
        this.category = category;
        List<Widget> widgets;
        categories.setValue(widgets = buildCategory(category));
        showCategory.setValue(widgets.isEmpty() ? "hide" : "show");
    }

    private List<Widget> buildCategory(String category) {
        return FeatureRegistry.getFeaturesByCategory().keySet().stream().filter(a -> a.startsWith(category+"."))
                .map(a -> a.substring(category.length()+1).split("\\.")[0])
                .collect(Collectors.toSet()).stream()
                .map( a -> new CategoryItem(() -> new CategoryPageWidget(category+"."+a), a,
                        FeatureRegistry.getCategoryDescription().getOrDefault(category+"."+a, "idk")))
                .collect(Collectors.toList());
    }

    public List<Widget> buildMenu(String category) {
        return FeatureRegistry.getFeaturesByCategory()
                .getOrDefault(category, Collections.emptyList()).stream().map(
                        a -> new FeatureItem(a)
                ).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CategoryPageWidget that = (CategoryPageWidget) o;

        return category.equals(that.category);
    }

    @Override
    public int hashCode() {
        return category.hashCode();
    }
}
