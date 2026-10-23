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

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import kr.syeyoung.dungeonsguide.launcher.FabricEventBridge;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenOpenEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.WorldUnloadEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    // 1.8.9: GuiOpenEvent
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void dungeonsguide$onSetScreen(Screen _screen, CallbackInfo ci, @Local(argsOnly = true) LocalRef<Screen> screen) {
        ScreenOpenEvent event = new ScreenOpenEvent(screen.get());
        if (FabricEventBridge.fire(event)) {
            ci.cancel();
            return;
        }
        screen.set(event.gui);
    }

    // 1.8.9: WorldEvent.Unload. fabric only has an event for the new level
    @Inject(method = "updateLevelInEngines(Lnet/minecraft/client/multiplayer/ClientLevel;Z)V", at = @At("HEAD"))
    private void dungeonsguide$onLevelChange(ClientLevel level, boolean stopSound, CallbackInfo ci) {
        ClientLevel previous = ((Minecraft) (Object) this).level;
        if (previous != null)
            FabricEventBridge.fire(new WorldUnloadEvent(previous));
    }
}
