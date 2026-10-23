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

import kr.syeyoung.dungeonsguide.mod.events.Cancelable;
import kr.syeyoung.dungeonsguide.mod.events.DGEvent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;

public abstract class ScreenMouseEvent extends DGEvent implements Cancelable {
    public final Screen gui;
    public final double mouseX;
    public final double mouseY;

    @Getter @Setter
    private boolean canceled;

    protected ScreenMouseEvent(Screen gui, double mouseX, double mouseY) {
        this.gui = gui;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public static class Click extends ScreenMouseEvent {
        public final MouseButtonEvent buttonEvent;

        public Click(Screen gui, MouseButtonEvent buttonEvent) {
            super(gui, buttonEvent.x(), buttonEvent.y());
            this.buttonEvent = buttonEvent;
        }
    }

    public static class Release extends ScreenMouseEvent {
        public final MouseButtonEvent buttonEvent;

        public Release(Screen gui, MouseButtonEvent buttonEvent) {
            super(gui, buttonEvent.x(), buttonEvent.y());
            this.buttonEvent = buttonEvent;
        }
    }

    public static class Drag extends ScreenMouseEvent {
        public final MouseButtonEvent buttonEvent;
        public final double dragX;
        public final double dragY;

        public Drag(Screen gui, MouseButtonEvent buttonEvent, double dragX, double dragY) {
            super(gui, buttonEvent.x(), buttonEvent.y());
            this.buttonEvent = buttonEvent;
            this.dragX = dragX;
            this.dragY = dragY;
        }
    }

    public static class Scroll extends ScreenMouseEvent {
        public final double scrollX;
        public final double scrollY;

        public Scroll(Screen gui, double mouseX, double mouseY, double scrollX, double scrollY) {
            super(gui, mouseX, mouseY);
            this.scrollX = scrollX;
            this.scrollY = scrollY;
        }
    }

    // comes from MouseHandlerMixin, fabric has no event for mouse moves inside screens
    public static class Move extends ScreenMouseEvent {
        public Move(Screen gui, double mouseX, double mouseY) {
            super(gui, mouseX, mouseY);
        }
    }
}
