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
import kr.syeyoung.dungeonsguide.mod.events.impl.ComponentHoverEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsExtractorMixin {
    // 1.8.9: GuiScreen.handleComponentHover
    @Inject(method = "componentHoverEffect", at = @At("HEAD"))
    private void dungeonsguide$onComponentHover(Font font, Style hoveredStyle, int xMouse, int yMouse, CallbackInfo ci) {
        FabricEventBridge.fire(new ComponentHoverEvent(hoveredStyle, xMouse, yMouse));
    }
}
