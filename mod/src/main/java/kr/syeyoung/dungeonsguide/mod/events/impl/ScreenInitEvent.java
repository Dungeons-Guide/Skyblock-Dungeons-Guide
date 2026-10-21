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
import net.minecraft.client.gui.screens.Screen;

public abstract class ScreenInitEvent extends DGEvent {
    public final Screen gui;
    public final int scaledWidth;
    public final int scaledHeight;

    protected ScreenInitEvent(Screen gui, int scaledWidth, int scaledHeight) {
        this.gui = gui;
        this.scaledWidth = scaledWidth;
        this.scaledHeight = scaledHeight;
    }

    public static class Post extends ScreenInitEvent {
        public Post(Screen gui, int scaledWidth, int scaledHeight) {
            super(gui, scaledWidth, scaledHeight);
        }
    }
}
