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

package kr.syeyoung.dungeonsguide.features.impl.party;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.chat.PartyContext;
import kr.syeyoung.dungeonsguide.chat.PartyManager;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.features.listener.DungeonStartListener;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.features.text.TextStyle;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.*;

public class FeaturePartyReady extends TextHUDFeature implements ChatListener, DungeonStartListener {
    public FeaturePartyReady() {
        super("Party","Party Ready List", "Check if your party member have said r or not", "party.readylist", false, getFontRenderer().getStringWidth("Watcher finished spawning all mobs!"), getFontRenderer().FONT_HEIGHT*4);
        getStyles().add(new TextStyle("player", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("ready", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("notready", new AColor(0xFF, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("terminal", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
        setEnabled(true);
    }

    @Override
    public boolean isHUDViewable() {
        return  PartyManager.INSTANCE.getPartyContext() != null && PartyManager.INSTANCE.getPartyContext().isPartyExistHypixel() && "Dungeon Hub".equals(DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getDungeonName());
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Arrays.asList("separator", "player", "ready", "notready", "terminal");
    }

    private static final List<StyledText> dummyText = new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("syeyoung","player"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("Ready","ready"));
        dummyText.add(new StyledText(" 4","terminal"));
        dummyText.add(new StyledText("\nrioho","player"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("Ready","ready"));
        dummyText.add(new StyledText(" 3","terminal"));
        dummyText.add(new StyledText("\nRaidShadowLegends","player"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("Not Ready","notready"));
        dummyText.add(new StyledText(" 2t","terminal"));
        dummyText.add(new StyledText("\nTricked","player"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("Ready","ready"));
        dummyText.add(new StyledText(" ss","terminal"));
        dummyText.add(new StyledText("\nMr. Penguin","player"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("Not Ready","notready"));
        dummyText.add(new StyledText(" 2b","terminal"));
    }

    @Override
    public List<StyledText> getDummyText() {
        return dummyText;
    }

    @Override
    public boolean doesScaleWithHeight() {
        return false;
    }

    private Set<String> ready = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    private Map<String, String> terminal = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @Override
    public List<StyledText> getText() {
        PartyContext pc = PartyManager.INSTANCE.getPartyContext();
        List<StyledText> text= new ArrayList<>();
        boolean first = true;
        for (String partyRawMember : pc.getPartyRawMembers()) {
            text.add(new StyledText((first ? "":"\n") + partyRawMember, "player"));
            text.add(new StyledText(": ","separator"));
            if (ready.contains(partyRawMember))
                text.add(new StyledText("Ready","ready"));
            else
                text.add(new StyledText("Not Ready","notready"));
            if (terminal.get(partyRawMember) != null) {
                text.add(new StyledText(" "+ terminal.get(partyRawMember), "terminal"));
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


    @Override
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

    @Override
    public void onDungeonStart() {
        ready.clear();
        terminal.clear();
    }
}
