/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class PopupMgr extends AnnotatedWidget {
    public PopupMgr() {
        super(new ResourceLocation("dungeonsguide:gui/elements/popupmgr.gui"));
    }
    // just stack

    @Bind(variableName = "stackRef")
    public final BindableAttribute<DomElement> domElementBindableAttribute = new BindableAttribute<>(DomElement.class);


    @Export(attributeName = "_")
    @Bind(variableName = "_")
    public final BindableAttribute<Widget> child = new BindableAttribute<>(Widget.class);


    @Override
    public void onMount() {
        super.onMount();
        getDomElement().getContext().CONTEXT.put("popup", this);
    }

    @Override
    public void onUnmount() {
        domElementBindableAttribute.getValue().getChildren().clear();
        super.onUnmount();
    }

    public static PopupMgr getPopupMgr(DomElement buildContext) {
        return buildContext.getContext().getValue(PopupMgr.class, "popup");
    }


    private Map<DomElement, Consumer<Object>> callbacks = new HashMap<>();
    public void openPopup(Widget element, Consumer<Object> callback) {
        DomElement domElement = element.createDomElement(getDomElement());
        domElementBindableAttribute.getValue().addElementFirst(domElement);
        callbacks.put(domElement, callback);
    }

    public void closePopup(Object value) {
        DomElement target = domElementBindableAttribute.getValue().getChildren().get(0);
        domElementBindableAttribute.getValue().removeElement(
                target
        );

        Consumer<Object> callback = callbacks.remove(target);
        if (callback != null)
            callback.accept(value);
    }

    public void closePopup(Widget widget, Object value) {
        domElementBindableAttribute.getValue().removeElement(widget.getDomElement());

        Consumer<Object> callback = callbacks.get(widget.getDomElement());
        if (callback != null)
            callback.accept(value);
    }
}
