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

import kr.syeyoung.dungeonsguide.mod.guiv2.*;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.SingleChildPassingLayouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.OnlyChildrenRenderer;
import net.minecraft.util.ResourceLocation;

public class PopupMgr extends Controller {
    public PopupMgr(DomElement element) {
        super(element);
        loadFile(new ResourceLocation("dungeonsguide:gui/popupmgr"));
    }
    // just stack

    @Bind(attributeName = "stackRef")
    BindableAttribute<DomElement> domElementBindableAttribute = new BindableAttribute<>(DomElement.class);

    @Override
    public void onMount() {
        super.onMount();
        getElement().getContext().CONTEXT.put("popup", this);
        domElementBindableAttribute.getValue().addElement(
                DomElementRegistry.createTree(getSlots().get("")));
    }

    @Override
    public void onUnmount() {
        domElementBindableAttribute.getValue().getChildren().clear();
        super.onUnmount();
    }

    public void openPopup(DomElement element) {
        domElementBindableAttribute.getValue().addElementFirst(element);
    }

    public void closePopup() {
        domElementBindableAttribute.getValue().removeElement(
                domElementBindableAttribute.getValue().getChildren().get(0)
        );
    }

    public static final DomElementRegistry.DomElementCreator CREATOR = new DomElementRegistry.GeneralDomElementCreator(
            SingleChildPassingLayouter::new, OnlyChildrenRenderer::new, PopupMgr::new
    );
}
