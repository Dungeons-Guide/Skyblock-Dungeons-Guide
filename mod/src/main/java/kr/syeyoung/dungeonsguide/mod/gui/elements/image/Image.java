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

package kr.syeyoung.dungeonsguide.mod.gui.elements.image;

import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class Image extends Widget implements Renderer {
    public final ResourceIdentifier location;
    public final int uvX;
    public final int uvY ;
    public final int textureWidth;
    public final int textureHeight;
    public final int uvWidth;
    public final int uvHeight;
    
    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.emptyList();
    }

    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        context.drawScaledCustomSizeModalRect(location, 0, 0,
                uvX, 
                uvY, 
                uvWidth, 
                uvHeight, 
                buildContext.getSize().getWidth(),
                buildContext.getSize().getHeight(),
                textureWidth,
                textureHeight);
    }
}
