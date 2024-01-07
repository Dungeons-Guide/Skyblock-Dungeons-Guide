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

package kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups;

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import net.minecraft.util.ResourceLocation;

public class AbsLocationPopup extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "x")
    public final BindableAttribute<Double> x = new BindableAttribute<>(Double.class);
    @Bind(variableName = "y")
    public final BindableAttribute<Double> y = new BindableAttribute<>(Double.class);;
    @Bind(variableName = "ref")
    public final BindableAttribute<DomElement> ref = new BindableAttribute<>(DomElement.class);
    @Bind(variableName = "child")
    public final BindableAttribute<Widget> child = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "size")
    public final BindableAttribute<Size> size = new BindableAttribute<>(Size.class, new Size(0,0));

    public final BindableAttribute<Double> absX = new BindableAttribute<>(Double.class);
    public final BindableAttribute<Double> absY = new BindableAttribute<>(Double.class);
    public boolean autoclose = false;
    public AbsLocationPopup(double x, double y, Widget child, boolean autoclose) {
        super(new ResourceLocation("dungeonsguide:gui/elements/locationedPopup.gui"));
        absX.setValue(x);
        absY.setValue(y);
        absX.addOnUpdate(this::updatePos);
        absY.addOnUpdate(this::updatePos);
        size.addOnUpdate((old, neu) -> updatePos(0,0));
        this.child.setValue(child);
        this.autoclose = autoclose;
    }
    public AbsLocationPopup(BindableAttribute<Double> x, BindableAttribute<Double> y, Widget child, boolean autoclose) {
        super(new ResourceLocation("dungeonsguide:gui/elements/locationedPopup.gui"));
        x.exportTo(this.absX);
        y.exportTo(this.absY);
        absX.addOnUpdate(this::updatePos);
        absY.addOnUpdate(this::updatePos);
        size.addOnUpdate((old, neu) -> updatePos(0,0));
        this.child.setValue(child);
        this.autoclose = autoclose;
    }

    @Override
    public void onMount() {
        updatePos(0,0);
    }

    public void updatePos(double old, double neu) {
        PopupMgr popupMgr = PopupMgr.getPopupMgr(getDomElement());
        Rect rect = popupMgr.getDomElement().getAbsBounds();
        Size rel = popupMgr.getDomElement().getSize();

        Size size = this.size.getValue();
        this.x.setValue(
                Layouter.clamp((absX.getValue() - rect.getX()) * rel.getWidth() / rect.getWidth(), 0, rel.getWidth()-size.getWidth())
        );
        this.y.setValue(
                Layouter.clamp((absY.getValue() - rect.getY()) * rel.getHeight() / rect.getHeight(), 0, rel.getHeight() - size.getHeight())
        );
    }

    @Override
    public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton, boolean childHandled) {
        if (childHandled) return false;
        if (!ref.getValue().getAbsBounds().contains(absMouseX, absMouseY) && autoclose) {
            PopupMgr.getPopupMgr(getDomElement()).closePopup(this,null);
        }
        return false;
    }

    public boolean cursorPassthrough = true;

    @Override
    public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, boolean childHandled) {
        return cursorPassthrough;
    }
}
