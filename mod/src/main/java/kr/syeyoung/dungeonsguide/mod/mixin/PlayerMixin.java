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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import kr.syeyoung.dungeonsguide.launcher.FabricEventBridge;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerNameFormatEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(Player.class)
public class PlayerMixin {
    // 1.8.9: PlayerEvent.NameFormat
    @ModifyExpressionValue(method = "getDisplayName", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getName()Lnet/minecraft/network/chat/Component;"))
    private Component dungeonsguide$formatName(Component name, @Share("dungeonsguide$prefixes") LocalRef<List<Component>> prefixes) {
        Player self = (Player) (Object) this;
        if (!self.level().isClientSide()) return name;
        PlayerNameFormatEvent event = new PlayerNameFormatEvent(self, name.getString(), name);
        FabricEventBridge.fire(event);
        prefixes.set(event.prefixes);
        return event.displayname;
    }

    // 1.8.9: Forge's getDisplayName put the player's prefixes before the team-formatted name
    @ModifyExpressionValue(method = "getDisplayName", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/PlayerTeam;formatNameForTeam(Lnet/minecraft/world/scores/Team;Lnet/minecraft/network/chat/Component;)Lnet/minecraft/network/chat/MutableComponent;"))
    private MutableComponent dungeonsguide$prefixName(MutableComponent formatted, @Share("dungeonsguide$prefixes") LocalRef<List<Component>> prefixes) {
        if (prefixes.get() == null || prefixes.get().isEmpty()) return formatted;
        MutableComponent result = Component.empty();
        for (Component prefix : prefixes.get()) {
            result.append(prefix);
        }
        return result.append(formatted);
    }
}
