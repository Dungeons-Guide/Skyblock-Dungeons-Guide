package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.features.listener.TickListener;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeatureDungeonDeaths extends GuiFeature implements ChatListener {
    public FeatureDungeonDeaths() {
        super("Dungeon", "Display Deaths", "Display names of player and death count in dungeon run", "dungeon.stats.deaths", false, getFontRenderer().getStringWidth("longestplayernamepos: 100"), getFontRenderer().FONT_HEIGHT * 6);
        this.setEnabled(false);
        parameters.put("color", new FeatureParameter<Color>("color", "Color", "Color of text", Color.orange, "color"));
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
    @Override
    public void drawHUD(float partialTicks) {
        if (!skyblockStatus.isOnDungeon()) return;
        DungeonContext context = skyblockStatus.getContext();
        if (context == null) return;
        Map<String, Integer> deaths = context.getDeaths();
        int i = 0;
        int deathsCnt = 0;
        FontRenderer fr = getFontRenderer();
        for (Map.Entry<String, Integer> death:deaths.entrySet()) {
            fr.drawString(death.getKey()+": "+death.getValue(), 0,i, this.<Color>getParameter("color").getValue().getRGB());
            i += 8;
            deathsCnt += death.getValue();
        }
        fr.drawString("Total Deaths: "+deathsCnt, 0,i, this.<Color>getParameter("color").getValue().getRGB());
    }

    public int getTotalDeaths() {
        if (!skyblockStatus.isOnDungeon()) return 0;
        DungeonContext context = skyblockStatus.getContext();
        if (context == null) return 0;
        int d = 0;
        for (Integer value : context.getDeaths().values()) {
            d += value;
        }
        return d;
    }

    @Override
    public void drawDemo(float partialTicks) {
        FontRenderer fr = getFontRenderer();
        fr.drawString("syeyoung: -130", 0,0, this.<Color>getParameter("color").getValue().getRGB());
        fr.drawString("notsyeyoung: -13", 0,8, this.<Color>getParameter("color").getValue().getRGB());
        fr.drawString("dungeonsguide: -42", 0,16, this.<Color>getParameter("color").getValue().getRGB());
        fr.drawString("--not more--", 0,24, this.<Color>getParameter("color").getValue().getRGB());
        fr.drawString("--maybe more--", 0,32, this.<Color>getParameter("color").getValue().getRGB());
        fr.drawString("Total Deaths: 0", 0,40, this.<Color>getParameter("color").getValue().getRGB());
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
            int deaths = context.getDeaths().containsKey(nickname)  ?  context.getDeaths().get(nickname) : 0;
            context.getDeaths().put(nickname, deaths + 1);
            e.sendDebugChat(new ChatComponentText("Death verified :: "+nickname+" / "+(deaths + 1)));
        }
        Matcher m2 = meDeathPattern.matcher(txt);
        if (m2.matches()) {
            String nickname = "me";
            int deaths = context.getDeaths().containsKey(nickname)  ?  context.getDeaths().get(nickname) : 0;
            context.getDeaths().put(nickname, deaths + 1);
            e.sendDebugChat(new ChatComponentText("Death verified :: me / "+(deaths + 1)));
        }
    }
}
