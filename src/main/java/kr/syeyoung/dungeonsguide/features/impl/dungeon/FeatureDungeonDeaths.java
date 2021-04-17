package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.events.DungeonDeathEvent;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.features.listener.TickListener;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.features.text.TextStyle;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeatureDungeonDeaths extends TextHUDFeature implements ChatListener {
    public FeatureDungeonDeaths() {
        super("Dungeon", "Display Deaths", "Display names of player and death count in dungeon run", "dungeon.stats.deaths", false, getFontRenderer().getStringWidth("longestplayernamepos: 100"), getFontRenderer().FONT_HEIGHT * 6);
        this.setEnabled(false);
        getStyles().add(new TextStyle("username", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("total", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("deaths", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("totalDeaths", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
    @Override
    public boolean isHUDViewable() {
        if (!skyblockStatus.isOnDungeon()) return false;
        DungeonContext context = skyblockStatus.getContext();
        if (context == null) return false;
        return true;
    }

    @Override
    public boolean doesScaleWithHeight() {
        return false;
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Arrays.asList(new String[] {
                "username", "separator", "deaths", "total", "totalDeaths"
        });
    }

    @Override
    public List<StyledText> getText() {

        List<StyledText> text=  new ArrayList<StyledText>();

        DungeonContext context = skyblockStatus.getContext();
        Map<String, Integer> deaths = context.getDeaths();
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
        for (NetworkPlayerInfo networkPlayerInfoIn : Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap()) {
            String name = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
            if (name.contains("Deaths")) {
                String whatever = TextUtils.keepIntegerCharactersOnly(TextUtils.keepScoreboardCharacters(TextUtils.stripColor(name)));
                if (whatever.isEmpty()) break;
                return Integer.parseInt(whatever);
            }
        }
        DungeonContext context = skyblockStatus.getContext();
        if (context == null) return 0;
        int d = 0;
        for (Integer value : context.getDeaths().values()) {
            d += value;
        }
        return d;
    }

    Pattern deathPattern = Pattern.compile("§r§c ☠ (.+?)§r§7 .+and became a ghost.+");
    Pattern meDeathPattern = Pattern.compile("§r§c ☠ §r§7You .+and became a ghost.+");

    @Override
    public void onChat(ClientChatReceivedEvent clientChatReceivedEvent) {
        if (clientChatReceivedEvent.type == 2) return;
        if (!skyblockStatus.isOnDungeon()) return;
        DungeonContext context = skyblockStatus.getContext();
        if (context == null) return;

        String txt = clientChatReceivedEvent.message.getFormattedText();
        Matcher m = deathPattern.matcher(txt);
        if (m.matches()) {
            String nickname = TextUtils.stripColor(m.group(1));
            int deaths = context.getDeaths().getOrDefault(nickname, 0);
            context.getDeaths().put(nickname, deaths + 1);
            context.createEvent(new DungeonDeathEvent(nickname, txt, deaths));
            e.sendDebugChat(new ChatComponentText("Death verified :: "+nickname+" / "+(deaths + 1)));
        }
        Matcher m2 = meDeathPattern.matcher(txt);
        if (m2.matches()) {
            String nickname = "me";
            int deaths = context.getDeaths().getOrDefault(nickname, 0);
            context.getDeaths().put(nickname, deaths + 1);
            context.createEvent(new DungeonDeathEvent(Minecraft.getMinecraft().thePlayer.getName(), txt, deaths));
            e.sendDebugChat(new ChatComponentText("Death verified :: me / "+(deaths + 1)));
        }
    }
}
