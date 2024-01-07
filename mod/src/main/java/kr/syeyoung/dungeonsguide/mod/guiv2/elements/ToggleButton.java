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
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Passthrough;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.EnumCursor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

@Passthrough(exportName = "_on", bindName = "wgtOn", type = Widget.class)
@Passthrough(exportName = "_off", bindName = "wgtOff", type = Widget.class)
@Passthrough(exportName = "_hoverOn", bindName = "wgtHoverOn", type = Widget.class)
@Passthrough(exportName = "_hoverOff", bindName = "wgtHoverOff", type = Widget.class)
public class ToggleButton extends AnnotatedWidget implements Renderer {

    @Bind(variableName = "refOn")
    public final BindableAttribute<DomElement> on = new BindableAttribute<DomElement>(DomElement.class);
    @Bind(variableName = "refOff")
    public final BindableAttribute<DomElement> off = new BindableAttribute<DomElement>(DomElement.class);
    @Bind(variableName = "refHoverOn")
    public final BindableAttribute<DomElement> hoverOn = new BindableAttribute<DomElement>(DomElement.class);
    @Bind(variableName = "refHoverOff")
    public final BindableAttribute<DomElement> hoverOff = new BindableAttribute<DomElement>(DomElement.class);


    @Export(attributeName = "enabled")
    public final BindableAttribute<Boolean> enabled = new BindableAttribute<>(Boolean.class);

    public ToggleButton() {
        super(new ResourceLocation("dungeonsguide:gui/elements/toggleButton.gui"));
    }

    @Override
    protected Layouter createLayouter() {
        return Stack.StackingLayouter.INSTANCE;
    }

    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        DomElement value;
        if (enabled.getValue()) {
            value = isHover ? hoverOn.getValue() : on.getValue();
        }  else {
            value = isHover ? hoverOff.getValue() : off.getValue();
        }
        Rect original = value.getRelativeBound();
        GlStateManager.translate(original.getX(), original.getY(), 0);

        double absXScale = buildContext.getAbsBounds().getWidth() / buildContext.getSize().getWidth();
        double absYScale = buildContext.getAbsBounds().getHeight() / buildContext.getSize().getHeight();

        Rect elementABSBound = new Rect(
                (buildContext.getAbsBounds().getX() + original.getX() * absXScale),
                (buildContext.getAbsBounds().getY() + original.getY() * absYScale),
                (original.getWidth() * absXScale),
                (original.getHeight() * absYScale)
        );
        value.setAbsBounds(elementABSBound);

        value.getRenderer().doRender(
                partialTicks, context, value);
    }

    @Override
    public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton) {
        getDomElement().obtainFocus();
        enabled.setValue(!enabled.getValue());
        return true;
    }

    private boolean isHover = false;
    @Override
    public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0) {
        getDomElement().setCursor(EnumCursor.POINTING_HAND);
        isHover = true;
        return true;
    }

    @Override
    public void mouseExited(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        isHover = false;
    }
}
