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

// like 1.8.9 subscribers of an action get both Pre and Post
public abstract class ScreenKeyboardEvent extends DGEvent implements Cancelable {
    public final Screen gui;

    @Getter @Setter
    private boolean canceled;

    protected ScreenKeyboardEvent(Screen gui) {
        this.gui = gui;
    }

    public abstract static class Press extends ScreenKeyboardEvent {
        public final KeyEvent keyEvent;

        protected Press(Screen gui, KeyEvent keyEvent) {
            super(gui);
            this.keyEvent = keyEvent;
        }

        public static class Pre extends Press {
            public Pre(Screen gui, KeyEvent keyEvent) {
                super(gui, keyEvent);
            }
        }

        public static class Post extends Press {
            public Post(Screen gui, KeyEvent keyEvent) {
                super(gui, keyEvent);
            }
        }
    }

    public abstract static class Release extends ScreenKeyboardEvent {
        public final KeyEvent keyEvent;

        protected Release(Screen gui, KeyEvent keyEvent) {
            super(gui);
            this.keyEvent = keyEvent;
        }

        public static class Pre extends Release {
            public Pre(Screen gui, KeyEvent keyEvent) {
                super(gui, keyEvent);
            }
        }

        public static class Post extends Release {
            public Post(Screen gui, KeyEvent keyEvent) {
                super(gui, keyEvent);
            }
        }
    }

    public abstract static class Char extends ScreenKeyboardEvent {
        public final CharacterEvent characterEvent;

        protected Char(Screen gui, CharacterEvent characterEvent) {
            super(gui);
            this.characterEvent = characterEvent;
        }

        public static class Pre extends Char {
            public Pre(Screen gui, CharacterEvent characterEvent) {
                super(gui, characterEvent);
            }
        }

        public static class Post extends Char {
            public Post(Screen gui, CharacterEvent characterEvent) {
                super(gui, characterEvent);
            }
        }
    }
}
