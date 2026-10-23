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
import io.netty.channel.ChannelHandlerContext;
import kr.syeyoung.dungeonsguide.mod.events.listener.PacketInjector;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {
    // 1.8.9: PacketInjector was a netty handler before "packet_handler"
    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
    private void dungeonsguide$beforeRead(ChannelHandlerContext ctx, Packet<?> _packet, CallbackInfo ci, @Local(argsOnly = true) LocalRef<Packet<?>> packet) {
        if (((Connection) (Object) this).getReceiving() != PacketFlow.CLIENTBOUND) return;
        packet.set(PacketInjector.onPacketReceived(packet.get()));
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At("TAIL"))
    private void dungeonsguide$afterRead(ChannelHandlerContext ctx, Packet<?> _packet, CallbackInfo ci, @Local(argsOnly = true) LocalRef<Packet<?>> packet) {
        if (((Connection) (Object) this).getReceiving() != PacketFlow.CLIENTBOUND) return;
        PacketInjector.afterPacketReceived(packet.get());
    }
}
