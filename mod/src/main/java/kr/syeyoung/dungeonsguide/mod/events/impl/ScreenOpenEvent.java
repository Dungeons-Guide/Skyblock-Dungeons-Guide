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

// fired before minecraft opens a screen. replace gui to open a different one, cancel to not open it
public class ScreenOpenEvent extends DGEvent implements Cancelable {
    public Screen gui;

    @Getter @Setter
    private boolean canceled;

    public ScreenOpenEvent(Screen gui) {
        this.gui = gui;
    }
}
