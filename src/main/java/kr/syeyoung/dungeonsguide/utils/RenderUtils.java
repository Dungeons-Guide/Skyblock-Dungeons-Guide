package kr.syeyoung.dungeonsguide.utils;

import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonDoor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;

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

    public static void highlightBlock(BlockPos blockpos, Color c, float partialTicks) {
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

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glColor3ub((byte)c.getRed(), (byte)c.getGreen(), (byte)c.getBlue());

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


        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();


//...

    }

    private void drawLineWithGL(Vec3 blockA, Vec3 blockB) {
        int d = Math.round((float)blockA.distanceTo(blockB)+0.2f);
        GL11.glColor3f(0F, 1F, 0F);
        float oz = (blockA.xCoord - blockB.xCoord == 0?0:-1f/16f);
        float ox = (blockA.zCoord - blockB.zCoord == 0?0:1f/16f);
        GL11.glBegin(GL11.GL_LINE_STRIP);

        //you will want to modify these offsets.
        GL11.glVertex3d(blockA.xCoord + 0.5,blockA.yCoord - 0.01,blockA.zCoord + 0.5);
        GL11.glVertex3d(blockB.xCoord + 0.5,blockB.yCoord - 0.01,blockB.zCoord + 0.5);

        GL11.glEnd();
    }
}
