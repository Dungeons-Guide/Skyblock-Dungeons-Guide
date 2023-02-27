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
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.impl.DungeonDeathEvent;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonLeftEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultTextHUDFeatureStyleFeature;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.NullTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextSpan;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabList;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabListEntry;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeatureDungeonDeaths extends TextHUDFeature {
    public FeatureDungeonDeaths() {
        super("Dungeon HUD.In Dungeon HUD", "Display Deaths", "Display names of player and death count in dungeon run", "dungeon.stats.deaths");
        this.setEnabled(false);
        registerDefaultStyle("username", DefaultingDelegatingTextStyle.derive("Feature Default - Username", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.NAME)));
        registerDefaultStyle("total", DefaultingDelegatingTextStyle.derive("Feature Default - Total", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.NAME)));
        registerDefaultStyle("separator", DefaultingDelegatingTextStyle.derive("Feature Default - Separator", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.SEPARATOR)));
        registerDefaultStyle("deaths", DefaultingDelegatingTextStyle.derive("Feature Default - Deaths", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.VALUE)));
        registerDefaultStyle("totalDeaths", DefaultingDelegatingTextStyle.derive("Feature Default - TotalDeaths", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.VALUE)));
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
    @Override
    public boolean isHUDViewable() {
        if (!skyblockStatus.isOnDungeon()) return false;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        return context != null;
    }

    @Getter
    private final Map<String, Integer> deaths = new HashMap<>();

    @Override
    public TextSpan getText() {

        TextSpan text=  new TextSpan(new NullTextStyle(), "");

        Map<String, Integer> deaths = getDeaths();
        int i = 0;
        int deathsCnt = 0;
        for (Map.Entry<String, Integer> death:deaths.entrySet()) {
            text.addChild(new TextSpan(getStyle("username"), death.getKey()));
            text.addChild(new TextSpan(getStyle("separator"), ": "));
            text.addChild(new TextSpan(getStyle("deaths"), death.getValue()+"\n"));
            deathsCnt += death.getValue();
        }
        text.addChild(new TextSpan(getStyle("total"), "Total Deaths"));
        text.addChild(new TextSpan(getStyle("separator"), ": "));
        text.addChild(new TextSpan(getStyle("totalDeaths"), getTotalDeaths()+""));

        return text;
    }

    @Override
    public TextSpan getDummyText() {
        TextSpan dummyText=  new TextSpan(new NullTextStyle(), "");
        dummyText.addChild(new TextSpan(getStyle("username"), "syeyoung"));
        dummyText.addChild(new TextSpan(getStyle("separator"), ": "));
        dummyText.addChild(new TextSpan(getStyle("deaths"), "-130\n"));
        dummyText.addChild(new TextSpan(getStyle("username"), "rioho"));
        dummyText.addChild(new TextSpan(getStyle("separator"), ": "));
        dummyText.addChild(new TextSpan(getStyle("deaths"), "-999999\n"));
        dummyText.addChild(new TextSpan(getStyle("username"), "dungeonsguide"));
        dummyText.addChild(new TextSpan(getStyle("separator"), ": "));
        dummyText.addChild(new TextSpan(getStyle("deaths"), "-42\n"));
        dummyText.addChild(new TextSpan(getStyle("username"), "penguinman"));
        dummyText.addChild(new TextSpan(getStyle("separator"), ": "));
        dummyText.addChild(new TextSpan(getStyle("deaths"), "0\n"));
        dummyText.addChild(new TextSpan(getStyle("username"), "probablysalt"));
        dummyText.addChild(new TextSpan(getStyle("separator"), ": "));
        dummyText.addChild(new TextSpan(getStyle("deaths"), "0\n"));
        dummyText.addChild(new TextSpan(getStyle("total"), "Total Deaths"));
        dummyText.addChild(new TextSpan(getStyle("separator"), ": "));
        dummyText.addChild(new TextSpan(getStyle("totalDeaths"), "0"));
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
