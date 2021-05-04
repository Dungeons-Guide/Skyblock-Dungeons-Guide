/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.commands;

import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

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
        requestReparty();
    }

    public enum Phase {
        NOT,
        REQUESTED,
        RECEIVE_PARTYMEMBERS,
        DISBAND,
        REPARTY
    }

    private final List<String> players = new ArrayList<String>();
    private long nextTrigger = Long.MAX_VALUE;
    private Phase phase = Phase.NOT;
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent e) {
        if (e.type == 2) return;
        if (e.message.getFormattedText().startsWith("§6Party Members ") && phase == Phase.REQUESTED) {
            players.clear();
            phase = Phase.RECEIVE_PARTYMEMBERS;
        }
        if (e.message.getFormattedText().startsWith("§cYou are not currently in a party.§r") && phase == Phase.REQUESTED) {
            phase = Phase.NOT;
        }
        String txt = e.message.getFormattedText();
        if (txt.startsWith("§eParty ") && txt.contains(":")) {
            String playerNames = TextUtils.stripColor(txt.split(":")[1]);
            String myname = Minecraft.getMinecraft().getSession().getUsername();
            for (String s : playerNames.split(" ")) {
                if (s.isEmpty()) continue;
                if (s.equals("●")) continue;
                if (s.startsWith("[")) continue;
                if (s.equalsIgnoreCase(myname)) continue;
                players.add(s);
            }
        }
        if (e.message.getFormattedText().equals("§9§m-----------------------------§r") && phase == Phase.RECEIVE_PARTYMEMBERS) {
            phase = Phase.DISBAND;
            nextTrigger = System.currentTimeMillis() + 500;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (nextTrigger < System.currentTimeMillis() && (phase == Phase.DISBAND || phase == Phase.REPARTY)) {
            if (phase == Phase.DISBAND) {
                nextTrigger = System.currentTimeMillis() + 1000;
                phase = Phase.REPARTY;
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/p disband");
            } else {
                phase = Phase.NOT;
                nextTrigger = Long.MAX_VALUE;
                StringBuilder sb = new StringBuilder();
                sb.append("/p invite");
                for (String player : players) {
                    sb.append(" ").append(player);
                }
                Minecraft.getMinecraft().thePlayer.sendChatMessage(sb.toString());
            }
        }
    }

    public void requestReparty() {
        if (phase == Phase.NOT) {
            phase = Phase.REQUESTED;
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/pl");
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
