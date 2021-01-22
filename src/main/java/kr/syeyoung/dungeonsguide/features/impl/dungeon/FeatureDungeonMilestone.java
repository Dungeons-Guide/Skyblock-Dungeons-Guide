package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.features.listener.TickListener;
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
import java.util.regex.Pattern;

public class FeatureDungeonMilestone extends GuiFeature implements ChatListener {
    public FeatureDungeonMilestone() {
        super("Dungeon", "Display Current Class Milestone", "Display current class milestone of yourself", "dungeon.stats.milestone", true, getFontRenderer().getStringWidth("Milestone: 12"), getFontRenderer().FONT_HEIGHT);
        this.setEnabled(false);
        parameters.put("color", new FeatureParameter<Color>("color", "Color", "Color of text", Color.orange, "color"));
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
    @Override
    public void drawHUD(float partialTicks) {
        if (!skyblockStatus.isOnDungeon()) return;
        FontRenderer fr = getFontRenderer();
        double scale = getFeatureRect().getHeight() / fr.FONT_HEIGHT;
        GlStateManager.scale(scale, scale, 0);
        for (NetworkPlayerInfo networkPlayerInfoIn : Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap()) {
            String name = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
            if (name.startsWith("§r Milestone: §r")) {
                String milestone = TextUtils.stripColor(name).substring(13);
                fr.drawString("Milestone: "+milestone, 0,0, this.<Color>getParameter("color").getValue().getRGB());
                return;
            }
        }
        fr.drawString("Milestone: ?", 0,0, this.<Color>getParameter("color").getValue().getRGB());
    }

    @Override
    public void drawDemo(float partialTicks) {
        FontRenderer fr = getFontRenderer();
        double scale = getFeatureRect().getHeight() / fr.FONT_HEIGHT;
        GlStateManager.scale(scale, scale, 0);
        fr.drawString("Milestone: 9", 0,0, this.<Color>getParameter("color").getValue().getRGB());
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
