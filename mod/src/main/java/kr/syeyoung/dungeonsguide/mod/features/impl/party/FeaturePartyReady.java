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

package kr.syeyoung.dungeonsguide.mod.features.impl.party;


import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonStartedEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultTextHUDFeatureStyleFeature;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.NullTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextSpan;
import kr.syeyoung.dungeonsguide.mod.party.PartyContext;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.*;

public class FeaturePartyReady extends TextHUDFeature {
    public FeaturePartyReady() {
        super("Dungeon Party","Party Ready List", "Check if your party member have said r or not", "party.readylist");
        registerDefaultStyle("player", DefaultingDelegatingTextStyle.derive("Feature Default - Player", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.NAME)));
        registerDefaultStyle("separator", DefaultingDelegatingTextStyle.derive("Feature Default - Separator", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.SEPARATOR)));
        registerDefaultStyle("ready", DefaultingDelegatingTextStyle.derive("Feature Default - Ready", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.DEFAULT))
                .setTextShader(new AColor(0x55, 0xFF,0xFF,255)));
        registerDefaultStyle("notready", DefaultingDelegatingTextStyle.derive("Feature Default - Notready", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.DEFAULT))
                .setTextShader(new AColor(0xFF, 0x55,0x55,255)));
        registerDefaultStyle("terminal", DefaultingDelegatingTextStyle.derive("Feature Default - Terminal", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.VALUE)));
        setEnabled(true);
    }

    @Override
    public boolean isHUDViewable() {
        return  PartyManager.INSTANCE.getPartyContext() != null && PartyManager.INSTANCE.getPartyContext().isPartyExistHypixel() && "Dungeon Hub".equals(SkyblockStatus.locationName);
    }


    @Override
    public TextSpan getDummyText() {
        TextSpan dummyText = new TextSpan(new NullTextStyle(), "");
        dummyText.addChild(new TextSpan(getStyle("player"), "syeyoung"));
        dummyText.addChild(new TextSpan(getStyle("separator"), ": "));
        dummyText.addChild(new TextSpan(getStyle("ready"), "Ready"));
        dummyText.addChild(new TextSpan(getStyle("terminal"), " 4"));
        dummyText.addChild(new TextSpan(getStyle("player"), "\nrioho"));
        dummyText.addChild(new TextSpan(getStyle("separator"), ": "));
        dummyText.addChild(new TextSpan(getStyle("ready"), "Ready"));
        dummyText.addChild(new TextSpan(getStyle("terminal"), " 3"));
        dummyText.addChild(new TextSpan(getStyle("player"), "\nRaidShadowLegends"));
        dummyText.addChild(new TextSpan(getStyle("separator"), ": "));
        dummyText.addChild(new TextSpan(getStyle("notready"), "Not Ready"));
        dummyText.addChild(new TextSpan(getStyle("terminal"), " 2t"));
        dummyText.addChild(new TextSpan(getStyle("player"), "\nTricked"));
        dummyText.addChild(new TextSpan(getStyle("separator"), ": "));
        dummyText.addChild(new TextSpan(getStyle("ready"), "Ready"));
        dummyText.addChild(new TextSpan(getStyle("terminal"), " ss"));
        dummyText.addChild(new TextSpan(getStyle("player"), "\nMr. Penguin"));
        dummyText.addChild(new TextSpan(getStyle("separator"), ": "));
        dummyText.addChild(new TextSpan(getStyle("notready"), "Not Ready"));
        dummyText.addChild(new TextSpan(getStyle("terminal"), " 2b"));
        return dummyText;
    }

    private Set<String> ready = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    private Map<String, String> terminal = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @Override
    public TextSpan getText() {
        PartyContext pc = PartyManager.INSTANCE.getPartyContext();
        TextSpan text = new TextSpan(new NullTextStyle(), "");
        boolean first = true;
        for (String partyRawMember : pc.getPartyRawMembers()) {
            text.addChild(new TextSpan(getStyle("player"), (first ? "":"\n") + partyRawMember));
            text.addChild(new TextSpan(getStyle("separator"), ": "));
            if (ready.contains(partyRawMember))
                text.addChild(new TextSpan(getStyle("ready"), "Ready"));
            else
                text.addChild(new TextSpan(getStyle("notready"), "Not Ready"));
            if (terminal.get(partyRawMember) != null) {
                text.addChild(new TextSpan(getStyle("terminal"), " "+ terminal.get(partyRawMember)));
            }
            first =false;
        }
        return text;
    }

    private static final List<String> readyPhrase = Arrays.asList("r", "rdy", "ready");
    private static final List<String> negator = Arrays.asList("not ", "not", "n", "n ");
    private static final List<String> terminalPhrase = Arrays.asList("ss", "s1", "1", "2b", "2t", "3", "4", "s3", "s4", "s2", "2");
    private static final Map<String, Boolean> readynessIndicator = new HashMap<>();
    static {
        readyPhrase.forEach(val -> readynessIndicator.put(val, true));
        for (String s : negator) {
            readyPhrase.forEach(val -> readynessIndicator.put(s+val, false));
        }
        readynessIndicator.put("dont start", false);
        readynessIndicator.put("don't start", false);
        readynessIndicator.put("dont go", false);
        readynessIndicator.put("don't go", false);
        readynessIndicator.put("start", true);
        readynessIndicator.put("go", true);
    }


    @DGEventHandler()
    public void onChat(ClientChatReceivedEvent clientChatReceivedEvent) {
        String txt = clientChatReceivedEvent.message.getFormattedText();
        if (!txt.startsWith("§r§9Party §8>")) return;

        String chat = TextUtils.stripColor(txt.substring(txt.indexOf(":")+1)).trim().toLowerCase();



        String usernamearea = TextUtils.stripColor(txt.substring(13, txt.indexOf(":")));
        String username = null;
        for (String s : usernamearea.split(" ")) {
            if (s.isEmpty()) continue;
            if (s.startsWith("[")) continue;
            username = s;
            break;
        }


        Boolean status = null;
        String longestMatch = "";
        for (Map.Entry<String, Boolean> stringBooleanEntry : readynessIndicator.entrySet()) {
            if (chat.startsWith(stringBooleanEntry.getKey()) || chat.endsWith(stringBooleanEntry.getKey()) || (stringBooleanEntry.getKey().length()>=3 && chat.contains(stringBooleanEntry.getKey()))) {
                if (stringBooleanEntry.getKey().length() > longestMatch.length()) {
                    longestMatch = stringBooleanEntry.getKey();
                    status = stringBooleanEntry.getValue();
                }
            }
        }
        if (status == null);
        else if (status) ready.add(username);
        else ready.remove(username);


        String term = "";
        for (String s : terminalPhrase) {
            if (chat.equals(s) || chat.startsWith(s+" ") || chat.endsWith(" "+s) || chat.contains(" "+s+" ")) {
                term += s+" ";
            }
        }
        if (!term.isEmpty())
            terminal.put(username, term);
    }

    @DGEventHandler()
    public void onDungeonStart(DungeonStartedEvent event) {
        ready.clear();
        terminal.clear();
    }
}
