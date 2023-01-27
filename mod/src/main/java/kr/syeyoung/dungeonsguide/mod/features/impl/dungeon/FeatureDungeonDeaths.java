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

package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon;


import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.impl.DungeonDeathEvent;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonLeftEvent;
import kr.syeyoung.dungeonsguide.mod.features.text.StyledText;
import kr.syeyoung.dungeonsguide.mod.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.features.text.TextStyle;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabList;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabListEntry;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeatureDungeonDeaths extends TextHUDFeature {
    public FeatureDungeonDeaths() {
        super("Dungeon.HUDs", "Display Deaths", "Display names of player and death count in dungeon run", "dungeon.stats.deaths");
        this.setEnabled(false);
        getStyles().add(new TextStyle("username", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("total", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("deaths", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("totalDeaths", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
    @Override
    public boolean isHUDViewable() {
        if (!skyblockStatus.isOnDungeon()) return false;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        return context != null;
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Arrays.asList("username", "separator", "deaths", "total", "totalDeaths");
    }
    @Getter
    private final Map<String, Integer> deaths = new HashMap<>();

    @Override
    public List<StyledText> getText() {

        List<StyledText> text=  new ArrayList<StyledText>();

        Map<String, Integer> deaths = getDeaths();
        int i = 0;
        int deathsCnt = 0;
        for (Map.Entry<String, Integer> death:deaths.entrySet()) {
            text.add(new StyledText(death.getKey(),"username"));
            text.add(new StyledText(": ","separator"));
            text.add(new StyledText(death.getValue()+"\n","deaths"));
            deathsCnt += death.getValue();
        }
        text.add(new StyledText("Total Deaths","total"));
        text.add(new StyledText(": ","separator"));
        text.add(new StyledText(getTotalDeaths()+"","totalDeaths"));

        return text;
    }

    private static final List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("syeyoung","username"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("-130\n","deaths"));
        dummyText.add(new StyledText("rioho","username"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("-999999\n","deaths"));
        dummyText.add(new StyledText("dungeonsguide","username"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("-42\n","deaths"));
        dummyText.add(new StyledText("penguinman","username"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("0\n","deaths"));
        dummyText.add(new StyledText("probablysalt","username"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("0\n","deaths"));
        dummyText.add(new StyledText("Total Deaths","total"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("0","totalDeaths"));
    }

    @Override
    public List<StyledText> getDummyText() {
        return dummyText;
    }

    public int getTotalDeaths() {
        if (!skyblockStatus.isOnDungeon()) return 0;
        for (TabListEntry tabListEntry : TabList.INSTANCE.getTabListEntries()) {
            String name = tabListEntry.getEffectiveName();
            if (name.contains("Deaths")) {
                String whatever = TextUtils.keepIntegerCharactersOnly(TextUtils.keepScoreboardCharacters(TextUtils.stripColor(name)));
                if (whatever.isEmpty()) break;
                return Integer.parseInt(whatever);
            }
        }
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return 0;
        int d = 0;
        for (Integer value : getDeaths().values()) {
            d += value;
        }
        return d;
    }

    Pattern deathPattern = Pattern.compile("§r§c ☠ (.+?)§r§7 .+and became a ghost.+");
    Pattern meDeathPattern = Pattern.compile("§r§c ☠ §r§7You .+and became a ghost.+");

    @DGEventHandler(ignoreDisabled = true)
    public void onDungeonEnd(DungeonLeftEvent dungeonEndedEvent) {
        this.deaths.clear();
    }
    @DGEventHandler()
    public void onChat(ClientChatReceivedEvent clientChatReceivedEvent) {
        if (clientChatReceivedEvent.type == 2) return;
        if (!skyblockStatus.isOnDungeon()) return;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return;

        String txt = clientChatReceivedEvent.message.getFormattedText();
        Matcher m = deathPattern.matcher(txt);
        if (m.matches()) {
            String nickname = TextUtils.stripColor(m.group(1));
            int deaths = getDeaths().getOrDefault(nickname, 0);
            getDeaths().put(nickname, deaths + 1);
            context.getRecorder().createEvent(new DungeonDeathEvent(nickname, txt, deaths));
            ChatTransmitter.sendDebugChat(new ChatComponentText("Death verified :: "+nickname+" / "+(deaths + 1)));
        }
        Matcher m2 = meDeathPattern.matcher(txt);
        if (m2.matches()) {
            String nickname = "me";
            int deaths = getDeaths().getOrDefault(nickname, 0);
            getDeaths().put(nickname, deaths + 1);
            context.getRecorder().createEvent(new DungeonDeathEvent(Minecraft.getMinecraft().thePlayer.getName(), txt, deaths));
            ChatTransmitter.sendDebugChat(new ChatComponentText("Death verified :: me / "+(deaths + 1)));
        }
    }
}
