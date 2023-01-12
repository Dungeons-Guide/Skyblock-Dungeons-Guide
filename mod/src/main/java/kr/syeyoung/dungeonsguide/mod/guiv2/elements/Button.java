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
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Passthrough;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.EnumCursor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

@Passthrough(exportName = "$", bindName = "wgtNormal", type = Widget.class)
@Passthrough(exportName = "$hovered", bindName = "wgtHover", type = Widget.class)
@Passthrough(exportName = "$pressed", bindName = "wgtPressed", type = Widget.class)
@Passthrough(exportName = "$disabled", bindName = "wgtDisabled", type = Widget.class)
public class Button extends AnnotatedWidget implements Renderer {

    @Bind(variableName = "refDisabled")
    public final BindableAttribute<DomElement> disabled = new BindableAttribute<DomElement>(DomElement.class);
    @Bind(variableName = "refPressed")
    public final BindableAttribute<DomElement> pressed = new BindableAttribute<DomElement>(DomElement.class);
    @Bind(variableName = "refHover")
    public final BindableAttribute<DomElement> hover = new BindableAttribute<DomElement>(DomElement.class);
    @Bind(variableName = "refNormal")
    public final BindableAttribute<DomElement> normal = new BindableAttribute<DomElement>(DomElement.class);


    @Export(attributeName = "click")
    public final BindableAttribute<Runnable> onClick = new BindableAttribute<>(Runnable.class);
    @Export(attributeName = "disabled")
    public final BindableAttribute<Boolean> isDisabled = new BindableAttribute<>(Boolean.class);

    public Button() {
        super(new ResourceLocation("dungeonsguide:gui/button.gui"));
    }

    @Override
    protected Layouter createLayouter() {
        return Stack.StackingLayouter.INSTANCE;
    }

    @Override
    public void doRender(int absMouseX, int absMouseY, double relMouseX, double relMouseY, float partialTicks, RenderingContext context, DomElement buildContext) {
        boolean isHover = buildContext.getSize().contains(relMouseX, relMouseY);
        DomElement value;
        if (isDisabled.getValue()) {
            value = disabled.getValue();
        } else if (isPressed) {
            value = pressed.getValue();
        } else if (isHover) {
            value = hover.getValue();
        } else {
            value = normal.getValue();
        }
        Rect original = value.getRelativeBound();
        GlStateManager.pushMatrix();
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

        value.getRenderer().doRender(absMouseX, absMouseY,
                relMouseX - original.getX(),
                relMouseY - original.getY(), partialTicks, context, value);
        GlStateManager.popMatrix();
    }

    private boolean isPressed;
    @Override
    public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton) {
        getDomElement().obtainFocus();
        isPressed = true;
        return onClick.getValue() != null;
    }

    @Override
    public void mouseReleased(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int state) {
        if (!isPressed) return;
        isPressed = false;


        if (isDisabled.getValue()) return;
        if (getDomElement().getAbsBounds().contains(absMouseX, absMouseY)) {
            if (onClick.getValue() != null) onClick.getValue().run();
        }
        super.mouseReleased(absMouseX, absMouseY, relMouseX, relMouseY, state);
    }

    @Override
    public void mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0) {
        if (isDisabled.getValue())
            getDomElement().setCursor(EnumCursor.NOT_ALLOWED);
        else
            getDomElement().setCursor(EnumCursor.POINTING_HAND);
    }
}
