package kr.syeyoung.dungeonsguide.features.impl;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.regex.Pattern;

public class FeatureDisableMessage extends SimpleFeature implements ChatListener {
    @Data
    @AllArgsConstructor
    public static class MessageData {
        private Pattern pattern;
        private String name;
        private String description;
        private String key;
    }

    private static final MessageData[] PRE_DEFINED = new MessageData[] {
            new MessageData(Pattern.compile("§r§cThere are blocks in the way!§r"), "Aote block message", "\"There are blocks in the way!\"", "aote"),
            new MessageData(Pattern.compile("§r§cThis ability is currently on cooldown for .+ more seconds?\\.§r"), "Ability cooldown message", "\"This ability is currently on cooldown for 3 more seconds.\"", "cooldown"),
            new MessageData(Pattern.compile("§r§cWhow! Slow down there!§r"), "Grappling hook cooldown", "\"Whow! Slow down there!\"", "grappling"),
            new MessageData(Pattern.compile("§r§cNo more charges, next one in §r§e.+§r§cs!§r"), "Zombie Sword Charging", "\"No more charges, next one in 3s!\"", "zombie"),
            new MessageData(Pattern.compile("§r§7Your .+ hit §r§c.+ §r§7enem(?:y|ies) for §r§c.+ §r§7damage\\.§r"), "Ability Damage", "\"Your blahblah hit 42 enemy for a lots of damage\"", "ability"),
            new MessageData(Pattern.compile("§r§cYou do not have enough mana to do this!§r"), "Not enough mana", "\"You do not have enough mana to do this!\"", "mana"),
            new MessageData(Pattern.compile("§r§aUsed §r.+§r§a!§r"), "Dungeon Ability Usage", "\"Used Guided Sheep!\" and such", "dungeonability"),
            new MessageData(Pattern.compile("§r.+§r§a is ready to use! Press §r.+§r§a to activate it!§r"), "Ready to use message", "\"Blah is ready to use! Press F to activate it!", "readytouse"),
            new MessageData(Pattern.compile("§r.+ §r§ais now available!§r"), "Ability Available","\"blah is now available!\"", "available"),
            new MessageData(Pattern.compile("§r§cThe Stone doesn't seem to do anything here\\.§r"), "Stone Message", "\"The Stone doesn't seem to do anything here\"", "stone")
    };

    public FeatureDisableMessage() {
        super("fixes", "Disable ability messages", "Do not let ability messages show up in chatbox\nclick on Edit for more precise settings", "fixes.messagedisable", true);
        for (MessageData messageData : PRE_DEFINED) {
            this.parameters.put(messageData.key, new FeatureParameter<Boolean>(messageData.key, messageData.name, messageData.description, true, "boolean"));
        }
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();

    @Override
    public void onChat(ClientChatReceivedEvent clientChatReceivedEvent) {
        if (clientChatReceivedEvent.type == 2) return;
        if (!isEnabled()) return;
        if (!skyblockStatus.isOnSkyblock()) return;
        String msg = clientChatReceivedEvent.message.getFormattedText();
        System.out.println(msg);
        for (MessageData md:PRE_DEFINED) {
            if (this.<Boolean>getParameter(md.key).getValue() && md.pattern.matcher(msg).matches()) {
                clientChatReceivedEvent.setCanceled(true);
                return;
            }
        }
    }
}
