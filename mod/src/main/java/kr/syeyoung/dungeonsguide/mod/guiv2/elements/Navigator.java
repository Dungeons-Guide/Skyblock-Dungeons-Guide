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

package kr.syeyoung.dungeonsguide.mod.guiv2.elements;

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Navigator extends AnnotatedExportOnlyWidget {
    public Stack<Widget> widgets = new Stack<>();

    @Export(attributeName = "_")
    public final BindableAttribute<Widget> child = new BindableAttribute<>(Widget.class);

    public Widget current;

    public Navigator() {
        child.addOnUpdate((old, a) -> {
            if (current == null) current = a;
        });
    }

    public void openPage(Widget widget) {
        widgets.push(current);
        getDomElement().removeElement(getDomElement().getChildren().get(0));
        getDomElement().addElement(widget.createDomElement(getDomElement()));
        current = widget;
    }

    public void goBack() {
        getDomElement().removeElement(getDomElement().getChildren().get(0));
        Widget page;
        if (widgets.isEmpty())  page = child.getValue();
        else  page = widgets.pop();
        getDomElement().addElement(page.createDomElement(getDomElement()));
        current = page;
    }

    public static Navigator getNavigator(DomElement element) {
        return element.getContext().getValue(Navigator.class, "navigator");
    }

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.singletonList(current);
    }

    @Override
    public void onMount() {
        getDomElement().getContext().CONTEXT.put("navigator", this);
    }

    @Override
    public void onUnmount() {
        getDomElement().getContext().CONTEXT.remove("navigator");
    }
}
