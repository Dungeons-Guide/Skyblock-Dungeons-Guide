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

package kr.syeyoung.dungeonsguide.mod.features.impl.boss;


import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.BossfightProcessorThorn;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGChatReceivedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.TitleEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultTextHUDFeatureStyleFeature;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.NullTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.gui.elements.richtext.TextSpan;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.util.IChatComponent;

public class FeatureThornSpiritBowTimer extends TextHUDFeature {
    public FeatureThornSpiritBowTimer() {
        super("Bossfight.Floor 4", "Display Spirit bow timer", "Displays how long until spirit bow gets destroyed", "bossfight.spiritbowdisplay");
        registerDefaultStyle("title", DefaultingDelegatingTextStyle.derive("Feature Default - Title", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.NAME)));
        registerDefaultStyle("separator", DefaultingDelegatingTextStyle.derive("Feature Default - Separator", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.SEPARATOR)));
        registerDefaultStyle("time", DefaultingDelegatingTextStyle.derive("Feature Default - Time", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.VALUE)));
    }

    @Override
    public boolean isHUDViewable() {
        return SkyblockStatus.isOnDungeon() && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() != null && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBossfightProcessor() instanceof BossfightProcessorThorn && time > System.currentTimeMillis();
    }


    @Override
    public TextSpan getDummyText() {
        TextSpan dummyText = new TextSpan(new NullTextStyle(), "");
        dummyText.addChild(new TextSpan(getStyle("title"), "Spirit Bow Destruction"));
        dummyText.addChild(new TextSpan(getStyle("separator"), ": "));
        dummyText.addChild(new TextSpan(getStyle("time"), "1s"));
        return dummyText;
    }

    @Override
    public TextSpan getText() {
        TextSpan actualBit = new TextSpan(new NullTextStyle(), "");
        actualBit.addChild(new TextSpan(getStyle("title"), "Spirit Bow Destruction"));
        actualBit.addChild(new TextSpan(getStyle("separator"), ": "));
        actualBit.addChild(new TextSpan(getStyle("time"), TextUtils.formatTime(time - System.currentTimeMillis())));
        return actualBit;
    }
    private long time = 0;

    @DGEventHandler()
    public void onChat(DGChatReceivedEvent event) {
        if (!(SkyblockStatus.isOnDungeon() && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() != null && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBossfightProcessor() instanceof BossfightProcessorThorn)) return;
        String text = event.getOriginalFormattedText();
        if (text.equals("§a§lThe §5§lSpirit Bow §a§lhas dropped!")) {
            time = System.currentTimeMillis() + 16000;
        } else if (text.startsWith("§c[BOSS] Thorn§f: ")) {
            if (text.contains("another wound")
            || text.contains("My energy, it goes away")
            || text.contains("dizzy")
            || text.contains("a delicate feeling")) {
                time = 0;
            }
        } else if (text.startsWith("§b[CROWD]")) {
            if (text.contains("That wasn't fair!!!") || text.contains("how to damage") || text.contains("Cheaters!") || text.contains("BOOOO")) {
                time = 0;
            } else if (text.contains("missing easy shots like that") || text.contains("missed the shot!") || text.contains("Keep dodging") || text.contains("no thumbs") || text.contains("can't aim")) {
                time = 0;
            }
        } else if (text.equals("§cThe §5Spirit Bow§c disintegrates as you fire off the shot!")) {
            time = 0;
        }
    }

    @DGEventHandler
    public void onTitle(TitleEvent event) {
        if (!(SkyblockStatus.isOnDungeon() && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() != null && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBossfightProcessor() instanceof BossfightProcessorThorn)) return;
        IChatComponent text = event.getPacketTitle().getMessage();
        if (text != null && text.getFormattedText() != null && text.getFormattedText().contains("picked up")) {
            time = System.currentTimeMillis() + 21000;
        }
    }
}
