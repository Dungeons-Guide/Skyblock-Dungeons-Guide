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
import kr.syeyoung.dungeonsguide.mod.events.impl.MouseInputEvent;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenMouseEvent;
import net.minecraft.client.gui.screens.Screen;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    // 1.8.9: InputEvent.MouseInputEvent
    // TODO: fires even while a screen is open, check against what 1.8.9 forge did
    @Inject(method = "onButton", at = @At("HEAD"))
    private void dungeonsguide$onButton(long window, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
        FabricEventBridge.fire(new MouseInputEvent(action == GLFW.GLFW_PRESS ? MouseInputEvent.Action.PRESS : MouseInputEvent.Action.RELEASE, buttonInfo));
    }

    // 1.8.9: GuiScreenEvent.MouseInputEvent for moves
    // 26.1 calls Screen.mouseMoved from handleAccumulatedMovement, not onMove
    @WrapOperation(method = "handleAccumulatedMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseMoved(DD)V"))
    private void dungeonsguide$onScreenMouseMove(Screen screen, double x, double y, Operation<Void> original) {
        if (FabricEventBridge.fire(new ScreenMouseEvent.Move(screen, x, y))) return;
        original.call(screen, x, y);
    }
}
