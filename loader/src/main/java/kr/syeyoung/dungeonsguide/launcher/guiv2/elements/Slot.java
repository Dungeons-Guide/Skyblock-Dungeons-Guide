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

package kr.syeyoung.dungeonsguide.launcher.guiv2.elements;

import kr.syeyoung.dungeonsguide.launcher.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.launcher.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.launcher.guiv2.Widget;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.annotations.Export;

import java.util.Collections;
import java.util.List;

public class Slot extends AnnotatedExportOnlyWidget {
    @Export(attributeName = "child")
    public final BindableAttribute<Widget> replacement = new BindableAttribute<>(Widget.class);
    @Export(attributeName = "_")
    public final BindableAttribute<Widget> original = new BindableAttribute<>(Widget.class);

    public Slot() {
        replacement.addOnUpdate(this::update);
        original.addOnUpdate(this::update);
    }

    private void update(Widget widget, Widget widget1) {
        if (this.getDomElement().getParent() == null) return;
        if (!this.getDomElement().getChildren().isEmpty())
            getDomElement().removeElement(getDomElement().getChildren().get(0));

        DomElement domElement = null;
        if (replacement.getValue() != null) domElement = replacement.getValue().createDomElement(getDomElement());
        else if (original.getValue() != null) domElement = original.getValue().createDomElement(getDomElement());

        if (domElement != null)
            getDomElement().addElement(domElement);
    }


    @Override
    public List<Widget> build(DomElement buildContext) {
        if (replacement.getValue() != null) return Collections.singletonList(replacement.getValue());
        if (original.getValue() != null) return Collections.singletonList(original.getValue());
        return Collections.EMPTY_LIST;
    }
}
