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

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import kr.syeyoung.dungeonsguide.mod.events.impl.ClientTickEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.RegisterCommandEvent;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import com.mojang.authlib.GameProfile;
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
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import kr.syeyoung.dungeonsguide.mod.events.annotations.SubscribeEvent;
import org.json.JSONObject;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class CommandDungeonsGuide {

    private static final String[] NAMES = {
            "dungeonsguide",
            "dg", //per issue #400
            "dungeonguide", //because people keep saying "dungeon guide" the singular
            "deegee", //in case another mod/server somehow uses a command "/dg" and no one wants to type out the entire name "dungeonsguide"
            "던전가이드", //easter egg
            "던전안내" //easter egg
            // for each new alias, add a new line
    };

    @SubscribeEvent
    public void onRegisterCommand(RegisterCommandEvent event) {
        for (String name : NAMES)
            event.dispatcher.register(build(name));
    }

    private LiteralArgumentBuilder<FabricClientCommandSource> build(String name) {
        LiteralArgumentBuilder<FabricClientCommandSource> root = literal(name)
                .executes(ctx -> run(() -> target = new ConfigGuiScreenAdapter(null)));

        root.then(literal("reparty").executes(ctx -> run(this::repartyCommand)));
        root.then(literal("gui").executes(ctx -> run(() -> target = new ConfigGuiScreenAdapter(null, new GlobalHUDScale(new HUDLocationConfig(null))))));
        root.then(literal("pv").then(argument("ign", StringArgumentType.word())
                .executes(ctx -> run(() -> pvCommand(StringArgumentType.getString(ctx, "ign"), ctx.getSource())))));
        root.then(literal("pvall").executes(ctx -> run(this::pvAllCommand)));
        for (String sub : new String[] {"asktojoin", "atj"})
            root.then(literal(sub).executes(ctx -> run(this::askToJoinCommand)));
        root.then(literal("purge").executes(ctx -> run(this::purgeCommand)));
        root.then(literal("pbroadcast").then(argument("payload", StringArgumentType.greedyString())
                .executes(ctx -> run(() -> pBroadcastCommand(new String[] {"pbroadcast", StringArgumentType.getString(ctx, "payload")})))));
        for (String sub : new String[] {"partymax", "pm"})
            root.then(literal(sub)
                    .executes(ctx -> run(() -> partyMaxCommand(new String[] {sub})))
                    .then(argument("number", StringArgumentType.word())
                            .executes(ctx -> run(() -> partyMaxCommand(new String[] {sub, StringArgumentType.getString(ctx, "number")})))));
        root.then(literal("unload").executes(ctx -> run(this::unloadCommand)));
        for (String sub : new String[] {"setuiscale", "setscale", "uiscale", "scale"})
            root.then(literal(sub)
                    .executes(ctx -> run(() -> setUIScale(new String[] {sub})))
                    .then(argument("scale", StringArgumentType.word())
                            .executes(ctx -> run(() -> setUIScale(new String[] {sub, StringArgumentType.getString(ctx, "scale")})))));
        root.then(literal("aliases").executes(ctx -> run(this::aliasesCommand)));
        root.then(literal("help").executes(ctx -> run(this::helpCommand)));
        return root;
    }

    private int run(Runnable runnable) {
        runnable.run();
        return Command.SINGLE_SUCCESS;
    }

    private void aliasesCommand() {
        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide aliases§7::"));
        ChatTransmitter.addToQueue(new ChatComponentText(" §7- §e/dungeonsguide"));
        ChatTransmitter.addToQueue(new ChatComponentText(" §7- §e/dungeonguide"));
        ChatTransmitter.addToQueue(new ChatComponentText(" §7- §e/deegee"));
    }

    private void helpCommand() {
        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg §7-§fOpens configuration gui"));
        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg gui §7-§fOpens configuration gui"));
        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg scale [scale] §7-§fSets the Global HUD scale (Also disables Minecraft default HUD scale for you as well if you haven't already.)"));
        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg help §7-§fShows command help"));
        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg reparty §7-§f Reparty."));
        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg asktojoin or /dg atj §7-§f Toggle ask to join §cRequires Discord Rich Presence enabled. (/dg -> Advanced)"));
        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg partymax [number] or /dg pm [number] §7-§f Sets the party max §7(maximum amount people in party, for discord rpc)"));
        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg pv [ign] §7-§f Profile Viewer"));
        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg pvall §7-§f Profile Viewer For all people on party"));
        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg purge §7-§f Purge api cache."));
        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e/dg unload §7-§f Unload Current Version of Dungeons Guide, to load the new version"));
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
                FeatureRegistry.GLOBAL_HUD_SCALE.<Double>getParameter("scale").setValue(1d);
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

    @Setter
    private GuiScreen target;

    @SubscribeEvent
    public void onTick(ClientTickEvent e) {
        try {
            if (target != null && e.phase == ClientTickEvent.Phase.START) {
                Minecraft.getMinecraft().displayGuiScreen(target);
                target = null;
            }
        } catch (Exception t) {
            FeatureCollectDiagnostics.queueSendLogAsync(t);
            t.printStackTrace();
        }
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

    private void pvCommand(String ign, FabricClientCommandSource sender) {
        try {
            ApiFetcher.fetchUUIDAsync(ign)
                    .thenAccept(a -> {
                        assert a.orElse(null) != null;
                        sender.sendFeedback(new ChatComponentText("§eDungeons Guide §7:: §e" + ign + "§f's Profile ").appendSibling(new ChatComponentText("§7view").setChatStyle(new ChatStyle().setChatHoverEvent(
                                new HoverEventRenderPlayer(
                                        new GameProfile(FeatureViewPlayerStatsOnJoin.fromString(a.orElse(null)), ign)
                                )))));
                    });
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
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
        DomElementRegistry.onResourceManagerReload();

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
            FeatureCollectDiagnostics.queueSendLogAsync(e);
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
