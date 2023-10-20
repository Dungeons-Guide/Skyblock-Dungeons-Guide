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

import com.mojang.authlib.GameProfile;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3.MainConfigWidget;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3.ParameterItem;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.location2.HUDLocationConfig;
import kr.syeyoung.dungeonsguide.mod.config.types.TCDouble;
import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticsManager;
import kr.syeyoung.dungeonsguide.mod.discord.DiscordIntegrationManager;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.FeatureViewPlayerStatsOnJoin;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.HoverEventRenderPlayer;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.ApiFetcher;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.SkinFetcher;
import kr.syeyoung.dungeonsguide.mod.guiv2.GuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.GlobalHUDScale;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.image.ImageTexture;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import kr.syeyoung.dungeonsguide.mod.stomp.StompManager;
import kr.syeyoung.dungeonsguide.mod.stomp.StompPayload;
import kr.syeyoung.dungeonsguide.mod.wsresource.StaticResourceCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandDungeonsGuide extends CommandBase {

    @Override
    public String getCommandName() {
        return "dungeonsguide";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "dungeonsguide";
    }

    @Override
    public List<String> getCommandAliases() {
        return new ArrayList<String>() {{
            add("dg"); //per issue #400
            add("dungeonguide"); //because people keep saying "dungeon guide" the singular
            add("deegee"); //in case another mod/server somehow uses a command "/dg" and no one wants to type out the entire name "dungeonsguide"
            add("던전가이드"); //easter egg
            add("던전안내"); //easter egg
            // for each new alias, make a new line, then: add("[INSERT YOUR ALIAS HERE]");
            // Shoutout to coobird for the anonymous inner class idea: stackoverflow.com/a/1005083
        }};
    }

    //List of subcommands for tab support
    private static final String[] SUBCOMMANDS = {
            "reparty",
            "gui",
            "v3",
            "pvall",
            "asktojoin",
            "atj",
            "pv",
            "purge",
            "pbroadcast",
            "partymax",
            "unload",
            "aliases"
    };

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return CommandBase.getListOfStringsMatchingLastWord(args, SUBCOMMANDS);
        }
        return Collections.emptyList();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        if (args.length == 0) {
            target = new GuiScreenAdapter(new GlobalHUDScale(new MainConfigWidget()));
            return;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "reparty":
                repartyCommand();
                break;

            case "gui":
                target = new GuiScreenAdapter(new GlobalHUDScale(new HUDLocationConfig(null)));
                break;
            case "pv":
                pvCommand(args[1], sender); //args[1] is the player name
                break;

            case "pvall":
                pvAllCommand();
                break;

            case "asktojoin":
            case "atj":
                askToJoinCommand();
                break;

            case "purge":
                purgeCommand();
                break;

            case "pbroadcast":
                pBroadcastCommand(args);
                break;

            case "partymax":
            case "pm":
                partyMaxCommand(args);
                break;

            case "unload":
                unloadCommand();
                break;


            case "aliases":
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide aliases§7::"));
                ChatTransmitter.addToQueue(new ChatComponentText(" §7- §e/dungeonsguide"));
                ChatTransmitter.addToQueue(new ChatComponentText(" §7- §e/dungeonguide"));
                ChatTransmitter.addToQueue(new ChatComponentText(" §7- §e/deegee"));
                break;

            case "setuiscale":
            case "setscale":
            case "uiscale":
            case "scale":
                setUIScale(args);
                break;

            default:
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg §7-§fOpens configuration gui"));
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg gui §7-§fOpens configuration gui"));
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg scale [scale] §7-§fSets the Global HUD scale (You must disable Minecraft default HUD scale first.)"));
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg help §7-§fShows command help"));
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg reparty §7-§f Reparty."));
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg asktojoin or /dg atj §7-§f Toggle ask to join §cRequires Discord Rich Presence enabled. (/dg -> Advanced)"));
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg partymax [number] or /dg pm [number] §7-§f Sets the party max §7(maximum amount people in party, for discord rpc)"));
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg pv [ign] §7-§f Profile Viewer"));
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg pvall §7-§f Profile Viewer For all people on party"));
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg purge §7-§f Purge api cache."));
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg unload §7-§f Unload Current Version of Dungeons Guide, to load the new version"));
                break;
        }
    }

    private void setUIScale(String[] args) {
        if (args.length != 2) {
            ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §cUsage: /dg " + args[0] + " [scale]"));
            return;
        }
        try {
            Double.parseDouble(args[1]);
        } catch ( NumberFormatException e ) {
            if (args[1] instanceof String && (args[1].toLowerCase().equals("reset") || args[1].toLowerCase().equals("r"))) {
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §eGlobal HUD scale successfully reset to 1."));
            } else {
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §cSorry, but " + args[1] + " is not a valid GUI scale. Try again."));
            }
            return;
        }
        Double theScale = Double.parseDouble(args[1]);
        if (theScale < 0.01 || theScale > (Math.PI + Math.E)) {
            ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §cSorry, but while " + args[1] + " is a valid number, it is not a suitable GUI scale. Try again, §eor reset your Global HUD scale with §6/dg " + args[0] + " reset§e."));
            return;
        }
        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §aSuccessfully set your Global HUD scale to " + args[1] + ". §eTo reset your Global HUD scale, run §6/dg " + args[0] + " reset§e."));
        FeatureRegistry.GLOBAL_HUD_SCALE.<Boolean>getParameter("mc").setValue(false);
        FeatureRegistry.GLOBAL_HUD_SCALE.<Double>getParameter("scale").setValue(theScale);
    }

    private GuiScreen target;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        try {
            if (target != null && e.phase == TickEvent.Phase.START) {
                Minecraft.getMinecraft().displayGuiScreen(target);
                target = null;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    private void repartyCommand() {
        if (!DungeonsGuide.getDungeonsGuide().getCommandReparty().requestReparty(false)) {
            ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §cRepartying..."));
        }
    }

    private void pvAllCommand() {
        PartyManager.INSTANCE.requestPartyList(context -> {
            if (context == null) {
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §cYou are not in a party!"));
                return;
            }
            FeatureViewPlayerStatsOnJoin.processPartyMembers(context);
        });
    }

    private void askToJoinCommand() {
        if (!DiscordIntegrationManager.INSTANCE.isLoaded()) {
            ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §cDiscord GameSDK has been disabled, or it failed to load!"));
            return;
        }
        if (!PartyManager.INSTANCE.canInvite()) {
            ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §cYou don't have permission to invite people to the party!"));
        } else {
            PartyManager.INSTANCE.toggleAllowAskToJoin();
            ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §fToggled Ask to join to " + (PartyManager.INSTANCE.getAskToJoinSecret() != null ? "§eon" : "§coff")));
        }

        if (!FeatureRegistry.DISCORD_RICHPRESENCE.isEnabled()) {
            ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §cDiscord Rich Presence is disabled! Enable at /dg -> Discord "));
        }
        if (!FeatureRegistry.DISCORD_ASKTOJOIN.isEnabled()) {
            ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §cDiscord Invite Viewer is disabled! Enable at /dg -> Discord ")); // how
        }
    }

    private void pvCommand(String ign, ICommandSender sender) {
        try {
            ApiFetcher.fetchUUIDAsync(ign)
                    .thenAccept(a -> {
                        assert a.orElse(null) != null;
                        sender.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §e" + ign + "§f's Profile ").appendSibling(new ChatComponentText("§7view").setChatStyle(new ChatStyle().setChatHoverEvent(
                                new HoverEventRenderPlayer(
                                        new GameProfile(FeatureViewPlayerStatsOnJoin.fromString(a.orElse(null)), ign)
                                )))));
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void purgeCommand() {
        ApiFetcher.purgeCache();
        CosmeticsManager cosmeticsManager = DungeonsGuide.getDungeonsGuide().getCosmeticsManager();
        cosmeticsManager.requestPerms();
        cosmeticsManager.requestCosmeticsList();
        cosmeticsManager.requestActiveCosmetics();
        StaticResourceCache.INSTANCE.purgeCache();
        ImageTexture.imageMap.clear();
        SkinFetcher.purgeCache();

        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §fSuccessfully purged API Cache!"));
    }

    private void pBroadcastCommand(String[] args) {
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


    }

    private void partyMaxCommand(String[] args) {
        if (args.length == 1) {
            ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §fCurrent party max is §e" + PartyManager.INSTANCE.getMaxParty()));
        } else if (args.length == 2) {
            try {
                int partyMax = Integer.parseInt(args[1]);
                if (partyMax < 2) {
                    ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §cparty max can't be smaller than 2"));
                    return;
                }

                PartyManager.INSTANCE.setMaxParty(partyMax);
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §fSuccessfully set the party max to §e" + PartyManager.INSTANCE.getMaxParty()));
            } catch (Exception e) {
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §c" + args[1] + " is not valid number."));
            }
        }
    }

    private void unloadCommand() {
        Main.getMain().unloadWithoutStacktraceReference();
    }
}
