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

package kr.syeyoung.dungeonsguide.mod.mixin;

import kr.syeyoung.dungeonsguide.launcher.FabricEventBridge;
import kr.syeyoung.dungeonsguide.mod.events.impl.KeyInputEvent;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    // 1.8.9: InputEvent.KeyInputEvent
    // TODO: fires even while a screen is open, check against what 1.8.9 forge did
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void dungeonsguide$onKeyPress(long window, int action, KeyEvent keyEvent, CallbackInfo ci) {
        KeyInputEvent.Action dgAction = action == GLFW.GLFW_PRESS ? KeyInputEvent.Action.PRESS
                : action == GLFW.GLFW_REPEAT ? KeyInputEvent.Action.REPEAT : KeyInputEvent.Action.RELEASE;
        FabricEventBridge.fire(new KeyInputEvent(dgAction, keyEvent));
    }
}
