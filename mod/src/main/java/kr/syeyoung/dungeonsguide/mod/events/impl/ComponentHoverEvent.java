/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2026  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.events.impl;

import kr.syeyoung.dungeonsguide.mod.events.DGEvent;
import net.minecraft.network.chat.Style;

// fired each frame vanilla shows the hover effect of the hovered text style (GuiGraphicsExtractor.componentHoverEffect)
public class ComponentHoverEvent extends DGEvent {
    public final Style style;
    // gui scaled
    public final int mouseX;
    public final int mouseY;

    public ComponentHoverEvent(Style style, int mouseX, int mouseY) {
        this.style = style;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }
}
