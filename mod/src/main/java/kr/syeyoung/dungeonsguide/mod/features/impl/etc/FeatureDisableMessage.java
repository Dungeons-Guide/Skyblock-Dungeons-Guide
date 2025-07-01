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

package kr.syeyoung.dungeonsguide.mod.features.impl.etc;


import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.TCBoolean;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGChatReceivedEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.regex.Pattern;

public class FeatureDisableMessage extends SimpleFeature {
    @Data
    @AllArgsConstructor
    public static class MessageData {
        private Pattern pattern;
        private String name;
        private String description;
        private String key;
    }

    private static final MessageData[] PRE_DEFINED = new MessageData[] {
            new MessageData(Pattern.compile("§cThere are blocks in the way!"), "Aote block message", "\"There are blocks in the way!\"", "aote"),
            new MessageData(Pattern.compile("§cThis ability is currently on cooldown for .+ more seconds?\\."), "Ability cooldown message", "\"This ability is currently on cooldown for 3 more seconds.\"", "cooldown"),
            new MessageData(Pattern.compile("§cThis ability is on cooldown for .+s\\."), "Ability cooldown message2", "\"This ability is on cooldown for 3s.\"", "cooldown2"),
            new MessageData(Pattern.compile("§cWhow! Slow down there!"), "Grappling hook cooldown", "\"Whow! Slow down there!\"", "grappling"),
            new MessageData(Pattern.compile("§cNo more charges, next one in §e.+§cs!"), "Zombie Sword Charging", "\"No more charges, next one in 3s!\"", "zombie"),
            new MessageData(Pattern.compile("§7Your .+ hit §c.+ §7enem(?:y|ies) for §c.+ §7damage\\."), "Ability Damage", "\"Your blahblah hit 42 enemy for a lots of damage\"", "ability"),
            new MessageData(Pattern.compile("§cYou do not have enough mana to do this!"), "Not enough mana", "\"You do not have enough mana to do this!\"", "mana"),
            new MessageData(Pattern.compile("§aUsed .+§a!"), "Dungeon Ability Usage", "\"Used Guided Sheep!\" and such", "dungeonability"),
            new MessageData(Pattern.compile(".+§a is ready to use! Press .+§a to activate it!"), "Ready to use message", "\"Blah is ready to use! Press F to activate it!", "readytouse"),
            new MessageData(Pattern.compile(".+ §ais now available!"), "Ability Available","\"blah is now available!\"", "available"),
            new MessageData(Pattern.compile("§cThe Stone doesn't seem to do anything here\\."), "Stone Message", "\"The Stone doesn't seem to do anything here\"", "stone"),
            new MessageData(Pattern.compile("§cNo target found!"), "Voodoo Doll No Target", "\"No target found!\"", "voodotarget"),
            new MessageData(Pattern.compile("§eYour §6Auto Recombobulator §erecombobulated .+§e!"), "Auto Recombobulator", "\"Your Auto Recombobulator recombobulated blahblah!\"", "autorecombobulator")
    };

    public FeatureDisableMessage() {
        super("Misc.Chat Utils", "Disable item messages", "Do not let item messages show up in chatbox\nclick on Edit for more precise settings", "fixes.messagedisable", true);
        for (MessageData messageData : PRE_DEFINED) {
            addParameter(messageData.key, new FeatureParameter<Boolean>(messageData.key, messageData.name, messageData.description, true, TCBoolean.INSTANCE));
        }
    }


    @DGEventHandler()
    public void onChat(DGChatReceivedEvent clientChatReceivedEvent) {
        if (!SkyblockStatus.isOnSkyblock()) return;
        String msg = clientChatReceivedEvent.getFormattedText();
        for (MessageData md:PRE_DEFINED) {
            if (this.<Boolean>getParameter(md.key).getValue() && md.pattern.matcher(msg).matches()) {
                clientChatReceivedEvent.setCanceled(true);
                return;
            }
        }
    }
}
