package kr.syeyoung.dungeonsguide.features.impl.advanced;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;

public class FeatureRoomCoordDisplay extends GuiFeature {
    public FeatureRoomCoordDisplay() {
        super("Advanced", "Display Coordinate Relative to the Dungeon Room and room's rotation", "X: 0 Y: 3 Z: 5 Facing: Z+" , "advanced.coords", false, getFontRenderer().getStringWidth("X: 48 Y: 100 Z: 48 Facing: Z+"), 10);
        this.setEnabled(false);
        parameters.put("color", new FeatureParameter<Color>("color", "Color", "Color of text", Color.yellow, "color"));
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
    private static final String[] facing = {"Z+", "X-", "Z-", "X+"};
    @Override
    public void drawHUD(float partialTicks) {
        if (!skyblockStatus.isOnDungeon()) return;
        DungeonContext context = skyblockStatus.getContext();
        if (context == null) return;

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
        if (dungeonRoom == null) {
            return;
        }

        int facing = (int) (thePlayer.rotationYaw + 45) % 360;
        if (facing < 0) facing += 360;
        int real = (facing / 90 + dungeonRoom.getRoomMatcher().getRotation()) % 4;

        OffsetPoint offsetPoint = new OffsetPoint(dungeonRoom, new BlockPos((int)thePlayer.posX, (int)thePlayer.posY, (int)thePlayer.posZ));

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        double scale = getFeatureRect().getHeight() / fontRenderer.FONT_HEIGHT;
        GlStateManager.scale(scale, scale, 0);

        int color = this.<Color>getParameter("color").getValue().getRGB();
        fontRenderer.drawString("X: "+offsetPoint.getX()+" Y: "+offsetPoint.getY()+" Z: "+offsetPoint.getZ()+" Facing: "+ FeatureRoomCoordDisplay.facing[real], 0, 0, color);
    }

    @Override
    public void drawDemo(float partialTicks) {
        FontRenderer fr = getFontRenderer();
        int facing = (int) (Minecraft.getMinecraft().thePlayer.rotationYaw + 45) % 360;
        if (facing < 0) facing += 360;
        fr.drawString("X: 0 Y: 3 Z: 5 Facing: "+FeatureRoomCoordDisplay.facing[(facing / 90) % 4], 0,0, this.<Color>getParameter("color").getValue().getRGB());
    }

}
