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

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.config.guiconfig.GuiConfigV2;
import kr.syeyoung.dungeonsguide.discord.rpc.RichPresenceManager;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.FeatureViewPlayerStatsOnJoin;
import kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.api.ApiFetcher;
import kr.syeyoung.dungeonsguide.party.PartyManager;
import kr.syeyoung.dungeonsguide.stomp.StompManager;
import kr.syeyoung.dungeonsguide.stomp.StompPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.json.JSONObject;

public class CommandDungeonsGuide extends CommandBase {
    @Override
    public String getCommandName() {
        return "dg";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "dg";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            openConfig = true;
        } else if (args[0].equalsIgnoreCase("reparty")) {
            if (!DungeonsGuide.getDungeonsGuide().getCommandReparty().requestReparty(false)) {
                ChatTransmitter.addToReciveChatQueue(new ChatComponentText("§eDungeons Guide §7:: §cCurrently Repartying"));
            }
        } else if (args[0].equalsIgnoreCase("gui")) {
            openConfig = true;
        } else if (args[0].equalsIgnoreCase("pvall")) {
            PartyManager.INSTANCE.requestPartyList((context) -> {
                if (context == null) {
                    ChatTransmitter.addToReciveChatQueue(new ChatComponentText("§eDungeons Guide §7:: §cNot in Party"));
                    return;
                }
                FeatureViewPlayerStatsOnJoin.processPartyMembers(context);
            });
        } else if (args[0].equalsIgnoreCase("asktojoin") || args[0].equalsIgnoreCase("atj")) {
            if (RichPresenceManager.INSTANCE.getLastSetupCode() == -9999) {
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cDiscord GameSDK has been disabled, or it failed to load!"));
                return;
            }
            if (!PartyManager.INSTANCE.canInvite()) {
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cYou don't have perms in the party to invite people!"));
            } else {
                PartyManager.INSTANCE.toggleAllowAskToJoin();
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fToggled Ask to join to " + (PartyManager.INSTANCE.getAskToJoinSecret() != null ? "§eon" : "§coff")));
            }

            if (!FeatureRegistry.DISCORD_RICHPRESENCE.isEnabled()) {
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cDiscord Rich Presence is disabled! Enable at /dg -> Discord "));
            }
            if (!FeatureRegistry.DISCORD_ASKTOJOIN.isEnabled()) {
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cDiscord Invite Viewer is disabled! Enable at /dg -> Discord ")); // how
            }
        } else if (args[0].equals("pv")) {
            try {
                ApiFetcher.fetchUUIDAsync(args[1])
                        .thenAccept(a -> {
                            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e" + args[1] + "§f's Profile ").appendSibling(new ChatComponentText("§7view").setChatStyle(new ChatStyle().setChatHoverEvent(new FeatureViewPlayerStatsOnJoin.HoverEventRenderPlayer(a.orElse(null))))));
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (args[0].equals("pbroadcast")) {
            try {
                String[] payload = new String[args.length - 1];
                System.arraycopy(args, 1, payload, 0, payload.length);
                String actualPayload = String.join(" ", payload).replace("$C$", "§");
                StompManager.getInstance().send(new StompPayload().header("destination", "/app/party.broadcast").payload(
                        new JSONObject().put("partyID", PartyManager.INSTANCE.getPartyContext().getPartyID())
                                .put("payload", actualPayload).toString()
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg §7-§fOpens configuration gui"));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg gui §7-§fOpens configuration gui"));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg help §7-§fShows command help"));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg saverooms §7-§f Saves usergenerated dungeon roomdata."));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg loadrooms §7-§f Reloads dungeon roomdata."));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg reloadah §7-§f Reloads price data from server."));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg brand §7-§f View server brand."));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg reparty §7-§f Reparty."));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg info §7-§f View Current DG User info."));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg asktojoin or /dg atj §7-§f Toggle ask to join §cRequires Discord Rich Presence enabled. (/dg -> Advanced)"));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg partymax [number] or /dg pm [number] §7-§f Sets partymax §7(maximum amount people in party, for discord rpc)"));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg pv [ign] §7-§f Profile Viewer"));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg pvall §7-§f Profile Viewer For all people on party"));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg purge §7-§f Purge api cache."));
            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg saverun §7-§f Save run to be sent to developer."));
        }
    }

    private boolean openConfig = false;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        try {
            if (openConfig && e.phase == TickEvent.Phase.START ) {
                openConfig = false;
                Minecraft.getMinecraft().displayGuiScreen(new GuiConfigV2());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
