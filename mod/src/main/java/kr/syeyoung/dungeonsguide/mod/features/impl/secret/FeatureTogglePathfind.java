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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret;


import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.events.impl.KeyBindPressedEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.listener.KeybindPressedListener;

import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Keyboard;

public class FeatureTogglePathfind extends SimpleFeature implements KeybindPressedListener {
    public FeatureTogglePathfind() {
        super("Dungeon.Secrets.Keybinds", "Toggle Pathfind Lines", "A key for toggling pathfound line visibility.\nPress settings to edit the key", "secret.togglePathfind");
        addParameter("key", new FeatureParameter<Integer>("key", "Key", "Press to toggle pathfind lines", Keyboard.KEY_NONE, "keybind"));
    }
    public boolean togglePathfindStatus = false;

    @Override
    public void onKeybindPress(KeyBindPressedEvent keyBindPressedEvent) {
        if (keyBindPressedEvent.getKey() == this.<Integer>getParameter("key").getValue() && isEnabled()) {
            togglePathfindStatus = !togglePathfindStatus;
            try {
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §fToggled Pathfind Line visibility to §e"+(togglePathfindStatus ? "on":"off")));
            } catch (Exception ignored) {}
        }
    }
}
