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

package kr.syeyoung.dungeonsguide.mod.guiv2.renderer;

import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class SingleChildRenderer extends Renderer {
    public SingleChildRenderer(DomElement domElement) {
        super(domElement);
    }

    public void doRender(int absMouseX, int absMouseY, int relMouseX, int relMouseY, float partialTicks) {
        if (getDomElement().getChildren().size() == 0) return;
        DomElement value = getDomElement().getChildren().get(0);
            Rectangle original = value.getRelativeBound();
            GlStateManager.pushMatrix();
            GlStateManager.translate(original.x, original.y, 0);

            double absXScale = getDomElement().getAbsBounds().getWidth() / getDomElement().getRelativeBound().width;
            double absYScale = getDomElement().getAbsBounds().getHeight() / getDomElement().getRelativeBound().height;

            Rectangle elementABSBound = new Rectangle(
                    (int) (getDomElement().getAbsBounds().x + original.x * absXScale),
                    (int) (getDomElement().getAbsBounds().y + original.y * absYScale),
                    (int) (original.width * absXScale),
                    (int) (original.height * absYScale)
            );
            value.setAbsBounds(elementABSBound);

            value.getRenderer().doRender(absMouseX, absMouseY,
                    relMouseX - original.x,
                    relMouseY - original.y, partialTicks);
            GlStateManager.popMatrix();
    }

    @Override
    public final Rectangle applyTransformation(DomElement target) {
        return target.getRelativeBound();
    }
}
