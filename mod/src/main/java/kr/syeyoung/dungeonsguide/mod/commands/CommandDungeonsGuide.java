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

package kr.syeyoung.dungeonsguide.mod.commands;

import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.GuiConfigV2;
import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticsManager;
import kr.syeyoung.dungeonsguide.mod.discord.DiscordIntegrationManager;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.FeatureViewPlayerStatsOnJoin;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.ApiFetcher;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import kr.syeyoung.dungeonsguide.mod.stomp.StompManager;
import kr.syeyoung.dungeonsguide.mod.stomp.StompPayload;
import kr.syeyoung.dungeonsguide.mod.wsresource.StaticResourceCache;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.json.JSONObject;

public class CommandDungeonsGuide extends CommandBase {
    private boolean openConfig = false;

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
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §cCurrently Repartying"));
            }
        } else if (args[0].equalsIgnoreCase("gui")) {
            openConfig = true;
        } else if (args[0].equalsIgnoreCase("pvall")) {
            PartyManager.INSTANCE.requestPartyList((context) -> {
                if (context == null) {
                    ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §cNot in Party"));
                    return;
                }
                FeatureViewPlayerStatsOnJoin.processPartyMembers(context);
            });
        } else if (args[0].equalsIgnoreCase("asktojoin") || args[0].equalsIgnoreCase("atj")) {
            if (!DiscordIntegrationManager.INSTANCE.isLoaded()) {
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
        } else if (args[0].equals("purge")) {
            ApiFetcher.purgeCache();
            CosmeticsManager cosmeticsManager = DungeonsGuide.getDungeonsGuide().getCosmeticsManager();
            cosmeticsManager.requestPerms();
            cosmeticsManager.requestCosmeticsList();
            cosmeticsManager.requestActiveCosmetics();
            StaticResourceCache.INSTANCE.purgeCache();
            FeatureRegistry.DISCORD_ASKTOJOIN.imageMap.clear();
            FeatureRegistry.DISCORD_ASKTOJOIN.futureMap.clear();

            sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fSuccessfully purged API Cache!"));
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


        } else if (args[0].equals("partymax") || args[0].equals("pm")) {
            if (args.length == 1) {
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fCurrent party max is §e" + PartyManager.INSTANCE.getMaxParty()));
            } else if (args.length == 2) {
                try {
                    int partyMax = Integer.parseInt(args[1]);
                    if (partyMax < 2) {
                        sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §cparty max can't be smaller than 2"));
                        return;
                    }

                    PartyManager.INSTANCE.setMaxParty(partyMax);
                    sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fSuccessfully set partymax to §e" + PartyManager.INSTANCE.getMaxParty()));
                } catch (Exception e) {
                    sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §c" + args[1] + " is not valid number."));
                }
            }
        } else if (args[0].equals("reload")) {
            Main.getMain().reloadWithoutStacktraceReference(Main.getMain().getCurrentLoader());
        } else {
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg §7-§fOpens configuration gui"));
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg gui §7-§fOpens configuration gui"));
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg help §7-§fShows command help"));
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg reloadah §7-§f Reloads price data from server."));
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg reparty §7-§f Reparty."));
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg asktojoin or /dg atj §7-§f Toggle ask to join §cRequires Discord Rich Presence enabled. (/dg -> Advanced)"));
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg partymax [number] or /dg pm [number] §7-§f Sets partymax §7(maximum amount people in party, for discord rpc)"));
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg pv [ign] §7-§f Profile Viewer"));
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg pvall §7-§f Profile Viewer For all people on party"));
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg purge §7-§f Purge api cache."));
                sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e/dg reload §7-§f Reload Current Version of Dungeons Guide. Auto update versions will not be updated."));
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        try {
            if (openConfig && e.phase == TickEvent.Phase.START) {
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
