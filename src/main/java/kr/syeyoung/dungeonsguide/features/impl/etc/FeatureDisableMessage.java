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

package kr.syeyoung.dungeonsguide.features.impl.etc;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.ChatListener;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

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
            new MessageData(Pattern.compile("§r§cThis ability is on cooldown for .+s\\.§r"), "Ability cooldown message2", "\"This ability is on cooldown for 3s.\"", "cooldown2"),
            new MessageData(Pattern.compile("§r§cWhow! Slow down there!§r"), "Grappling hook cooldown", "\"Whow! Slow down there!\"", "grappling"),
            new MessageData(Pattern.compile("§r§cNo more charges, next one in §r§e.+§r§cs!§r"), "Zombie Sword Charging", "\"No more charges, next one in 3s!\"", "zombie"),
            new MessageData(Pattern.compile("§r§7Your .+ hit §r§c.+ §r§7enem(?:y|ies) for §r§c.+ §r§7damage\\.§r"), "Ability Damage", "\"Your blahblah hit 42 enemy for a lots of damage\"", "ability"),
            new MessageData(Pattern.compile("§r§cYou do not have enough mana to do this!§r"), "Not enough mana", "\"You do not have enough mana to do this!\"", "mana"),
            new MessageData(Pattern.compile("§r§aUsed §r.+§r§a!§r"), "Dungeon Ability Usage", "\"Used Guided Sheep!\" and such", "dungeonability"),
            new MessageData(Pattern.compile("§r.+§r§a is ready to use! Press §r.+§r§a to activate it!§r"), "Ready to use message", "\"Blah is ready to use! Press F to activate it!", "readytouse"),
            new MessageData(Pattern.compile("§r.+ §r§ais now available!§r"), "Ability Available","\"blah is now available!\"", "available"),
            new MessageData(Pattern.compile("§r§cThe Stone doesn't seem to do anything here\\.§r"), "Stone Message", "\"The Stone doesn't seem to do anything here\"", "stone"),
            new MessageData(Pattern.compile("§r§cNo target found!§r"), "Voodoo Doll No Target", "\"No target found!\"", "voodotarget")
    };

    public FeatureDisableMessage() {
        super("Misc.Chat", "Disable ability messages", "Do not let ability messages show up in chatbox\nclick on Edit for more precise settings", "fixes.messagedisable", true);
        for (MessageData messageData : PRE_DEFINED) {
            this.parameters.put(messageData.key, new FeatureParameter<Boolean>(messageData.key, messageData.name, messageData.description, true, "boolean"));
        }
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

    @Override
    public void onChat(ClientChatReceivedEvent clientChatReceivedEvent) {
        if (clientChatReceivedEvent.type == 2) return;
        if (!isEnabled()) return;
        if (!skyblockStatus.isOnSkyblock()) return;
        String msg = clientChatReceivedEvent.message.getFormattedText();
        for (MessageData md:PRE_DEFINED) {
            if (this.<Boolean>getParameter(md.key).getValue() && md.pattern.matcher(msg).matches()) {
                clientChatReceivedEvent.setCanceled(true);
                return;
            }
        }
    }
}
