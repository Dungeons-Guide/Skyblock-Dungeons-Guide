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

package kr.syeyoung.dungeonsguide.mod.features.impl.etc;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.types.TCString;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.command.UCommandContext;
import kr.syeyoung.modapi.event.events.RegisterCommandEvent;

import java.util.stream.Collectors;

public class FeatureRepartyCommand extends SimpleFeature {
    public FeatureRepartyCommand() {
       super("Dungeon Party.Reparty", "Enable Reparty Command From DG", "if you disable, /dg reparty will still work, Auto reparty will still work\nRequires Restart to get applied", "qol.reparty");
        addParameter("command", new FeatureParameter<String>("command", "The Command", "Command that the reparty will be bound to", "reparty", TCString.INSTANCE));
    }


    @DGEventHandler
    public void onCommandRegister(RegisterCommandEvent event) {
        event.getCommandManager().registerCommand(
                LiteralArgumentBuilder.<UCommandContext>literal(
                        this.<String>getParameter("command").getValue()
                ).executes((ctx) -> {
                    processCommand();
                    return 1;
                })
        );
    }



    public void processCommand() {
        if (!requestReparty(false)) {
            ChatTransmitter.addToQueue("§eDungeons Guide §7:: §cRepartying...");
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
                    ChatTransmitter.addToQueue("§eDungeons Guide §7:: §cYou are not in a Party!");
                reparting = false;
                return;
            }
            if (!pc.hasLeader(ModAPI.getAPI().getSession().getUsername())) {
                if (!noerror)
                    ChatTransmitter.addToQueue("§eDungeons Guide §7:: §cYou're not the leader");
                reparting = false;
                return;
            }
            if (pc.isSelfSolo()) {
                if (!noerror)
                    ChatTransmitter.addToQueue("§eDungeons Guide §7:: §cYou can not reparty yourself");
                reparting = false;
                return;
            }
            String members = pc.getPartyRawMembers().stream().filter(a -> !a.equalsIgnoreCase(ModAPI.getAPI().getSession().getUsername())).collect(Collectors.joining(" "));
            String command = "/p invite "+members;

            ChatTransmitter.addToQueue("§eDungeons Guide §7:: §eDisbanding Party...");
            ChatProcessor.INSTANCE.addToChatQueue("/p disband", () -> {
                ChatTransmitter.addToQueue("§eDungeons Guide §7:: §eRunning invite command §f"+command);
                ChatProcessor.INSTANCE.addToChatQueue(command, () -> {
                    ChatTransmitter.addToQueue("§eDungeons Guide §7:: §eSuccessfully repartied!§f");

                    reparting = false;
                }, false);
            }, false);
        });
        return true;
    }

}
