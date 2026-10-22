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
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import kr.syeyoung.dungeonsguide.launcher.FabricEventBridge;
import kr.syeyoung.dungeonsguide.mod.events.impl.TabListEntryEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Shadow @Final private Map<UUID, PlayerInfo> playerInfoMap;

    // players new to the map, Add fires for them once the packet's actions are applied
    @Inject(method = "handlePlayerInfoUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/social/PlayerSocialManager;addPlayer(Lnet/minecraft/client/multiplayer/PlayerInfo;)V"))
    private void dungeonsguide$onPlayerInfoAdded(ClientboundPlayerInfoUpdatePacket packet, CallbackInfo ci, @Local PlayerInfo playerInfo, @Share("dungeonsguide$added") LocalRef<Set<UUID>> added) {
        if (added.get() == null) added.set(new HashSet<>());
        added.get().add(playerInfo.getProfile().id());
    }

    @Inject(method = "handlePlayerInfoUpdate", at = @At("TAIL"))
    private void dungeonsguide$afterPlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket packet, CallbackInfo ci, @Share("dungeonsguide$added") LocalRef<Set<UUID>> added) {
        for (ClientboundPlayerInfoUpdatePacket.Entry entry : packet.entries()) {
            PlayerInfo info = this.playerInfoMap.get(entry.profileId());
            if (info == null) continue;
            if (added.get() != null && added.get().contains(entry.profileId()))
                FabricEventBridge.fire(new TabListEntryEvent.Add(info));
            else
                FabricEventBridge.fire(new TabListEntryEvent.Update(info, packet.actions()));
        }
    }

    @ModifyExpressionValue(method = "handlePlayerInfoRemove", at = @At(value = "INVOKE", target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object dungeonsguide$onPlayerInfoRemoved(Object info) {
        if (info != null) FabricEventBridge.fire(new TabListEntryEvent.Remove((PlayerInfo) info));
        return info;
    }
}
