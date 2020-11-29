package kr.syeyoung.dungeonsguide.utils;

import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonDoor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.List;

public class RenderUtils {
    public static void renderDoor(DungeonDoor dungeonDoor, float partialTicks) {
        Entity player = Minecraft.getMinecraft().thePlayer;
        double playerX = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
        double playerY = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
        double playerZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
//because of the way 3D rendering is done, all coordinates are relative to the camera.  This "resets" the "0,0,0" position to the location that is (0,0,0) in the world.

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glPushMatrix();
        GL11.glTranslated(-playerX, -playerY, -playerZ);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GlStateManager.enableAlpha();

        if (dungeonDoor.isExist())
            GL11.glColor4ub((byte)0,(byte)255,(byte)0, (byte)255);
        else
            GL11.glColor4ub((byte)255,(byte)0,(byte)0, (byte)255);

        double x = dungeonDoor.getPosition().getX() + 0.5;
        double y = dungeonDoor.getPosition().getY() -0.99;
        double z = dungeonDoor.getPosition().getZ() + 0.5;
        GL11.glBegin(GL11.GL_QUADS);

        GL11.glVertex3d(x - 2.5, y, z - 2.5);
        GL11.glVertex3d(x - 2.5, y, z + 2.5);
        GL11.glVertex3d(x + 2.5, y, z + 2.5);
        GL11.glVertex3d(x + 2.5, y, z - 2.5);

        GL11.glEnd();

        if (dungeonDoor.isExist()) {
            GL11.glBegin(GL11.GL_QUADS);

            GL11.glColor4ub((byte)0,(byte)0,(byte)255, (byte)255);
            if (dungeonDoor.isZDir()) {
                GL11.glVertex3d(x - 0.5, y + 0.1, z - 2.5);
                GL11.glVertex3d(x - 0.5, y+ 0.1, z + 2.5);
                GL11.glVertex3d(x + 0.5, y+ 0.1, z + 2.5);
                GL11.glVertex3d(x + 0.5, y+ 0.1, z - 2.5);
            } else {
                GL11.glVertex3d(x - 2.5, y+ 0.1, z - 0.5);
                GL11.glVertex3d(x - 2.5, y+ 0.1, z + 0.5);
                GL11.glVertex3d(x + 2.5, y+ 0.1, z + 0.5);
                GL11.glVertex3d(x + 2.5, y+ 0.1, z - 0.5);
            }

            GL11.glEnd();
        } else {
            GL11.glLineWidth(5);
            GL11.glBegin(GL11.GL_LINE_LOOP);
            GL11.glVertex3d(x - 2.5, y, z - 2.5);
            GL11.glVertex3d(x + 2.5, y + 5, z - 2.5);
            GL11.glVertex3d(x + 2.5, y, z + 2.5);
            GL11.glVertex3d(x - 2.5, y + 5, z + 2.5);
            GL11.glEnd();
            GL11.glBegin(GL11.GL_LINE_LOOP);
            GL11.glVertex3d(x - 2.5, y +5, z - 2.5);
            GL11.glVertex3d(x + 2.5, y, z - 2.5);
            GL11.glVertex3d(x + 2.5, y + 5, z + 2.5);
            GL11.glVertex3d(x - 2.5, y, z + 2.5);
            GL11.glEnd();
            GL11.glLineWidth(1);
        }
//        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_CULL_FACE);

        GL11.glPopAttrib();
        GL11.glPopMatrix();

    }

    public static void drawLine(Vec3 pos1, Vec3 pos2, Color colour, float partialTicks , boolean depth) {
        Entity render = Minecraft.getMinecraft().getRenderViewEntity();
        WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();

        double realX = render.lastTickPosX + (render.posX - render.lastTickPosX) * partialTicks;
        double realY = render.lastTickPosY + (render.posY - render.lastTickPosY) * partialTicks;
        double realZ = render.lastTickPosZ + (render.posZ - render.lastTickPosZ) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-realX, -realY, -realZ);
        GlStateManager.disableTexture2D();
        if (!depth) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
        }
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glLineWidth(2);
        GlStateManager.color(colour.getRed() / 255f, colour.getGreen() / 255f, colour.getBlue()/ 255f, colour.getAlpha() / 255f);
        worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

        worldRenderer.pos(pos1.xCoord, pos1.yCoord, pos1.zCoord).endVertex();
        worldRenderer.pos(pos2.xCoord, pos2.yCoord, pos2.zCoord).endVertex();
        Tessellator.getInstance().draw();

        GlStateManager.translate(realX, realY, realZ);
        GlStateManager.disableBlend();
        if (!depth) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
        }
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    public static void drawLines(List<BlockPos> poses, Color colour, float partialTicks, boolean depth) {
        Entity render = Minecraft.getMinecraft().getRenderViewEntity();
        WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();

        double realX = render.lastTickPosX + (render.posX - render.lastTickPosX) * partialTicks;
        double realY = render.lastTickPosY + (render.posY - render.lastTickPosY) * partialTicks;
        double realZ = render.lastTickPosZ + (render.posZ - render.lastTickPosZ) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-realX, -realY, -realZ);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        if (!depth) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
        }
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glLineWidth(2);
        GlStateManager.color(colour.getRed() / 255f, colour.getGreen() / 255f, colour.getBlue()/ 255f, colour.getAlpha() / 255f);
        worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
        for (BlockPos pos:poses) {
            worldRenderer.pos(pos.getX() +0.5, pos.getY() +0.5, pos.getZ() +0.5).endVertex();
        }
        Tessellator.getInstance().draw();

        GlStateManager.translate(realX, realY, realZ);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        if (!depth) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    public static void highlightBlock(BlockPos blockpos, Color c, float partialTicks) {
        highlightBlock(blockpos,c,partialTicks,false);
    }
    public static void highlightBlock(BlockPos blockpos, Color c, float partialTicks, boolean depth) {
        Entity viewing_from = Minecraft.getMinecraft().getRenderViewEntity();

        double x_fix = viewing_from.lastTickPosX + ((viewing_from.posX - viewing_from.lastTickPosX) * partialTicks);
        double y_fix = viewing_from.lastTickPosY + ((viewing_from.posY - viewing_from.lastTickPosY) * partialTicks);
        double z_fix = viewing_from.lastTickPosZ + ((viewing_from.posZ - viewing_from.lastTickPosZ) * partialTicks);

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.translate(-x_fix, -y_fix, -z_fix);

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();

        if (!depth) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
        }
        GL11.glColor4ub((byte)c.getRed(), (byte)c.getGreen(), (byte)c.getBlue(), (byte)c.getAlpha());

        GL11.glTranslated(blockpos.getX(), blockpos.getY(), blockpos.getZ());

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(0, 0, 1);
        GL11.glVertex3d(0, 1, 1);
        GL11.glVertex3d(0, 1, 0); // TOP LEFT / BOTTOM LEFT / TOP RIGHT/ BOTTOM RIGHT

        GL11.glVertex3d(1, 0, 1);
        GL11.glVertex3d(1, 0, 0);
        GL11.glVertex3d(1, 1, 0);
        GL11.glVertex3d(1, 1, 1);

        GL11.glVertex3d(0, 1, 1);
        GL11.glVertex3d(0, 0, 1);
        GL11.glVertex3d(1, 0, 1);
        GL11.glVertex3d(1, 1, 1); // TOP LEFT / BOTTOM LEFT / TOP RIGHT/ BOTTOM RIGHT

        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(0, 1, 0);
        GL11.glVertex3d(1, 1, 0);
        GL11.glVertex3d(1, 0, 0);

        GL11.glVertex3d(0,1,0);
        GL11.glVertex3d(0,1,1);
        GL11.glVertex3d(1,1,1);
        GL11.glVertex3d(1,1,0);

        GL11.glVertex3d(0,0,1);
        GL11.glVertex3d(0,0,0);
        GL11.glVertex3d(1,0,0);
        GL11.glVertex3d(1,0,1);



        GL11.glEnd();


        if (!depth) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
        }
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();


//...

    }
    public static void drawTextAtWorld(String text, float x, float y, float z, int color, float scale, boolean increase, boolean renderBlackBox, float partialTicks) {
        float lScale = scale;

        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        Vector3f renderPos = getRenderPos(x, y, z, partialTicks);

        if (increase) {
            double distance = Math.sqrt(renderPos.x * renderPos.x + renderPos.y * renderPos.y + renderPos.z * renderPos.z);
            double multiplier = distance / 120f; //mobs only render ~120 blocks away
            lScale *= 0.45f * multiplier;
        }

        GL11.glColor4f(1f, 1f, 1f, 0.5f);
        GL11.glPushMatrix();
        GL11.glTranslatef(renderPos.x, renderPos.y, renderPos.z);
        GL11.glRotatef(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(renderManager.playerViewX, 1.0f, 0.0f, 0.0f);
        GL11.glScalef(-lScale, -lScale, lScale);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int textWidth = fontRenderer.getStringWidth(text);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        if (renderBlackBox) {
            double j = textWidth / 2;
            GlStateManager.disableTexture2D();
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            worldRenderer.pos((double)(-j - 1), (double)(-1), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
            worldRenderer.pos((double)(-j - 1), (double)8, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
            worldRenderer.pos((double)(j + 1), (double)8, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
            worldRenderer.pos((double)(j + 1), (double)(-1), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture2D();
        }

        fontRenderer.drawString(text, -textWidth / 2, 0, color);

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopMatrix();
    }

    private static Vector3f getRenderPos(float x, float y, float z, float partialTicks) {
        EntityPlayerSP sp = Minecraft.getMinecraft().thePlayer;
        return new Vector3f(
                x - (float) (sp.lastTickPosX + (sp.posX - sp.lastTickPosX) * partialTicks),
                y - (float) (sp.lastTickPosY + (sp.posY - sp.lastTickPosY) * partialTicks),
                z - (float) (sp.lastTickPosZ + (sp.posZ - sp.lastTickPosZ) * partialTicks)
        );
    }
}
