package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class FeatureDungeonMilestone extends TextHUDFeature implements ChatListener {
    public FeatureDungeonMilestone() {
        super("Dungeon", "Display Current Class Milestone", "Display current class milestone of yourself", "dungeon.stats.milestone", true, getFontRenderer().getStringWidth("Milestone: 12"), getFontRenderer().FONT_HEIGHT);
        this.setEnabled(false);
        getStyles().add(new TextStyle("title", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("number", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();

    private static final List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("Milestone","title"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("9","number"));
    }

    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon();
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Arrays.asList(new String[] {
                "title", "separator", "number"
        });
    }

    @Override
    public List<StyledText> getDummyText() {
        return dummyText;
    }

    @Override
    public List<StyledText> getText() {
        List<StyledText> actualBit = new ArrayList<StyledText>();
        actualBit.add(new StyledText("Milestone","title"));
        actualBit.add(new StyledText(": ","separator"));
        for (NetworkPlayerInfo networkPlayerInfoIn : Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap()) {
            String name = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
            if (name.startsWith("§r Milestone: §r")) {
                String milestone = TextUtils.stripColor(name).substring(13);
                actualBit.add(new StyledText(milestone+"","number"));
                break;
            }
        }
        return actualBit;
    }

    public static final Pattern milestone_pattern = Pattern.compile("§r§e§l(.+) Milestone §r§e(.)§r§7: .+ §r§a(.+)§r");


    @Override
    public void onChat(ClientChatReceivedEvent clientChatReceivedEvent) {
        if (clientChatReceivedEvent.type == 2) return;;
        if (!skyblockStatus.isOnDungeon()) return;
        DungeonContext context = skyblockStatus.getContext();
        if (context == null) return;
        String txt = clientChatReceivedEvent.message.getFormattedText();
        if (milestone_pattern.matcher(txt).matches()) {
            context.getMilestoneReached().add(new String[] {
                    TextUtils.formatTime(FeatureRegistry.DUNGEON_REALTIME.getTimeElapsed()),
                    TextUtils.formatTime(FeatureRegistry.DUNGEON_SBTIME.getTimeElapsed())
            });
            e.sendDebugChat(new ChatComponentText("Reached Milestone At " +  TextUtils.formatTime(FeatureRegistry.DUNGEON_REALTIME.getTimeElapsed()) + " / "+TextUtils.formatTime(FeatureRegistry.DUNGEON_SBTIME.getTimeElapsed())));
        }
    }
}
