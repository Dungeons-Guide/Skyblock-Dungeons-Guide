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
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

// like 1.8.9 subscribers of the base class get both Pre and Post
public abstract class ScreenKeyboardEvent extends DGEvent implements Cancelable {
    public final Screen gui;
    public final Action action;
    // set for PRESS and RELEASE
    public final KeyEvent keyEvent;
    // set for CHAR
    public final CharacterEvent characterEvent;

    @Getter @Setter
    private boolean canceled;

    public enum Action {
        PRESS, RELEASE, CHAR
    }

    protected ScreenKeyboardEvent(Screen gui, Action action, KeyEvent keyEvent, CharacterEvent characterEvent) {
        this.gui = gui;
        this.action = action;
        this.keyEvent = keyEvent;
        this.characterEvent = characterEvent;
    }

    public static class Pre extends ScreenKeyboardEvent {
        public Pre(Screen gui, Action action, KeyEvent keyEvent, CharacterEvent characterEvent) {
            super(gui, action, keyEvent, characterEvent);
        }
    }

    public static class Post extends ScreenKeyboardEvent {
        public Post(Screen gui, Action action, KeyEvent keyEvent, CharacterEvent characterEvent) {
            super(gui, action, keyEvent, characterEvent);
        }
    }
}
