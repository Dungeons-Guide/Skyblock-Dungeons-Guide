/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.events.listener;

import kr.syeyoung.dungeonsguide.mod.events.impl.PacketProcessedEvent;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.common.MinecraftForge;

import java.io.IOException;

@RequiredArgsConstructor
public class WrappedPacket implements Packet<INetHandlerPlayClient> {
    private final Packet<INetHandlerPlayClient> delegate;
    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        delegate.readPacketData(buf);
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        delegate.writePacketData(buf);
    }

    private INetHandlerPlayClient iNetHandlerPlayClient;
    private boolean processed = false;
    @Override
    public void processPacket(INetHandlerPlayClient handler) {
        if (processed) {
            delegate.processPacket(handler);
        }
        this.iNetHandlerPlayClient = handler;
        processed =true;
        // lmao
        Minecraft.getMinecraft().addScheduledTask(this::legitProcessPacket);
    }

    private void legitProcessPacket() {
        MinecraftForge.EVENT_BUS.post(new PacketProcessedEvent.Pre(delegate));
        delegate.processPacket(iNetHandlerPlayClient);
        MinecraftForge.EVENT_BUS.post(new PacketProcessedEvent.Post(delegate));
    }
}
