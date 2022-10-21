/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.features.impl.advanced;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;

public class FeatureRoomDebugInfo extends GuiFeature {
    public FeatureRoomDebugInfo() {
        super("Debug", "Display Room Debug Info", "ONLY WORKS WITH SECRET SETTING", "advanced.debug.roominfo", false, getFontRenderer().getStringWidth("longestplayernamepos: 100"), getFontRenderer().FONT_HEIGHT * 6);
        this.setEnabled(false);
        addParameter("color", new FeatureParameter<Color>("color", "Color", "Color of text", Color.white, "color", nval -> color = nval.getRGB()));
    }

    int color = 0;

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
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

        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
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

        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        fr.drawString("Line 1", 0,0, color);
        fr.drawString("Line 2", 0,10, color);
        fr.drawString("Line 3", 0,20, color);
        fr.drawString("Line 4", 0,30, color);
        fr.drawString("Line 5", 0,40, color);
    }

}
