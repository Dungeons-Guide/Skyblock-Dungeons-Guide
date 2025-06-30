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
import com.mojang.brigadier.context.CommandContext;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3.ConfigGuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.location2.HUDLocationConfig;
import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticsManager;
import kr.syeyoung.dungeonsguide.mod.discord.DiscordIntegrationManager;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.FeatureViewPlayerStatsOnJoin;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.HoverEventRenderPlayer;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.ApiFetcher;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.SkinFetcher;
import kr.syeyoung.dungeonsguide.mod.gui.elements.GlobalHUDScale;
import kr.syeyoung.dungeonsguide.mod.gui.elements.image.ImageTexture;
import kr.syeyoung.dungeonsguide.mod.gui.xml.DomElementRegistry;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import kr.syeyoung.dungeonsguide.mod.stomp.StompManager;
import kr.syeyoung.dungeonsguide.mod.stomp.StompPayload;
import kr.syeyoung.dungeonsguide.mod.wsresource.StaticResourceCache;
import kr.syeyoung.modapi.command.UCommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import org.json.JSONObject;

public class CommandDungeonsGuide  {
//        return new ArrayList<String>() {{
//            add("dg"); //per issue #400
//            add("dungeonguide"); //because people keep saying "dungeon guide" the singular
//            add("deegee"); //in case another mod/server somehow uses a command "/dg" and no one wants to type out the entire name "dungeonsguide"
//            add("던전가이드"); //easter egg
//            add("던전안내"); //easter egg
//            // for each new alias, make a new line, then: add("[INSERT YOUR ALIAS HERE]");
//            // Shoutout to coobird for the anonymous inner class idea: stackoverflow.com/a/1005083
//        }};

    @DGCommand("dg gui")
    public void openGuiConfig() {
        DungeonsGuide.getDungeonsGuide().runNextTick(() -> {
            Minecraft.getMinecraft().displayGuiScreen(new ConfigGuiScreenAdapter(null, new GlobalHUDScale(new HUDLocationConfig(null))));
        });
    }

    @DGCommand("dg")
    public void openConfig() {
        DungeonsGuide.getDungeonsGuide().runNextTick(() -> {
            Minecraft.getMinecraft().displayGuiScreen(new ConfigGuiScreenAdapter(null));
        });
    }

    @DGCommand("dg aliases")
    public void showAlias() {
        ChatTransmitter.addToQueue("§eDungeons Guide aliases§7::");
        ChatTransmitter.addToQueue(" §7- §e/dungeonsguide");
        ChatTransmitter.addToQueue(" §7- §e/dungeonguide");
        ChatTransmitter.addToQueue(" §7- §e/deegee");
    }


    @DGCommand("dg {text}")
    public void showHelp(@CommandParam(value = "text", stringType = CommandParam.EnumStringType.GREEDY) String text) {
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §e/dg §7-§fOpens configuration gui");
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §e/dg gui §7-§fOpens configuration gui");
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §e/dg scale [scale] §7-§fSets the Global HUD scale (Also disables Minecraft default HUD scale for you as well if you haven't already.)");
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §e/dg help §7-§fShows command help");
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §e/dg reparty §7-§f Reparty.");
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §e/dg asktojoin or /dg atj §7-§f Toggle ask to join §cRequires Discord Rich Presence enabled. (/dg -> Advanced)");
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §e/dg partymax [number] or /dg pm [number] §7-§f Sets the party max §7(maximum amount people in party, for discord rpc)");
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §e/dg pv [ign] §7-§f Profile Viewer");
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §e/dg pvall §7-§f Profile Viewer For all people on party");
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §e/dg purge §7-§f Purge api cache.");
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §e/dg unload §7-§f Unload Current Version of Dungeons Guide, to load the new version");
    }

    @DGCommand("dg scale reset")
    @DGCommand("dg setscale reset")
    @DGCommand("dg uiscale reset")
    @DGCommand("dg setuiscale reset")
    public void resetUIScale() {
        FeatureRegistry.GLOBAL_HUD_SCALE.<Double>getParameter("scale").setValue(1d);
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §eGlobal HUD scale successfully reset to 1.");
        
    }
    
    @DGCommand("dg scale {scale}")
    @DGCommand("dg setuiscale {scale}")
    @DGCommand("dg setscale {scale}")
    @DGCommand("dg uiscale {scale}")
    public void setUIScale(@CommandParam(value = "scale", min=0.5, max = 10) double theScale, CommandContext<UCommandContext> context) {
        String alias = context.getNodes().get(1).getNode().getName();
        
        if (theScale < 0.01 || theScale > (Math.PI + Math.E)) {
            ChatTransmitter.addToQueue("§eDungeons Guide §7:: §cSorry, but while " + theScale + " is a valid number, it is not a suitable GUI scale. Try again, §eor reset your Global HUD scale with §6/dg "+alias+" reset§e.");
            return;
        }
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §aSuccessfully set your Global HUD scale to " + theScale + ". §eTo reset your Global HUD scale, run §6/dg "+alias+" reset§e.");
        FeatureRegistry.GLOBAL_HUD_SCALE.<Boolean>getParameter("mc").setValue(false);
        FeatureRegistry.GLOBAL_HUD_SCALE.<Double>getParameter("scale").setValue(theScale);
    }

    @DGCommand("dg reparty")
    public void repartyCommand() {
        FeatureRegistry.ETC_REPARTY.processCommand();
    }
    @DGCommand("dg pvall")
    public void pvAllCommand() {
        PartyManager.INSTANCE.requestPartyList(context -> {
            if (context == null) {
                ChatTransmitter.addToQueue("§eDungeons Guide §7:: §cYou are not in a party!");
                return;
            }
            FeatureViewPlayerStatsOnJoin.processPartyMembers(context);
        });
    }

    @DGCommand("dg atj")
    @DGCommand("dg asktojoin")
    public void askToJoinCommand() {
        if (!DiscordIntegrationManager.INSTANCE.isLoaded()) {
            ChatTransmitter.addToQueue("§eDungeons Guide §7:: §cDiscord GameSDK has been disabled, or it failed to load!");
            return;
        }
        if (!PartyManager.INSTANCE.canInvite()) {
            ChatTransmitter.addToQueue("§eDungeons Guide §7:: §cYou don't have permission to invite people to the party!");
        } else {
            PartyManager.INSTANCE.toggleAllowAskToJoin();
            ChatTransmitter.addToQueue("§eDungeons Guide §7:: §fToggled Ask to join to " + (PartyManager.INSTANCE.getAskToJoinSecret() != null ? "§eon" : "§coff"));
        }

        if (!FeatureRegistry.DISCORD_RICHPRESENCE.isEnabled()) {
            ChatTransmitter.addToQueue("§eDungeons Guide §7:: §cDiscord Rich Presence is disabled! Enable at /dg -> Discord ");
        }
        if (!FeatureRegistry.DISCORD_ASKTOJOIN.isEnabled()) {
            ChatTransmitter.addToQueue("§eDungeons Guide §7:: §cDiscord Invite Viewer is disabled! Enable at /dg -> Discord "); // how
        }
    }

    @DGCommand("dg pv {ign}")
    public void pvCommand(String ign, CommandContext<UCommandContext> ctx) {
        try {
            ApiFetcher.fetchUUIDAsync(ign)
                    .thenAccept(a -> {
                        assert a.orElse(null) != null;
                        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e" + ign + "§f's Profile ").appendSibling(new ChatComponentText("§7view").setChatStyle(new ChatStyle().setChatHoverEvent(
                                new HoverEventRenderPlayer(
                                        new GameProfile(FeatureViewPlayerStatsOnJoin.fromString(a.orElse(null)), ign)
                                )))));
                    });
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
    }

    @DGCommand("dg purge")
    public void purgeCommand() {
        ApiFetcher.purgeCache();
        CosmeticsManager cosmeticsManager = DungeonsGuide.getDungeonsGuide().getCosmeticsManager();
        cosmeticsManager.requestPerms();
        cosmeticsManager.requestCosmeticsList();
        cosmeticsManager.requestActiveCosmetics();
        StaticResourceCache.INSTANCE.purgeCache();
        ImageTexture.imageMap.clear();
        SkinFetcher.purgeCache();
        DomElementRegistry.onResourceManagerReload();

        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §fSuccessfully purged API Cache!");
    }

    @DGCommand("dg pbroadcast {payload}")
    public void pBroadcastCommand(@CommandParam(value = "payload", stringType = CommandParam.EnumStringType.GREEDY) String args) {
        try {
            String actualPayload = args.replace("$C$", "§");
            StompManager.getInstance().send(new StompPayload().header("destination", "/app/party.broadcast").payload(
                    new JSONObject().put("partyID", PartyManager.INSTANCE.getPartyContext().getPartyID())
                            .put("payload", actualPayload).toString()
            ));
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }


    }

    @DGCommand("dg pm")
    @DGCommand("dg partymax")
    public void showPartyMax() {
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §fCurrent party max is §e" + PartyManager.INSTANCE.getMaxParty());
    }

    @DGCommand("dg pm {max}")
    @DGCommand("dg partymax {max}")
    public void setPartyMax(@CommandParam(value = "max", min = 2, max = 10000) int max) {
        if (max < 2) {
            ChatTransmitter.addToQueue("§eDungeons Guide §7:: §cparty max can't be smaller than 2");
            return;
        }
        PartyManager.INSTANCE.setMaxParty(max);
        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §fSuccessfully set the party max to §e" + PartyManager.INSTANCE.getMaxParty());
    }

    @DGCommand("dg unload")
    public void unloadCommand() {
        Main.getMain().unloadWithoutStacktraceReference();
    }
}
