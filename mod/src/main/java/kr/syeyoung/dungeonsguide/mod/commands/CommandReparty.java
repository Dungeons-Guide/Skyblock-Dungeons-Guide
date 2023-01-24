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
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.util.stream.Collectors;

public class CommandReparty extends CommandBase {
    private String command;
    public CommandReparty() {
        command = FeatureRegistry.ETC_REPARTY.<String>getParameter("command").getValue();
        command = command.replace(" ", "");
    }

    @Override
    public String getCommandName() {
        return command;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return command;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!requestReparty(false)) {
            ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §cRepartying..."));
        }
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
                    ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §cYou are not in a Party!"));
                reparting = false;
                return;
            }
            if (!pc.hasLeader(Minecraft.getMinecraft().getSession().getUsername())) {
                if (!noerror)
                    ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §cYou're not the leader"));
                reparting = false;
                return;
            }
            if (pc.isSelfSolo()) {
                if (!noerror)
                    ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §cYou can not reparty yourself"));
                reparting = false;
                return;
            }
            String members = pc.getPartyRawMembers().stream().filter(a -> !a.equalsIgnoreCase(Minecraft.getMinecraft().getSession().getUsername())).collect(Collectors.joining(" "));
            String command = "/p invite "+members;

            ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §eDisbanding Party..."));
            ChatProcessor.INSTANCE.addToChatQueue("/p disband", () -> {
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §eRunning invite command §f"+command));
                ChatProcessor.INSTANCE.addToChatQueue(command, () -> {
                    ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §eSuccessfully repartied!§f"));

                    reparting = false;
                }, false);
            }, false);
        });
        return true;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
