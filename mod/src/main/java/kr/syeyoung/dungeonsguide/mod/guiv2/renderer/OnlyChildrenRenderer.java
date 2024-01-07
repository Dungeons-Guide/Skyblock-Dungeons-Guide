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
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import net.minecraft.client.renderer.GlStateManager;

public class OnlyChildrenRenderer implements Renderer {
    public static OnlyChildrenRenderer INSTANCE = new OnlyChildrenRenderer();
    protected OnlyChildrenRenderer() {}
    public void doRender(float partialTicks, RenderingContext renderingContext, DomElement buildContext) {
        for (DomElement value : buildContext.getChildren()) {
            Rect original = value.getRelativeBound();
            if (original == null) continue;
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

            value.getRenderer().doRender(
                    partialTicks,renderingContext, value);
            GlStateManager.popMatrix();
        }
    }
}
