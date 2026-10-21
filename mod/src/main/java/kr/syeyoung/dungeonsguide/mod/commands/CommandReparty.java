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

package kr.syeyoung.dungeonsguide.mod.commands;

import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import com.mojang.brigadier.Command;
import kr.syeyoung.dungeonsguide.mod.events.annotations.SubscribeEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.RegisterCommandEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;

import java.util.stream.Collectors;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;
import net.minecraft.network.chat.Component;

public class CommandReparty {
    private String command;
    public CommandReparty() {
        command = FeatureRegistry.ETC_REPARTY.<String>getParameter("command").getValue();
        command = command.replace(" ", "");
    }

    // only registered while the reparty feature is enabled, like 1.8.9
    @SubscribeEvent
    public void onRegisterCommand(RegisterCommandEvent event) {
        if (!FeatureRegistry.ETC_REPARTY.isEnabled()) return;
        event.dispatcher.register(literal(command).executes(ctx -> {
            if (!requestReparty(false)) {
                ChatTransmitter.addToQueue(Component.literal("§eDungeons Guide §7:: §cRepartying..."));
            }
            return Command.SINGLE_SUCCESS;
        }));
    }

    private boolean reparting = false;


    public boolean requestReparty(boolean noerror) {
        if (reparting) {
            return false;
        }
        reparting = true;

        PartyManager.INSTANCE.requestPartyList(pc -> {
            if (pc == null) {
                if (!noerror)
                    ChatTransmitter.addToQueue(Component.literal("§eDungeons Guide §7:: §cYou are not in a Party!"));
                reparting = false;
                return;
            }
            if (!pc.hasLeader(Minecraft.getInstance().getSession().getUsername())) {
                if (!noerror)
                    ChatTransmitter.addToQueue(Component.literal("§eDungeons Guide §7:: §cYou're not the leader"));
                reparting = false;
                return;
            }
            if (pc.isSelfSolo()) {
                if (!noerror)
                    ChatTransmitter.addToQueue(Component.literal("§eDungeons Guide §7:: §cYou can not reparty yourself"));
                reparting = false;
                return;
            }
            String members = pc.getPartyRawMembers().stream().filter(a -> !a.equalsIgnoreCase(Minecraft.getInstance().getSession().getUsername())).collect(Collectors.joining(" "));
            String command = "/p invite "+members;

            ChatTransmitter.addToQueue(Component.literal("§eDungeons Guide §7:: §eDisbanding Party..."));
            ChatProcessor.INSTANCE.addToChatQueue("/p disband", () -> {
                ChatTransmitter.addToQueue(Component.literal("§eDungeons Guide §7:: §eRunning invite command §f"+command));
                ChatProcessor.INSTANCE.addToChatQueue(command, () -> {
                    ChatTransmitter.addToQueue(Component.literal("§eDungeons Guide §7:: §eSuccessfully repartied!§f"));

                    reparting = false;
                }, false);
            }, false);
        });
        return true;
    }
}
