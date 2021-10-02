/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide;

import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class Keybinds
{
    public static KeyBinding editingSession;
    public static KeyBinding sendBombdefuse;
    public static KeyBinding nextSecret;
    public static KeyBinding refreshPathfind;
    public static KeyBinding togglePathfind;
    public static KeyBinding freezeLines;

    public static void register()
    {
        editingSession = new KeyBinding("Start editing session", Keyboard.KEY_NONE, "Dungeons Guide");
        ClientRegistry.registerKeyBinding(editingSession);
        sendBombdefuse = new KeyBinding("Send and save bombdefuse solution", Keyboard.KEY_F, "Dungeons Guide");
        ClientRegistry.registerKeyBinding(sendBombdefuse);
        nextSecret = new KeyBinding("Navigate to next secret. (Req option enabled at /dg)", Keyboard.KEY_R, "Dungeons Guide");
        ClientRegistry.registerKeyBinding(nextSecret);
        togglePathfind = new KeyBinding("Toggle Pathfind. (Req option enabled at /dg)", Keyboard.KEY_NONE, "Dungeons Guide");
        ClientRegistry.registerKeyBinding(togglePathfind);
        refreshPathfind = new KeyBinding("Refresh or Pathfind Pathfindline to hovered secret", Keyboard.KEY_NONE, "Dungeons Guide");
        ClientRegistry.registerKeyBinding(refreshPathfind);
        freezeLines = new KeyBinding("Toggle freeze pathfind lines", Keyboard.KEY_NONE, "Dungeons Guide");
        ClientRegistry.registerKeyBinding(freezeLines);
    }

    public static boolean togglePathfindStatus = false;

    @SubscribeEvent
    public void onTogglePathfindStatus(InputEvent.KeyInputEvent keyInputEvent) {
        if (togglePathfind.isKeyDown())
            togglePathfindStatus = !togglePathfindStatus;

        if (freezeLines.isKeyDown()) {
            FeatureRegistry.SECRET_FREEZE_LINES.setEnabled(!FeatureRegistry.SECRET_FREEZE_LINES.isEnabled());
            try {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fToggled Pathfind Freeze to §e"+(FeatureRegistry.SECRET_FREEZE_LINES.isEnabled() ? "on":"off")));
            } catch (Exception ignored) {}
        }
    }
}
