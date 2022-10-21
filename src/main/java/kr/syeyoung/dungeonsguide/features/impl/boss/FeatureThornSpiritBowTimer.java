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

package kr.syeyoung.dungeonsguide.features.impl.boss;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.features.listener.TitleListener;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.features.text.TextStyle;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bossfight.BossfightProcessorThorn;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureThornSpiritBowTimer extends TextHUDFeature implements ChatListener, TitleListener {
    public FeatureThornSpiritBowTimer() {
        super("Dungeon.Bossfight.Floor 4", "Display Spirit bow timer", "Displays how long until spirit bow gets destroyed", "bossfight.spiritbowdisplay", false, getFontRenderer().getStringWidth("Spirit Bow Destruction: 2m 00s"), getFontRenderer().FONT_HEIGHT);
        getStyles().add(new TextStyle("title", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("time", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

    private static final List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("Spirit Bow Destruction","title"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("1s","time"));
    }
    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon() && skyblockStatus.getContext() != null && skyblockStatus.getContext().getBossfightProcessor() instanceof BossfightProcessorThorn && time > System.currentTimeMillis();
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Arrays.asList("title", "separator", "time");
    }

    @Override
    public List<StyledText> getDummyText() {
        return dummyText;
    }

    @Override
    public List<StyledText> getText() {
        List<StyledText> actualBit = new ArrayList<StyledText>();
        actualBit.add(new StyledText("Spirit Bow Destruction","title"));
        actualBit.add(new StyledText(": ","separator"));
        actualBit.add(new StyledText(TextUtils.formatTime(time - System.currentTimeMillis()),"time"));
        return actualBit;
    }
    private long time = 0;

    @Override
    public void onChat(ClientChatReceivedEvent clientChatReceivedEvent) {
        if (!(skyblockStatus.isOnDungeon() && skyblockStatus.getContext() != null && skyblockStatus.getContext().getBossfightProcessor() instanceof BossfightProcessorThorn)) return;
        String text = clientChatReceivedEvent.message.getFormattedText();
        if (text.equals("§r§a§lThe §r§5§lSpirit Bow §r§a§lhas dropped!§r")) {
            time = System.currentTimeMillis() + 16000;
        } else if (text.startsWith("§r§c[BOSS] Thorn§r§f: ")) {
            if (text.contains("another wound")
            || text.contains("My energy, it goes away")
            || text.contains("dizzy")
            || text.contains("a delicate feeling")) {
                time = 0;
            }
        } else if (text.startsWith("§r§b[CROWD]")) {
            if (text.contains("That wasn't fair!!!") || text.contains("how to damage") || text.contains("Cheaters!") || text.contains("BOOOO")) {
                time = 0;
            } else if (text.contains("missing easy shots like that") || text.contains("missed the shot!") || text.contains("Keep dodging") || text.contains("no thumbs") || text.contains("can't aim")) {
                time = 0;
            }
        } else if (text.equals("§r§cThe §r§5Spirit Bow§r§c disintegrates as you fire off the shot!§r")) {
            time = 0;
        }
    }

    @Override
    public void onTitle(S45PacketTitle renderPlayerEvent) {
        if (!(skyblockStatus.isOnDungeon() && skyblockStatus.getContext() != null && skyblockStatus.getContext().getBossfightProcessor() instanceof BossfightProcessorThorn)) return;
        if (renderPlayerEvent.getMessage().getFormattedText().contains("picked up")) {
            time = System.currentTimeMillis() + 21000;
        }
    }
}
