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
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;

// fired once per frame for the whole hud (1.8.9: RenderGameOverlayEvent with ElementType.ALL)
public abstract class RenderHUDEvent extends DGEvent {
    public final float partialTicks;
    public final GuiGraphicsExtractor graphics;
    // handlers draw here, in gui units; rendered into an image after dispatch (D126)
    public final RenderingContext context;

    protected RenderHUDEvent(float partialTicks, GuiGraphicsExtractor graphics, RenderingContext context) {
        this.partialTicks = partialTicks;
        this.graphics = graphics;
        this.context = context;
    }

    public static class Pre extends RenderHUDEvent {
        public Pre(float partialTicks, GuiGraphicsExtractor graphics, RenderingContext context) {
            super(partialTicks, graphics, context);
        }
    }

    public static class Post extends RenderHUDEvent {
        public Post(float partialTicks, GuiGraphicsExtractor graphics, RenderingContext context) {
            super(partialTicks, graphics, context);
        }
    }
}
