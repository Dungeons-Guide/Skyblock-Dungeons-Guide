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
    public final Action action;
    // null for SCROLL
    public final MouseButtonEvent buttonEvent;
    public final double mouseX;
    public final double mouseY;
    public final double dragX;
    public final double dragY;
    public final double scrollX;
    public final double scrollY;

    @Getter @Setter
    private boolean canceled;

    // TODO: no MOVE yet, 26.1 has no fabric event for mouse moves inside screens. decided when porting input
    public enum Action {
        CLICK, RELEASE, DRAG, SCROLL
    }

    protected ScreenMouseEvent(Screen gui, Action action, MouseButtonEvent buttonEvent, double mouseX, double mouseY, double dragX, double dragY, double scrollX, double scrollY) {
        this.gui = gui;
        this.action = action;
        this.buttonEvent = buttonEvent;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.dragX = dragX;
        this.dragY = dragY;
        this.scrollX = scrollX;
        this.scrollY = scrollY;
    }

    public static class Pre extends ScreenMouseEvent {
        public Pre(Screen gui, Action action, MouseButtonEvent buttonEvent, double mouseX, double mouseY, double dragX, double dragY, double scrollX, double scrollY) {
            super(gui, action, buttonEvent, mouseX, mouseY, dragX, dragY, scrollX, scrollY);
        }
    }
}
