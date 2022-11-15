/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
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

package kr.syeyoung.dungeonsguide.mod;

import kr.syeyoung.dungeonsguide.mod.events.impl.KeyBindPressedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class Keybinds
{

    @SubscribeEvent
    public void onTogglePathfindStatus(InputEvent.KeyInputEvent keyInputEvent) {
        if (Keyboard.getEventKeyState()) {
            int key = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
            KeyBindPressedEvent keyBindPressedEvent = new KeyBindPressedEvent(key);
            MinecraftForge.EVENT_BUS.post(keyBindPressedEvent);
        }
    }
    @SubscribeEvent
    public void onMousePressed(InputEvent.MouseInputEvent mouseInputEvent) {
        if (Mouse.getEventButtonState()) {
            int key = Mouse.getEventButton() - 100;
            KeyBindPressedEvent keyBindPressedEvent = new KeyBindPressedEvent(key);
            MinecraftForge.EVENT_BUS.post(keyBindPressedEvent);
        }
    }
}
