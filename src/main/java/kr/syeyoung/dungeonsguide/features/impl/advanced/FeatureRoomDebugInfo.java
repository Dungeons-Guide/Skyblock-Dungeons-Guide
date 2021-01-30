package kr.syeyoung.dungeonsguide.features.impl.advanced;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeatureRoomDebugInfo extends GuiFeature {
    public FeatureRoomDebugInfo() {
        super("advanced", "Display Room Debug Info", "ONLY WORKS WITH SECRET SETTING", "advanced.debug.roominfo", false, getFontRenderer().getStringWidth("longestplayernamepos: 100"), getFontRenderer().FONT_HEIGHT * 6);
        this.setEnabled(false);
        parameters.put("color", new FeatureParameter<Color>("color", "Color", "Color of text", Color.white, "color"));
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
    @Override
    public void drawHUD(float partialTicks) {
        if (!skyblockStatus.isOnDungeon()) return;
        if (!FeatureRegistry.DEBUG.isEnabled()) return;
        DungeonContext context = skyblockStatus.getContext();
        if (context == null) return;
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        int color = this.<Color>getParameter("color").getValue().getRGB();
        if (dungeonRoom == null) {
            if (context.getBossfightProcessor() == null) {
                fontRenderer.drawString("Where are you?!", 0, 0, 0xFFFFFF);
            } else {
                fontRenderer.drawString("You're prob in bossfight", 0, 0, color);
                fontRenderer.drawString("processor: "+context.getBossfightProcessor(), 0, 10, color);
                fontRenderer.drawString("phase: "+context.getBossfightProcessor().getCurrentPhase(), 0, 20, color);
                fontRenderer.drawString("nextPhase: "+ StringUtils.join(context.getBossfightProcessor().getNextPhases(), ","), 0, 30, color);
                fontRenderer.drawString("phases: "+ StringUtils.join(context.getBossfightProcessor().getPhases(), ","), 0, 40, color);
            }
        } else {
                fontRenderer.drawString("you're in the room... color/shape/rot " + dungeonRoom.getColor() + " / " + dungeonRoom.getShape() + " / "+dungeonRoom.getRoomMatcher().getRotation(), 0, 0, color);
                fontRenderer.drawString("room uuid: " + dungeonRoom.getDungeonRoomInfo().getUuid() + (dungeonRoom.getDungeonRoomInfo().isRegistered() ? "" : " (not registered)"), 0, 10, color);
                fontRenderer.drawString("room name: " + dungeonRoom.getDungeonRoomInfo().getName(), 0, 20, color);
                fontRenderer.drawString("room state / max secret: " + dungeonRoom.getCurrentState() + " / "+dungeonRoom.getTotalSecrets(), 0, 30, color);

        }
    }

    @Override
    public void drawDemo(float partialTicks) {
        FontRenderer fr = getFontRenderer();
        fr.drawString("Line 1", 0,0, this.<Color>getParameter("color").getValue().getRGB());
        fr.drawString("Line 2", 0,10, this.<Color>getParameter("color").getValue().getRGB());
        fr.drawString("Line 3", 0,20, this.<Color>getParameter("color").getValue().getRGB());
        fr.drawString("Line 4", 0,30, this.<Color>getParameter("color").getValue().getRGB());
        fr.drawString("Line 5", 0,40, this.<Color>getParameter("color").getValue().getRGB());
    }

}
